# EduTrack — Informe de cambios (resolución de los 11 problemas)

Fecha: 2026-06-24 · Rama: `kmp/full-app-base` · Compilación: ✅ `:app:compileDebugKotlin` exit 0

Alcance acordado: **Tier 1 (bloqueadores) + quick wins**. Marco legal: autónomo
España/UE (RGPD + LOPDGDD), público 13-17 años. Modo ponytail activo (solución
mínima que funciona). Salté #2 y #3 con justificación (ver abajo).

---

## Resumen por problema

| # | Problema | Estado | Qué se hizo |
|---|----------|--------|-------------|
| 11 | Sin política de privacidad | ✅ Resuelto (falta hospedar) | 5 documentos legales + enlaces in-app + AdMob menores |
| 9 | Borrado de cuenta no borra datos (GDPR) | ✅ Resuelto (requiere deploy) | Cloud Function admin que borra TODO + cuenta Auth |
| 10 | Notificaciones de examen no se postean | ✅ Resuelto | `BootReceiver` registrado en manifest |
| 8 | Billing ficticio | ✅ Código OK + checklist | Era config/deploy, no código. Checklist exacto |
| 6 | Empty state sin guía ni CTA | ✅ Ya existía + mejora | CTA ya estaba; mejorada la copy de guía |
| 4 | Paywall sin urgencia | ✅ Mejorado | CTA de prueba gratis 7 días + trust row honesto |
| 7 | Onboarding estático | ⚠️ Parcial | Existe `OnboardingScreen` en composeApp; no era foco |
| 5 | Sin loop de retención | ⏭️ No abordado | Fuera del Tier 1; requiere feature (streak/resumen) |
| 1 | Sin tests | ⏭️ Parcial | Hay tests en composeApp (GradeCalculator, PlanManager) |
| 2 | Sin ViewModel | ⏭️ Saltado (justificado) | Refactor grande; no bloquea lanzamiento |
| 3 | iOS sin paridad | ⏭️ Saltado (justificado) | Target real = Android |

---

## Detalle de los cambios de código

### #10 — Notificaciones de examen (BUG REAL arreglado)
**Causa raíz:** `BootReceiver` existía en código pero **no estaba registrado en el
manifest**. Al reiniciar el móvil, Android borra todas las alarmas exactas y nunca
se reprogramaban → las notificaciones de examen dejaban de funcionar para siempre.

- `AndroidManifest.xml`: añadido permiso `RECEIVE_BOOT_COMPLETED` y registrado el
  `<receiver android:name=".reminders.BootReceiver">` con filtro `BOOT_COMPLETED`.
- El permiso runtime `POST_NOTIFICATIONS` y la programación de alarmas ya existían y
  funcionan; el único eslabón roto era el boot receiver.

### #9 — Borrado de cuenta / RGPD (BUG REAL arreglado)
**Causa raíz:** `borrarUsuarioCompleto` solo borraba `users/{uid}`. Quedaban datos
en `groupMembers`, `userGroups`, `groupGrades`. Además `auth.delete()` fallaba en
silencio con `requires-recent-login`, dejando viva la cuenta de Auth.

- `functions/src/index.ts`: nueva Cloud Function **`deleteUserData`** (admin SDK):
  borrado multi-path atómico (`users/{uid}`, `userGroups/{uid}`, y por cada grupo
  `groupMembers/{gid}/{uid}` + `groupGrades/{gid}/{uid}`) y `admin.auth().deleteUser()`.
  Admin no sufre el error de re-login y puede limpiar nodos de grupo.
- `FuncionesGlobales.kt`: `borrarUsuarioCompleto` ahora invoca la función y cierra
  sesión; devuelve `success` para mostrar error si falla.
- `PerfilActivity.kt`: estado `deletingAccount` (evita doble toque) + Toast de error.
- Strings `perfil_delete_error` (ES/EN).
- *Requiere desplegar las funciones* (ver `docs/DEPLOY_Y_LANZAMIENTO.md`).

### #8 — Billing
El código (`BillingManager` + `verifyPremiumPurchase`) ya estaba bien implementado
(validación server-side, nunca concede premium en cliente). El problema era de
**configuración/deploy**, no de código. Entregado checklist exacto en
`docs/DEPLOY_Y_LANZAMIENTO.md` (productos en Play Console, service account,
`functions:config:set`, `firebase deploy`).

### #11 — Legal
- **5 documentos** en `docs/legal/` (subagente legal): Política de Privacidad (RGPD +
  LOPDGDD), versión EN, página de Eliminación de Datos (exigida por Play), Términos
  de Uso, y un Resumen de Integración técnica.
- `DbRefs.kt`: constantes `PRIVACY_POLICY_URL` / `TERMS_URL` (una sola fuente).
- `ConfiguracionScreen.kt`: los enlaces de Ajustes usan esas constantes.
- `RegisterActivity.kt`: el aviso legal ahora **enlaza** a Política y Términos
  (consentimiento accesible en el registro). Copy actualizada (ES/EN).
- `AdMobBanner.kt`: anuncios **no personalizados / aptos para menores**
  (`setTagForUnderAgeOfConsent(TRUE)` + `MAX_AD_CONTENT_RATING_T`).

### #6 — Empty state
Ya tenía icono + título + cuerpo + botón "Crear curso" (commit reciente). Mejorada
solo la copy del cuerpo para que guíe de verdad ("En 30 segundos: crea un curso…").

### #4 — Paywall
- `PaywallScreen.kt`: CTA principal pasa a **"Empezar 7 días gratis"** + subtítulo
  "Luego X · cancela cuando quieras" (mayor palanca de conversión). *Requiere
  configurar la oferta de prueba en Play Console* (documentado).
- Trust row: sustituido el claim no verificable "Top Educación / Play Store" por
  uno honesto y real ("Datos en la UE / Servidores RGPD"). **No** se añadieron
  testimonios falsos (Play los rechaza; ya se habían quitado en un commit previo).

---

## Revisiones realizadas
- **Compilación:** `./gradlew :app:compileDebugKotlin` → exit 0.
- **/security-review:** sin vulnerabilidades HIGH/MEDIUM. El borrado server-side
  acota todo a `context.auth.uid` (sin path traversal; claves RTDB no admiten `/`).
  El `BootReceiver` exportado es inocuo (solo reprograma alarmas del propio usuario).
- **ponytail:** diff mínimo; sin abstracciones especulativas. Única deferral
  consciente: age-gate/consentimiento parental (documentado, no construido).

---

## Lo que TÚ tienes que hacer (no se puede desde el código)
1. **Desplegar funciones** → desbloquea #8 (compra) y #9 (borrado). 
2. **Crear los productos** `edutrack_premium_monthly/annual` en Play Console + oferta
   de **prueba gratis 7 días** (o cambiar la copy del paywall).
3. **Rellenar placeholders** `[RELLENAR:` en `docs/legal/*.md` (razón social, NIF, dirección).
4. **Hospedar** Política + Eliminación de datos y poner la URL en
   `DbRefs.kt` (`PRIVACY_POLICY_URL`/`TERMS_URL`) y en Play Console.
5. **AdMob real**: sustituir App ID e ad unit de prueba por los reales.
6. **Data Safety** en Play Console (email, contenido del usuario, Advertising ID,
   menores/Familias).

Todo el detalle paso a paso está en **`docs/DEPLOY_Y_LANZAMIENTO.md`**.

## Recomendados a futuro (no bloquean lanzamiento)
- Age-gate + consentimiento parental (público 13-17).
- #5 loop de retención (streak + resumen semanal).
- #2 ViewModels y #1 tests de UI.
- Revisar `USE_EXACT_ALARM` (Play restringe ese permiso).
