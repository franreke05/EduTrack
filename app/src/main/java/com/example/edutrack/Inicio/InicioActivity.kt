package com.example.edutrack.Inicio

//El contenido del archivo esta comentado (si se agrega alguna otra funcion cambiar el check )✅

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.edutrack.Anio.AnioActivity
import com.example.edutrack.Perfil.PerfilActivity
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CuerpoInicio(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

/**
 * Cuerpo de la pantalla de inicio.
 * @param modifier El modificador para personalizar la apariencia de la pantalla.
 * @return El diseño de la pantalla de inicio.
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoInicio(modifier: Modifier = Modifier) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp // Altura de la pantalla
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp //Anchura de la pantalla
    val context = LocalContext.current //Contexto Local de la app
    val sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE) //SharedPreferences
    val id_user = sharedPreferences.getString("USER", "") // Id del usuario


    // Lista original de Firebase
    val aniosFromFirebase by rememberAniosState(id_user)
    // Lista que se muestra en pantalla (puede ser filtrada)
    var displayedAnios by remember { mutableStateOf<List<Anio>>(emptyList()) }

    // Actualiza la lista a mostrar cuando la de Firebase cambia
    LaunchedEffect(aniosFromFirebase) {
        displayedAnios = aniosFromFirebase
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var anioToDelete by remember { mutableStateOf<Anio?>(null) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.055f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))
                )
            ),
    ) {}
    Column(modifier = modifier.fillMaxSize()) {


        // La función AnimationSearch ahora se encarga de los botones Y de la búsqueda
        AnimationSearch(
            initialAnios = aniosFromFirebase, // Le pasamos la lista original
            onAniosFiltered = { filteredList ->
                displayedAnios = filteredList // Actualizamos la lista que se muestra
            },
            screenHeight = screenHeight,
            screenWidth = screenWidth
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = screenHeight * 0.02f)
        ) {
            itemsIndexed(displayedAnios, key = { _, anio -> anio.id ?: anio.hashCode().toString() }) { index, anio ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            anioToDelete = anio
                            showDeleteDialog = true
                            false // no ejecutar el dismiss animation automático; esperamos confirmación
                        } else {
                            true
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.fillMaxWidth(),
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = screenWidth * 0.05f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(screenHeight * 0.04f)
                            )
                        }
                    }
                ) {
                    AnioCard(anio, index + 1, screenHeight, screenWidth, function = {
                        sharedPreferences.edit().putString("id_anio", anio.id).apply()
                        //entramos en anioActivity
                        Intent(context, AnioActivity::class.java).apply {
                            context.startActivity(this)
                        }
                    })
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar el año '${anioToDelete?.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        anioToDelete?.id?.let { anioId ->
                            if (anioId.isNotEmpty()) {
                                Firebase.database.reference.child("Edutrack").child("Anio").child(anioId)
                                    .removeValue()
                                    .addOnSuccessListener { Log.d("Firebase", "Año eliminado exitosamente") }
                                    .addOnFailureListener { Log.e("Firebase", "Error al eliminar el año", it) }
                            }
                        }
                        showDeleteDialog = false
                        anioToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Tarjeta de año.
 * @param anio El año a mostrar.
 * @param index El índice del año en la lista.
 * @param screenHeight La altura de la pantalla.
 * @param screenWidth La anchura de la pantalla.
 * @param function La función a ejecutar cuando se hace click en la tarjeta.
 *
 */
@Composable
fun AnioCard(anio: Anio, index: Int, screenHeight: Dp, screenWidth: Dp, function : () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f),
        elevation = CardDefaults.cardElevation(defaultElevation = screenHeight * 0.01f),
        onClick = function,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,) // Added shadow
    ) {
        Row(
            modifier = Modifier.padding(screenHeight * 0.02f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(screenHeight * 0.06f)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = toRoman(index),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(screenWidth * 0.04f))
            Column(modifier = Modifier.weight(0.55f).padding(start = screenWidth * 0.02f)) {
                anio.nombre?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) }
                anio.descripcion?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                anio.numero_asignaturas?.let { Text(text = "Número de asignaturas: $it", style = MaterialTheme.typography.bodyMedium) }


            }
            Column(
                modifier = Modifier.width(screenWidth * 0.04f).weight(0.35f).padding(end = screenWidth * 0.02f),
                horizontalAlignment = Alignment.End
            ) {
                //Columnm para poner a la derecha las fechas del curso
                Text(text = "Fecha  Inicio", style = MaterialTheme.typography.titleMedium)
                anio.fechaInicio?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                Text(text = "Fecha Fin", style = MaterialTheme.typography.titleMedium)
                anio.fechaFin?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {
        AnioCard(anio = Anio("Año 1", "Descripción del año 1","descripcion","14/10/25","14/10/25"), index = 1, screenHeight = 100.dp, screenWidth = 100.dp) { }
    }
}




/**
 * Funcion para crear un boton circular.
 * (Es la configuracion predefinida para los botones de las opciones de la app)
 * @param icon El icono del boton.
 * @param label El texto del boton.
 * @param onClick La funcion que se ejecuta cuando se hace click en el boton.
 * @param screenHeight La altura de la pantalla.
 * @return El boton circular.
 */

@Composable
fun CircularActionButton(icon: ImageVector, label: String, onClick: () -> Unit, screenHeight: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(screenHeight * 0.07f)
                .shadow(elevation = screenHeight * 0.01f, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)

        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(screenHeight * 0.045f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(screenHeight * 0.09f)
        )
    }
}




/**
 * Funcion para abrir el selector de fecha.
 * (Solo funciona si la fecha de fin es mayor que la de inicio)
 *
 * @param onFechaSeleccionada Una funcion que se llama cuando se selecciona una fecha.
 * @param onDismiss Una funcion que se llama cuando se cierra el selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDeFecha(onFechaSeleccionada: (String) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    val fechaSeleccionada = datePickerState.selectedDateMillis?.let {
                        formatearFecha(it)
                    } ?: ""
                    onFechaSeleccionada(fechaSeleccionada)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}