import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { google } from "googleapis";

admin.initializeApp();

const db = admin.database();

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

export const healthCheck = functions.https.onCall(async (_data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "No autenticado");
  }
  return { status: "ok", timestamp: Date.now() };
});
