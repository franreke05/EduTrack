package com.example.edutrack

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class EduTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance(DB_URL).setPersistenceEnabled(true)
    }
}
