package com.example.edutrack.gestures

/**
 * EJEMPLOS_INTEGRACION.kt
 *
 * Ejemplos listos para copiar y pegar mostrando cómo integrar gestos
 * en las pantallas de EduTrack.
 *
 * NO es código funcional, es solo referencia.
 * Copia lo que necesites a tus pantallas reales.
 */

// ============================================================================
// EJEMPLO 1: Swipe Back en AnioActivity
// ============================================================================

/*
// EN AnioActivity.kt, en la función AnioScreen():

import com.example.edutrack.gestures.swipeBackGesture

@Composable
fun AnioScreen(
    modifier: Modifier = Modifier,
    anio: Anio,
    userId: String? = null,
    pageIndex: Int,
    onBack: () -> Unit = {},  // ← Recibe callback
    onOpenNotas: (Asignatura, String?) -> Unit = { _, _ -> },
    onPaywall: () -> Unit = {}
) {
    // ... tu código ...

    Scaffold(
        // ... topBar, floatingActionButton, etc ...
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .swipeBackGesture(onBack = onBack),  // ← AGREGAR ESTO
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Tu contenido aquí...
            }
        }
    }
}
*/

// ============================================================================
// EJEMPLO 2: Long Press en AsignaturaCard
// ============================================================================

/*
// EN AnioActivity.kt, en la función AnioScreen():
// Esta parte ya está implementada, pero aquí está el ejemplo:

AsignaturaCard(
    index = index + 1,
    asignatura = asignatura,
    notaMinAprobado = anio.nota_minima_aprobado ?: 5.0,
    onClick = { onOpenNotas(asignatura, anio.id) },
    onLongClick = if (userGroups.isNotEmpty()) {  // ← Long press activado
        { asignaturaToShare = asignatura }         // ← Abre menú
    } else null
)

// Cuando el usuario long-press, se ejecuta:
asignaturaToShare?.let { asig ->
    PublicarAsignaturaDialog(
        asignatura = asig,
        grupos = userGroups,
        userId = userId,
        onDismiss = { asignaturaToShare = null }
    )
}
*/

// ============================================================================
// EJEMPLO 3: Swipe Dismiss en ModalBottomSheet
// ============================================================================

/*
// EN AnioActivity.kt, en la función AnioScreen():
// ModalBottomSheet ya maneja esto automáticamente:

if (showAsignaturaSheet) {
    ModalBottomSheet(
        onDismissRequest = { showAsignaturaSheet = false },
        sheetState = sheetState,  // ← Esto maneja swipe down automático
        modifier = Modifier
            // El swipe dismiss YA está integrado en ModalBottomSheet
    ) {
        CrearAsignaturaSheetContent(
            // ...
        )
    }
}

// El usuario puede cerrar con:
// 1. Swipe down (gesto)
// 2. Click fuera del sheet
// 3. Botón de cerrar del contenido
*/

// ============================================================================
// EJEMPLO 4: Cómo Agregar a Otra Pantalla
// ============================================================================

/*
// Si quieres agregar swipe back a InicioActivity:

// 1. Importar:
import com.example.edutrack.gestures.swipeBackGesture

// 2. En tu función composable principal:
@Composable
fun InicioScreen(
    onBack: () -> Unit = {},  // ← Agregar parámetro
    // ... otros parámetros
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .swipeBackGesture(onBack = onBack)  // ← Agregar modifier
    ) {
        // Tu contenido
    }
}

// 3. Llamar desde la Activity:
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        InicioRoute(
            onBack = { finish() }  // ← Pasar callback
        )
    }
}
*/

// ============================================================================
// EJEMPLO 5: Personalizar Umbrales de Gestos
// ============================================================================

/*
// Si quieres cambiar la sensibilidad del swipe back:

Surface(
    modifier = Modifier
        .fillMaxSize()
        .swipeBackGesture(
            onBack = onBack,
            edgeThreshold = 75f,      // Aumentar zona sensible (default 50f)
            velocityThreshold = 250f  // Menos velocidad requerida (default 300f)
        )
)

// Valores recomendados:
// - edgeThreshold: 30-80dp (zona desde borde izquierdo)
// - velocityThreshold: 200-400 dp/s (velocidad mínima)
*/

// ============================================================================
// EJEMPLO 6: Gesture Bundles (múltiples gestos en un elemento)
// ============================================================================

/*
// Puedes combinar múltiples detectores:

Card(
    modifier = Modifier
        .fillMaxWidth()
        .shadow(elevation = 6.dp)
        .combinedClickable(
            onClick = { openDetails() },        // Click normal
            onLongClick = { showMenu = true }   // Long press
        )
        .horizontalSwipeGesture(              // Swipe L/R
            onSwipeRight = { previousItem() },
            onSwipeLeft = { nextItem() }
        )
) {
    // Contenido
}

// Orden de ejecución:
// 1. Click → onClick()
// 2. Long press (500ms) → onLongClick()
// 3. Swipe derecha → onSwipeRight()
// 4. Swipe izquierda → onSwipeLeft()
*/

// ============================================================================
// EJEMPLO 7: Debugging - Logging de Gestos
// ============================================================================

/*
// Si quieres debuggear gestos, envuelve tu modifier:

fun Modifier.swipeBackGestureDebug(
    onBack: () -> Unit,
    tag: String = "SwipeBack"
): Modifier = pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { offset ->
            Log.d(tag, "Drag start at ${offset.x}, ${offset.y}")
        },
        onDragEnd = { velocity ->
            Log.d(tag, "Drag end with velocity ${velocity.x}")
            if (velocity.x > 300f) {
                Log.d(tag, "Swipe detected!")
                onBack()
            }
        }
    )
}

// Luego usa:
.swipeBackGestureDebug(onBack = onBack, tag = "AnioActivity")

// Ve logcat para ver los eventos:
// adb logcat | grep SwipeBack
*/

// ============================================================================
// EJEMPLO 8: Considerar Tablet Support (>600dp)
// ============================================================================

/*
// Para dispositivos grandes, ajusta los umbrales:

val isTablet = LocalConfiguration.current.screenWidthDp > 600

Surface(
    modifier = Modifier
        .fillMaxSize()
        .swipeBackGesture(
            onBack = onBack,
            edgeThreshold = if (isTablet) 100f else 50f,  // Zona mayor en tablets
            velocityThreshold = 300f
        )
)
*/

// ============================================================================
// CHECKLIST: Implementar un Nuevo Gesto
// ============================================================================

/*
PASOS:
1. ☐ Importar la función del gesto: import com.example.edutrack.gestures.*
2. ☐ Agregar el modifier a tu elemento base (Surface, Card, etc)
3. ☐ Pasar los callbacks necesarios (onBack, onLongPress, etc)
4. ☐ Compilar y probar: ./gradlew assembleDebug
5. ☐ Probar en dispositivo físico (gestos a veces fallan en emulador)
6. ☐ Verificar que no conflictúe con otros detectores de gestos
7. ☐ Asegurar accesibilidad: mantener botones alternativos

TESTING:
- Swipe desde diferentes puntos del borde
- Variar velocidad (rápido vs lento)
- Probar en orientación portrait y landscape
- Verificar que scroll normal siga funcionando
*/
