package com.example.edutrack.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.edutrack.DB_URL
import com.example.edutrack.R
import com.example.edutrack.examenesRef
import com.google.firebase.database.FirebaseDatabase

class ExamAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nombre   = intent.getStringExtra("asignaturaNombre") ?: "Examen"
        val fecha    = intent.getStringExtra("fechaExamen") ?: ""
        val hora     = intent.getStringExtra("horaExamen") ?: ""
        val materia  = intent.getStringExtra("asignaturaNombreReal") ?: ""
        val userId   = intent.getStringExtra("userId") ?: ""
        val anioId   = intent.getStringExtra("anioId") ?: ""
        val asigId   = intent.getStringExtra("asignaturaId") ?: ""
        val examenId = intent.getStringExtra("examenId") ?: ""

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de exámenes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Aviso a la hora del examen" }
            nm.createNotificationChannel(channel)
        }

        val bodyText = buildString {
            if (materia.isNotBlank()) append(materia)
            if (fecha.isNotBlank()) { if (isNotEmpty()) append(" · "); append(fecha) }
            if (hora.isNotBlank())  { append(" a las "); append(hora) }
            if (isEmpty()) append("Revisa tu horario")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("¡Examen hoy! $nombre")
            .setContentText(bodyText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        nm.notify(nombre.hashCode(), notification)

        // Borrar el examen de Firebase una vez que ha llegado su hora
        if (userId.isNotBlank() && anioId.isNotBlank() && asigId.isNotBlank() && examenId.isNotBlank()) {
            val pending = goAsync()
            try {
                FirebaseDatabase.getInstance(DB_URL).setPersistenceEnabled(true)
            } catch (_: Exception) {}
            examenesRef(userId, anioId, asigId).child(examenId)
                .removeValue()
                .addOnCompleteListener { pending.finish() }
                .addOnFailureListener  { pending.finish() }
        }
    }

    companion object {
        const val CHANNEL_ID = "exam_reminders"
    }
}
