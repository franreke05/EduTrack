package com.example.edutrack.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.edutrack.premiumCacheRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

enum class UserPlan { FREE, PREMIUM }

enum class SimulatorAccess { BASIC_APPROVE_ONLY, FULL }

data class PlanLimits(
    val maxCourses: Int?,
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
    maxCourses = 2,
    maxGroupsJoined = 1,
    canCreateGroups = false,
    canManageGroups = false,
    canUseAdvancedGroupFeatures = false,
    unlimitedSubjects = false,
    simulatorMode = SimulatorAccess.BASIC_APPROVE_ONLY,
    advancedStats = false,
    reminders = false,
    pdfExport = false,
    advancedPersonalization = false,
    adsEnabled = true
)

private val PREMIUM_LIMITS = PlanLimits(
    maxCourses = null,
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

data class PremiumCache(
    val isPremium: Boolean = false,
    val expiresAt: Long? = null,
    val productId: String? = null
)

@Composable
fun rememberPremiumCache(userId: String?): State<PremiumCache> {
    val state = remember { mutableStateOf(PremiumCache()) }
    DisposableEffect(userId) {
        if (userId.isNullOrBlank()) return@DisposableEffect onDispose {}
        val ref = premiumCacheRef(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = PremiumCache(
                    isPremium = snapshot.child("isPremium").getValue(Boolean::class.java) ?: false,
                    expiresAt = snapshot.child("expiresAt").getValue(Long::class.java),
                    productId = snapshot.child("productId").getValue(String::class.java)
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

@Composable
fun rememberUserPlan(userId: String?): State<UserPlan> {
    val state = remember { mutableStateOf(UserPlan.FREE) }
    DisposableEffect(userId) {
        if (userId.isNullOrBlank()) {
            state.value = UserPlan.FREE
            return@DisposableEffect onDispose {}
        }
        val ref = premiumCacheRef(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isPremium = snapshot.child("isPremium").getValue(Boolean::class.java) ?: false
                state.value = if (isPremium) UserPlan.PREMIUM else UserPlan.FREE
            }
            override fun onCancelled(error: DatabaseError) {
                state.value = UserPlan.FREE
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}
