# Bug Fix: Calendario Navigation Error

## Problema
```
java.lang.IllegalArgumentException: Wrong argument type for 'anioId' in argument savedState. string expected.
```

Ocurría cuando hacías click en el botón "Ver" en el CalendarioBottomSheet.

## Causa
En `InicioActivity.kt:1449`, se estaba pasando `null` a `onNavAsignatura()`:
```kotlin
onNavAsignatura(null)  // ❌ null no es válido
```

El sistema de navegación espera un String válido para `anioId`, no null.

## Solución
Se removió la navegación y se dejó solo cerrar el modal:

```kotlin
// ANTES
TextButton(onClick = {
    onNavAsignatura(null)  // ❌ Error
    onDismiss()
})

// DESPUÉS
TextButton(onClick = {
    onDismiss()  // ✅ Solo cierra
})
```

## Nota Sobre el Calendario
El CalendarioBottomSheet actualmente:
- ✅ Lee datos de `asignatura.fechaExamen` (antiguos)
- ✅ Muestra exámenes del mes actual
- ⚠️ Debería ser actualizado para leer de la nueva entidad `Examen` desde Firebase

**Próxima mejora:** Actualizar CalendarioBottomSheet para cargar Exámenes desde Firebase en tiempo real, en lugar de usar el campo deprecado `fechaExamen`.

## Status
✅ Compilado sin errores
✅ App debería funcionar sin crashes en navegación
