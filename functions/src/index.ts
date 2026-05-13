import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { google } from "googleapis";

admin.initializeApp();

const db = admin.database();
const auth = admin.auth();

/**
 * verifyPremiumPurchase: Valida una compra de Premium contra Google Play API
 *
 * IMPORTANTE:
 * - Solo el usuario autenticado puede llamar esta función
 * - El backend valida el token contra Google Play Developer API
 * - Solo estados ACTIVE o IN_GRACE_PERIOD son aceptados
 * - El resultado se escribe en premiumCache del usuario
 * - El cliente NUNCA recibe el token completo de vuelta
 *
 * Parámetros:
 * - packageName: string (debe ser "com.edutrack.app")
 * - productId: string (ID del producto suscrito)
 * - purchaseToken: string (token de la compra)
 *
 * Retorna:
 * {
 *   success: boolean
 *   isPremium: boolean
 *   expiresAt: number (timestamp)
 *   message: string
 * }
 */
export const verifyPremiumPurchase = functions.https.onCall(
  async (data, context) => {
    // Verificar autenticación
    if (!context.auth || !context.auth.uid) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "El usuario debe estar autenticado"
      );
    }

    const uid = context.auth.uid;
    const { packageName, productId, purchaseToken } = data;

    // Validar parámetros
    if (packageName !== "com.edutrack.app") {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Package name incorrecto"
      );
    }

    if (!productId || !purchaseToken) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "productId y purchaseToken son requeridos"
      );
    }

    try {
      // TODO: Configurar credenciales de Google Play Developer API
      // 1. Crear cuenta de servicio en Google Cloud Console
      // 2. Descargar JSON de credenciales
      // 3. Configurar variable de entorno: GOOGLE_PLAY_CREDENTIALS_PATH
      // 4. Hacer que el código abajo funcione con autenticación real

      /*
      const androidpublisher = google.androidpublisher({
        version: 'v3',
        auth: new google.auth.GoogleAuth({
          keyFilename: process.env.GOOGLE_PLAY_CREDENTIALS_PATH,
        }),
      });

      const subscriptionResponse = await androidpublisher.purchases.subscriptionsv2.get({
        packageName: packageName,
        subscriptionId: productId,
        token: purchaseToken,
      });

      const subscription = subscriptionResponse.data;

      // Validar estado de suscripción
      const validStates = ['SUBSCRIPTION_STATE_ACTIVE', 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD'];
      if (!validStates.includes(subscription.subscriptionState || '')) {
        await db.ref(`Edutrack/users/${uid}/premiumCache`).set({
          isPremium: false,
          validatedAt: Date.now(),
          source: 'google_play_server',
        });

        return {
          success: false,
          isPremium: false,
          message: 'Suscripción no está activa',
        };
      }

      // Calcular fecha de expiración
      const expiryTimeMillis = subscription.expiryTimeMillis ?
        parseInt(subscription.expiryTimeMillis, 10) :
        Date.now() + (365 * 24 * 60 * 60 * 1000);

      // Escribir resultado en database (SOLO desde backend)
      await db.ref(`Edutrack/users/${uid}/premiumCache`).set({
        isPremium: true,
        productId: productId,
        expiresAt: expiryTimeMillis,
        validatedAt: Date.now(),
        source: 'google_play_server',
      });

      return {
        success: true,
        isPremium: true,
        expiresAt: expiryTimeMillis,
        message: 'Suscripción verificada exitosamente',
      };
      */

      // PLACEHOLDER: Retornar error hasta que se configure
      throw new functions.https.HttpsError(
        "failed-precondition",
        "Google Play API no está configurada aún. Contacta al administrador."
      );
    } catch (error: any) {
      console.error("Error verificando compra:", error);

      // Log para debugging (nunca exponer token completo)
      await db
        .ref(`Edutrack/users/${uid}/premiumCache`)
        .update({
          isPremium: false,
          validatedAt: Date.now(),
          source: "google_play_server",
        });

      throw new functions.https.HttpsError(
        "internal",
        "Error verificando suscripción: " + (error.message || "Unknown error")
      );
    }
  }
);

/**
 * Función auxiliar: Verificar integridad del database
 * Ejecutar manualmente: firebase functions:shell
 * > healthCheck()
 */
export const healthCheck = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Usuario no autenticado"
    );
  }

  return {
    status: "ok",
    timestamp: Date.now(),
    message: "Cloud Functions están activas",
  };
});
