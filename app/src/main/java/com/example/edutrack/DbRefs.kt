package com.example.edutrack

internal const val ROOT_NODE = "Edutrack"
const val DB_URL = "https://edutrack-5579f-default-rtdb.europe-west1.firebasedatabase.app/"

// Documentos legales (docs/legal/). RELLENAR con la URL pública real tras
// hospedarlos (GitHub Pages / Firebase Hosting). Play Console exige una URL
// pública de política de privacidad y otra de eliminación de datos.
const val PRIVACY_POLICY_URL = "https://edutrack.app/privacy"
const val TERMS_URL = "https://edutrack.app/terms"

internal fun db() = com.google.firebase.database.FirebaseDatabase.getInstance(DB_URL).reference

fun userRef(userId: String) = db().child(ROOT_NODE).child("users").child(userId)
fun profileRef(userId: String) = userRef(userId).child("profile")
fun premiumCacheRef(userId: String) = userRef(userId).child("premiumCache")
fun aniosRef(userId: String) = userRef(userId).child("anios")
fun anioRef(userId: String, anioId: String) = aniosRef(userId).child(anioId)
fun asignaturasRef(userId: String, anioId: String) = anioRef(userId, anioId).child("asignaturas")
fun asignaturaRef(userId: String, anioId: String, asignaturaId: String) = asignaturasRef(userId, anioId).child(asignaturaId)
fun notasRef(userId: String, anioId: String, asignaturaId: String) = asignaturaRef(userId, anioId, asignaturaId).child("notas")
fun examenesRef(userId: String, anioId: String, asignaturaId: String) = asignaturaRef(userId, anioId, asignaturaId).child("examenes")

fun groupsRef() = db().child(ROOT_NODE).child("groups")
fun groupRef(groupId: String) = groupsRef().child(groupId)
fun groupMembersRef(groupId: String) = db().child(ROOT_NODE).child("groupMembers").child(groupId)
fun groupMemberRef(groupId: String, uid: String) = groupMembersRef(groupId).child(uid)
fun userGroupsRef(uid: String) = db().child(ROOT_NODE).child("userGroups").child(uid)
fun groupSharedSubjectsRef(groupId: String) = db().child(ROOT_NODE).child("groupSharedSubjects").child(groupId)
fun groupFeedRef(groupId: String) = db().child(ROOT_NODE).child("groupFeed").child(groupId)
fun groupResourcesRef(groupId: String) = db().child(ROOT_NODE).child("groupResources").child(groupId)
fun groupExamsRef(groupId: String) = db().child(ROOT_NODE).child("groupExams").child(groupId)
fun groupGradesRef(groupId: String) = db().child(ROOT_NODE).child("groupGrades").child(groupId)
fun studentGradesRef(groupId: String, studentUid: String) = groupGradesRef(groupId).child(studentUid)

fun groupImportsRef(uid: String) = userRef(uid).child("groupImports")
fun groupImportsMetaRef(uid: String, monthKey: String) = groupImportsRef(uid).child("_meta").child(monthKey)

fun currentMonthKey(): String {
    val cal = java.util.Calendar.getInstance()
    return "%04d-%02d".format(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
}
