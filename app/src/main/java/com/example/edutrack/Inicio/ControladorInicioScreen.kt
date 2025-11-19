package com.example.edutrack.Inicio

import android.util.Log
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import kotlin.text.format
import androidx.annotation.RequiresApi
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

fun toRoman(num: Int): String {
    val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var number = num
    val result = StringBuilder()
    for (i in values.indices) {
        while (number >= values[i]) {
            number -= values[i]
            result.append(symbols[i])
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
fun rememberAniosState(id_user: String?): State<List<Anio>> {
    val aniosState = remember { mutableStateOf<List<Anio>>(emptyList()) }

    val dbRef = Firebase.database.reference.child("Edutrack").child("Anio")

    DisposableEffect(id_user) {
        if (id_user.isNullOrEmpty()) {
            aniosState.value = emptyList()
            onDispose {}
        } else {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val aniosList = snapshot.children.mapNotNull { it.getValue(Anio::class.java) }
                        .filter { it.id_user == id_user }
                    aniosState.value = aniosList
                    Log.d("FirebaseListener", "Lista de años actualizada: ${aniosList.size} elementos para el usuario $id_user.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("FirebaseListener", "Error al leer los años.", error.toException())
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

fun comprobarCampos(
    num: String,
    nombretxt: String,
    descripciontxt: String? = "",
    fechaInicio: String,
    fechaFin: String,

): Boolean{
    var comprobado =false

    if (num < 0.toString() || num > 21.toString()){
       if (nombretxt.isNotEmpty()){
           if(fechaInicio.isNotEmpty() && fechaFin.isNotEmpty()) {
               if (fechaInicio <= fechaFin) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDeFecha(
    onFechaSeleccionada: (String) -> Unit,
    onDismiss: () -> Unit
) {
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

private fun formatearFecha(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(timeInMillis))
}