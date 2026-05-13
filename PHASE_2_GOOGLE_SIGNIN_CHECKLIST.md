# FASE 2 — Corregir Google Sign-In

## ⚠️ ACCIÓN MANUAL REQUERIDA

El archivo `google-services.json` actual tiene una inconsistencia:
- ✅ androidClientInfo.package_name = "com.edutrack.app" (CORRECTO)
- ❌ oauth_client.android_info.package_name = "com.company.EduTrack" (INCORRECTO)
- ❌ certificate_hash puede estar desactualizado

## 📋 Checklist de Configuración

### Paso 1: Obtener SHA-1 y SHA-256 del Debug Keystore

En Windows (PowerShell):
```powershell
# SHA-1
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"

# SHA-256
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr "SHA256"
```

En Linux/macOS:
```bash
# SHA-1
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep "SHA1"

# SHA-256
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep "SHA256"
```

**Resultado esperado:**
```
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA256: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

### Paso 2: Firebase Console - Configurar Google Sign-In

1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Seleccionar proyecto "edutrack-5579f"
3. Ir a **Authentication** → **Sign-in method**
4. Habilitar **Google** si no está habilitado
5. Ir a **Settings** → **Your project** → **General**

### Paso 3: Google Cloud Console - Agregar Huellas Digitales

1. Ir a [Google Cloud Console](https://console.cloud.google.com/)
2. Seleccionar proyecto "edutrack-5579f"
3. Navegar a **APIs & Services** → **Credentials**
4. Buscar y editar la credencial OAuth 2.0 Android para com.edutrack.app
5. **Agregar dos huellas digitales:**
   - **Debug SHA-1:** (copiar del paso 1)
   - **Debug SHA-256:** (copiar del paso 1)

6. **Para Release (cuando se suban a Play Store):**
   - Ir a Google Play Console
   - Seleccionar la app "EduTrack"
   - Ir a **Setup** → **App integrity**
   - Copiar **SHA-1** y **SHA-256** de App Signing Certificate
   - Agregar ambas a Google Cloud Console OAuth

### Paso 4: Descargar google-services.json Nuevamente

1. En Firebase Console, ir a **Project settings** → **Your apps**
2. Seleccionar la app Android
3. Click en **google-services.json**
4. Reemplazar el archivo en: `app/google-services.json`
5. Verificar que contiene:
   - `"package_name": "com.edutrack.app"` (en androidClientInfo)
   - `"package_name": "com.edutrack.app"` (en oauth_client -> android_info)
   - `"certificate_hash": "XX:XX:..."` (SHA-1 actualizado)

### Paso 5: Compilar y Probar

```bash
# Limpiar gradle cache
./gradlew clean

# Compilar
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug
```

### Paso 6: Probar en Dispositivo

1. Abrir la app EduTrack
2. Click en "Iniciar sesión con Google"
3. Seleccionar cuenta de Google
4. Debería redirigir a onboarding sin errores
5. Verificar que el usuario se creó en Firebase Auth

## 🔧 Código Actualizado

### RegisteerActivity.kt (Sin cambios críticos)

La función `resolveEmailForUsername()` sigue devolviendo `null`:
```kotlin
private fun resolveEmailForUsername(username: String, onResolved: (String?) -> Unit) {
    onResolved(null)
}
```

**RAZÓN:** Solo Google Sign-In está soportado. El login por usuario/email no está implementado en backend.

Si en futuro se implementa:
1. Crear Realtime Database index en `users/emailLowercase`
2. Implementar Cloud Function `resolveEmail(email: String)`
3. Descomentar código en `RegisteerActivity.kt`

### Alternativa: Remover completamente login por usuario

Si no se va a implementar nunca:
1. Remover TextField para username
2. Dejar solo Google Sign-In
3. Remover `resolveEmailForUsername()`

## ⚠️ Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| "Provider not enabled" | Google Auth no habilitado en Firebase | Habilitar en Firebase Console |
| "OAuth configuration mismatch" | SHA-1 no coincide | Agregar SHA-1 correcto a Google Cloud |
| "Package name mismatch" | google-services.json con package incorrecto | Descargar nuevo google-services.json |
| "Client ID not found" | oauth_client no existe para el package | Crear credencial OAuth en Google Cloud |

## ✅ Verificación Final

Después de completar todos los pasos, debería ver:
- ✅ google-services.json con package_name correcto
- ✅ SHA-1 y SHA-256 agregados a Google Cloud
- ✅ Google provider habilitado en Firebase
- ✅ App compila sin errores
- ✅ Google Sign-In funciona en dispositivo

## 📚 Referencias

- [Firebase Google Sign-In Setup](https://firebase.google.com/docs/auth/android/google-signin)
- [Google Cloud OAuth Configuration](https://cloud.google.com/docs/authentication/application-default-credentials)
- [Android Debug Keystore](https://developer.android.com/studio/publish/app-signing#debug-signing)
