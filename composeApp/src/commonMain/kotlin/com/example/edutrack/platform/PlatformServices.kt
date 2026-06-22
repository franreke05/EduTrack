package com.example.edutrack.platform

import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Examen
import com.example.edutrack.domain.UserPlan

interface BillingService {
    suspend fun purchasePremium(): Result<Unit>
}

interface AdService {
    fun shouldShowBanner(plan: UserPlan): Boolean = plan == UserPlan.FREE
}

interface NotificationScheduler {
    suspend fun scheduleExamReminder(examen: Examen): Result<Unit>
    suspend fun cancelExamReminder(examenId: String): Result<Unit>
}

interface PdfExporter {
    suspend fun exportYear(anio: Anio): Result<Unit>
}

interface ShareService {
    suspend fun shareText(text: String): Result<Unit>
}

interface ExternalUrlOpener {
    suspend fun openUrl(url: String): Result<Unit>
    suspend fun openSupportEmail(email: String, subject: String): Result<Unit>
}
