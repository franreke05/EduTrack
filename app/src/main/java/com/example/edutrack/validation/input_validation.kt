package com.example.edutrack.validation

import android.util.Log
import java.util.regex.Pattern

// ============================================================================
// SEALED CLASS: ValidationResult
// ============================================================================

sealed class ValidationResult {
    data class Success(val value: String = "") : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

// ============================================================================
// STRING EXTENSIONS
// ============================================================================

/**
 * Valida que una cadena sea un email válido
 */
fun String.isValidEmail(): Boolean {
    return if (this.isEmpty()) {
        false
    } else {
        val emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$"
        Pattern.compile(emailPattern).matcher(this).matches()
    }
}

/**
 * Detecta si una cadena contiene HTML
 */
fun String.containsHtml(): Boolean {
    val htmlPattern = "<[^>]*>"
    return Pattern.compile(htmlPattern).matcher(this).find()
}

/**
 * Obtiene la longitud después del trim
 */
fun String.trimmedLength(): Int {
    return this.trim().length
}

/**
 * Sanitiza una cadena eliminando HTML, caracteres especiales peligrosos y espacios extras
 */
fun String.sanitized(): String {
    return this
        .trim()                                          // Elimina espacios al inicio y final
        .replace(Regex("<[^>]*>"), "")                 // Elimina etiquetas HTML
        .replace(Regex("\\u003c[^\\u003e]*\\u003e"), "")  // Elimina HTML codificado (< >)
        .replace(Regex("[<>\"'`]"), "")                // Elimina caracteres potencialmente peligrosos
        .replace(Regex("\\s+"), " ")                   // Normaliza espacios múltiples
}

/**
 * Valida que sea un timestamp Unix válido (segundos desde epoch)
 */
fun Long.isValidUnixTimestamp(): Boolean {
    // Timestamp debe estar entre 1970 y aproximadamente 2100
    return this in 0..4102444800000L  // milisegundos hasta año 2100
}

// ============================================================================
// SEALED CLASS: InputValidator
// ============================================================================

object InputValidator {

    private const val TAG = "InputValidator"

    // Constantes de validación
    private const val SUBJECT_NAME_MIN = 1
    private const val SUBJECT_NAME_MAX = 50
    private const val GROUP_NAME_MIN = 1
    private const val GROUP_NAME_MAX = 100
    private const val DESCRIPTION_MAX = 500
    private const val NOTE_MIN = 0.0
    private const val NOTE_MAX = 10.0
    private const val NOTE_DECIMAL_PLACES = 2

    // Patrones regex
    private val SUBJECT_NAME_PATTERN = Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]+$")
    private val ALPHANUMERIC_PATTERN = Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s\\-._]+$")

    // ========================================================================
    // VALIDAR NOMBRE DE ASIGNATURA
    // ========================================================================

    /**
     * Valida el nombre de una asignatura
     * Reglas:
     * - Longitud: 1-50 caracteres
     * - Sin HTML
     * - Alfanumérico + espacios (caracteres latinos incluidos)
     * - Sanitización automática
     */
    fun validateSubjectName(name: String): ValidationResult {
        val trimmedName = name.trim()

        // Validar longitud
        if (trimmedName.length < SUBJECT_NAME_MIN) {
            val error = "El nombre de la asignatura no puede estar vacío"
            logInvalidAttempt("validateSubjectName", name, error)
            return ValidationResult.Error(error)
        }

        if (trimmedName.length > SUBJECT_NAME_MAX) {
            val error = "El nombre de la asignatura no puede exceder $SUBJECT_NAME_MAX caracteres"
            logInvalidAttempt("validateSubjectName", name, error)
            return ValidationResult.Error(error)
        }

        // Detectar HTML
        if (trimmedName.containsHtml()) {
            val error = "El nombre de la asignatura no puede contener HTML"
            logInvalidAttempt("validateSubjectName", name, error)
            return ValidationResult.Error(error)
        }

        // Validar caracteres permitidos (solo alfanuméricos, espacios y caracteres latinos)
        if (!SUBJECT_NAME_PATTERN.matches(trimmedName)) {
            val error = "El nombre de la asignatura solo puede contener letras, números y espacios"
            logInvalidAttempt("validateSubjectName", name, error)
            return ValidationResult.Error(error)
        }

        val sanitized = trimmedName.sanitized()
        return ValidationResult.Success(sanitized)
    }

    // ========================================================================
    // VALIDAR VALOR DE NOTA
    // ========================================================================

    /**
     * Valida una nota/calificación
     * Reglas:
     * - Rango: 0-10
     * - Máximo 2 decimales
     */
    fun validateNoteValue(value: Double): ValidationResult {
        if (value < NOTE_MIN) {
            val error = "La nota no puede ser menor que $NOTE_MIN"
            logInvalidAttempt("validateNoteValue", value.toString(), error)
            return ValidationResult.Error(error)
        }

        if (value > NOTE_MAX) {
            val error = "La nota no puede ser mayor que $NOTE_MAX"
            logInvalidAttempt("validateNoteValue", value.toString(), error)
            return ValidationResult.Error(error)
        }

        // Verificar decimales
        val decimalPlaces = value.toString().substringAfterLast('.').length
        if (decimalPlaces > NOTE_DECIMAL_PLACES) {
            val error = "La nota puede tener máximo $NOTE_DECIMAL_PLACES decimales"
            logInvalidAttempt("validateNoteValue", value.toString(), error)
            return ValidationResult.Error(error)
        }

        return ValidationResult.Success(String.format("%.2f", value))
    }

    // ========================================================================
    // VALIDAR NOMBRE DE GRUPO
    // ========================================================================

    /**
     * Valida el nombre de un grupo
     * Reglas:
     * - Longitud: 1-100 caracteres
     * - Sin HTML
     * - Sanitización automática
     */
    fun validateGroupName(name: String): ValidationResult {
        val trimmedName = name.trim()

        // Validar longitud
        if (trimmedName.length < GROUP_NAME_MIN) {
            val error = "El nombre del grupo no puede estar vacío"
            logInvalidAttempt("validateGroupName", name, error)
            return ValidationResult.Error(error)
        }

        if (trimmedName.length > GROUP_NAME_MAX) {
            val error = "El nombre del grupo no puede exceder $GROUP_NAME_MAX caracteres"
            logInvalidAttempt("validateGroupName", name, error)
            return ValidationResult.Error(error)
        }

        // Detectar HTML
        if (trimmedName.containsHtml()) {
            val error = "El nombre del grupo no puede contener HTML"
            logInvalidAttempt("validateGroupName", name, error)
            return ValidationResult.Error(error)
        }

        val sanitized = trimmedName.sanitized()
        return ValidationResult.Success(sanitized)
    }

    // ========================================================================
    // VALIDAR DESCRIPCIÓN
    // ========================================================================

    /**
     * Valida una descripción
     * Reglas:
     * - Longitud: 0-500 caracteres
     * - Sin HTML
     * - Sanitización automática
     */
    fun validateDescription(desc: String): ValidationResult {
        val trimmedDesc = desc.trim()

        if (trimmedDesc.isEmpty()) {
            // Las descripciones pueden estar vacías
            return ValidationResult.Success("")
        }

        if (trimmedDesc.length > DESCRIPTION_MAX) {
            val error = "La descripción no puede exceder $DESCRIPTION_MAX caracteres"
            logInvalidAttempt("validateDescription", desc, error)
            return ValidationResult.Error(error)
        }

        // Detectar HTML
        if (trimmedDesc.containsHtml()) {
            val error = "La descripción no puede contener HTML"
            logInvalidAttempt("validateDescription", desc, error)
            return ValidationResult.Error(error)
        }

        val sanitized = trimmedDesc.sanitized()
        return ValidationResult.Success(sanitized)
    }

    // ========================================================================
    // VALIDAR EMAIL
    // ========================================================================

    /**
     * Valida un email
     * Usa regex estándar para validación de email
     */
    fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isEmpty()) {
            val error = "El email no puede estar vacío"
            logInvalidAttempt("validateEmail", email, error)
            return ValidationResult.Error(error)
        }

        if (!trimmedEmail.isValidEmail()) {
            val error = "El formato del email no es válido"
            logInvalidAttempt("validateEmail", email, error)
            return ValidationResult.Error(error)
        }

        return ValidationResult.Success(trimmedEmail.lowercase())
    }

    // ========================================================================
    // VALIDAR TIMESTAMP
    // ========================================================================

    /**
     * Valida un timestamp Unix
     * Reglas:
     * - Debe ser un número entero válido
     * - Debe estar entre 1970 y año 2100
     */
    fun validateTimestamp(timestamp: Long): ValidationResult {
        if (!timestamp.isValidUnixTimestamp()) {
            val error = "El timestamp no es válido. Debe estar entre 1970 y 2100"
            logInvalidAttempt("validateTimestamp", timestamp.toString(), error)
            return ValidationResult.Error(error)
        }

        return ValidationResult.Success(timestamp.toString())
    }

    // ========================================================================
    // SANITIZACIÓN GENERAL
    // ========================================================================

    /**
     * Sanitiza una cadena de forma general
     * - Elimina HTML
     * - Elimina caracteres especiales peligrosos
     * - Normaliza espacios
     * - Convierte a minúsculas (opcional)
     */
    fun sanitizeString(
        input: String,
        toLowerCase: Boolean = false
    ): String {
        var result = input.sanitized()
        if (toLowerCase) {
            result = result.lowercase()
        }
        return result
    }

    /**
     * Sanitiza múltiples campos a la vez
     */
    fun sanitizeFields(vararg fields: String): List<String> {
        return fields.map { it.sanitized() }
    }

    // ========================================================================
    // VALIDACIÓN COMBINADA
    // ========================================================================

    /**
     * Valida un conjunto de campos comúnmente usados
     * Útil para formularios de creación de asignatura
     */
    fun validateSubjectForm(
        name: String,
        description: String
    ): Map<String, ValidationResult> {
        return mapOf(
            "name" to validateSubjectName(name),
            "description" to validateDescription(description)
        )
    }

    /**
     * Valida un conjunto de campos para creación de grupo
     */
    fun validateGroupForm(
        name: String,
        description: String
    ): Map<String, ValidationResult> {
        return mapOf(
            "name" to validateGroupName(name),
            "description" to validateDescription(description)
        )
    }

    /**
     * Verifica si todos los resultados de validación son exitosos
     */
    fun allValid(results: Map<String, ValidationResult>): Boolean {
        return results.values.all { it is ValidationResult.Success }
    }

    /**
     * Obtiene los errores de un mapa de validaciones
     */
    fun getErrors(results: Map<String, ValidationResult>): Map<String, String> {
        return results
            .filter { it.value is ValidationResult.Error }
            .mapValues { (it.value as ValidationResult.Error).message }
    }

    // ========================================================================
    // LOGGING
    // ========================================================================

    /**
     * Registra intentos de input inválido (para seguridad y debugging)
     */
    private fun logInvalidAttempt(
        method: String,
        input: String,
        reason: String
    ) {
        val sanitized = input.sanitized().take(50)  // Mostrar solo primeros 50 chars
        Log.w(
            TAG,
            "INVALID INPUT [$method]: '$sanitized...' - Reason: $reason"
        )
    }

    /**
     * Registra una validación exitosa (opcional, para debugging)
     */
    fun logValidInput(method: String, input: String) {
        val sanitized = input.sanitized().take(50)
        Log.d(TAG, "VALID INPUT [$method]: '$sanitized...'")
    }
}

// ============================================================================
// EXTENSION FUNCTION PARA ValidationResult
// ============================================================================

/**
 * Obtiene el valor de un ValidationResult o el default
 */
fun ValidationResult.getOrNull(): String? = when (this) {
    is ValidationResult.Success -> this.value
    is ValidationResult.Error -> null
}

/**
 * Obtiene el valor de un ValidationResult o lanza una excepción
 */
fun ValidationResult.getOrThrow(): String = when (this) {
    is ValidationResult.Success -> this.value
    is ValidationResult.Error -> throw IllegalArgumentException(this.message)
}

/**
 * Transforma un ValidationResult
 */
fun <R> ValidationResult.map(transform: (String) -> R): ValidationResult {
    return when (this) {
        is ValidationResult.Success -> ValidationResult.Success(transform(this.value).toString())
        is ValidationResult.Error -> this
    }
}

/**
 * Ejecuta una acción si es Success
 */
fun ValidationResult.onSuccess(action: (String) -> Unit): ValidationResult {
    if (this is ValidationResult.Success) {
        action(this.value)
    }
    return this
}

/**
 * Ejecuta una acción si es Error
 */
fun ValidationResult.onError(action: (String) -> Unit): ValidationResult {
    if (this is ValidationResult.Error) {
        action(this.message)
    }
    return this
}

// ============================================================================
// EJEMPLOS DE USO
// ============================================================================

/*

// Ejemplo 1: Validar nombre de asignatura
val result = InputValidator.validateSubjectName("Matemáticas")
when (result) {
    is ValidationResult.Success -> println("Válido: ${result.value}")
    is ValidationResult.Error -> println("Error: ${result.message}")
}

// Ejemplo 2: Validar nota
val noteResult = InputValidator.validateNoteValue(8.5)
noteResult
    .onSuccess { println("Nota guardada: $it") }
    .onError { println("Error: $it") }

// Ejemplo 3: Validar múltiples campos
val formResults = InputValidator.validateSubjectForm(
    name = "Física",
    description = "Asignatura de ciencias naturales"
)

if (InputValidator.allValid(formResults)) {
    println("Formulario válido")
} else {
    InputValidator.getErrors(formResults).forEach { (field, error) ->
        println("$field: $error")
    }
}

// Ejemplo 4: Sanitizar input directamente
val cleanName = "  <script>alert('hack')</script>Mi Asignatura  ".sanitized()
println(cleanName)  // Output: "alert('hack')Mi Asignatura"

// Ejemplo 5: Validación con extensión
val email = "usuario@example.com"
if (email.isValidEmail()) {
    println("Email válido")
}

// Ejemplo 6: Validar timestamp
val timestamp = System.currentTimeMillis()
InputValidator.validateTimestamp(timestamp)
    .onSuccess { println("Timestamp válido: $it") }

*/
