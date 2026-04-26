package com.example.edutrack.data.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.edutrack.R

class ReminderWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Recordatorio académico"
        val text = inputData.getString(KEY_TEXT) ?: "Tienes una fecha importante en Edutrack."
        ensureChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        runCatching {
            NotificationManagerCompat.from(applicationContext).notify(title.hashCode(), notification)
        }
        return Result.success()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios académicos",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            applicationContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "edutrack_reminders"
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
    }
}
