# Guía de Integración de Gestos en EduTrack

## Resumen Ejecutivo

Sistema modular de detección de gestos sin conflictos entre:
- **Swipe vs Scroll**: Detecta velocidad e ángulo para distinguir
- **Long Press vs Drag**: Timeout de 500ms antes de permitir drag
- **Bottom Sheet Swipe**: Threshold de 30% de altura
- **Edge Swipe (atrás)**: Solo activa en primer 50dp del borde izquierdo

---

## 1. INICIO (InicioActivity)

### Gesto 1: Long Press en AnioFeedCard

**Ubicación**: `CuerpoInicioContent()` → `AnioFeedCard()`

**Cambio**:
```kotlin
// ANTES (solo click)
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
    onClick = onOpen,  // ← solo esto
    ...
)

// DESPUÉS (click + long press)
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .combinedClickable(
            onClick = onOpen,
            onLongClick = {
                // Abre menú: Abrir | Simulador | Eliminar
                showMenuFor = anio.id
            }
        ),
    ...
)
```

**Beneficio**: Usuario puede acceder a simulador sin abrir el curso primero

### Gesto 2: Swipe Up para Expandir ResumenGeneralCard (Peek Pattern)

**Ubicación**: `CuerpoInicioContent()` → agregar estado

```kotlin
var expandedCard by remember { mutableStateOf<String?>(null) }

// En el Card del resumen:
Card(
    modifier = Modifier
        .verticalSwipeGesture(
            onSwipeUp = { expandedCard = "resumen" }
        )
    ...
)
```

---

## 2. AÑO/ASIGNATURAS (AnioScreen)

### Gesto 1: Swipe Right para Volver Atrás

**Ubicación**: `AnioScreen()` → Surface raíz

```kotlin
Surface(
    modifier = modifier
        .fillMaxSize()
        .swipeBackGesture(onBack = onBack),  // ← Agregar
    color = MaterialTheme.colorScheme.background
) {
    // contenido existente
}
```

**Umbrales**:
- Debe comenzar en los primeros 50dp del borde izquierdo
- Velocidad mínima: 300 dp/s
- O distancia > 50dp

### Gesto 2: Long Press en AsignaturaCard

**Ubicación**: `AnioScreen()` → `AsignaturaCard()`

**Cambio**:
```kotlin
// ANTES
Box(
    modifier = Modifier
        .fillMaxWidth()
        .shadow(elevation = 6.dp, shape = cardShape, ...)
        .background(colorScheme.surface, cardShape)
        .combinedClickable(onClick = onClick)  // ← solo click
)

// DESPUÉS
var showActionsMenu by remember { mutableStateOf(false) }

Box(
    modifier = Modifier
        .fillMaxWidth()
        .shadow(elevation = 6.dp, shape = cardShape, ...)
        .background(colorScheme.surface, cardShape)
        .combinedClickable(
            onClick = onClick,
            onLongClick = { showActionsMenu = true }  // ← agregar
        )
) {
    // ... contenido card ...

    // Menú contextual
    DropdownMenu(
        expanded = showActionsMenu,
        onDismissRequest = { showActionsMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Editar") },
            leadingIcon = { Icon(Icons.Default.Edit, null) },
            onClick = { /* editarAsignatura() */ }
        )
        DropdownMenuItem(
            text = { Text("Compartir") },
            leadingIcon = { Icon(Icons.Default.Share, null) },
            onClick = { /* compartirAsignatura() */ }
        )
        DropdownMenuItem(
            text = { Text("Eliminar") },
            leadingIcon = { Icon(Icons.Default.Delete, null, tint = colorScheme.error) },
            onClick = { /* eliminarAsignatura() */ }
        )
    }
}
```

### Gesto 3: Swipe Up en AsignaturaCard (Expandir Detalles)

**Ubicación**: `AsignaturaCard()`

```kotlin
var isExpanded by remember { mutableStateOf(false) }
val maxHeight by animateFloatAsState(
    targetValue = if (isExpanded) 300f else 150f
)

Box(
    modifier = Modifier
        .fillMaxWidth()
        .verticalSwipeGesture(
            onSwipeUp = { isExpanded = true }
        )
    // ...
) {
    Column(modifier = Modifier.heightIn(max = maxHeight.dp)) {
        // Headers existentes

        // Detalles expandidos (nuevos)
        AnimatedVisibility(visible = isExpanded) {
            Column {
                Divider()
                Text("Créditos: ${asignatura.creditos}")
                Text("Examen: ${asignatura.fechaExamen}")
                Text("Descripción: ${asignatura.descripcion}")
            }
        }
    }
}
```

---

## 3. BOTTOM SHEETS (Calendario, Crear Asignatura)

### Gesto: Swipe Down para Cerrar

**Ubicación**: Cualquier `ModalBottomSheet`

**Cambio**:
```kotlin
// ANTES
ModalBottomSheet(
    onDismissRequest = { mostrarCalendario = false },
    modifier = Modifier.fillMaxWidth()
) {
    // contenido
}

// DESPUÉS
@OptIn(ExperimentalMaterial3Api::class)
val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

ModalBottomSheet(
    onDismissRequest = { mostrarCalendario = false },
    modifier = Modifier
        .fillMaxWidth()
        .swipeDismissGesture(
            sheetState = sheetState,
            coroutineScope = rememberCoroutineScope(),
            onDismissed = { mostrarCalendario = false }
        ),
    sheetState = sheetState
) {
    // Agregar drag handle visual
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 16.dp)
            .background(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(2.dp)
            )
            .width(32.dp)
            .height(4.dp)
            .align(Alignment.CenterHorizontally)
    )

    // contenido existente
}
```

---

## 4. TABLETS (>600dp)

### Consideraciones Especiales

```kotlin
BoxWithConstraints {
    val isTablet = maxWidth > 600.dp

    if (isTablet) {
        // En tablets, agregar gestos horizontales entre columnas
        Row(
            modifier = Modifier
                .horizontalSwipeGesture(
                    onSwipeLeft = { /* siguiente columna */ },
                    onSwipeRight = { /* columna anterior */ }
                )
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Contenido izquierdo
            }
            Column(modifier = Modifier.weight(1f)) {
                // Contenido derecho
            }
        }
    } else {
        // En móviles, comportamiento normal
    }
}
```

---

## 5. FUNCIONALIDADES PREMIUM

### Reordenar Asignaturas (Drag & Drop)

**IMPORTANTE**: Solo activar si `isPremium = true`

```kotlin
// En AnioScreen, solo con Premium:
if (isPremium) {
    // Mostrar hint: "Mantén presionado una asignatura para reordenar"
    // Implementar com.example.edutrack.gestures.ReorderableAsignaturaCard
}
```

---

## 6. CONFLICTOS RESUELTOS

### Conflicto 1: Swipe Right vs LazyRow.scroll

**Solución**: Usar `velocityThreshold > 300 dp/s`
- Si es swipe rápido (>300 dp/s) → atrás
- Si es movimiento lento → LazyRow hace scroll normal

### Conflicto 2: Swipe Up vs LazyColumn.scroll

**Solución**: Detectar ángulo del gesto
- Si ángulo < 30° de vertical → LazyColumn scroll
- Si es movimiento puro arriba → expandir card

### Conflicto 3: Long Press vs Drag

**Solución**: Timeout de 500ms
- Primeros 500ms de presión = long press
- Después de 500ms + movimiento > 50dp = drag

### Conflicto 4: Bottom Sheet swipe vs contenido scroll

**Solución**: `ModalBottomSheet` maneja automáticamente
- Si scroll es necesario adentro, no interfiere
- Swipe only desde el drag handle

---

## 7. ACCESIBILIDAD

### Mantener Botones Además de Gestos

```kotlin
// SIEMPRE tener botones explícitos:
Row {
    IconButton(onClick = { /* editar */ }) {
        Icon(Icons.Default.Edit, contentDescription = "Editar")
    }
    IconButton(onClick = { /* compartir */ }) {
        Icon(Icons.Default.Share, contentDescription = "Compartir")
    }
    IconButton(onClick = { /* eliminar */ }) {
        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
    }
}

// Long press abre el MISMO menú que estos botones
// No depender solo de gestos
```

---

## 8. CHECKLIST DE IMPLEMENTACIÓN

### Fase 1: Gestos Básicos
- [ ] Swipe Right para atrás (AnioScreen)
- [ ] Long Press en AsignaturaCard
- [ ] Swipe Down en Bottom Sheets
- [ ] Swipe Left/Right entre años (ya existe, reforzar)

### Fase 2: Gestos Avanzados
- [ ] Swipe Up para expandir cards
- [ ] Intelligent scroll detector (LazyColumn)
- [ ] Fast fling para dismiss
- [ ] Peek & Dismiss pattern en calendarios

### Fase 3: Premium Features
- [ ] Drag para reordenar asignaturas
- [ ] Drag entre columnas (tablets)
- [ ] Custom animations durante drag

### Fase 4: Testing & Polish
- [ ] Probar en diferentes velocidades
- [ ] Verificar en tablets (>600dp)
- [ ] Pruebas de accesibilidad
- [ ] Retroalimentación visual (ripples, elevation)

---

## 9. PARÁMETROS AJUSTABLES

Están en `GestureDetectors.kt`:

```kotlin
// Edge swipe threshold
edgeThreshold: Float = 50f           // píxeles desde borde

// Velocidad mínima
velocityThreshold: Float = 300f      // dp/s

// Dismiss threshold
dismissThreshold: Float = 0.3f       // 30% de altura

// Ángulo de tolerancia
angleThreshold: Float = 30f          // grados

// Fling mínimo
minimumVelocity: Float = 500f        // dp/s
```

Ajustar según feedback de usuarios.

---

## 10. RECURSOS

- **Documentación oficial**: androidx.compose.foundation.gestures
- **Patrón Peek & Dismiss**: Material Design Guidelines
- **Velocidad**: Velocity = pixels / milliseconds
- **Ángulo**: Math.atan2(velocityY, velocityX) * 180 / PI

---

## Preguntas Frecuentes

**Q: ¿Qué pasa si no detecto el swipe?**
A: Revisa `velocityThreshold` (por defecto 300 dp/s) y `edgeThreshold` (por defecto 50dp)

**Q: ¿Cómo hago que el swipe sea más "suave"?**
A: Usa `Spring` animationSpec en lugar de `tween`:
```kotlin
animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
```

**Q: ¿Funciona en tablets?**
A: Sí, pero verifica `isTablet = maxWidth > 600.dp` antes de cambiar comportamiento

**Q: ¿Puedo combinar múltiples gestos?**
A: Sí, usa `Modifier.then()` para encadenarlos:
```kotlin
Modifier
    .swipeBackGesture(onBack)
    .horizontalSwipeGesture(onLeft, onRight)
```

---

**Última actualización**: 2025-05-13
**Versión**: 1.0
