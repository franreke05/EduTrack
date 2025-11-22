package com.example.edutrack.Anio

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Un contenedor que permite navegar entre elementos (como años) deslizando
 * el dedo hacia la izquierda o la derecha.
 *
 * @param currentIndex El índice del elemento actual.
 * @param totalItems El número total de elementos.
 * @param onIndexChange La función lambda que se llama cuando el índice cambia.
 * @param content El contenido que se mostrará y que se podrá deslizar.
 */
@Composable
fun SwipeToNavigate(
    currentIndex: Int,
    totalItems: Int,
    onIndexChange: (Int) -> Unit,
    content: @Composable (index: Int) -> Unit
) {
    // Este estado asegura que solo cambiemos el índice una vez por gesto de deslizamiento.
    var dragConsumed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.pointerInput(totalItems) { // La clave es totalItems para reiniciar si la lista cambia.
            detectHorizontalDragGestures(
                onDragStart = {
                    // Reinicia la bandera al comenzar un nuevo arrastre.
                    dragConsumed = false
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()

                    // Solo actuamos una vez por gesto para evitar múltiples cambios de página.
                    if (!dragConsumed) {
                        // Un dragAmount negativo significa un deslizamiento de derecha a izquierda (para el siguiente elemento).
                        if (dragAmount < -50) { // Usando un umbral de 50px para activar el cambio.
                            val nextIndex = (currentIndex + 1) % totalItems
                            onIndexChange(nextIndex)
                            dragConsumed = true // Marca el gesto como consumido para este arrastre.
                        }
                        // Un dragAmount positivo significa un deslizamiento de izquierda a derecha (para el elemento anterior).
                        else if (dragAmount > 50) {
                            val prevIndex = (currentIndex - 1 + totalItems) % totalItems
                            onIndexChange(prevIndex)
                            dragConsumed = true // Marca el gesto como consumido para este arrastre.
                        }
                    }
                }
            )
        }
    ) {
        content(currentIndex)
    }
}
