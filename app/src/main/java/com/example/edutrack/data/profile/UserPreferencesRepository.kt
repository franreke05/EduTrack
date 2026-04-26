package com.example.edutrack.data.profile

import com.example.edutrack.data.firebase.FirebasePaths
import com.example.edutrack.dataclass.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

class UserPreferencesRepository(
    private val database: DatabaseReference = Firebase.database.reference
) {
    suspend fun savePreferences(userId: String, preferences: UserPreferences) {
        FirebasePaths.settings(userId)
            .child("preferences")
            .setValue(preferences)
            .await()
    }
}
