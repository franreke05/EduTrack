package com.example.edutrack.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.edutrack.R

class ExamAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nombre = intent.getStringExtra("asignaturaNombre") ?: "Examen"
        val fecha = intent.getStringExtra("fechaExamen") ?: ""
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de exámenes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Aviso el día anterior al examen" }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Examen mañana: $nombre")
            .setContentText(if (fecha.isNotBlank()) "Fecha: $fecha" else "Revisa tu horario")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(nombre.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "exam_reminders"
    }
}
