package com.example.edutrack.domain.stats

import com.example.edutrack.Inicio.calcularMediaAnio
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas

data class AcademicStats(
    val courseCount: Int,
    val subjectCount: Int,
    val average: Double?,
    val bestSubject: Asignatura?,
    val worstSubject: Asignatura?,
    val usedPercentageAverage: Double
)

fun calculateAcademicStats(courses: List<Anio>): AcademicStats {
    val subjects = courses.flatMap { it.lista_asignaturas?.values.orEmpty() }
    val mediaCursos = courses.mapNotNull { calcularMediaAnio(it) }
    val average = mediaCursos.takeIf { it.isNotEmpty() }?.average()
    val subjectsWithAverage = subjects.filter { it.media != null && (it.media ?: 0.0) > 0.0 }
    val usedPercentages = subjects.map { subject ->
        subject.lista_notas.orEmpty().sumOf { it.porcentaje ?: 0.0 }
    }
    return AcademicStats(
        courseCount = courses.size,
        subjectCount = subjects.size,
        average = average,
        bestSubject = subjectsWithAverage.maxByOrNull { it.media ?: 0.0 },
        worstSubject = subjectsWithAverage.minByOrNull { it.media ?: 0.0 },
        usedPercentageAverage = usedPercentages.takeIf { it.isNotEmpty() }?.average() ?: 0.0
    )
}

fun averageForNotes(notes: List<Notas>): Double {
    val total = notes.sumOf { it.porcentaje ?: 0.0 }
    if (total <= 0.0) return 0.0
    return notes.sumOf { (it.nota ?: 0.0) * (it.porcentaje ?: 0.0) } / total
}
