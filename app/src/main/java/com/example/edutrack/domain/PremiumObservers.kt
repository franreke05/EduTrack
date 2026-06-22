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

            override fun onCancelled(error: DatabaseError) = Unit
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
