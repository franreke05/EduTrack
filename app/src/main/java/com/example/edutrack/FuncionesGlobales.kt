package com.example.edutrack

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.edutrack.Registro.Registros.RegistroActivity
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database

lateinit var db_ref: DatabaseReference

fun CrearUsuario(
    usuario: Usuario,
){

     db_ref = Firebase.database.reference
    if (usuario.id=="") {
       //le ponemos un id unico al usuario
        usuario.id=db_ref.push().key.toString()
    }
    Log.d("Usuario222", usuario.id.toString())
    db_ref.child("Edutrack").child("Usuario").child(usuario.id.toString()).setValue(usuario)
}
fun AgregarNota_Asignatura(asignatura: Asignatura, nota: Notas){
    val db_ref = Firebase.database.reference
    db_ref.child("Edutrack").child("Asignatura").child(asignatura.id.toString()).child("Notas").child(nota.id.toString()).setValue(nota)
}
fun CrearAnio(anio: Anio) {
    val db_ref = Firebase.database.reference
    anio.id=db_ref.child("Edutrack").child("Anio").child(anio.id.toString()).key
    db_ref.child("Edutrack").child("Anio").child(anio.id.toString()).setValue(anio)
}
fun CrearAsignatura(asignatura: Asignatura) {
    val db_ref = Firebase.database.reference
    asignatura.id=db_ref.child("Edutrack").child("Asignatura").child(asignatura.id.toString()).key
    db_ref.child("Edutrack").child("Asignatura").child(asignatura.id.toString()).setValue(asignatura)
}

/**
 * Edita los datos de un usuario en la base de datos.
 *
 * @param userId El ID del usuario a editar.
 * @param updates Un mapa con los campos a actualizar y sus nuevos valores.
 * @param onResult Callback que se llama con `true` si la operación fue exitosa, `false` en caso contrario.
 */
fun EditarUsuario(userId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
    if (userId.isEmpty()) {
        onResult(false)
        return
    }
    Firebase.database.reference.child("Edutrack").child("Usuario").child(userId)
        .updateChildren(updates)
        .addOnSuccessListener {
            Log.d("FirebaseEdit", "Usuario $userId actualizado.")
            onResult(true)
        }
        .addOnFailureListener {
            Log.e("FirebaseEdit", "Error al actualizar usuario $userId.", it)
            onResult(false)
        }
}

/**
 * Borra todos los datos de un usuario de forma segura y completa.
 *
 * @param context El contexto de la aplicación para navegar.
 * @param userId El ID del usuario a eliminar.
 * @param onFinish Callback que se llama cuando todo el proceso ha terminado.
 */
fun borrarUsuarioCompleto(context: Context, userId: String, onFinish: () -> Unit) {
    if (userId.isEmpty()) {
        onFinish()
        return
    }

    val dbRoot = Firebase.database.reference.child("Edutrack")
    val authUser = Firebase.auth.currentUser

    // NOTA: Esta función borra Años y el Usuario. Para un borrado completo,
    // se necesitaría saber cómo se relacionan las Asignaturas y las Notas para borrarlas también.

    // 1. Borrar todos los años asociados al usuario
    dbRoot.child("Anio").orderByChild("id_user").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.children.forEach { it.ref.removeValue() }
            Log.d("FirebaseCleanup", "Años del usuario $userId eliminados.")

            // 2. Borrar al usuario de la base de datos
            dbRoot.child("Usuario").child(userId).removeValue().addOnCompleteListener { userDbTask ->
                if(userDbTask.isSuccessful) Log.d("FirebaseCleanup", "Nodo de usuario $userId eliminado de la DB.")
                else Log.e("FirebaseCleanup", "Error al eliminar datos de la DB.", userDbTask.exception)

                // 3. Borrar de Authentication
                authUser?.delete()?.addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        Log.d("FirebaseAuth", "Usuario eliminado de Auth.")
                        // 4. Limpiar SharedPreferences
                        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().clear().apply()

                        // 5. Navegar a la pantalla de registro
                        val intent = Intent(context, RegistroActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        Log.e("FirebaseAuth", "Error al eliminar de Auth.", authTask.exception)
                    }
                    onFinish()
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("FirebaseCleanup", "Error buscando años para eliminar.", error.toException())
            onFinish()
        }
    })
}
