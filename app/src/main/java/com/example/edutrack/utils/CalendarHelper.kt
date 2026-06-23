package com.example.edutrack.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.edutrack.dataclass.GroupExam
import java.util.Calendar
import java.util.TimeZone

fun addGroupExamToCalendar(context: Context, exam: GroupExam) {
    val parts = (exam.fecha ?: return).split("/")
    if (parts.size != 3) return
    val cal = Calendar.getInstance().apply {
        set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
        val hora = exam.hora?.split(":") ?: emptyList()
        set(Calendar.HOUR_OF_DAY, hora.getOrNull(0)?.toIntOrNull() ?: 9)
        set(Calendar.MINUTE, hora.getOrNull(1)?.toIntOrNull() ?: 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val title = buildString {
        append(exam.nombre ?: "Examen")
        exam.asignatura?.takeIf { it.isNotBlank() }?.let { append(" — $it") }
    }
    context.startActivity(
        Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DTSTART, cal.timeInMillis)
            putExtra(CalendarContract.Events.DTEND, cal.timeInMillis + 2 * 60 * 60 * 1000L)
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
    )
}
