# Edutrack beta setup

Ultima auditoria local: 24/04/2026.

## Estado del proyecto

- Nombre final: `Edutrack`.
- `applicationId`: `com.edutrack.app`.
- `versionCode`: `2`.
- `versionName`: `0.2.0-beta01`.
- `minSdk`: 26.
- `targetSdk`: 36.
- Builds verificados previamente: debug, unit tests y AAB release.

## Decision de monetizacion

Se eligio Google Play Billing directo.

Motivo: RevenueCat no procesa pagos y Google Play seguiria siendo el canal de cobro, pero RevenueCat puede anadir coste propio cuando el proyecto supera su tramo gratis. Para una beta cerrada con dos suscripciones, Billing directo reduce capas externas y evita comisiones adicionales.

Fuentes revisadas:

- Google Play service fee: https://support.google.com/googleplay/android-developer/answer/11131145
- Play Billing Library: https://developer.android.com/google/play/billing/integrate
- RevenueCat pricing: https://www.revenuecat.com/pricing/

## Google Sign-In y Firebase

La app usa Credential Manager + Firebase Auth.

Implementado:

- `AuthRepository` autentica email/password y Google.
- `AuthViewModel` maneja token nulo, cancelacion, ausencia de credenciales, errores de Credential Manager y errores Firebase.
- Tras login se crea o actualiza `Edutrack/Usuario/{uid}`.
- Email/password sigue funcionando.

Pendiente externo obligatorio:

1. En Firebase Console, crear o actualizar la app Android con package exacto `com.edutrack.app`.
2. Obtener SHA-1 y SHA-256:
   ```powershell
   .\gradlew.bat signingReport
   ```
3. Pegar SHA-1 y SHA-256 en Firebase Console > Project settings > Your apps > Android app.
4. Activar Authentication > Sign-in method > Google.
5. Descargar un nuevo `google-services.json` desde Firebase y reemplazar `app/google-services.json`.
6. Compilar y comprobar que Gradle genera:
   `app/build/generated/res/processDebugGoogleServices/values/values.xml`
7. Verificar ahi que `default_web_client_id` termina en `.apps.googleusercontent.com` y corresponde al OAuth Web Client, no al Android client.

Riesgo detectado:

- El `google-services.json` actual fue ajustado a `com.edutrack.app` para compilar, pero debe ser reemplazado por uno generado por Firebase para ese package y esos SHA. Si no, Google Sign-In puede devolver credenciales invalidas aunque compile.

Nota sobre login por nombre de usuario:

- Se deja el login con email/password como camino soportado.
- No se permite resolver email desde `Usuario/nombre` antes de autenticar porque las reglas seguras no deben exponer usuarios a lectores anonimos.
- Si mas adelante se quiere login por nombre de usuario, implementarlo con Cloud Functions o un indice publico minimo que no exponga emails ni datos personales. No guardar passwords en Realtime Database.

## Google Play Billing

Product IDs:

- `edutrack_premium_monthly` = 2,49 EUR/mes.
- `edutrack_premium_yearly` = 14,99 EUR/ano.

Implementado:

- Billing directo con `BillingRepository`.
- Consulta de productos.
- Compra mensual/anual.
- `acknowledgePurchase` solo para compras `PURCHASED`.
- Compras `PENDING` no desbloquean Premium.
- Restaurar compras desde paywall.
- Refresco de compras activas al abrir la app si hay sesion.
- `isPremium` se guarda localmente en DataStore.

Pendiente obligatorio antes de produccion abierta:

- Validar purchase tokens en backend con Google Play Developer API.
- Configurar Real-time Developer Notifications.
- Sincronizar Premium multi-dispositivo desde backend o Firebase controlado por servidor.

## AdMob

La app usa solo IDs de prueba:

- App ID: `ca-app-pub-3940256099942544~3347511713`
- Banner: `ca-app-pub-3940256099942544/6300978111`
- Rewarded: `ca-app-pub-3940256099942544/5224354917`

Reglas aplicadas:

- Sin anuncios en login/registro.
- Sin anuncios en formularios.
- Sin anuncios en paywall.
- Sin anuncios para Premium.
- Sin interstitials.
- Rewarded ads quedan preparados como opcion voluntaria.

Antes de monetizar:

1. Crear app en AdMob.
2. Sustituir solo los IDs de test por IDs reales.
3. Mantener test IDs durante beta tecnica si aun no hay aprobacion AdMob.

## Firebase Realtime Database

Pegar el contenido de `firebase-rules.json` en Realtime Database Rules.

Estructuras protegidas:

- `Edutrack/Usuario/{uid}`: solo su usuario.
- `Edutrack/Anio`: lectura por query `orderByChild('id_user').equalTo(auth.uid)`.
- `Edutrack/Asignatura/{asignaturaId}`: solo propietario.
- `Edutrack/Groups/{groupId}`: solo miembros.
- `Edutrack/Groups/{groupId}/settings`: solo admin.
- `Edutrack/UserGroups/{uid}`: indice privado de grupos por usuario.

Indices:

- `Usuario`: `nombre`, `email`.
- `Anio`: `id_user`.
- `Asignatura`: `id_usuario`, `id_anio`.
- `Groups`: `creatorId`, `name`.

Riesgo pendiente:

- Falta testear reglas con Firebase Rules Unit Testing.
- La arquitectura antigua duplica asignaturas dentro de `Anio/lista_asignaturas`; es aceptable para beta cerrada, pero conviene normalizar antes de escalar.

## Limites freemium implementados

Gratis:

- Maximo 2 cursos.
- Maximo 8 asignaturas si el tipo es trimestre.
- Maximo 9 asignaturas si el tipo es cuatrimestre.
- Notas ilimitadas.
- Porcentaje total por asignatura maximo 100%.
- Unirse solo a 1 grupo.
- Personalizacion basica.
- Banners suaves.

Premium:

- Sin anuncios.
- Cursos ilimitados.
- Asignaturas ilimitadas.
- Simulador de nota necesaria.
- Estadisticas avanzadas.
- Recordatorios.
- Exportar PDF.
- Personalizacion avanzada.
- Crear y gestionar grupos.

## Checklist final beta cerrada

Firebase:

- [ ] App Android en Firebase con package `com.edutrack.app`.
- [ ] SHA-1 debug anadido.
- [ ] SHA-256 debug anadido.
- [ ] SHA-1 release/upload anadido.
- [ ] SHA-256 release/upload anadido.
- [ ] SHA de Play App Signing anadido si Play firma la app.
- [ ] Google provider activado en Firebase Auth.
- [ ] Nuevo `google-services.json` descargado y reemplazado.
- [ ] `default_web_client_id` comprobado en recursos generados.
- [ ] `firebase-rules.json` pegado en Realtime Database.

Play Console:

- [ ] App creada con package `com.edutrack.app`.
- [ ] Closed Testing creado.
- [ ] Testers anadidos.
- [ ] License testers configurados.
- [ ] Suscripcion `edutrack_premium_monthly` creada a 2,49 EUR/mes.
- [ ] Suscripcion `edutrack_premium_yearly` creada a 14,99 EUR/ano.
- [ ] Politica de privacidad publicada.
- [ ] Terminos de servicio publicados.
- [ ] Data Safety completado.
- [ ] Clasificacion de contenido completada.

AdMob:

- [ ] Mantener test IDs durante beta tecnica.
- [ ] Crear app AdMob antes de monetizacion real.
- [ ] Sustituir IDs de prueba solo cuando toque publicar con anuncios reales.

Build:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:bundleRelease
```

AAB esperado:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Pendientes recomendados post-beta cerrada

- Validacion server-side de compras.
- Consentimiento de anuncios/UMP si aplica.
- Tests de reglas Firebase.
- Tests UI de login, limites y paywall.
- Branding final: icono, screenshots y textos legales.
- Normalizacion del modelo de notas/asignaturas.
