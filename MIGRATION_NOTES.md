# Notas de migracion Firebase de EduTrack

Este documento resume el cambio de estructura de Firebase Realtime Database y te deja una guia de carpetas/archivos para revisar el codigo con orden.

## Estructura beta antigua

- `Edutrack/Usuario/{uid}`
- `Edutrack/Anio/{yearId}`
- `Edutrack/Asignatura/{subjectId}`
- `Edutrack/Asignatura/{subjectId}/Notas/{noteId}`
- `Edutrack/Groups/{groupId}` con `members` dentro del propio grupo
- `Edutrack/UserGroups/{uid}/{groupId}`

## Problemas detectados

- Los datos academicos estaban en colecciones globales y dependian de filtros tipo `id_user == auth.uid`.
- Las asignaturas estaban duplicadas entre `Anio/lista_asignaturas` y `Asignatura`.
- Las notas colgaban de una asignatura global y no tenian suficiente contexto de propietario.
- Perfil, settings y estado premium estaban mezclados en un unico nodo de usuario.
- Los miembros de grupo estaban embebidos dentro del grupo, lo que complicaba indices, permisos y reglas.
- Premium no debe depender de un boolean que el cliente pueda escribir.
- Borrar cursos/asignaturas/notas podia dejar datos huerfanos por escrituras parciales.

## Nueva estructura beta

```text
users/{uid}/profile
users/{uid}/settings
users/{uid}/years/{yearId}
users/{uid}/years/{yearId}/subjects/{subjectId}
users/{uid}/years/{yearId}/subjects/{subjectId}/notes/{noteId}
users/{uid}/premiumCache

groups/{groupId}
groupMembers/{groupId}/{uid}
userGroups/{uid}/{groupId}
groupMessages/{groupId}/{messageId}
groupSharedSubjects/{groupId}/{subjectId}
usernames/{normalizedUsername}
```

## Cambio incompatible

Esta migracion no intenta leer datos antiguos de `Edutrack/*`. Como la app sigue en beta, lo mas limpio es empezar con Realtime Database vacia o ejecutar una migracion puntual con permisos de administrador antes de publicar estas reglas.

## Guia de lectura por carpetas

Lee los archivos en este orden si quieres entender el cambio de menor a mayor nivel.

```text
EduTrack/
+-- firebase-rules.json
|   +-- Reglas finales de Realtime Database. Empieza aqui para ver que paths existen y quien puede leer/escribir.
|
+-- MIGRATION_NOTES.md
|   +-- Este documento. Resume la migracion, riesgos y orden de lectura.
|
+-- app/src/main/java/com/example/edutrack/
    +-- data/firebase/
    |   +-- FirebasePaths.kt
    |       +-- Centraliza todos los paths nuevos: users, groups, groupMembers, userGroups, usernames.
    |
    +-- dataclass/
    |   +-- Usuario.kt
    |   |   +-- Modelo de perfil/settings sin password ni premium editable.
    |   +-- Anio.kt
    |   |   +-- Curso/ano con ownerId, createdAt y updatedAt.
    |   +-- Asignatura.kt
    |   |   +-- Asignatura con ownerId, yearId, subjectId, average, noteCount y usedPercentage.
    |   +-- Notas.kt
    |   |   +-- Nota con ownerId, yearId, subjectId, grade, percentage y period.
    |   +-- Group.kt
    |       +-- Grupo, miembro, settings y privacidad de notas.
    |
    +-- data/auth/
    |   +-- AuthRepository.kt
    |       +-- Crea/actualiza users/{uid}/profile y settings. No guarda passwords.
    |
    +-- data/profile/
    |   +-- UserPreferencesRepository.kt
    |       +-- Guarda personalizacion en users/{uid}/settings/preferences.
    |
    +-- data/groups/
    |   +-- GroupRepository.kt
    |       +-- Crea grupos, une miembros y actualiza groupMembers + userGroups con updateChildren.
    |
    +-- data/premium/
    |   +-- BillingRepository.kt
    |   |   +-- Gestiona Google Play Billing y cache local.
    |   +-- PremiumManager.kt
    |       +-- Expone estado premium local. No debe ser fuente definitiva en produccion abierta.
    |
    +-- FuncionesGlobales.kt
    |   +-- Punto principal de escrituras academicas: crear curso, crear asignatura, guardar/borrar nota y borrar usuario.
    |
    +-- Inicio/
    |   +-- InicioActivity.kt
    |   |   +-- Lee cursos desde users/{uid}/years y borra cursos en el path privado.
    |   +-- CreacionAnioActivity.kt
    |   |   +-- Crea cursos usando la nueva estructura.
    |   +-- ControladorInicioScreen.kt
    |       +-- Parseo de anos/asignaturas y calculo de medias.
    |
    +-- Anio/
    |   +-- AnioActivity.kt
    |       +-- Crea asignaturas dentro de users/{uid}/years/{yearId}/subjects.
    |
    +-- Notas/
    |   +-- NotasActivity.kt
    |       +-- Lee y escribe notas dentro de users/{uid}/years/{yearId}/subjects/{subjectId}/notes.
    |
    +-- Perfil/
    |   +-- PerfilActivity.kt
    |   |   +-- UI de perfil, personalizacion y borrado de cuenta.
    |   +-- ControladorPerfilScreen.kt
    |       +-- Lee profile + settings desde users/{uid}.
    |
    +-- feature/groups/
    |   +-- GroupScreens.kt
    |       +-- Pantallas de grupos leyendo groups + groupMembers + userGroups.
    |
    +-- navigation/
    |   +-- NavigationActivity.kt
    |       +-- Pasa yearId/subjectId a las pantallas para que las notas sepan su path completo.
    |
    +-- domain/
    |   +-- grades/GradeSimulator.kt
    |   |   +-- Logica del simulador de nota.
    |   +-- stats/AcademicStats.kt
    |       +-- Estadisticas academicas calculadas desde cursos/asignaturas/notas.
    |
    +-- ui/
        +-- components/
        |   +-- Componentes visuales reutilizables.
        +-- theme/
            +-- Tema visual de la app.
```

## Archivos clave para revisar primero

1. `firebase-rules.json`
2. `app/src/main/java/com/example/edutrack/data/firebase/FirebasePaths.kt`
3. `app/src/main/java/com/example/edutrack/FuncionesGlobales.kt`
4. `app/src/main/java/com/example/edutrack/data/auth/AuthRepository.kt`
5. `app/src/main/java/com/example/edutrack/data/groups/GroupRepository.kt`
6. `app/src/main/java/com/example/edutrack/Inicio/InicioActivity.kt`
7. `app/src/main/java/com/example/edutrack/Anio/AnioActivity.kt`
8. `app/src/main/java/com/example/edutrack/Notas/NotasActivity.kt`
9. `app/src/main/java/com/example/edutrack/Perfil/ControladorPerfilScreen.kt`
10. `app/src/main/java/com/example/edutrack/feature/groups/GroupScreens.kt`

## Validaciones que aun quedan en cliente

- Usuarios gratis limitados a 2 cursos.
- Usuarios gratis limitados a 1 grupo.
- Crear grupo esta bloqueado en UI si no hay Premium.
- Limites de asignaturas por curso.
- Suma de porcentajes de notas hasta 100%.

El repository vuelve a comprobar el porcentaje antes de escribir, pero Realtime Database Rules no puede validar agregados de forma fuerte cuando varias notas cambian a la vez.

## Trabajo de servidor requerido antes de produccion abierta

- Validar compras de Google Play en un servidor confiable.
- Escribir `users/{uid}/premiumCache` desde servidor, no desde cliente.
- Hacer cumplir limites Premium/freemium server-side.
- Validar de forma transaccional que el porcentaje total de notas no supere `100`.
- Si se activa login por username, reclamar/renombrar `usernames/{normalizedUsername}` mediante backend o transaccion segura.

## Como probar aislamiento entre usuarios

Con Firebase Emulator Suite:

1. Inicia sesion como usuario A.
2. Escribe en `users/A/years/...`: debe funcionar.
3. Intenta leer `users/B/profile`: debe fallar.
4. Intenta escribir `users/B/years/...`: debe fallar.
5. Intenta escribir `users/A/premiumCache`: debe fallar.
6. Crea un grupo como A y entra como B.
7. B debe poder leer `groups/{groupId}` solo si existe `groupMembers/{groupId}/B`.
8. Un usuario que no sea miembro no debe poder leer `groups/{groupId}`.

## Comandos de verificacion

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:bundleRelease
```

## AAB generado

```text
app/build/outputs/bundle/release/app-release.aab
```
