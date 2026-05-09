package com.example.edutrack.domain

import com.example.edutrack.dataclass.Notas
import kotlin.math.roundToInt

sealed class RequiredGradeResult {
    data class Needed(val grade: Double) : RequiredGradeResult()
    object AlreadyEnough : RequiredGradeResult()
    object Impossible : RequiredGradeResult()
    object Completed : RequiredGradeResult()
    object InvalidData : RequiredGradeResult()
}

/**
 * Calcula la nota necesaria en el porcentaje restante de un periodo para alcanzar el objetivo.
 *
 * Fórmula: requiredGrade = (targetAverage * 100 - currentWeightedPoints) / remainingWeight
 *
 * @param currentWeightedPoints suma de (nota * porcentaje) para las notas ya registradas
 * @param usedWeight suma de porcentajes ya usados en el periodo (0..100)
 * @param remainingWeight porcentaje restante = 100 - usedWeight
 * @param targetAverage nota objetivo para el periodo (0..maxGrade)
 * @param maxGrade nota máxima posible, por defecto 10.0
 */
fun calculateRequiredGrade(
    currentWeightedPoints: Double,
    usedWeight: Double,
    remainingWeight: Double,
    targetAverage: Double,
    maxGrade: Double = 10.0
): RequiredGradeResult {
    if (targetAverage < 0.0 || targetAverage > maxGrade) return RequiredGradeResult.InvalidData
    if (usedWeight < 0.0 || remainingWeight < 0.0) return RequiredGradeResult.InvalidData

    if (remainingWeight <= 1e-6) {
        val currentAvg = if (usedWeight > 1e-6) currentWeightedPoints / usedWeight else 0.0
        return if (currentAvg >= targetAverage - 1e-9) RequiredGradeResult.AlreadyEnough
        else RequiredGradeResult.Completed
    }

    val needed = (targetAverage * 100.0 - currentWeightedPoints) / remainingWeight
    return when {
        needed <= 0.0 -> RequiredGradeResult.AlreadyEnough
        needed > maxGrade + 1e-9 -> RequiredGradeResult.Impossible
        else -> RequiredGradeResult.Needed(roundToTwoDecimals(needed))
    }
}

/**
 * Versión de conveniencia que calcula directamente desde una lista de notas de un periodo.
 * Asume que el peso total del periodo es 100%.
 */
fun calculateRequiredGradeFromNotes(
    notes: List<Notas>,
    targetAverage: Double,
    maxGrade: Double = 10.0
): RequiredGradeResult {
    val usedWeight = notes.sumOf { it.porcentaje ?: 0.0 }
    val currentWeightedPoints = notes.sumOf { (it.nota ?: 0.0) * (it.porcentaje ?: 0.0) }
    val remainingWeight = 100.0 - usedWeight
    return calculateRequiredGrade(
        currentWeightedPoints = currentWeightedPoints,
        usedWeight = usedWeight,
        remainingWeight = remainingWeight,
        targetAverage = targetAverage,
        maxGrade = maxGrade
    )
}

private fun roundToTwoDecimals(value: Double): Double =
    (value * 100.0).roundToInt() / 100.0
