package com.example.edutrack.domain

import com.example.edutrack.dataclass.Notas
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradeCalculatorTest {

    // Caso 1: nota necesaria calculada correctamente (ejemplo del enunciado)
    @Test
    fun `needed grade calculated correctly from example`() {
        // T1: Examen 30% nota 6, Trabajo 20% nota 7
        // currentWeightedPoints = 6*30 + 7*20 = 320
        // remainingWeight = 50, target = 5
        // needed = (5*100 - 320) / 50 = 3.6
        val result = calculateRequiredGrade(
            currentWeightedPoints = 320.0,
            usedWeight = 50.0,
            remainingWeight = 50.0,
            targetAverage = 5.0
        )
        assertTrue(result is RequiredGradeResult.Needed)
        assertEquals(3.6, (result as RequiredGradeResult.Needed).grade, 0.01)
    }

    // Caso 2: ya tiene suficiente (needed <= 0)
    @Test
    fun `already enough when accumulated points exceed target`() {
        // 8.0 * 80% = 640 puntos, target=5, needed = (5*100-640)/20 = -7 → AlreadyEnough
        val result = calculateRequiredGrade(
            currentWeightedPoints = 640.0,
            usedWeight = 80.0,
            remainingWeight = 20.0,
            targetAverage = 5.0
        )
        assertTrue(result is RequiredGradeResult.AlreadyEnough)
    }

    // Caso 3: imposible, nota necesaria > 10
    @Test
    fun `impossible when required grade exceeds max`() {
        // media=1.0 en 90%, target=9
        // needed = (9*100 - 90) / 10 = 810/10 = 81 > 10 → Impossible
        val result = calculateRequiredGrade(
            currentWeightedPoints = 90.0,
            usedWeight = 90.0,
            remainingWeight = 10.0,
            targetAverage = 9.0
        )
        assertTrue(result is RequiredGradeResult.Impossible)
    }

    // Caso 4: periodo completo y no alcanza objetivo
    @Test
    fun `completed when no remaining weight and average below target`() {
        // 100% evaluado, media = 3.0, target = 5 → Completed (no se puede hacer nada)
        val result = calculateRequiredGrade(
            currentWeightedPoints = 300.0,
            usedWeight = 100.0,
            remainingWeight = 0.0,
            targetAverage = 5.0
        )
        assertTrue(result is RequiredGradeResult.Completed)
    }

    // Caso 5: periodo completo y ya supera objetivo
    @Test
    fun `already enough when period complete and average above target`() {
        val result = calculateRequiredGrade(
            currentWeightedPoints = 700.0,
            usedWeight = 100.0,
            remainingWeight = 0.0,
            targetAverage = 5.0
        )
        assertTrue(result is RequiredGradeResult.AlreadyEnough)
    }

    // Caso 6: datos inválidos — target fuera de rango
    @Test
    fun `invalid data when target out of range`() {
        val result = calculateRequiredGrade(
            currentWeightedPoints = 300.0,
            usedWeight = 50.0,
            remainingWeight = 50.0,
            targetAverage = 11.0
        )
        assertTrue(result is RequiredGradeResult.InvalidData)
    }

    // Caso 7: porcentaje restante calculado correctamente desde notas
    @Test
    fun `remaining weight calculated correctly from notes list`() {
        val notes = listOf(
            Notas(id = "1", nota = 6.0, porcentaje = 30.0, periodo = 1),
            Notas(id = "2", nota = 7.0, porcentaje = 20.0, periodo = 1)
        )
        val result = calculateRequiredGradeFromNotes(notes, targetAverage = 5.0)
        assertTrue(result is RequiredGradeResult.Needed)
        assertEquals(3.6, (result as RequiredGradeResult.Needed).grade, 0.01)
    }

    // Caso 8: notas con porcentajes parciales, resultado verificable
    @Test
    fun `partial notes calculate needed grade correctly`() {
        // 8.0 en 60%, target=7 → needed = (7*100 - 480)/40 = 220/40 = 5.5
        val notes = listOf(
            Notas(id = "1", nota = 8.0, porcentaje = 60.0, periodo = 1)
        )
        val result = calculateRequiredGradeFromNotes(notes, targetAverage = 7.0)
        assertTrue(result is RequiredGradeResult.Needed)
        assertEquals(5.5, (result as RequiredGradeResult.Needed).grade, 0.01)
    }

    // Caso 9: lista vacía de notas → todo el periodo pendiente
    @Test
    fun `empty notes list means 100 percent remaining`() {
        val result = calculateRequiredGradeFromNotes(emptyList(), targetAverage = 5.0)
        assertTrue(result is RequiredGradeResult.Needed)
        // needed = (5*100 - 0) / 100 = 5.0
        assertEquals(5.0, (result as RequiredGradeResult.Needed).grade, 0.01)
    }

    // Caso 10: objetivo negativo → InvalidData
    @Test
    fun `invalid data when target is negative`() {
        val result = calculateRequiredGrade(
            currentWeightedPoints = 0.0,
            usedWeight = 0.0,
            remainingWeight = 100.0,
            targetAverage = -1.0
        )
        assertTrue(result is RequiredGradeResult.InvalidData)
    }
}
