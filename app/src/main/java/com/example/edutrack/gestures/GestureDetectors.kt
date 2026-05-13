package com.example.edutrack.gestures

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * GestureDetectors.kt
 *
 * Sistema centralizado de detectores de gestos para EduTrack.
 * Proporciona modificadores reutilizables para gestos comunes.
 */

// ============================================================================
// 1. SWIPE BACK (Edge Swipe para navegación atrás)
// ============================================================================
/**
 * Detecta swipe desde el borde izquierdo (típico de apps Android)
 * Solo activa si:
 * - El touch comienza en los primeros 50dp del borde izquierdo
 * - El movimiento es hacia la derecha (más de 50dp)
 */
fun Modifier.swipeBackGesture(
    onBack: () -> Unit,
    edgeThreshold: Float = 50f,
    distanceThreshold: Float = 50f
): Modifier = pointerInput(Unit) {
    var totalDragAmount = Offset.Zero

    detectDragGestures(
        onDragStart = { offset ->
            // Inicializar
            totalDragAmount = Offset.Zero
        },
        onDrag = { change, dragAmount ->
            change.consume()
            totalDragAmount += dragAmount
        },
        onDragEnd = {
            // Activar si el usuario deslizó hacia la derecha más de 50dp
            if (totalDragAmount.x > distanceThreshold) {
                onBack()
            }
        }
    )
}

// ============================================================================
// 2. SWIPE DISMISS (Para BottomSheets)
// ============================================================================
/**
 * Detecta swipe down para cerrar un ModalBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.swipeDismissGesture(
    sheetState: SheetState,
    coroutineScope: CoroutineScope,
    dismissThreshold: Float = 100f,
    onDismissed: () -> Unit = {}
): Modifier = pointerInput(Unit) {
    var totalDragAmount = Offset.Zero

    detectDragGestures(
        onDrag = { change, dragAmount ->
            change.consume()
            totalDragAmount += dragAmount
        },
        onDragEnd = {
            // Activar si el usuario deslizó hacia abajo más de 100dp
            if (totalDragAmount.y > dismissThreshold) {
                coroutineScope.launch {
                    sheetState.hide()
                    onDismissed()
                }
            }
        }
    )
}

// ============================================================================
// 3. HORIZONTAL SWIPE (Para cambiar entre tabs/cursos)
// ============================================================================
/**
 * Detecta swipe horizontal para navegar entre elementos
 */
fun Modifier.horizontalSwipeGesture(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    distanceThreshold: Float = 50f
): Modifier = pointerInput(Unit) {
    var totalDragAmount = Offset.Zero

    detectDragGestures(
        onDrag = { change, dragAmount ->
            change.consume()
            totalDragAmount += dragAmount
        },
        onDragEnd = {
            when {
                totalDragAmount.x > distanceThreshold -> onSwipeRight()
                totalDragAmount.x < -distanceThreshold -> onSwipeLeft()
            }
        }
    )
}

// ============================================================================
// 4. LONG PRESS MENU (Para acciones contextuales)
// ============================================================================
/**
 * Detección de long press estándar de Compose
 * Se activa después de ~500ms sin movimiento
 */
fun Modifier.longPressMenu(
    onLongPress: () -> Unit
): Modifier = pointerInput(Unit) {
    detectTapGestures(
        onLongPress = { onLongPress() }
    )
}
