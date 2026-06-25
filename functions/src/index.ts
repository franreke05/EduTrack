import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { google } from "googleapis";

admin.initializeApp();

const db = admin.database("https://edutrack-5579f-default-rtdb.europe-west1.firebasedatabase.app/");

// Credenciales de Google Play: configurar en Firebase con:
// firebase functions:config:set googleplay.credentials_json='<json_string>'
// TODO antes del launch: crear cuenta de servicio en Google Cloud Console con
// permiso "Google Play Android Developer" y pegar el JSON aquí.
function getAndroidPublisher() {
  const credentialsJson = functions.config().googleplay?.credentials_json;
  if (!credentialsJson) throw new Error("GOOGLEPLAY_CREDENTIALS no configuradas");
  const credentials = JSON.parse(credentialsJson);
  return google.androidpublisher({
    version: "v3",
    auth: new google.auth.GoogleAuth({
      credentials,
      scopes: ["https://www.googleapis.com/auth/androidpublisher"],
    }),
  });
}

/**
 * verifyPremiumPurchase: Valida una compra contra Google Play API y escribe
 * premiumCache en RTDB. El cliente NUNCA recibe el token completo.
 */
export const verifyPremiumPurchase = functions.https.onCall(
  async (data, context) => {
    if (!context.auth?.uid) {
      throw new functions.https.HttpsError("unauthenticated", "Usuario no autenticado");
    }

    const uid = context.auth.uid;
    const { packageName, productId, purchaseToken } = data as {
      packageName: string;
      productId: string;
      purchaseToken: string;
    };

    if (packageName !== "com.edutrack.app") {
      throw new functions.https.HttpsError("invalid-argument", "Package name incorrecto");
    }
    if (!productId || !purchaseToken) {
      throw new functions.https.HttpsError("invalid-argument", "productId y purchaseToken requeridos");
    }

    try {
      const androidpublisher = getAndroidPublisher();

      const { data: subscription } = await androidpublisher.purchases.subscriptionsv2.get({
        packageName,
        subscriptionId: productId,
        token: purchaseToken,
      });

      const validStates = ["SUBSCRIPTION_STATE_ACTIVE", "SUBSCRIPTION_STATE_IN_GRACE_PERIOD"];
      const isActive = validStates.includes(subscription.subscriptionState ?? "");

      if (!isActive) {
        await db.ref(`Edutrack/users/${uid}/premiumCache`).set({
          isPremium: false,
          validatedAt: Date.now(),
        });
        return { success: false, isPremium: false, message: "Suscripción no activa" };
      }

      // lineItems[0].expiryTime es ISO 8601 string en subscriptionsv2
      const expiryTimeMillis = subscription.lineItems?.[0]?.expiryTime
        ? new Date(subscription.lineItems[0].expiryTime).getTime()
        : Date.now() + 30 * 24 * 60 * 60 * 1000; // fallback: +30 días

      await db.ref(`Edutrack/users/${uid}/premiumCache`).set({
        isPremium: true,
        productId,
        expiresAt: expiryTimeMillis,
        validatedAt: Date.now(),
      });

      return { success: true, isPremium: true, expiresAt: expiryTimeMillis, message: "OK" };
    } catch (error: any) {
      // Si las credenciales no están configuradas, devuelve error claro
      const msg = error.message ?? "Error desconocido";
      functions.logger.error("verifyPremiumPurchase error:", msg);

      await db.ref(`Edutrack/users/${uid}/premiumCache`).update({
        isPremium: false,
        validatedAt: Date.now(),
      });

      throw new functions.https.HttpsError("internal", msg);
    }
  }
);

/**
 * deleteUserData (RGPD art. 17 — derecho de supresión).
 * Borra TODOS los datos personales del usuario en todas las ramas de la RTDB
 * y elimina su cuenta de Authentication. Usa admin SDK, así que:
 *   - puede limpiar nodos de grupo que las reglas no permiten borrar al cliente
 *   - elimina la cuenta de Auth sin el error "requires-recent-login"
 */
export const deleteUserData = functions.https.onCall(async (_data, context) => {
  if (!context.auth?.uid) {
    throw new functions.https.HttpsError("unauthenticated", "Usuario no autenticado");
  }
  const uid = context.auth.uid;
  const root = db.ref("Edutrack");

  const userGroupsSnap = await root.child(`userGroups/${uid}`).get();
  const groupIds = userGroupsSnap.exists() ? Object.keys(userGroupsSnap.val() ?? {}) : [];

  const updates: Record<string, null> = {
    [`users/${uid}`]: null,
    [`userGroups/${uid}`]: null,
  };

  await Promise.all(
    groupIds.map(async (gid) => {
      const [groupSnap, membersSnap] = await Promise.all([
        root.child(`groups/${gid}`).get(),
        root.child(`groupMembers/${gid}`).get(),
      ]);

      const isOwner = groupSnap.val()?.ownerUid === uid;

      if (isOwner) {
        // Propietario: eliminar el grupo ENTERO y sacarlo del userGroups de cada miembro.
        updates[`groups/${gid}`] = null;
        updates[`groupMembers/${gid}`] = null;
        updates[`groupGrades/${gid}`] = null;
        updates[`groupFeed/${gid}`] = null;
        updates[`groupResources/${gid}`] = null;
        updates[`groupExams/${gid}`] = null;
        updates[`groupSharedSubjects/${gid}`] = null;
        membersSnap.forEach((child) => {
          const memberUid = child.key;
          if (memberUid && memberUid !== uid) {
            updates[`userGroups/${memberUid}/${gid}`] = null;
          }
          return false;
        });
      } else {
        // Miembro: solo borrar su membresía, notas y TODO su contenido en el grupo.
        updates[`groupMembers/${gid}/${uid}`] = null;
        updates[`groupGrades/${gid}/${uid}`] = null;

        const [feedSnap, resourcesSnap, examsSnap, sharedSnap] = await Promise.all([
          root.child(`groupFeed/${gid}`).get(),
          root.child(`groupResources/${gid}`).get(),
          root.child(`groupExams/${gid}`).get(),
          root.child(`groupSharedSubjects/${gid}`).get(),
        ]);

        feedSnap.forEach((child) => {
          if (child.val()?.actorUid === uid) updates[`groupFeed/${gid}/${child.key}`] = null;
          return false;
        });
        resourcesSnap.forEach((child) => {
          if (child.val()?.authorId === uid) updates[`groupResources/${gid}/${child.key}`] = null;
          return false;
        });
        examsSnap.forEach((child) => {
          if (child.val()?.authorId === uid) updates[`groupExams/${gid}/${child.key}`] = null;
          return false;
        });
        sharedSnap.forEach((child) => {
          if (child.val()?.sharedBy === uid) updates[`groupSharedSubjects/${gid}/${child.key}`] = null;
          return false;
        });
      }
    })
  );

  await root.update(updates);

  // Avatar en Storage (best-effort; puede no existir si el usuario nunca subió foto)
  try {
    await admin.storage().bucket().file(`avatars/${uid}.jpg`).delete();
  } catch (_) {}

  await admin.auth().deleteUser(uid);
  return { success: true };
});

export const healthCheck = functions.https.onCall(async (_data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "No autenticado");
  }
  return { status: "ok", timestamp: Date.now() };
});
