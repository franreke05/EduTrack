package com.example.edutrack.Inicio

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoInicio(modifier: Modifier = Modifier) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
    val id_user = sharedPreferences.getString("USER", "")

    val anios by rememberAniosState(id_user)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var anioToDelete by remember { mutableStateOf<Anio?>(null) }

    val actions = listOf(
        "Perfil" to Icons.Default.Person,
        "Buscar" to Icons.Default.Search,
        "Añadir" to Icons.Default.Add,
        "Opciones" to Icons.Default.MoreVert
    )
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


        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = screenHeight * 0.02f),
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f),
            contentPadding = PaddingValues(horizontal = screenWidth * 0.04f)
        ) {
            items(actions) { (label, icon) ->
                CircularActionButton(
                    icon = icon,
                    label = label,
                    onClick = {
                        when (label) {
                            "Perfil" -> { /* TODO: Implementar pantalla de perfil */
                            }

                            "Buscar" -> { /* TODO: Implementar búsqueda */
                            }

                            "Añadir" -> {
                                Intent(context, CreacionAnioActivity::class.java).apply {
                                    context.startActivity(this)
                                }
                            }

                            "Opciones" -> { /* TODO: Implementar pantalla de opciones */
                            }
                        }
                    },
                    screenHeight = screenHeight
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = screenHeight * 0.02f)
        ) {
            itemsIndexed(
                anios,
                key = { _, anio -> anio.id ?: anio.hashCode().toString() }) { index, anio ->
                // state específico para SwipeToDismissBox (Material3)
                val swipeState = rememberSwipeToDismissBoxState(
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
                    state = swipeState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // ajusta según tu UI
                    // Opcional: limitar direcciones (usa enableDismissFrom... si quieres)
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        // cambia el fondo según la dirección objetivo
                        val bg = when (swipeState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                            SwipeToDismissBoxValue.StartToEnd -> Color.Green.copy(alpha = 0.8f)
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bg)
                                .padding(horizontal = screenWidth * 0.05f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size((screenHeight * 0.04f))
                            )
                        }
                    }
                ) {
                    // Contenido principal (tu card)
                    AnioCard(anio, index + 1, screenHeight, screenWidth, function = {
                        // navegación o acción
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

@Composable
fun CircularActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    screenHeight: Dp
) {
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
            Column(modifier = Modifier.weight(1f)) {
                anio.nombre?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) }
                anio.descripcion?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {
        CuerpoInicio()
    }
}
