package com.example.edutrack.Anio

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

// --- Funciones Helper ---

fun abbreviateName(name: String): String {
    return name.split(" ")
        .filter { it.isNotEmpty() }
        .map { it.first().uppercase() }
        .joinToString("")
        .take(3)
}

/**
 * Contenedor que permite navegar entre elementos deslizando horizontalmente.
 */
@Composable
fun SwipeToNavigate(
    currentIndex: Int,
    totalItems: Int,
    onIndexChange: (Int) -> Unit,
    content: @Composable (index: Int) -> Unit
) {
    val accumulatedDrag = remember { mutableStateOf(0f) }
    val threshold = 80f

    Box(
        modifier = Modifier.pointerInput(totalItems) {
            detectHorizontalDragGestures(
                onDragStart = {
                    accumulatedDrag.value = 0f
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    accumulatedDrag.value += dragAmount
                },
                onDragEnd = {
                    if (totalItems <= 0) return@detectHorizontalDragGestures
                    when {
                        accumulatedDrag.value <= -threshold -> {
                            val nextIndex = (currentIndex + 1) % totalItems
                            onIndexChange(nextIndex)
                        }
                        accumulatedDrag.value >= threshold -> {
                            val prevIndex = (currentIndex - 1 + totalItems) % totalItems
                            onIndexChange(prevIndex)
                        }
                    }
                    accumulatedDrag.value = 0f
                }
            )
        }
    ) {
        content(currentIndex)
    }
}
