package com.example.edutrack

import android.content.Context
import android.util.Log
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

private const val ROOT_NODE = "Edutrack"

private fun rootRef() = Firebase.database.reference.child(ROOT_NODE)

// Crea un usuario en Firebase y asigna un id si falta.
fun CrearUsuario(
    usuario: Usuario,
){

    val dbRoot = rootRef()
    val userId = usuario.id?.takeIf { it.isNotBlank() } ?: dbRoot.child("Usuario").push().key
    if (userId.isNullOrBlank()) return
    // Le ponemos un id unico al usuario si falta.
    usuario.id = userId
    Log.d("Usuario", "Creando usuario con id $userId")
    dbRoot.child("Usuario").child(userId).setValue(usuario)
}

// Agrega una nota a la asignatura indicada en Firebase.
fun AgregarNota_Asignatura(asignatura: Asignatura, nota: Notas) {
    val asignaturaId = asignatura.id?.takeIf { it.isNotBlank() } ?: return
    val notaId = nota.id?.takeIf { it.isNotBlank() } ?: return
    rootRef().child("Asignatura").child(asignaturaId).child("Notas").child(notaId).setValue(nota)
}

// Crea un anio y lo guarda en Firebase.
fun CrearAnio(anio: Anio) {
    val dbRoot = rootRef()
    val anioId = anio.id?.takeIf { it.isNotBlank() } ?: dbRoot.child("Anio").push().key
    if (anioId.isNullOrBlank()) return
    anio.id = anioId
    dbRoot.child("Anio").child(anioId).setValue(anio)
}

// Crea una asignatura y la guarda con una clave estable si tiene nombre.
fun CrearAsignatura(asignatura: Asignatura) {
    val dbRoot = rootRef()
    val creditos = asignatura.creditos ?: 0
    if (creditos <= 0) {
        Log.e("CrearAsignatura", "Creditos invalidos para la asignatura ${asignatura.nombre}")
        return
    }
    // Usar el nombre como clave estable para no pisar datos y evitar duplicados accidentales
    val keyFromName = asignatura.nombre?.let(::normalizarClave)
    val finalKey = if (!keyFromName.isNullOrBlank()) keyFromName else dbRoot.child("Asignatura").push().key
    asignatura.id = finalKey
    dbRoot.child("Asignatura").child(finalKey ?: "asignatura").setValue(asignatura)
}

/**
 * Borra una asignatura y todas sus notas, limpiando tambien la referencia dentro del anio.
 */
fun borrarAsignaturaCompleta(
    asignaturaId: String?,
    anioId: String?,
    onResult: (Boolean) -> Unit = {}
) {
    if (asignaturaId.isNullOrBlank()) {
        onResult(false)
        return
    }

    val dbRoot = rootRef()
    val asignaturaRef = dbRoot.child("Asignatura").child(asignaturaId)

    // Al borrar el nodo de Asignatura desaparecen tambien sus notas hijas.
    asignaturaRef.removeValue().addOnCompleteListener { asignaturaTask ->
        if (!asignaturaTask.isSuccessful) {
            Log.e("FirebaseCleanup", "Error al borrar asignatura $asignaturaId", asignaturaTask.exception)
            onResult(false)
            return@addOnCompleteListener
        }

        if (anioId.isNullOrBlank()) {
            onResult(true)
            return@addOnCompleteListener
        }

        dbRoot.child("Anio")
            .child(anioId)
            .child("lista_asignaturas")
            .child(asignaturaId)
            .removeValue()
            .addOnCompleteListener { anioTask ->
                if (!anioTask.isSuccessful) {
                    Log.e("FirebaseCleanup", "Asignatura borrada pero fallo limpiar en anio $anioId", anioTask.exception)
                }
                onResult(anioTask.isSuccessful)
            }
    }
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
    rootRef().child("Usuario").child(userId)
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

    val dbRoot = rootRef()
    val authUser = Firebase.auth.currentUser

    // NOTA: Esta función borra Años y el Usuario. Para un borrado completo,
    // se necesitaría saber cómo se relacionan las Asignaturas y las Notas para borrarlas también.

    // 1. Borrar todos los años asociados al usuario
    dbRoot.child("Anio").orderByChild("id_user").equalTo(userId)
        .addListenerForSingleValueEvent(object : ValueEventListener {
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

private fun normalizarClave(nombre: String): String =
    nombre
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "_")
        .replace("[^a-z0-9_\\-]".toRegex(), "")
