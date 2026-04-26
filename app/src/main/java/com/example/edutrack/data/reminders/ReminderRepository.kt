package com.example.edutrack.data.reminders

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.edutrack.dataclass.Reminder
import java.util.concurrent.TimeUnit

class ReminderRepository(private val context: Context) {
    fun schedule(reminder: Reminder) {
        val delay = (reminder.dueAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, reminder.title)
            .putString(ReminderWorker.KEY_TEXT, "${reminder.type}: ${reminder.notes}".trim())
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueue(request)
    }
}
