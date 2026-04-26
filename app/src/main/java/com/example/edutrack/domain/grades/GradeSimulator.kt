package com.example.edutrack.domain.grades

import com.example.edutrack.core.FreemiumLimits
import com.example.edutrack.dataclass.Notas

sealed interface GradeSimulationResult {
    data class Required(val grade: Double) : GradeSimulationResult
    data object AlreadyEnough : GradeSimulationResult
    data object Impossible : GradeSimulationResult
    data object NoRemainingPercentage : GradeSimulationResult
    data object InvalidTarget : GradeSimulationResult
}

fun calculateRequiredGrade(notes: List<Notas>, targetAverage: Double): GradeSimulationResult {
    if (targetAverage !in 0.0..10.0) return GradeSimulationResult.InvalidTarget
    val usedPercentage = notes.sumOf { it.porcentaje ?: 0.0 }
    val weightedCurrent = notes.sumOf { (it.nota ?: 0.0) * ((it.porcentaje ?: 0.0) / 100.0) }
    val remaining = FreemiumLimits.MAX_TOTAL_GRADE_PERCENT - usedPercentage

    if (weightedCurrent >= targetAverage) return GradeSimulationResult.AlreadyEnough
    if (remaining <= 0.0) return GradeSimulationResult.NoRemainingPercentage

    val required = (targetAverage - weightedCurrent) / (remaining / 100.0)
    return if (required > 10.0) GradeSimulationResult.Impossible else GradeSimulationResult.Required(required)
}

fun usedPercentage(notes: List<Notas>): Double =
    notes.sumOf { it.porcentaje ?: 0.0 }
