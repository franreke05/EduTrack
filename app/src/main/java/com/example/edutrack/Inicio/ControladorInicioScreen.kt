package com.example.edutrack.Inicio

/**
 * Convierte números a romanos.
 */
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
 * Valida los campos para crear un año.
 */
fun comprobarCampos(num: String, nombretxt: String, fechaInicio: String, fechaFin: String): Boolean {
    var comprobado = false
    if (num < 0.toString() || num > 21.toString()) {
        if (nombretxt.isNotEmpty()) {
            if (fechaInicio.isNotEmpty() && fechaFin.isNotEmpty()) {
                if (fechaInicio <= fechaFin) {
                    if (fechaInicio.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                        if (fechaFin.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                            comprobado = true
                        }
                    }
                }
            }
        }
    }
    return comprobado
}
