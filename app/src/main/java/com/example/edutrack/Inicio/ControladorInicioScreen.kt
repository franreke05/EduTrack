package com.example.edutrack.Inicio

//El contenido del archivo esta comentado (si se agrega alguna otra funcion cambiar el check )✅

//Todos los imports necesarios para el controlador del incio de la app (Revisar si estan optimizado
import android.content.Intent import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.edutrack.Inicio.CircularActionButton
import com.example.edutrack.Perfil.PerfilActivity
import com.example.edutrack.dataclass.Anio
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Funcion para convertir numeros a romanos.
 * Se usa para en la pantalla de inicio que los años tengan un icono romano en vez de un numero
 *
 * @param num El ID del usuario para filtrar los años.
 * @return El numero correspondiente en romano.
 */
fun toRoman(num: Int): String {
    val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1) // Lista de valores en orden descendente
    val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I") // Lista de símbolos correspondientes
    var number = num
    val result = StringBuilder()
    for (i in values.indices) { // Recorremos la lista de valores
        while (number >= values[i]) {
            number -= values[i] // Restamos el valor actual al número
            result.append(symbols[i]) // Agregamos el símbolo correspondiente al resultado
        }
    }
    return result.toString()
}

/**
 * Un Composable que escucha los cambios en la lista de años de Firebase
 * y devuelve un State<List<Anio>> que se actualiza automáticamente.
 *
 * @param id_user El ID del usuario para filtrar los años.
 * @return Un estado que contiene la lista de años del usuario.
 */
@Composable
fun rememberAniosState(id_user: String?): androidx.compose.runtime.State<List<Anio>> {

    val aniosState = remember { mutableStateOf<List<Anio>>(emptyList()) } // Estado para almacenar la lista de años
    val dbRef = Firebase.database.reference.child("Edutrack").child("Anio") // Referencia a la base de datos de Firebase

    DisposableEffect(id_user) { //Composable para mantener el estado actualizado

        if (id_user.isNullOrEmpty()) { //si el id del usuario esta vacio, no hace nada
            aniosState.value = emptyList()
            onDispose {}
        } else { // Si el id tiene contenido entra a comprobar si hay cambio en la base de datos
            val valueEventListener = object : ValueEventListener { // Escuchador de cambios en la base de datos
                override fun onDataChange(snapshot: DataSnapshot) { // Cuando hay un cambio en la base de datos
                    val aniosList = snapshot.children.mapNotNull { it.getValue(Anio::class.java) } // Convierte los datos en una lista de objetos Anio
                        .filter { it.id_user == id_user } //filtra la lista de años por el id del usuario
                    aniosState.value = aniosList // Actualiza el estado con la nueva lista de años
                }

                override fun onCancelled(error: DatabaseError) { // Cuando hay un error en la base de datos
                    Log.e("Firebase", "Error al leer los datos de Firebase", error.toException())
                }
            }
            dbRef.addValueEventListener(valueEventListener)

            onDispose {
                dbRef.removeEventListener(valueEventListener)
            }
        }
    }

    return aniosState
}


/**
 * Funcion para animar el boton de busqueda y poder filtrar los años.
 *
 * @param initialAnios La lista inicial de años.
 * @param onAniosFiltered Una función que se llama cuando se filtra la lista de años.
 * @param screenHeight La altura de la pantalla.
 * @param screenWidth El ancho de la pantalla.
 */
@OptIn(ExperimentalAnimationApi::class) // Necesario para usar AnimatedContent
@Composable
fun AnimationSearch(initialAnios: List<Anio>, onAniosFiltered: (List<Anio>) -> Unit, screenHeight: Dp, screenWidth: Dp) {

    //Declaracion de variables
    var isSearchExpanded by remember { mutableStateOf(false) } // Estado para saber si el buscador esta abierto o cerrado
    var searchQuery by remember { mutableStateOf("") } // Estado para la busqueda
    var showFilterMenu by remember { mutableStateOf(false) }// Estado para saber si el menu de filtros esta abierto o cerrado
    var selectedSortOption by remember { mutableStateOf("Fecha Reciente") } // Estado para la opcion de ordenamiento de la lista de años

    val context = LocalContext.current //Contexto de la aplicación
    val sortOptions = listOf("Fecha Reciente", "Último a primero", "Por nombre", "Por cantidad de asignaturas") // Opciones de ordenamiento
    val actions = listOf(
        "Perfil" to Icons.Default.Person,
        "Buscar" to Icons.Default.Search,
        "Añadir" to Icons.Default.Add,
        "Opciones" to Icons.Default.MoreVert
    ) /// Lista de opciones de la barra de acciones


    //Launcheffects para filtrar la lista de años
    LaunchedEffect(searchQuery, selectedSortOption, initialAnios) {
        val filteredList =
            if (searchQuery.isNotBlank()) { // Si la busqueda no esta vacia, filtra la lista de años
                // Filtra la lista de años por el nombre
                initialAnios.filter { anio -> anio.nombre?.contains(searchQuery, ignoreCase = true) == true }
        } else { //Si no, muestra la lista inicial

            initialAnios
        }

        val sortedList = when (selectedSortOption) {
            //"Fecha Reciente" -> filtra por fecha de inicio
            "Fecha Reciente" -> filteredList.sortedByDescending {
               // Try para formatear la fecha y asi se pueda comparar
                // Si no se puede formatear, devuelve null
                try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.fechaInicio ?: "")
                } catch (e: Exception) {
                    null
                }
            }
            //"Último a primero" -> filtra por fecha de fin
            "Último a primero" -> filteredList.reversed()
            //"Por nombre" -> filtra por nombre
            "Por nombre" -> filteredList.sortedBy { it.nombre }
            //"Por cantidad de asignaturas" -> filtra por cantidad de asignaturas
            "Por cantidad de asignaturas" -> filteredList.sortedByDescending { it.lista_asignaturas?.size ?: 0 }
            //Y si no es ninguna de las anteriores, muestra la lista inicial
            else -> filteredList
        }

        onAniosFiltered(sortedList) // Llama a la funcion que se pasa como parametro
    }

    AnimatedContent(
        targetState = isSearchExpanded,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
        },
        modifier = Modifier.padding(vertical = screenHeight * 0.02f)
    ) { isExpanded ->
        if (!isExpanded) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f),
                contentPadding = PaddingValues(horizontal = screenWidth * 0.04f)
            ) {
                items(actions) { (label, icon) ->
                    CircularActionButton(
                        icon = icon,
                        label = label,
                        onClick = {
                            when (label) {
                                "Buscar" -> isSearchExpanded = true
                                "Añadir" -> {
                                    Intent(context, CreacionAnioActivity::class.java).apply {
                                        context.startActivity(this)
                                    }
                                }
                                "Perfil" ->{
                                    Intent(context, PerfilActivity::class.java).apply {
                                        context.startActivity(this)
                                    }

                                }
                                "Opciones" ->{

                                }
                                else -> {
                                    Toast.makeText(context, "Opcion no implementada", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        screenHeight = screenHeight
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenWidth * 0.04f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.02f)
            ) {
                IconButton(onClick = { isSearchExpanded = false; searchQuery = "" }) {
                    Icon(Icons.Default.SearchOff, contentDescription = "Cerrar Búsqueda")
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar Año...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar Años")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSortOption = option
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * Funcion para comprobrar que los campos que introduce el usuario cuando crea un año son válidos
 * y no rompe el codigo.
 *
 * @param num El numero del año.
 * @param nombretxt El nombre del año.
 * @param fechaInicio La fecha de inicio del año.
 * @param fechaFin La fecha de fin del año.
 * @return Si los campos son validos o no.
 */
fun comprobarCampos(num: String, nombretxt: String, fechaInicio: String, fechaFin: String, ): Boolean{
    var comprobado =false // Estado para saber si los campos son validos o no

    if (num < 0.toString() || num > 21.toString()){ // Si el numero de asignatura esta fuera de rango

       if (nombretxt.isNotEmpty()){ // Si el nombre no esta vacio

           if(fechaInicio.isNotEmpty() && fechaFin.isNotEmpty()) {  // Si las fechas no estan vacias Comprobamos que la fecha de inicio sea menor que la de fin

               if (fechaInicio <= fechaFin) { // Si la fecha de inicio es menor que la de fin


                   //Comprobamos que el formato de la fecha sea correcto
                   if (fechaInicio.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))){

                       if (fechaFin.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))){
                           comprobado = true

                       }
                   }
               }
               }
           }
       }
    return comprobado
}


/**
 * Funcion para formatear la fecha.
 *
 * @param timeInMillis La fecha en milisegundos.
 * @return La fecha formateada.
 */
fun formatearFecha(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(timeInMillis))
}


