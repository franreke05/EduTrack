package com.example.edutrack.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.edutrack.dataclass.Asignatura
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ExamReminderScheduler {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun schedule(context: Context, asignatura: Asignatura) {
        val fechaStr = asignatura.fechaExamen ?: return
        val horaStr = asignatura.horaExamen ?: "09:00"
        val cal = Calendar.getInstance()
        runCatching {
            cal.time = dateFormat.parse(fechaStr) ?: return
        }.onFailure { return }
        val parts = horaStr.split(":")
        cal.set(Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 9)
        cal.set(Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
        cal.set(Calendar.SECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        if (cal.timeInMillis <= System.currentTimeMillis()) return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, asignatura, fechaStr)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    fun cancel(context: Context, asignatura: Asignatura) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, asignatura, "") ?: return
        am.cancel(pi)
    }

    private fun buildPendingIntent(context: Context, asignatura: Asignatura, fechaStr: String): PendingIntent {
        val intent = Intent(context, ExamAlarmReceiver::class.java).apply {
            putExtra("asignaturaNombre", asignatura.nombre ?: "Examen")
            putExtra("fechaExamen", fechaStr)
        }
        return PendingIntent.getBroadcast(
            context,
            asignatura.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
