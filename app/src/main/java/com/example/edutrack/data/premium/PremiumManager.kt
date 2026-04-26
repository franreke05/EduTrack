package com.example.edutrack.data.premium

import android.content.Context
import com.example.edutrack.data.isPremiumFlow
import com.example.edutrack.data.rewardedSimulatorUntilFlow
import com.example.edutrack.data.setPremiumEntitlement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class PremiumManager(private val context: Context) {
    val isPremium: Flow<Boolean> = context.isPremiumFlow()

    val hasSimulatorAccess: Flow<Boolean> =
        combine(context.isPremiumFlow(), context.rewardedSimulatorUntilFlow()) { isPremium, rewardedUntil ->
            isPremium || rewardedUntil > System.currentTimeMillis()
        }

    suspend fun updatePremiumEntitlement(value: Boolean) {
        context.setPremiumEntitlement(value)
    }
}
