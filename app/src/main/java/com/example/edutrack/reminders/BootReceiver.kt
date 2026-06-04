package com.example.edutrack.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.edutrack.DB_URL
import com.example.edutrack.aniosRef
import com.example.edutrack.dataclass.Examen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try { FirebaseDatabase.getInstance(DB_URL).setPersistenceEnabled(true) } catch (_: Exception) {}
        aniosRef(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { anioSnap ->
                    val anioId = anioSnap.key ?: return@forEach
                    anioSnap.child("asignaturas").children.forEach { asigSnap ->
                        asigSnap.child("examenes").children.forEach { exSnap ->
                            val examen = exSnap.getValue(Examen::class.java) ?: return@forEach
                            if (examen.fecha.isNotBlank() && examen.hora.isNotBlank()) {
                                ExamReminderScheduler.schedule(context, examen, uid, anioId)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
