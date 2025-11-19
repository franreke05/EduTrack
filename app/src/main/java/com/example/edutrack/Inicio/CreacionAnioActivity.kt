package com.example.edutrack.Inicio

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.CrearAnio
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.db_ref
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.database.FirebaseDatabase


class CreacionAnioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            EduTrackTheme {
                Scaffold(
                    content = { paddingValues ->

                        CrearAnioScreen(onFinish = { finish() },modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues))

                    }
                )


            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearAnioScreen(modifier: Modifier = Modifier, onFinish: () -> Unit = {}) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var mostrarDialoginicio by remember { mutableStateOf(false) }
    var mostrarDialogfin by remember { mutableStateOf(false) }
    var fechaFin by remember { mutableStateOf("") }
    var numero_asignaturas by remember { mutableStateOf("") }
    val context = LocalContext.current


    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.051f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))
                )
            ),
    ) {
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopAppBar simulada
        TopAppBar(
            title = { Text("Nuevo Año Escolar", color = Color.Blue) },
            navigationIcon = {
                IconButton(onClick = onFinish) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.Blue)
                }
            },
            actions = {
                IconButton(onClick = {
                    //Inicializamos el db_ref
                    db_ref = FirebaseDatabase.getInstance().getReference("Anios")
                    //Recogemos el id del usuario de las sharedpreferences
                    var sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    var id_user = sharedPreferences.getString("USER", "")
                    Log.d ("Login Buscando usuario f",id_user.toString())
                    if(comprobarCampos(numero_asignaturas,nombre,descripcion,fechaInicio,fechaFin)){
                     val anio = Anio(db_ref.push().key,nombre= nombre,descripcion= descripcion,fechaInicio= fechaInicio, fechaFin= fechaFin,numero_asignaturas.toInt(), id_user=id_user)
                        CrearAnio(anio)
                        onFinish()
                    }



                },modifier = Modifier.padding(end = screenHeight * 0.005f)) {
                    Icon(Icons.Default.Done, contentDescription = "Guardar", tint = Color.Blue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Contenido que antes estaba dentro del Scaffold
        Column(
            modifier = Modifier
                .padding(horizontal = screenHeight * 0.02f)

                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f)
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            Text(
                text = "Información del Año",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Blue,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = screenHeight * 0.0025f)
            ) {
                Column(modifier = Modifier.padding(screenHeight * 0.02f)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Curso (ej. 2024-2025)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(screenHeight * 0.015f))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            }

            Text(
                text = "Duración",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Blue,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = screenHeight * 0.0025f)
            ) {
                Column(modifier = Modifier.padding(screenHeight * 0.02f)) {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { },
                        label = { Text("Fecha de Inicio") },
                        readOnly = true, // Evita que el usuario escriba directamente.
                        trailingIcon = {
                            IconButton(onClick = {
                                // Al hacer clic, se pone el estado a true para mostrar el diálogo.
                                mostrarDialoginicio = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Abrir calendario"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(screenHeight * 0.015f))
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = { },
                        label = { Text("Fecha de Fin") },
                        readOnly = true, // Evita que el usuario escriba directamente.
                        trailingIcon = {
                            IconButton(onClick = {
                                // Al hacer clic, se pone el estado a true para mostrar el diálogo.
                                mostrarDialogfin = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Abrir calendario"
                                )
                            }
                        }
                    )
                }
            }

            if (mostrarDialoginicio) {
                SelectorDeFecha(
                    onFechaSeleccionada = { fecha ->
                        // Actualiza la fecha en el estado cuando el usuario confirma.
                        fechaInicio = fecha
                    },
                    onDismiss = {
                        // Cierra el diálogo.
                        mostrarDialoginicio = false
                    }
                )
            }
            if (mostrarDialogfin) {
                SelectorDeFecha(
                    onFechaSeleccionada = { fecha ->
                        // Actualiza la fecha en el estado cuando el usuario confirma.
                        fechaFin = fecha
                    },
                    onDismiss = {
                        // Cierra el diálogo.
                        mostrarDialogfin = false
                    }
                )
            }
            Text(
                text = "Numero de Asignaturas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Blue,
                modifier = Modifier.fillMaxWidth()
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = screenHeight * 0.0025f)
            ) {
                Column(modifier = Modifier.padding(screenHeight * 0.02f)) {
                    OutlinedTextField(
                        value = numero_asignaturas,
                        onValueChange = { nuevoValor ->
                            // 1. Filtra la entrada para que solo contenga dígitos.
                            val textoFiltrado = nuevoValor.filter { it.isDigit() }

                            // 2. Comprueba la restricción de longitud máxima (2 dígitos para "20")
                            if (textoFiltrado.length <= 2) {
                                // 3. Verifica si el texto está vacío o si el número es <= 20
                                if (textoFiltrado.isEmpty() || textoFiltrado.toInt() <= 20) {
                                    numero_asignaturas = textoFiltrado
                                }
                            }
                        },
                        label = { Text("Numero de Asignaturas (máx. 20)") },
                        modifier = Modifier.fillMaxWidth(),
                        // Muestra el teclado numérico.
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        // Opcional: para asegurar que el campo no tenga múltiples líneas.
                        singleLine = true
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CrearAnioScreenPreview() {
    EduTrackTheme {
        CrearAnioScreen()
    }
}
