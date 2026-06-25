# Resumen de integración legal — EduTrack (para el desarrollador)

Este documento NO es legal: es una checklist técnica para cumplir con el RGPD/LOPDGDD y los requisitos de Google Play. Resume qué tocar en el código, qué placeholders rellenar, dónde hospedar las políticas y qué declarar en Play Console.

---

## (a) Cambios MÍNIMOS en la App

### 1. Consentimiento + enlace a la política en el registro `[ ]`
- En la pantalla de **registro**, añade un **checkbox obligatorio** (no premarcado): *"He leído y acepto la [Política de Privacidad] y los [Términos de Uso]"* con enlaces que abran las URLs públicas.
- Bloquea el botón de registro hasta que el checkbox esté marcado.
- Guarda evidencia del consentimiento (timestamp + versión de la política) en el perfil del usuario, p. ej. `users/{uid}/consent/{acceptedAt, policyVersion}`.

### 2. Enlace a la política y términos en Ajustes/Perfil `[ ]`
- Añade en **Perfil** (o Ajustes) entradas que abran: Política de Privacidad, Términos de Uso y Eliminación de datos. Suficiente con abrir las URLs públicas en el navegador.

### 3. Age-gate (control de edad) en el registro `[ ]`
- Añade un campo de **fecha de nacimiento** (o edad) en el registro, antes de crear la cuenta.
- Lógica: si edad < edad de consentimiento (14 ES / hasta 16 UE), marca el usuario como menor (`users/{uid}/isMinor = true`) y solicita **consentimiento parental verificable** antes de continuar (mínimo: confirmación + correo del tutor; idealmente verificación real).
- Persiste un flag de menor para usarlo en la configuración de anuncios.

### 4. Configurar AdMob para menores (NO personalizados) `[ ]`
- Hoy el manifest usa el **App ID de prueba de Google** (`ca-app-pub-3940256099942544~3347511713`). **Sustitúyelo por tu App ID real de AdMob** antes de publicar.
- Configura el SDK para tratamiento de menores y anuncios no personalizados. En `MobileAds`:
  - `RequestConfiguration.tagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)` (o `tagForUnderAgeOfConsent`) cuando el usuario es menor.
  - `setMaxAdContentRating(MAX_AD_CONTENT_RATING_G)`.
  - Pasa `npa=1` (non-personalized ads) en el `AdRequest` para usuarios sin consentimiento o menores.
- Recomendado: integrar el **User Messaging Platform (UMP) SDK** de Google para el consentimiento de anuncios (GDPR/CMP) de usuarios adultos en la UE.

### 5. (Verificar) Borrado completo en "Eliminar cuenta" `[ ]`
- Existe `deleteUserAccount(uid)` y el botón en **Perfil → Eliminar cuenta**. Confirma que borra TODO: nodo del usuario en Realtime Database (perfil, académico, grupos, premiumCache) **y** la cuenta de Firebase Authentication (puede requerir re-autenticación reciente del usuario).

---

## (b) Placeholders que DEBES rellenar

En los 5 documentos de `docs/legal/` (busca `[RELLENAR: ...]`):

- `[RELLENAR: razón social / nombre del autónomo]`
- `[RELLENAR: NIF/DNI]`
- `[RELLENAR: dirección fiscal]`

El correo de privacidad (franreke506@gmail.com), la fecha (2026-06-24) y la región de datos (`europe-west1`, UE) ya están rellenados y verificados en el código.

---

## (c) Dónde hospedar las políticas (Google Play exige URL pública)

Google Play exige una **URL pública** para la Política de Privacidad y para la Eliminación de datos. Opciones gratuitas:

1. **GitHub Pages** (recomendado, cero coste): activa Pages sobre la carpeta `docs/` del repo. Las URLs quedarían tipo `https://<usuario>.github.io/EduTrack/legal/POLITICA_DE_PRIVACIDAD.html`. Renderiza Markdown automáticamente con un tema.
2. **Firebase Hosting** (ya usas Firebase): `firebase init hosting`, sube los `.md` convertidos a `.html`, despliega con `firebase deploy`. URL tipo `https://edutrack-5579f.web.app/legal/...`.

Necesitas al menos 2 URLs públicas: **Política de Privacidad** y **Eliminación de datos**. Conviene publicar también Términos e inglés.

---

## (d) Qué declarar en el formulario de Data Safety de Play Console

En **Play Console → Contenido de la app → Seguridad de los datos**:

- **Recopila datos:** SÍ.
- **Datos personales:**
  - *Correo electrónico* — recopilado, vinculado al usuario. Finalidad: gestión de la cuenta. (Firebase Auth)
  - *Nombre de usuario* — recopilado, vinculado. Finalidad: funciones de la app.
- **Información de la app / contenido del usuario:**
  - *Otra información generada por el usuario* (notas, asignaturas, exámenes, feed de grupo) — recopilada, vinculada. Finalidad: funciones de la app.
- **Identificadores del dispositivo / publicidad:**
  - *Advertising ID* — recopilado. Finalidad: **Publicidad**. (AdMob)
- **¿Se comparten datos con terceros?** Con Google (proveedor/encargado) para alojamiento, autenticación y publicidad. Declara compartición acorde a AdMob.
- **Cifrado en tránsito:** SÍ (HTTPS/TLS).
- **¿El usuario puede solicitar el borrado de datos?** SÍ — aporta la URL de Eliminación de datos.
- **Público objetivo y contenido (Families):** declara que la app está dirigida también a menores. Esto activa el **programa de Familias** de Google Play y la obligación de **anuncios no personalizados** y SDKs aprobados para familias. Revisa que AdMob esté en la lista de SDKs autocertificados para familias.
- **Sección de Privacidad:** pega la URL pública de la Política de Privacidad.

> Nota: El estado premium (premiumCache) es un flag interno; los datos de pago los trata Google Play, no la app, y se declaran por separado según corresponda.

---

## Checklist rápido antes de publicar

- [ ] Rellenados los 3 placeholders en los 5 documentos.
- [ ] Documentos hospedados con URL pública (Privacidad + Eliminación de datos como mínimo).
- [ ] Checkbox de consentimiento + enlaces en el registro.
- [ ] Enlaces a políticas en Perfil/Ajustes.
- [ ] Age-gate + flag de menor + consentimiento parental.
- [ ] AdMob: App ID real + tratamiento de menores + anuncios no personalizados.
- [ ] Verificado que "Eliminar cuenta" borra RTDB + Firebase Auth.
- [ ] Formulario de Data Safety completado y URL de privacidad pegada.
