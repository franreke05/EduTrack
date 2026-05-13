/**
 * QUICK REFERENCE - Gestos EduTrack
 *
 * Copia y pega estos snippets directamente en tus Composables
 * Está 100% lista, solo cambiar nombres de funciones/states
 */

package com.example.edutrack.gestures

// ============================================================================
// SNIPPET 1: SWIPE BACK (AnioScreen - Surface raíz)
// ============================================================================

/*
Surface(
    modifier = modifier
        .fillMaxSize()
        .swipeBackGesture(onBack = onBack),  // ← COPIAR ESTA LÍNEA
    color = MaterialTheme.colorScheme.background
) {
    // contenido existente
}
*/

// ============================================================================
// SNIPPET 2: LONG PRESS EN CARD (AsignaturaCard)
// ============================================================================

/*
var showActionsMenu by remember { mutableStateOf(false) }

Box(
    modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = onClick,
            onLongClick = { showActionsMenu = true }  // ← AGREGAR onLongClick
        )
) {
    // contenido de la card

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
            leadingIcon = { Icon(Icons.Default.Delete, null) },
            onClick = { /* eliminarAsignatura() */ }
        )
    }
}
*/

// ============================================================================
// SNIPPET 3: SWIPE DOWN EN BOTTOM SHEET (CalendarioBottomSheet)
// ============================================================================

/*
@OptIn(ExperimentalMaterial3Api::class)
val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
val coroutineScope = rememberCoroutineScope()

ModalBottomSheet(
    onDismissRequest = { onDismiss() },
    modifier = Modifier.swipeDismissGesture(
        sheetState = sheetState,
        coroutineScope = coroutineScope,
        onDismissed = { onDismiss() }
    ),
    sheetState = sheetState
) {
    // Drag handle visual (indica "se puede deslizar")
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(2.dp)
            )
            .width(32.dp)
            .height(4.dp)
            .align(Alignment.CenterHorizontally)
    )

    // Tu contenido
    Column(modifier = Modifier.padding(16.dp)) {
        // Calendario, formulario, etc
    }
}
*/

// ============================================================================
// SNIPPET 4: SWIPE HORIZONTAL EN STORIES (StoriesRow)
// ============================================================================

/*
LazyRow(
    contentPadding = PaddingValues(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.horizontalSwipeGesture(
        onSwipeLeft = {
            if (currentIndex < totalCourses - 1) {
                onNavigate(currentIndex + 1)
            }
        },
        onSwipeRight = {
            if (currentIndex > 0) {
                onNavigate(currentIndex - 1)
            }
        }
    )
) {
    item { StoryItem(...) }
    // más items
}
*/

// ============================================================================
// SNIPPET 5: SWIPE UP PARA EXPANDIR CARD
// ============================================================================

/*
var isExpanded by remember { mutableStateOf(false) }

Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .verticalSwipeGesture(
            onSwipeUp = { isExpanded = true }
        )
) {
    Column {
        // Header siempre visible
        Text("Título")

        // Detalles (mostrar si expandido)
        AnimatedVisibility(visible = isExpanded) {
            Column {
                HorizontalDivider()
                Text("Créditos: X")
                Text("Examen: XX/XX/XX")
                Text("Descripción: ...")
            }
        }
    }
}
*/

// ============================================================================
// SNIPPET 6: INTELLIGENT SWIPE DETECTOR (LazyColumn completa)
// ============================================================================

/*
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = Modifier
        .fillMaxWidth()
        .intelligentSwipeDetector(
            onHorizontalSwipe = { direction ->
                if (direction > 0) {
                    // Swipe derecha
                    onNavigatePrevious()
                } else {
                    // Swipe izquierda
                    onNavigateNext()
                }
            }
        )
) {
    items(asignaturas.size) { index ->
        AsignaturaCard(asignaturas[index])
    }
}
*/

// ============================================================================
// SNIPPET 7: DRAG & REORDER (Premium Feature)
// ============================================================================

/*
var isDragging by remember { mutableStateOf(false) }
var draggedIndex by remember { mutableStateOf<Int?>(null) }
val dragState = rememberDraggableState { delta ->
    // Rastrear offset
}

LazyVerticalGrid(
    columns = GridCells.Fixed(2)
) {
    itemsIndexed(asignaturas) { index, asignatura ->
        if (isPremium) {
            AsignaturaCard(
                modifier = Modifier
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStarted = { isDragging = true; draggedIndex = index },
                        onDragStopped = { isDragging = false; draggedIndex = null }
                    ),
                asignatura = asignatura,
                isDragging = isDragging && draggedIndex == index
            )
        } else {
            AsignaturaCard(
                asignatura = asignatura,
                modifier = Modifier.combinedClickable(
                    onClick = { onOpenNotes(asignatura) }
                )
            )
        }
    }
}
*/

// ============================================================================
// SNIPPET 8: FLING PARA DISMISS (Card transitorias)
// ============================================================================

/*
var isDismissed by remember { mutableStateOf(false) }

if (!isDismissed) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .flingDetector(
                minimumVelocity = 500f,
                onFling = { vx, vy ->
                    isDismissed = true
                    onDismiss()
                }
            )
    ) {
        Text("Desliza rápido para cerrar")
    }
}
*/

// ============================================================================
// REFERENCE TABLE: VELOCIDADES Y THRESHOLDS
// ============================================================================

/*
ACCIÓN                              VELOCIDAD REQUERIDA     NOTAS
────────────────────────────────────────────────────────────────────
Swipe back (volver atrás)           > 300 dp/s + borde      Rápido + borde
Swipe normal (navegar)              > 300 dp/s              Bastante rápido
Swipe lento (scroll)                < 300 dp/s              Lento = LazyColumn
Long press (menú)                   500 ms sin movimiento   Half second
Drag (reordenar)                    150+ ms + >50px mov     Movimiento claro
Fling (dismiss)                     > 500 dp/s              Muy rápido
*/

// ============================================================================
// PARÁMETROS GLOBALES (en GestureDetectors.kt)
// ============================================================================

/*
edgeThreshold = 50f                 // Píxeles desde borde izquierdo
velocityThreshold = 300f            // dp/s mínimos
dismissThreshold = 0.3f             // 30% altura para cerrar
angleThreshold = 30f                // Grados de tolerancia
minimumVelocity = 500f              // dp/s para fling
pressDuration = 500L                // Milisegundos para long press

MÓVIL (320-599dp):
  edgeThreshold = 40f
  velocityThreshold = 250f
  angleThreshold = 35f

TABLET (600dp+):
  edgeThreshold = 60f
  velocityThreshold = 400f
  angleThreshold = 25f
*/

// ============================================================================
// DEBUGGING
// ============================================================================

/*
1. Loguear velocidad en Logcat:

   adb logcat | grep "velocity"

2. Ver ángulo y magnitud:

   .debugSwipe("MyScreen")  // Agregar este modifier

3. Revisar qué threshold se está usando:

   En GestureDetectors.kt, buscar línea ~50

4. Probar en emulador con rates variables:

   emulator -avd MyDevice -netdelay fixed-10 -netspeed full
   (Afecta gesture responsiveness)
*/

// ============================================================================
// IMPORTACIONES NECESARIAS
// ============================================================================

/*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlinx.coroutines.rememberCoroutineScope
*/

// ============================================================================
// COMMON PATTERNS
// ============================================================================

// Pattern 1: Click vs Long Click alternativo
/*
Box(
    modifier = Modifier
        .combinedClickable(
            onClick = { /* acción normal */ },
            onLongClick = { /* acción larga */ },
            onLongClickLabel = "Menú de opciones"  // Para accesibilidad
        )
)
*/

// Pattern 2: Animación en el gesto
/*
var isPressed by remember { mutableStateOf(false) }
val backgroundColor by animateColorAsState(
    targetValue = if (isPressed)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface
)

Card(modifier = Modifier.background(backgroundColor)) { ... }
*/

// Pattern 3: Múltiples gestos en el mismo elemento
/*
Box(
    modifier = Modifier
        .swipeBackGesture(onBack)                   // 1. Swipe right
        .combinedClickable(
            onClick = { ... },                      // 2. Click
            onLongClick = { ... }                   // 3. Long click
        )
        .horizontalSwipeGesture(
            onSwipeLeft = { ... },                  // 4. Swipe left
            onSwipeRight = { ... }                  // 5. Swipe right (conflictua con back!)
        )
        // CUIDADO: Múltiples swipes horizontales pueden conflictuar
)
*/

// ============================================================================
// CHECKLIST ANTES DE USAR CADA GESTO
// ============================================================================

/*
Swipe Back:
  □ ¿Es una pantalla de detalles? (sí = implementar)
  □ ¿Existe botón atrás en TopAppBar? (siempre sí)
  □ ¿Se puede volver atrás con ambos gestos?

Long Press:
  □ ¿Existe menú contextual? (sí = implementar)
  □ ¿Las opciones del menú tienen botones alternativos?
  □ ¿Se muestra hint al usuario?

Swipe Dismiss:
  □ ¿Es un Bottom Sheet? (no obligatorio pero recomendado)
  □ ¿Existe X/botón Cerrar? (siempre sí)
  □ ¿Se cierra en ambos casos?

Drag & Reorder:
  □ ¿Solo para Premium? (verificar con PlanManager)
  □ ¿Se muestra hint "Mantén presionado"?
  □ ¿Hay fallback visual si usuario se equivoca?
*/

// ============================================================================
// VERSIONADO DE CÓDIGO
// ============================================================================

/*
@Composable
fun MyScreen() {
    // v1.0: Gestores básicos
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .swipeBackGesture(onBack = onBack)  // Añadido v1.0
            .intelligentSwipeDetector(...)      // Añadido v1.1
    )
}

Documentar cambios:
  v1.0 (2025-05-13): Swipe back
  v1.1 (2025-05-20): Intelligent scroll detection
  v1.2 (2025-05-27): Long press en cards
*/

// ============================================================================
// ESTA ES TU QUICK REFERENCE
// Guarda este archivo y vuelve cuando necesites copiar un patrón
// ============================================================================
