package com.example.edutrack.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.storage.storage

object FirebaseClients {
    val auth get() = Firebase.auth
    val database get() = Firebase.database
    val storage get() = Firebase.storage
}
