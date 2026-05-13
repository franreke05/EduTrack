package com.example.edutrack.gestures

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * GestureExamples.kt
 *
 * Ejemplos prácticos de cómo implementar gestos en EduTrack.
 * Código listo para copiar y pegar en componentes reales.
 */

// ============================================================================
// EJEMPLO 1: LONG PRESS EN ASIGNATURA CARD
// ============================================================================

@Composable
fun CardWithLongPressMenu(
    title: String,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .combinedClickable(
                onClick = { /* normal click */ },
                onLongClick = { showMenu = true }  // ← Long press activa menú
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // Menú contextual (aparece al long press)
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                DropdownMenuItem(
                    text = { Text("Editar") },
                    onClick = { onEdit(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                )
                DropdownMenuItem(
                    text = { Text("Compartir") },
                    onClick = { onShare(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Share, null) }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = { onDelete(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Delete, null) }
                )
            }
        }
    }
}

// ============================================================================
// EJEMPLO 2: SWIPE BACK EN PANTALLA COMPLETA
// ============================================================================

@Composable
fun ScreenWithSwipeBack(
    title: String,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .swipeBackGesture(onBack = onBack)  // ← Swipe desde borde izquierdo
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )
            Text(
                text = "(Desliza desde el borde izquierdo para volver)",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ============================================================================
// EJEMPLO 3: HORIZONTAL SWIPE EN ROW
// ============================================================================

@Composable
fun ContainerWithHorizontalSwipe(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .horizontalSwipeGesture(
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

// ============================================================================
// EJEMPLO 4: MENÚ ALTERNO SIN GESTO (BOTÓN)
// ============================================================================

@Composable
fun CardWithAlternateMenu(
    title: String,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "(Botón de menú abajo o long-press en pantalla anterior)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Menú")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Editar") },
                    onClick = { onEdit(); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Compartir") },
                    onClick = { onShare(); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = { onDelete(); showMenu = false }
                )
            }
        }
    }
}

// ============================================================================
// EJEMPLO 5: COMPOSABLE DE PRUEBA (para Preview)
// ============================================================================

@Composable
fun GesturesPreviewDemo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Demostración de Gestos",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CardWithLongPressMenu(
            title = "Tarjeta con Long Press",
            onEdit = {},
            onShare = {},
            onDelete = {}
        )

        ContainerWithHorizontalSwipe(
            onSwipeLeft = { /* siguiente */ },
            onSwipeRight = { /* anterior */ }
        ) {
            Text("Desliza horizontalmente")
        }

        CardWithAlternateMenu(
            title = "Tarjeta con Botón de Menú",
            onEdit = {},
            onShare = {},
            onDelete = {}
        )
    }
}
