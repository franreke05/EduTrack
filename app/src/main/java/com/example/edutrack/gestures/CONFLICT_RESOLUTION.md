# Resolución de Conflictos de Gestos en EduTrack

## Problema General

Cuando múltiples gestos comparten el mismo input (por ejemplo, swipe horizontal en un LazyRow que también permite scroll), el sistema debe decidir cuál activar.

---

## 1. CONFLICTO: Swipe Back vs LazyRow Scroll

### Escenario
- Usuario en AnioScreen está viendo grid de asignaturas
- Comienza swipe desde borde izquierdo
- ¿Es intención de volver atrás O scrollear?

### Solución Implementada

**Estrategia: Detectar velocidad y posición**

```kotlin
fun Modifier.swipeBackGesture(
    onBack: () -> Unit,
    edgeThreshold: Float = 50f,      // Solo primer 50dp del borde
    velocityThreshold: Float = 300f  // Velocidad mínima
): Modifier = pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { offset ->
            // ✓ Solo si comienza en el primer 50dp del borde izquierdo
            if (offset.x <= edgeThreshold) {
                // Válido, puede ser back gesture
            } else {
                // Ignorar, probablemente es scroll
                return@detectDragGestures
            }
        },
        onDragEnd = { velocity ->
            // ✓ Y velocidad es "rápida" (>300 dp/s)
            if (velocity.x > velocityThreshold) {
                onBack()
            }
            // Sino, LazyRow hace su scroll normal
        }
    )
}
```

### Resultado

| Gestura | Interpretación |
|---------|----------------|
| Swipe lento desde centro | LazyRow scroll |
| Swipe rápido desde borde izquierdo | Atrás |
| Long press + drag lento | Reordenar (si Premium) |

---

## 2. CONFLICTO: Swipe Up vs LazyColumn Scroll

### Escenario
- Usuario scrollea LazyColumn de cards
- ¿Es scroll normal O intención de expandir card?

### Solución Implementada

**Estrategia: Detectar ángulo del movimiento**

```kotlin
fun Modifier.intelligentSwipeDetector(
    onHorizontalSwipe: (direction: Int) -> Unit = {},
    onVerticalSwipe: (direction: Int) -> Unit = {},
    angleThreshold: Float = 30f  // tolerancia de 30 grados
): Modifier = pointerInput(Unit) {
    detectDragGestures(
        onDragEnd = { velocity ->
            // Calcular ángulo: 0° = horizontal, 90° = vertical
            val angle = atan2(velocity.y, velocity.x) * 180f / PI
            
            val isHorizontal = 
                angle.absoluteValue < angleThreshold ||  // -30° a +30°
                angle.absoluteValue > 180f - angleThreshold  // 150° a 180°
            
            val isVertical = 
                (angle - 90f).absoluteValue < angleThreshold ||  // 60° a 120°
                (angle + 90f).absoluteValue < angleThreshold
            
            when {
                isHorizontal && velocity.x > 300f -> onHorizontalSwipe(1)    // Derecha
                isHorizontal && velocity.x < -300f -> onHorizontalSwipe(-1)  // Izquierda
                isVertical && velocity.y > 300f -> onVerticalSwipe(1)        // Abajo
                isVertical && velocity.y < -300f -> onVerticalSwipe(-1)      // Arriba
            }
        }
    )
}
```

### Diagrama de Ángulos

```
                    -90° (arriba)
                        |
        -135°  -90°     |  -45°
          \      |      |    /
           \     |      |   /
  ±180° ----+----+------+----+---- 0° (derecha)
           /     |      |    \
          /      |      |     \
        135°     90°   90°    45°
                        |
                      90° (abajo)

ZONA HORIZONTAL (ángulo < 30°): Permite swipe izq/der
ZONA VERTICAL (ángulo ~90°): Permite swipe arriba/abajo
ZONA MUERTA (30° a 60°): LazyColumn hace scroll normal
```

### Resultado

| Movimiento | Interpretación |
|-----------|----------------|
| Swipe puro arriba (85°) | Expandir card |
| Swipe puro abajo (95°) | Contraer card |
| Swipe diagonal 45° | LazyColumn scroll |
| Swipe izquierda rápida | Ir atrás O cambiar página |

---

## 3. CONFLICTO: Long Press vs Drag

### Escenario
- Usuario mantiene presionado una card
- Después comienza a moverla
- ¿Es long press (abrir menú) O drag (reordenar)?

### Solución Implementada

**Estrategia: Timeout de 500ms + movimiento mínimo**

```kotlin
// EN COMPOSABLE
val LONG_PRESS_DURATION = 500L  // milisegundos
var pressStartTime by remember { mutableStateOf<Long?>(null) }
var isDragging by remember { mutableStateOf(false) }

Card(
    modifier = Modifier.combinedClickable(
        onClick = { /* normal click */ },
        onLongClick = { 
            if (!isDragging) {  // ✓ Solo si NO hay drag activo
                openContextMenu()
            }
        }
    )
    .draggable(
        state = dragState,
        orientation = Orientation.Vertical,
        onDragStarted = { 
            isDragging = true  // ✓ Marcar que está dragging
        },
        onDragStopped = { 
            isDragging = false
        }
    )
)
```

### Timeline de Eventos

```
Tiempo (ms)     Acción              Estado
0               User presiona       pressStartTime = 0
100             -                   -
200             -                   -
300             -                   -
400             -                   -
500             Timeout finaliza    onLongClick() ← ACTIVA MENÚ
                User sigue presionando
550             User mueve 10dp     (ignorado, ya se abrió menú)

--- ALTERNATIVA CON DRAG ---

Tiempo (ms)     Acción              Estado
0               User presiona       pressStartTime = 0
100             -                   -
200             -                   -
300             User mueve 60dp     isDragging = true
                (>50dp threshold)   ✓ CANCELA long press
400             -                   -
500             Timeout expira      onLongClick() ← NO ACTIVA (isDragging=true)
                                    ✓ PERMITE DRAG NORMAL
```

### Resultado

| Acción | Duración | Resultado |
|--------|----------|-----------|
| Presión 500ms sin movimiento | 500ms | Abre menú |
| Presión + movimiento <500ms | Variable | Comienza drag (Premium) |
| Click rápido (<200ms) | <200ms | Abre notas |

---

## 4. CONFLICTO: Bottom Sheet Swipe vs Contenido Scroll

### Escenario
- Bottom sheet abierto con lista scrollable adentro
- User scrollea lista vs quiere cerrar sheet

### Solución Implementada

**Estrategia: ModalBottomSheet maneja capas automáticamente**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false  // ✓ Permite estados intermedios
    ),
    modifier = Modifier.swipeDismissGesture(
        sheetState = sheetState,
        dismissThreshold = 0.3f  // ✓ 30% de altura para cerrar
    )
) {
    LazyColumn {
        // ✓ Scroll aquí funciona normal
        // ✓ Sheet swipe solo se activa en drag handle
        items(100) { index ->
            Text("Item $index")
        }
    }
}
```

### Comportamiento

```
Estado          Acción                Resultado
Closed          Swipe desde abajo      Sheet abre a 50%
Peeking (50%)   Swipe arriba           Sheet expande (100%)
Expanded        Swipe abajo            Sheet se oculta parcialmente
                Swipe contenido        LazyColumn scrollea
                Swipe >30% de altura   Sheet cierra
```

### Drag Handle Visual

```kotlin
// Agregar esto en el sheet para indicar que se puede deslizar:
Box(
    modifier = Modifier
        .padding(vertical = 8.dp)
        .background(
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            shape = RoundedCornerShape(2.dp)
        )
        .width(32.dp)
        .height(4.dp)
)
```

---

## 5. CONFLICTO: Stories Horizontal Swipe vs LazyRow Scroll

### Escenario
- StoriesRow en InicioActivity (horizontal)
- Swipe izquierda: ¿ir al siguiente curso O scroll de la row?

### Solución Implementada

**Estrategia: Velocidad + Dirección clara**

```kotlin
fun Modifier.storySwipeNavigation(
    onNavigateNext: () -> Unit = {},
    onNavigatePrev: () -> Unit = {}
): Modifier = pointerInput(Unit) {
    detectDragGestures(
        onDragEnd = { velocity ->
            // ✓ Velocidad muy clara (>300 dp/s)
            when {
                velocity.x > 300f -> onNavigatePrev()   // Derecha
                velocity.x < -300f -> onNavigateNext()  // Izquierda
                // Sino: LazyRow hace scroll normal
            }
        }
    )
}
```

### Resultado

| Velocidad | Resultado |
|-----------|-----------|
| >300 dp/s izquierda | Ir al siguiente curso |
| <300 dp/s izquierda | Scroll suave |
| >300 dp/s derecha | Ir al curso anterior |

---

## 6. MATRIZ DE PRIORIDAD DE GESTOS

Cuando hay ambigüedad, este orden determina qué gesto "gana":

```
Prioridad   Gesto                               Cuando Aplica
1 (máxima)  Edge Swipe Back                     edgeThreshold < 50dp AND velocityX > 300
2           Bottom Sheet Dismiss                isInBottomSheet AND dragDistance > threshold
3           Long Press (Menú)                   pressDuration > 500ms AND !isDragging
4           Drag & Reorder                      isDragging AND isPremium AND movement > 50dp
5           Swipe Up/Down (expandir)            velocity puro vertical AND |angle - 90°| < 30°
6           Swipe Horizontal (cambiar página)   velocity puro horizontal AND |angle| < 30°
7 (mínima)  LazyColumn/Row Scroll              Cualquier movimiento restante
```

---

## 7. TESTING DE CONFLICTOS

### Test 1: Swipe Back vs LazyRow

```kotlin
// Simular swipe izquierda desde centro de pantalla
val pointerInput = PointerInputScope()
val startOffset = Offset(300f, 500f)  // Centro
val endOffset = Offset(600f, 500f)    // Hacia derecha
val duration = 200L  // 600 dp/s → debería activar back

// Esperado: Llama onBack() en lugar de scroll
```

### Test 2: Long Press vs Drag

```kotlin
// Presionar 300ms sin movimiento
pressDuration = 300L
movement = 0f
// Esperado: Menú NO abre (aún no llega a 500ms)

// Presionar 600ms sin movimiento
pressDuration = 600L
movement = 0f
// Esperado: Menú ABRE

// Presionar 300ms + movimiento >50dp
pressDuration = 300L
movement = 75f
// Esperado: Comienza drag, NO abre menú
```

### Test 3: Sheet Swipe vs Contenido Scroll

```kotlin
// Swipe arriba en LazyColumn adentro del sheet
// Esperado: LazyColumn scrollea, NO cierra sheet

// Swipe abajo > 30% altura
// Esperado: Sheet se cierra
```

---

## 8. AJUSTES RECOMENDADOS POR DISPOSITIVO

### Móviles (320-599dp)

```kotlin
// Umbrales más sensibles
edgeThreshold = 40f              // Borde más pequeño
velocityThreshold = 250f         // Menos velocidad requerida
angleThreshold = 35f             // Más tolerancia en ángulos
```

### Tablets (600dp+)

```kotlin
// Umbrales menos sensibles (pantalla más grande)
edgeThreshold = 60f              // Borde más grande
velocityThreshold = 400f         // Más velocidad requerida
angleThreshold = 25f             // Menos tolerancia en ángulos
```

---

## 9. DEBUGGING

### Loguear Gestos

```kotlin
fun Modifier.debugSwipe(tag: String): Modifier = pointerInput(Unit) {
    detectDragGestures(
        onDrag = { change, dragAmount ->
            Log.d(tag, "Drag: ${dragAmount.x}, ${dragAmount.y}")
        },
        onDragEnd = { velocity ->
            val angle = atan2(velocity.y, velocity.x) * 180f / PI
            val mag = sqrt(velocity.x * velocity.x + velocity.y * velocity.y)
            Log.d(tag, "End velocity: ${velocity.x}, ${velocity.y}")
            Log.d(tag, "Angle: $angle°, Magnitude: $mag dp/s")
        }
    )
}

// Usar en desarrollo:
// Box(modifier = Modifier.debugSwipe("MyScreen"))
```

### Verificar Umbrales

```kotlin
// En Logcat, buscar:
adb logcat | grep "End velocity"

// Ejemplo:
// End velocity: 400, 50  ← Swipe rápido derecha
// Angle: 7°, Magnitude: 401 dp/s ← Horizontal puro
```

---

## 10. CHECKLIST DE VALIDACIÓN

Antes de shipping, verificar:

- [ ] Swipe back funciona solo desde borde izquierdo
- [ ] LazyRow scroll no interfiere con page navigation
- [ ] Long press no activa si hay drag activo
- [ ] Bottom sheet dismiss no interfiere con contenido scrollable
- [ ] Velocidades están calibradas (no muy sensible ni muy lenta)
- [ ] Funciona en móviles Y tablets
- [ ] Accesibilidad: botones siguen funcionando
- [ ] Performance: sin lag o jank durante gestos
- [ ] Testing: reproducir escenarios en Logcat

---

**Última actualización**: 2025-05-13
**Versión**: 1.0-conflict-resolution
