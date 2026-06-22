package com.example.edutrack.domain

import kotlinx.serialization.Serializable

@Serializable
enum class UserPlan { FREE, PREMIUM }

@Serializable
enum class SimulatorAccess { BASIC_APPROVE_ONLY, FULL }

@Serializable
data class PlanLimits(
    val maxCourses: Int?,
    val maxSubjectsPerCourse: Int?,   // ponytail: null = unlimited
    val maxNotesPerDay: Int?,         // límite diario de notas en free
    val maxGroupsJoined: Int?,
    val canCreateGroups: Boolean,
    val canManageGroups: Boolean,
    val canUseAdvancedGroupFeatures: Boolean,
    val unlimitedSubjects: Boolean,
    val simulatorMode: SimulatorAccess,
    val advancedStats: Boolean,
    val reminders: Boolean,
    val pdfExport: Boolean,
    val advancedPersonalization: Boolean,
    val adsEnabled: Boolean
)

private val FREE_LIMITS = PlanLimits(
    maxCourses = 4,
    maxSubjectsPerCourse = 4,
    maxNotesPerDay = 10,
    maxGroupsJoined = 1,
    canCreateGroups = false,
    canManageGroups = false,
    canUseAdvancedGroupFeatures = false,
    unlimitedSubjects = false,
    simulatorMode = SimulatorAccess.BASIC_APPROVE_ONLY,
    advancedStats = false,
    reminders = true,
    pdfExport = false,
    advancedPersonalization = false,
    adsEnabled = true
)

private val PREMIUM_LIMITS = PlanLimits(
    maxCourses = null,
    maxSubjectsPerCourse = null,
    maxNotesPerDay = null,
    maxGroupsJoined = null,
    canCreateGroups = true,
    canManageGroups = true,
    canUseAdvancedGroupFeatures = true,
    unlimitedSubjects = true,
    simulatorMode = SimulatorAccess.FULL,
    advancedStats = true,
    reminders = true,
    pdfExport = true,
    advancedPersonalization = true,
    adsEnabled = false
)

object PlanManager {

    fun limitsFor(plan: UserPlan): PlanLimits = when (plan) {
        UserPlan.FREE -> FREE_LIMITS
        UserPlan.PREMIUM -> PREMIUM_LIMITS
    }

    fun isPremium(plan: UserPlan) = plan == UserPlan.PREMIUM

    fun canCreateCourse(plan: UserPlan, currentCount: Int): Boolean {
        val max = limitsFor(plan).maxCourses ?: return true
        return currentCount < max
    }

    fun canAddSubject(plan: UserPlan, currentSubjectsInCourse: Int): Boolean {
        val max = limitsFor(plan).maxSubjectsPerCourse ?: return true
        return currentSubjectsInCourse < max
    }

    fun canAddNoteToday(plan: UserPlan, notesTodayCount: Int): Boolean {
        val max = limitsFor(plan).maxNotesPerDay ?: return true
        return notesTodayCount < max
    }

    fun canJoinMoreGroups(plan: UserPlan, currentCount: Int): Boolean {
        val max = limitsFor(plan).maxGroupsJoined ?: return true
        return currentCount < max
    }

    fun canCreateGroup(plan: UserPlan) = limitsFor(plan).canCreateGroups
    fun canManageGroup(plan: UserPlan) = limitsFor(plan).canManageGroups
    fun canUseAdvancedGroups(plan: UserPlan) = limitsFor(plan).canUseAdvancedGroupFeatures
    fun canUseCustomTargets(plan: UserPlan) = limitsFor(plan).simulatorMode == SimulatorAccess.FULL
    fun canExportPdf(plan: UserPlan) = limitsFor(plan).pdfExport
    fun canUseAdvancedStats(plan: UserPlan) = limitsFor(plan).advancedStats
    fun canUseReminders(plan: UserPlan) = limitsFor(plan).reminders
    fun canUseAdvancedPersonalization(plan: UserPlan) = limitsFor(plan).advancedPersonalization
    fun shouldShowAds(plan: UserPlan) = limitsFor(plan).adsEnabled
}

@Serializable
data class PremiumCache(
    val isPremium: Boolean = false,
    val expiresAt: Long? = null,
    val productId: String? = null
)
