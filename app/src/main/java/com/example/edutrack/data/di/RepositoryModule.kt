package com.example.edutrack.data.di

import com.example.edutrack.data.repository.GradeRepository
import com.example.edutrack.data.repository.GroupRepository
import com.example.edutrack.data.repository.PremiumRepository
import com.example.edutrack.data.repository.SubjectRepository
import com.example.edutrack.data.repository.UserRepository
import com.google.firebase.database.FirebaseDatabase

object RepositoryModule {

    private const val DB_URL = "https://edutrack-5579f-default-rtdb.europe-west1.firebasedatabase.app/"

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(DB_URL)
    }

    val userRepository: UserRepository by lazy { UserRepository(database) }
    val subjectRepository: SubjectRepository by lazy { SubjectRepository(database) }
    val gradeRepository: GradeRepository by lazy { GradeRepository(database) }
    val groupRepository: GroupRepository by lazy { GroupRepository(database) }
    val premiumRepository: PremiumRepository by lazy { PremiumRepository(database) }
}
