# AdMob Setup para EduTrack

## Estado Actual
✅ AdMob está configurado con **ID de prueba** de Google  
✅ La app ahora compila sin errores  
✅ Puedes probar ads en desarrollo

## ID Actual (Prueba)
```
App ID: ca-app-pub-3940256099942544~3347511713
Banner Ad Unit: ca-app-pub-3940256099942544/6300978111
```

Estos IDs son **para desarrollo/testing SOLAMENTE**. Google devuelve ads de prueba, no reales.

---

## ⚠️ Para Producción (Antes de Subir a Play Store)

### Paso 1: Crear Proyecto en Google AdMob

1. Ve a [Google AdMob](https://admob.google.com/)
2. Sign in con tu Google Play Console account
3. Click "Create" → "App"
4. Selecciona Platform: **Android**
5. Ingresa "com.edutrack.app" como package name
6. Google buscará tu app en Play Store (o créala primero si no existe)

### Paso 2: Obtener Tu App ID Real

Después de crear la app en AdMob:
1. Ve a "App settings"
2. Copia el **App ID** (formato: `ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx`)

### Paso 3: Reemplazar IDs en el Código

#### En `AndroidManifest.xml` (línea ~24):
```xml
<!-- ANTES (Prueba) -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713" />

<!-- DESPUÉS (Tu ID Real) -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX" />
```

#### En `AdMobBanner.kt` (línea ~31):
```kotlin
// ANTES (Prueba)
const val ADMOB_APP_ID = "ca-app-pub-xxxxxxxxxxxxxxxx"

// DESPUÉS (Tu ID Real)
const val ADMOB_APP_ID = "ca-app-pub-XXXXXXXXXXXXXXXX"
```

#### En `AdMobBanner.kt` (línea ~32):
```kotlin
// ANTES (Ad Unit de Prueba)
const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

// DESPUÉS (Tu Ad Unit Real - creado en AdMob Console)
const val BANNER_AD_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
```

### Paso 4: Crear Ad Units en AdMob

En Google AdMob, para tu app:
1. Click "Ad units" (lado izquierdo)
2. Click "Create new ad unit"
3. Selecciona formato: **Banner**
4. Completa los datos
5. Google generará un **Ad Unit ID** (formato: `ca-app-pub-xxx/yyy`)
6. Copia ese ID y úsalo en `BANNER_AD_UNIT_ID`

### Paso 5: Recompilar y Subir a Play Store

```bash
./gradlew clean assembleRelease
# Subir app.aab a Play Console
```

---

## 📋 Checklist

- [ ] Crear app en Google AdMob
- [ ] Obtener App ID real
- [ ] Crear Banner Ad Unit en AdMob
- [ ] Actualizar AndroidManifest.xml con App ID real
- [ ] Actualizar AdMobBanner.kt con App ID y Ad Unit real
- [ ] Verificar que no haya IDs de prueba en el código
- [ ] Compilar versión Release
- [ ] Subir a Play Console
- [ ] Esperar aprobación de Google (24-48h)
- [ ] Publicar en Play Store

---

## 🧪 Testing IDs de Google (NO USAR EN PRODUCCIÓN)

Si necesitas más ads de prueba:

```
App ID:        ca-app-pub-3940256099942544~3347511713
Banner:        ca-app-pub-3940256099942544/6300978111
Interstitial:  ca-app-pub-3940256099942544/1033173712
Rewarded:      ca-app-pub-3940256099942544/5224354917
Native:        ca-app-pub-3940256099942544/2247696110
```

---

## ⚙️ Cómo Funcionan los Ads en la App

### Dónde se Muestran:
```
✅ InicioActivity (pantalla principal, después de cards)
✅ PerfilActivity (entre secciones)
✅ Final de LazyColumns (lista de cursos, etc)

❌ PaywallScreen (nunca mezclar ads y compras)
❌ NotasActivity (distracting mientras estudias)
❌ Simulador (interfiere con estudio)
❌ Pantallas de autenticación
```

### Lógica:
```kotlin
if (userPlan == UserPlan.FREE) {
    AdMobBanner(...)  // Solo FREE ve ads
} else {
    // PREMIUM: sin ads
}
```

---

## 💰 Monetización

Con AdMob correctamente configurado:
- Cada impresión de ad genera ingresos (varía por país)
- Los usuarios FREE ven ads
- Los usuarios PREMIUM no ven ads (incentivo para upgrade)

Google suele pagar entre $0.50 - $5.00 por 1000 impresiones (depende del país, device, contexto).

---

## 🚨 Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| "Missing application ID" | No está en AndroidManifest | Agregar `<meta-data>` tag |
| "Invalid Ad Unit ID" | Typo en el ID | Copiar/pegar directo de AdMob |
| "Blank ads" | Ad Unit bloqueado o sin permisos | Revisar AdMob Console |
| "No fill" | No hay ads disponibles en tu región | Normal al inicio, esperar días |

---

## 📞 Soporte

- [Google AdMob Docs](https://support.google.com/admob/)
- [Google Play Billing Docs](https://developer.android.com/google-play/billing)
- Comunidad: Stack Overflow tag `google-mobile-ads`

---

**Estado**: ✅ Listo para desarrollo (IDs de prueba)  
**Acción requerida**: Antes de producción, reemplazar con IDs reales
