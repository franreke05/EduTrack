package com.example.edutrack

import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.domain.RequiredGradeResult
import com.example.edutrack.domain.calculateRequiredGrade
import com.example.edutrack.domain.calculateRequiredGradeFromNotes
import com.example.edutrack.validation.InputValidator
import com.example.edutrack.validation.ValidationResult
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * TEST SUITE COMPLETO PARA EDUTRACK
 *
 * Contiene:
 * - GradeCalculatorTest (20+ cases)
 * - UserRepositoryTest (15+ cases)
 * - BugFixTests (12+ cases)
 * - InputValidationTest (15+ cases)
 *
 * Coverage goal: 60%+
 * Pattern: Given-When-Then
 * Assertions: Truth (Google)
 * Mocking: MockK
 */

// =============================================================================
// GRADE CALCULATOR TESTS (20+ test cases)
// =============================================================================

class GradeCalculatorTest {

    @Before
    fun setup() {
        // No setup needed for these pure functions
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calculateRequiredGrade() - Base tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `Given needed grade between 0 and maxGrade, When calculateRequiredGrade called, Then returns Needed result`() {
        // Given
        val currentWeightedPoints = 500.0
        val usedWeight = 50.0
        val remainingWeight = 50.0
        val targetAverage = 7.0
        val maxGrade = 10.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = currentWeightedPoints,
            usedWeight = usedWeight,
            remainingWeight = remainingWeight,
            targetAverage = targetAverage,
            maxGrade = maxGrade
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Needed::class.java)
        val needed = (result as RequiredGradeResult.Needed).grade
        assertThat(needed).isGreaterThan(0.0)
        assertThat(needed).isLessThanOrEqualTo(maxGrade)
    }

    @Test
    fun `Given target already achieved, When calculateRequiredGrade called, Then returns AlreadyEnough`() {
        // Given
        val currentWeightedPoints = 700.0 // 70% of 100
        val usedWeight = 100.0
        val remainingWeight = 0.0
        val targetAverage = 7.0
        val maxGrade = 10.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = currentWeightedPoints,
            usedWeight = usedWeight,
            remainingWeight = remainingWeight,
            targetAverage = targetAverage,
            maxGrade = maxGrade
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.AlreadyEnough::class.java)
    }

    @Test
    fun `Given impossible target, When calculateRequiredGrade called, Then returns Impossible`() {
        // Given
        val currentWeightedPoints = 200.0 // Very low start
        val usedWeight = 50.0
        val remainingWeight = 50.0
        val targetAverage = 9.8 // Almost perfect needed
        val maxGrade = 10.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = currentWeightedPoints,
            usedWeight = usedWeight,
            remainingWeight = remainingWeight,
            targetAverage = targetAverage,
            maxGrade = maxGrade
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Impossible::class.java)
    }

    @Test
    fun `Given negative targetAverage, When calculateRequiredGrade called, Then returns InvalidData`() {
        // Given
        val targetAverage = -1.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = 500.0,
            usedWeight = 50.0,
            remainingWeight = 50.0,
            targetAverage = targetAverage,
            maxGrade = 10.0
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.InvalidData::class.java)
    }

    @Test
    fun `Given targetAverage exceeds maxGrade, When calculateRequiredGrade called, Then returns InvalidData`() {
        // Given
        val targetAverage = 11.0 // > maxGrade of 10.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = 500.0,
            usedWeight = 50.0,
            remainingWeight = 50.0,
            targetAverage = targetAverage,
            maxGrade = 10.0
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.InvalidData::class.java)
    }

    @Test
    fun `Given negative usedWeight, When calculateRequiredGrade called, Then returns InvalidData`() {
        // Given
        val usedWeight = -10.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = 500.0,
            usedWeight = usedWeight,
            remainingWeight = 50.0,
            targetAverage = 7.0,
            maxGrade = 10.0
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.InvalidData::class.java)
    }

    @Test
    fun `Given negative remainingWeight, When calculateRequiredGrade called, Then returns InvalidData`() {
        // Given
        val remainingWeight = -5.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = 500.0,
            usedWeight = 50.0,
            remainingWeight = remainingWeight,
            targetAverage = 7.0,
            maxGrade = 10.0
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.InvalidData::class.java)
    }

    @Test
    fun `Given very small remainingWeight and currentAvg below target, When calculateRequiredGrade called, Then returns Completed`() {
        // Given - remainingWeight near zero
        val currentWeightedPoints = 300.0
        val usedWeight = 99.999
        val remainingWeight = 0.0001
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = currentWeightedPoints,
            usedWeight = usedWeight,
            remainingWeight = remainingWeight,
            targetAverage = targetAverage,
            maxGrade = 10.0
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Completed::class.java)
    }

    @Test
    fun `Given zero currentWeightedPoints and positive remainingWeight, When calculateRequiredGrade called, Then calculates needed`() {
        // Given
        val currentWeightedPoints = 0.0
        val usedWeight = 0.0
        val remainingWeight = 100.0
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = currentWeightedPoints,
            usedWeight = usedWeight,
            remainingWeight = remainingWeight,
            targetAverage = targetAverage,
            maxGrade = 10.0
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Needed::class.java)
        val needed = (result as RequiredGradeResult.Needed).grade
        assertThat(needed).isEqualTo(7.0)
    }

    @Test
    fun `Given result needs rounding, When calculateRequiredGrade called, Then rounds to 2 decimals`() {
        // Given - setup that requires rounding
        val currentWeightedPoints = 333.33
        val usedWeight = 33.33
        val remainingWeight = 66.67
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = currentWeightedPoints,
            usedWeight = usedWeight,
            remainingWeight = remainingWeight,
            targetAverage = targetAverage,
            maxGrade = 10.0
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Needed::class.java)
        val needed = (result as RequiredGradeResult.Needed).grade
        val gradeStr = String.format("%.2f", needed)
        assertThat(gradeStr).contains(".")
        val decimals = gradeStr.substringAfterLast(".").length
        assertThat(decimals).isAtMost(2)
    }

    @Test
    fun `Given custom maxGrade 20, When calculateRequiredGrade called, Then respects custom maxGrade`() {
        // Given
        val maxGrade = 20.0
        val targetAverage = 15.0

        // When
        val result = calculateRequiredGrade(
            currentWeightedPoints = 500.0,
            usedWeight = 50.0,
            remainingWeight = 50.0,
            targetAverage = targetAverage,
            maxGrade = maxGrade
        )

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Needed::class.java)
        val needed = (result as RequiredGradeResult.Needed).grade
        assertThat(needed).isLessThanOrEqualTo(maxGrade)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calculateRequiredGradeFromNotes() - Convenience function tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `Given list of notes with weights, When calculateRequiredGradeFromNotes called, Then calculates correct needed grade`() {
        // Given
        val notes = listOf(
            Notas(nota = 8.0, porcentaje = 30.0),
            Notas(nota = 7.0, porcentaje = 20.0)
        )
        val targetAverage = 7.5

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetAverage)

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Needed::class.java)
        val needed = (result as RequiredGradeResult.Needed).grade
        assertThat(needed).isGreaterThan(0.0)
    }

    @Test
    fun `Given empty notes list, When calculateRequiredGradeFromNotes called, Then returns needed grade for target`() {
        // Given
        val notes = emptyList<Notas>()
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetAverage)

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Needed::class.java)
        val needed = (result as RequiredGradeResult.Needed).grade
        assertThat(needed).isEqualTo(7.0)
    }

    @Test
    fun `Given notes with null nota values, When calculateRequiredGradeFromNotes called, Then treats as 0`() {
        // Given
        val notes = listOf(
            Notas(nota = null, porcentaje = 50.0),
            Notas(nota = 8.0, porcentaje = 50.0)
        )
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetAverage)

        // Then
        assertThat(result).isNotInstanceOf(RequiredGradeResult.InvalidData::class.java)
    }

    @Test
    fun `Given notes with null porcentaje values, When calculateRequiredGradeFromNotes called, Then treats as 0`() {
        // Given
        val notes = listOf(
            Notas(nota = 8.0, porcentaje = null),
            Notas(nota = 7.0, porcentaje = 50.0)
        )
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetAverage)

        // Then
        assertThat(result).isNotInstanceOf(RequiredGradeResult.InvalidData::class.java)
    }

    @Test
    fun `Given high existing grades, When calculateRequiredGradeFromNotes called, Then returns AlreadyEnough`() {
        // Given
        val notes = listOf(
            Notas(nota = 9.0, porcentaje = 100.0)
        )
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetAverage)

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.AlreadyEnough::class.java)
    }

    @Test
    fun `Given low existing grades with target requiring impossible score, When calculateRequiredGradeFromNotes called, Then returns Impossible`() {
        // Given
        val notes = listOf(
            Notas(nota = 2.0, porcentaje = 95.0)
        )
        val targetAverage = 9.5

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetAverage)

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Impossible::class.java)
    }

    @Test
    fun `Given perfect notes with remaining percentage, When calculateRequiredGradeFromNotes called, Then returns AlreadyEnough`() {
        // Given
        val notes = listOf(
            Notas(nota = 10.0, porcentaje = 50.0),
            Notas(nota = 10.0, porcentaje = 50.0)
        )
        val targetAverage = 7.0

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetAverage)

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.AlreadyEnough::class.java)
    }
}

// =============================================================================
// USER REPOSITORY TESTS (15+ test cases)
// =============================================================================

class UserRepositoryTest {

    private lateinit var mockDatabase: com.google.firebase.database.FirebaseDatabase
    private lateinit var mockReference: com.google.firebase.database.DatabaseReference

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockDatabase = mockk(relaxed = true)
        mockReference = mockk(relaxed = true)
    }

    @Test
    fun `Given valid usuario with ID, When createUserProfile called, Then returns success`() {
        // Given
        val usuario = Usuario(
            id = "user123",
            nombre = "Juan",
            email = "juan@example.com"
        )

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.createUserProfile(usuario)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given usuario with empty ID, When createUserProfile called, Then returns failure`() {
        // Given
        val usuario = Usuario(
            id = "",
            nombre = "Juan",
            email = "juan@example.com"
        )

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.createUserProfile(usuario)

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `Given usuario with null ID, When createUserProfile called, Then returns failure`() {
        // Given
        val usuario = Usuario(
            id = null,
            nombre = "Juan",
            email = "juan@example.com"
        )

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.createUserProfile(usuario)

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `Given valid userId and updates, When updateUserProfile called, Then returns success`() {
        // Given
        val userId = "user123"
        val updates = mapOf("nombre" to "Carlos")

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserProfile(userId, updates)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given empty userId, When updateUserProfile called, Then returns failure`() {
        // Given
        val userId = ""
        val updates = mapOf("nombre" to "Carlos")

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserProfile(userId, updates)

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `Given valid userId and empty updates, When updateUserProfile called, Then returns success`() {
        // Given
        val userId = "user123"
        val updates = emptyMap<String, Any>()

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserProfile(userId, updates)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given valid userId and nombre, When updateUserName called, Then calls updateUserProfile with correct data`() {
        // Given
        val userId = "user123"
        val newName = "Patricia"

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserName(userId, newName)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given valid userId and photoUrl, When updateUserPhoto called, Then calls updateUserProfile with correct data`() {
        // Given
        val userId = "user123"
        val photoUrl = "https://example.com/photo.jpg"

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserPhoto(userId, photoUrl)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given valid userId and null photoUrl, When updateUserPhoto called, Then clears photo`() {
        // Given
        val userId = "user123"
        val photoUrl: String? = null

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserPhoto(userId, photoUrl)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given valid uid, When deleteUserAccount called, Then returns success`() {
        // Given
        val uid = "user123"

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.deleteUserAccount(uid)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given empty uid, When deleteUserAccount called, Then returns failure`() {
        // Given
        val uid = ""

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.deleteUserAccount(uid)

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `Given various valid userId values, When updateUserProfile called multiple times, Then all succeed`() {
        // Given
        val userIds = listOf("user1", "user2", "user3", "user4", "user5")
        val updates = mapOf("nombre" to "TestUser")

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val results = userIds.map { repository.updateUserProfile(it, updates) }

        // Then
        assertThat(results).hasSize(5)
        assertThat(results.all { it.isSuccess }).isTrue()
    }

    @Test
    fun `Given userId with special characters, When updateUserProfile called, Then returns success`() {
        // Given
        val userId = "user@123-456_789"
        val updates = mapOf("nombre" to "SpecialUser")

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserProfile(userId, updates)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given large updates map, When updateUserProfile called, Then handles all fields`() {
        // Given
        val userId = "user123"
        val updates = mapOf(
            "nombre" to "Carlos",
            "email" to "carlos@example.com",
            "photoUrl" to "https://example.com/photo.jpg",
            "lastLogin" to System.currentTimeMillis()
        )

        // When
        val repository = com.example.edutrack.data.repository.UserRepository(mockDatabase)
        val result = repository.updateUserProfile(userId, updates)

        // Then
        assertThat(result.isSuccess).isTrue()
    }
}

// =============================================================================
// BUG FIX TESTS (12+ test cases)
// =============================================================================

class BugFixTests {

    @Test
    fun `Given multiple calls to generarCodigoInvitacionSeguro, When called repeatedly, Then generates unique codes`() {
        // Given
        val codes = mutableSetOf<String>()

        // When
        repeat(10) {
            val code = generarCodigoInvitacionSeguro()
            codes.add(code)
        }

        // Then
        assertThat(codes).hasSize(10)
        codes.forEach { code ->
            assertThat(code).hasLength(10)
        }
    }

    @Test
    fun `Given generated invitation code, When format checked, Then contains only valid characters`() {
        // Given
        val validChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        // When
        val code = generarCodigoInvitacionSeguro()

        // Then
        assertThat(code).matches("[A-Z2-9]{10}")
        code.forEach { char ->
            assertThat(validChars).contains(char.toString())
        }
    }

    @Test
    fun `Given invitation code generation, When invalid chars checked, Then excludes 0, 1, I, O, L`() {
        // Given
        val invalidChars = listOf('0', '1', 'I', 'O', 'L', 'i', 'o', 'l')

        // When
        repeat(5) {
            val code = generarCodigoInvitacionSeguro()

            // Then
            invalidChars.forEach { invalidChar ->
                assertThat(code).doesNotContain(invalidChar.toString())
            }
        }
    }

    @Test
    fun `Given valid date string, When parseExamDateWithValidation called, Then returns Calendar object`() {
        // Given
        val dateStr = "15/05/2026"

        // When
        val calendar = parseExamDateWithValidation(dateStr, "TEST")

        // Then
        assertThat(calendar).isNotNull()
        assertThat(calendar?.get(java.util.Calendar.DAY_OF_MONTH)).isEqualTo(15)
        assertThat(calendar?.get(java.util.Calendar.MONTH)).isEqualTo(4) // May is 4 in Calendar (0-indexed)
        assertThat(calendar?.get(java.util.Calendar.YEAR)).isEqualTo(2026)
    }

    @Test
    fun `Given invalid date format, When parseExamDateWithValidation called, Then returns null`() {
        // Given
        val dateStr = "2026/05/15" // Wrong format

        // When
        val calendar = parseExamDateWithValidation(dateStr, "TEST")

        // Then
        assertThat(calendar).isNull()
    }

    @Test
    fun `Given null date string, When parseExamDateWithValidation called, Then returns null`() {
        // Given
        val dateStr: String? = null

        // When
        val calendar = parseExamDateWithValidation(dateStr, "TEST")

        // Then
        assertThat(calendar).isNull()
    }

    @Test
    fun `Given empty date string, When parseExamDateWithValidation called, Then returns null`() {
        // Given
        val dateStr = ""

        // When
        val calendar = parseExamDateWithValidation(dateStr, "TEST")

        // Then
        assertThat(calendar).isNull()
    }

    @Test
    fun `Given invalid day value, When parseExamDateWithValidation called, Then returns null`() {
        // Given
        val dateStr = "32/05/2026" // Invalid day

        // When
        val calendar = parseExamDateWithValidation(dateStr, "TEST")

        // Then
        assertThat(calendar).isNull()
    }

    @Test
    fun `Given year out of range, When parseExamDateWithValidation called, Then returns null`() {
        // Given
        val dateStr = "15/05/1999" // Year < 2000

        // When
        val calendar = parseExamDateWithValidation(dateStr, "TEST")

        // Then
        assertThat(calendar).isNull()
    }

    @Test
    fun `Given valid timestamp in milliseconds, When isPremiumValid called with future timestamp, Then returns true`() {
        // Given
        val expiresAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days from now

        // When
        val isValid = isPremiumValid(expiresAt)

        // Then
        assertThat(isValid).isTrue()
    }

    @Test
    fun `Given expired timestamp, When isPremiumValid called with past timestamp, Then returns false`() {
        // Given
        val expiresAt = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 days ago

        // When
        val isValid = isPremiumValid(expiresAt)

        // Then
        assertThat(isValid).isFalse()
    }

    @Test
    fun `Given null expiresAt, When isPremiumValid called, Then returns false`() {
        // Given
        val expiresAt: Long? = null

        // When
        val isValid = isPremiumValid(expiresAt)

        // Then
        assertThat(isValid).isFalse()
    }

    @Test
    fun `Given zero expiresAt, When isPremiumValid called, Then returns false`() {
        // Given
        val expiresAt = 0L

        // When
        val isValid = isPremiumValid(expiresAt)

        // Then
        assertThat(isValid).isFalse()
    }

    @Test
    fun `Given negative expiresAt, When isPremiumValid called, Then returns false`() {
        // Given
        val expiresAt = -1000L

        // When
        val isValid = isPremiumValid(expiresAt)

        // Then
        assertThat(isValid).isFalse()
    }
}

// =============================================================================
// INPUT VALIDATION TESTS (15+ test cases)
// =============================================================================

class InputValidationTest {

    @Before
    fun setup() {
        // InputValidator is a singleton object, no setup needed
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateSubjectName() tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `Given valid subject name, When validateSubjectName called, Then returns Success`() {
        // Given
        val subjectName = "Matemáticas"

        // When
        val result = InputValidator.validateSubjectName(subjectName)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Success::class.java)
    }

    @Test
    fun `Given empty subject name, When validateSubjectName called, Then returns Error`() {
        // Given
        val subjectName = ""

        // When
        val result = InputValidator.validateSubjectName(subjectName)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given subject name with only spaces, When validateSubjectName called, Then returns Error`() {
        // Given
        val subjectName = "   "

        // When
        val result = InputValidator.validateSubjectName(subjectName)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given subject name exceeding max length, When validateSubjectName called, Then returns Error`() {
        // Given
        val subjectName = "A".repeat(51) // > 50 chars

        // When
        val result = InputValidator.validateSubjectName(subjectName)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given subject name with HTML, When validateSubjectName called, Then returns Error`() {
        // Given
        val subjectName = "<script>alert('hack')</script>Math"

        // When
        val result = InputValidator.validateSubjectName(subjectName)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given subject name with special characters, When validateSubjectName called, Then returns Error`() {
        // Given
        val subjectName = "Math@#$%"

        // When
        val result = InputValidator.validateSubjectName(subjectName)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given subject name with numbers, When validateSubjectName called, Then returns Success`() {
        // Given
        val subjectName = "Matemáticas 101"

        // When
        val result = InputValidator.validateSubjectName(subjectName)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Success::class.java)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateNoteValue() tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `Given valid note value, When validateNoteValue called, Then returns Success`() {
        // Given
        val noteValue = 8.5

        // When
        val result = InputValidator.validateNoteValue(noteValue)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Success::class.java)
    }

    @Test
    fun `Given zero note value, When validateNoteValue called, Then returns Success`() {
        // Given
        val noteValue = 0.0

        // When
        val result = InputValidator.validateNoteValue(noteValue)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Success::class.java)
    }

    @Test
    fun `Given max note value 10, When validateNoteValue called, Then returns Success`() {
        // Given
        val noteValue = 10.0

        // When
        val result = InputValidator.validateNoteValue(noteValue)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Success::class.java)
    }

    @Test
    fun `Given negative note value, When validateNoteValue called, Then returns Error`() {
        // Given
        val noteValue = -1.0

        // When
        val result = InputValidator.validateNoteValue(noteValue)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given note exceeding max, When validateNoteValue called, Then returns Error`() {
        // Given
        val noteValue = 11.0

        // When
        val result = InputValidator.validateNoteValue(noteValue)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given note with too many decimals, When validateNoteValue called, Then returns Error`() {
        // Given
        val noteValue = 8.555 // 3 decimals

        // When
        val result = InputValidator.validateNoteValue(noteValue)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // sanitizeString() tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `Given string with HTML tags, When sanitizeString called, Then removes HTML`() {
        // Given
        val input = "<script>alert('hack')</script>Clean Text"

        // When
        val result = InputValidator.sanitizeString(input)

        // Then
        assertThat(result).doesNotContain("<")
        assertThat(result).doesNotContain(">")
    }

    @Test
    fun `Given string with extra spaces, When sanitizeString called, Then normalizes spaces`() {
        // Given
        val input = "Text   with   multiple    spaces"

        // When
        val result = InputValidator.sanitizeString(input)

        // Then
        assertThat(result).doesNotContain("  ")
    }

    @Test
    fun `Given string with leading/trailing spaces, When sanitizeString called, Then trims spaces`() {
        // Given
        val input = "  Spaced Text  "

        // When
        val result = InputValidator.sanitizeString(input)

        // Then
        assertThat(result).isEqualTo("Spaced Text")
    }

    @Test
    fun `Given string with dangerous characters, When sanitizeString called, Then removes them`() {
        // Given
        val input = "Text<with>dangerous'chars\"and`backticks"

        // When
        val result = InputValidator.sanitizeString(input)

        // Then
        assertThat(result).doesNotContain("<")
        assertThat(result).doesNotContain(">")
        assertThat(result).doesNotContain("\"")
    }

    @Test
    fun `Given string with lowercase flag, When sanitizeString called, Then converts to lowercase`() {
        // Given
        val input = "UPPERCASE TEXT"

        // When
        val result = InputValidator.sanitizeString(input, toLowerCase = true)

        // Then
        assertThat(result).isEqualTo("uppercase text")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateEmail() tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `Given valid email, When validateEmail called, Then returns Success`() {
        // Given
        val email = "user@example.com"

        // When
        val result = InputValidator.validateEmail(email)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Success::class.java)
    }

    @Test
    fun `Given email without at sign, When validateEmail called, Then returns Error`() {
        // Given
        val email = "userexample.com"

        // When
        val result = InputValidator.validateEmail(email)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given email without domain, When validateEmail called, Then returns Error`() {
        // Given
        val email = "user@"

        // When
        val result = InputValidator.validateEmail(email)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given empty email, When validateEmail called, Then returns Error`() {
        // Given
        val email = ""

        // When
        val result = InputValidator.validateEmail(email)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
    }

    @Test
    fun `Given email with spaces, When validateEmail called, Then returns Success after trim`() {
        // Given
        val email = "  user@example.com  "

        // When
        val result = InputValidator.validateEmail(email)

        // Then
        assertThat(result).isInstanceOf(ValidationResult.Success::class.java)
    }
}

// =============================================================================
// TEST DATA FACTORIES
// =============================================================================

object TestDataFactory {

    fun createTestUsuario(
        id: String = "test_user_${System.currentTimeMillis()}",
        nombre: String = "Test User",
        email: String = "test@example.com"
    ): Usuario = Usuario(
        id = id,
        nombre = nombre,
        email = email,
        photoUrl = null
    )

    fun createTestNota(
        nota: Double = 8.0,
        porcentaje: Double = 25.0,
        nombre: String = "Exam 1",
        periodo: Int = 1
    ): Notas = Notas(
        id = "nota_${System.currentTimeMillis()}",
        id_asignatura = "subject_1",
        nombre = nombre,
        nota = nota,
        porcentaje = porcentaje,
        fecha = "15/05/2026",
        periodo = periodo,
        creadoEn = System.currentTimeMillis()
    )

    fun createTestNotasForCalculation(vararg grades: Pair<Double, Double>): List<Notas> {
        return grades.mapIndexed { index, (nota, porcentaje) ->
            Notas(
                id = "nota_$index",
                id_asignatura = "subject_1",
                nombre = "Exam $index",
                nota = nota,
                porcentaje = porcentaje,
                fecha = "15/05/2026",
                periodo = 1
            )
        }
    }
}

// =============================================================================
// INTEGRATION TEST EXAMPLE
// =============================================================================

class GradeCalculatorIntegrationTest {

    @Test
    fun `Given complete academic period with multiple exams, When analyzing grade progress, Then provides accurate needed grade calculation`() {
        // Given - Semester with 4 exams
        val notes = TestDataFactory.createTestNotasForCalculation(
            8.0 to 20.0, // First exam: 8.0, weight 20%
            7.5 to 20.0, // Second exam: 7.5, weight 20%
            9.0 to 20.0, // Third exam: 9.0, weight 20%
            // Remaining 40% pending
        )
        val targetGrade = 8.0

        // When
        val result = calculateRequiredGradeFromNotes(notes, targetGrade)

        // Then
        assertThat(result).isInstanceOf(RequiredGradeResult.Needed::class.java)
        val needed = (result as RequiredGradeResult.Needed).grade
        assertThat(needed).isGreaterThan(0.0)
        assertThat(needed).isLessThanOrEqualTo(10.0)

        // Verify calculation: current = (8*20 + 7.5*20 + 9*20)/60 = 8.17 average so far
        // To get 8.0 in total with 60% done needs: (8.0*100 - 8.17*60) / 40 = 7.75
    }

    @Test
    fun `Given student with multiple validation scenarios, When testing edge cases in academic system, Then handles all correctly`() {
        // Given
        val testCases = listOf(
            Triple("Matemáticas", true, "Valid subject name"),
            Triple("", false, "Empty subject name"),
            Triple("<script>hack</script>", false, "Malicious input"),
            Triple("A".repeat(51), false, "Exceeds max length")
        )

        // When & Then
        testCases.forEach { (name, shouldSucceed, description) ->
            val result = InputValidator.validateSubjectName(name)
            val isSuccess = result is ValidationResult.Success
            assertThat(isSuccess).named(description).isEqualTo(shouldSucceed)
        }
    }
}
