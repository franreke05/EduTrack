package com.example.edutrack.dataclass

data class Reminder(
    val id: String = "",
    val title: String = "",
    val type: String = "Examen",
    val dueAtMillis: Long = 0L,
    val notes: String = ""
)
