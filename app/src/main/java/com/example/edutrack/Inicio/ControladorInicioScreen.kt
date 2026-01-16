package com.example.edutrack.Inicio

import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.google.firebase.database.DataSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val DATE_REGEX = Regex("\\d{2}/\\d{2}/\\d{4}")

// Convierte numeros a romanos.
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

// Valida los campos para crear un anio.
fun comprobarCampos(num: String, nombretxt: String, fechaInicio: String, fechaFin: String): Boolean {
    val numeroAsignaturas = num.toIntOrNull() ?: return false
    if (numeroAsignaturas !in 1..20) return false
    if (nombretxt.isBlank()) return false
    if (fechaInicio.isBlank() || fechaFin.isBlank()) return false
    if (!fechaInicio.matches(DATE_REGEX) || !fechaFin.matches(DATE_REGEX)) return false

    val inicio = parseFechaOrNull(fechaInicio) ?: return false
    val fin = parseFechaOrNull(fechaFin) ?: return false
    return !inicio.after(fin)
}

// Formatea un timestamp a "dd/MM/yyyy".
fun formatearFecha(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(timeInMillis))
}

// Calcula la media ponderada del anio segun creditos.
fun calcularMediaAnio(anio: Anio): Double? {
    val asignaturas = anio.lista_asignaturas?.values ?: return null
    var sumaPonderada = 0.0
    var sumaPesos = 0.0

    asignaturas.forEach { asignatura ->
        val mediaAsignatura = asignatura.media ?: return@forEach
        val peso = asignatura.creditos?.takeIf { it > 0 } ?: 1
        sumaPonderada += mediaAsignatura * peso
        sumaPesos += peso
    }

    if (sumaPesos <= 0.0) return null
    return sumaPonderada / sumaPesos
}

// Formatea una media con 2 decimales.
fun formatMedia(media: Double): String =
    String.format(Locale.getDefault(), "%.2f", media)

// Calcula la media ponderada de anios seleccionados segun porcentajes.
fun calcularMediaConjunta(
    seleccionados: Map<String, Anio>,
    porcentajes: Map<String, String>
): Double? {
    var acumulado = 0.0
    var pesoTotal = 0.0

    seleccionados.values.forEach { anio ->
        val id = anio.id ?: return@forEach
        val peso = porcentajes[id]?.toDoubleOrNull() ?: 0.0
        val mediaAnio = calcularMediaAnio(anio) ?: return@forEach

        if (peso > 0) {
            acumulado += mediaAnio * peso
            pesoTotal += peso
        }
    }

    return if (pesoTotal > 0) acumulado / pesoTotal else null
}

// Convierte un snapshot de Firebase a un modelo Anio.
fun parseAnioSnapshot(snapshot: DataSnapshot): Anio? {
    val id = snapshot.child("id").getValue(String::class.java)
    val nombre = snapshot.child("nombre").getValue(String::class.java)
    val descripcion = snapshot.child("descripcion").getValue(String::class.java)
    val fechaInicio = snapshot.child("fechaInicio").getValue(String::class.java)
    val fechaFin = snapshot.child("fechaFin").getValue(String::class.java)
    val numeroAsignaturas = snapshot.child("numero_asignaturas").getValue(Long::class.java)?.toInt()
    val idUser = snapshot.child("id_user").getValue(String::class.java)

    val asignaturasMap = snapshot.child("lista_asignaturas")
        .children
        .mapNotNull { child ->
            child.getValue(Asignatura::class.java)?.let { asignatura ->
                val key = child.key ?: asignatura.id ?: asignatura.nombre
                key?.let { it to asignatura }
            }
        }
        .toMap()
        .takeIf { it.isNotEmpty() }

    if (nombre == null && descripcion == null && fechaInicio == null && fechaFin == null) return null

    return Anio(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        numero_asignaturas = numeroAsignaturas,
        lista_asignaturas = asignaturasMap,
        id_user = idUser
    )
}

private fun parseFechaOrNull(fecha: String): Date? {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.isLenient = false
    return try {
        formatter.parse(fecha)
    } catch (e: Exception) {
        null
    }
}
