package com.example.edutrack.core

object AppConstants {
    const val SUPPORT_EMAIL = "support@edutrack.app"
}

object BillingProductIds {
    const val PREMIUM_MONTHLY = "edutrack_premium_monthly"
    const val PREMIUM_YEARLY = "edutrack_premium_yearly"
    val ALL = listOf(PREMIUM_MONTHLY, PREMIUM_YEARLY)
}

object FreemiumLimits {
    const val MAX_FREE_COURSES = 2
    const val MAX_FREE_GROUPS = 1
    const val MAX_FREE_TRIMESTER_SUBJECTS = 8
    const val MAX_FREE_CUATRIMESTER_SUBJECTS = 9
    const val MAX_TOTAL_GRADE_PERCENT = 100.0

    fun maxSubjectsForPeriodType(type: String?): Int =
        if (type.equals("Cuatrimestre", ignoreCase = true)) {
            MAX_FREE_CUATRIMESTER_SUBJECTS
        } else {
            MAX_FREE_TRIMESTER_SUBJECTS
        }
}

object AdMobIds {
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
}
