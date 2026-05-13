# Calendario Actualizado - Carga de Exámenes desde Firebase

## Cambios Realizados

### Antes ❌
- Calendario mostraba `asignatura.fechaExamen` (datos antiguos)
- Mostraba asignaturas con notas
- No reflejaba los nuevos Exámenes

### Ahora ✅
- Calendario carga **Exámenes reales** desde Firebase
- Muestra solo exámenes sin calificación
- Carga en tiempo real usando DisposableEffect
- Los exámenes creados en NotasActivity aparecen en el calendario

## Implementación

### Cambios en CalendarioBottomSheet:

```kotlin
@Composable
private fun CalendarioBottomSheet(
    anios: List<Anio>,
    userId: String?,  // ✅ Nuevo parámetro
    onDismiss: () -> Unit,
    onNavAsignatura: (String?) -> Unit
) {
    val examenesState = remember { mutableStateOf<Map<Int, List<Pair<String, String>>>>(emptyMap()) }
    
    // Carga exámenes de Firebase en tiempo real
    DisposableEffect(anios, userId) {
        // Para cada año → asignatura → carga los exámenes
        // Filtra por mes/año actual
        // Construye examensByDay
    }
}
```

### Flujo de Datos:

```
NotasActivity
  ↓ (Usuario crea examen)
Firebase: examenesRef(userId, anioId, asigId)
  ↓ (CalendarioBottomSheet está listening)
DisposableEffect
  ↓
Renderiza exámenes en morado
```

## Qué Ver en el Calendario

**En Morado:**
- ✅ Exámenes creados en NotasActivity
- ✅ Solo nombre y hora
- ✅ Sin calificación

**NO debe ver:**
- ❌ Fecha de asignatura (removida)
- ❌ Asignaturas con notas
- ❌ Datos antiguos

## Variables Clave

- `examenesState` - Mapa de exámenes por día
- `allDaysInMonth` - Días con exámenes
- `selectedDay` - Día seleccionado

## Notas Técnicas

- Usa `DisposableEffect` para listener de Firebase
- Completa queries solo cuando se finalizan todos
- Parsea fecha `dd/MM/yyyy` desde Examen
- Filtra por mes/año actual
