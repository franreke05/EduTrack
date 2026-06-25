# EduTrack — Checklist de deploy y lanzamiento

Última actualización: 2026-06-24

Este documento cubre los pasos de **configuración y despliegue** que no se pueden
resolver desde el código y que tú debes ejecutar con tus credenciales de Google
Play, Firebase y AdMob. Resuelve los problemas **#8 (billing)**, parte de **#9
(borrado GDPR)** y parte de **#11 (legal)**.

---

## 1. Cloud Functions (bloquea #8 billing y #9 borrado de cuenta)

El código de las funciones ya está escrito en `functions/src/index.ts`:
- `verifyPremiumPurchase` — valida la compra contra Google Play (server-side).
- `deleteUserData` — **NUEVA**: borra todos los datos del usuario + su cuenta de Auth (RGPD).
- `healthCheck`.

**Sin desplegar estas funciones, la compra Premium y el borrado de cuenta NO funcionan.**

### Pasos
1. Crear cuenta de servicio en Google Cloud Console con el rol
   *Google Play Android Developer* y descargar el JSON.
2. En Play Console → *Configuración → Acceso a API* → vincular el proyecto de Google Cloud
   y conceder acceso a esa cuenta de servicio.
3. Configurar la credencial en Firebase:
   ```bash
   firebase functions:config:set googleplay.credentials_json='<contenido-del-json>'
   ```
4. Instalar dependencias y desplegar:
   ```bash
   cd functions
   npm install
   npm run build       # tsc
   firebase deploy --only functions
   ```
5. Verificar en la consola de Firebase que aparecen las 3 funciones desplegadas.

> Tras el deploy, **#9 queda 100% funcional** (el cliente ya llama a `deleteUserData`).

---

## 2. Productos de suscripción en Play Console (bloquea #8)

Los IDs ya están referenciados en el código
(`app/.../billing/BillingManager.kt`):
- `edutrack_premium_monthly`
- `edutrack_premium_annual`

### Pasos
1. Play Console → *Monetizar → Productos → Suscripciones* → crear ambas con
   **exactamente** esos IDs.
2. Precios: 2,49 €/mes y 14,99 €/año (los que muestra el paywall). Si cambias el
   precio, actualiza también los textos de `PaywallScreen.kt`.
3. **Oferta de prueba gratuita (7 días)** — el paywall ahora anuncia "Empezar 7
   días gratis". Para que sea real, añade una fase de *free trial* de 7 días a la
   oferta base de cada suscripción. Si NO quieres trial, cambia el texto del botón
   en `PaywallScreen.kt` (`BottomCta`) por "Suscribirme — …".
4. Publicar las suscripciones y esperar a que estén *Activas* (puede tardar horas).

> `applicationId` debe ser `com.edutrack.app` (ya configurado). El billing solo
> funciona con un APK/AAB **firmado y subido** a un track de Play (interno vale).

---

## 3. AdMob (relacionado con #11, menores)

- El manifest usa el **App ID de prueba** de Google
  (`ca-app-pub-3940256099942544~3347511713`) y el banner usa el **ad unit de
  prueba**. Sustitúyelos por los reales antes de publicar
  (`AndroidManifest.xml` y `ads/AdMobBanner.kt`).
- ✅ Ya configurado en código: anuncios **no personalizados / aptos para menores**
  (`setTagForUnderAgeOfConsent(TRUE)` + `MAX_AD_CONTENT_RATING_T`) porque el
  público es 13-17.
- En AdMob, marca la app dentro del **programa para Familias** y como dirigida
  también a menores.

---

## 4. Legal (#11) — hospedar y declarar

1. **Rellenar placeholders** en `docs/legal/*.md` (busca `[RELLENAR:`):
   razón social/autónomo, NIF/DNI, dirección fiscal.
2. **Hospedar** al menos la Política de privacidad y la página de Eliminación de
   datos en una URL pública (GitHub Pages o Firebase Hosting, gratis).
3. **Actualizar las URLs** en el código: una sola edición en
   `app/.../DbRefs.kt` (`PRIVACY_POLICY_URL`, `TERMS_URL`). Ya se usan en Ajustes
   y en la pantalla de registro.
4. **Play Console → Data Safety**: declarar que se recopila email, contenido del
   usuario (datos académicos) y Advertising ID; servidores en la UE; y que la app
   se dirige también a menores.
5. Pegar la URL de la política de privacidad en la ficha de Play Store.

---

## 5. Pendientes de código recomendados (no bloqueantes, documentados)

- **Age-gate + consentimiento parental** (público 13-17): pedir fecha de
  nacimiento en el registro y, para <14 (LOPDGDD art. 7), flujo de consentimiento
  parental. Ver `docs/legal/RESUMEN_INTEGRACION.md`.
- **`USE_EXACT_ALARM`**: Play restringe este permiso a apps de alarma/calendario.
  Si Play lo rechaza, quítalo del manifest; el código ya degrada a alarma inexacta
  con `SCHEDULE_EXACT_ALARM`.
- **#2 ViewModel** y **#1 tests de UI**: deuda técnica, no bloquean lanzamiento.
- **#3 paridad iOS**: el módulo `composeApp` comparte parte de la UI (KMP), pero
  el target real es Android. Fuera de alcance.
