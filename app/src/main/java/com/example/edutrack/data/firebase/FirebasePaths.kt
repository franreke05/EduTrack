package com.example.edutrack.data.firebase

import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

object FirebasePaths {
    val root: DatabaseReference
        get() = Firebase.database.reference

    fun user(uid: String): DatabaseReference = root.child("users").child(uid)

    fun profile(uid: String): DatabaseReference = user(uid).child("profile")

    fun settings(uid: String): DatabaseReference = user(uid).child("settings")

    fun premiumCache(uid: String): DatabaseReference = user(uid).child("premiumCache")

    fun years(uid: String): DatabaseReference = user(uid).child("years")

    fun year(uid: String, yearId: String): DatabaseReference = years(uid).child(yearId)

    fun subjects(uid: String, yearId: String): DatabaseReference =
        year(uid, yearId).child("subjects")

    fun subject(uid: String, yearId: String, subjectId: String): DatabaseReference =
        subjects(uid, yearId).child(subjectId)

    fun notes(uid: String, yearId: String, subjectId: String): DatabaseReference =
        subject(uid, yearId, subjectId).child("notes")

    fun note(uid: String, yearId: String, subjectId: String, noteId: String): DatabaseReference =
        notes(uid, yearId, subjectId).child(noteId)

    fun groups(): DatabaseReference = root.child("groups")

    fun group(groupId: String): DatabaseReference = groups().child(groupId)

    fun groupMembers(groupId: String): DatabaseReference =
        root.child("groupMembers").child(groupId)

    fun groupMember(groupId: String, uid: String): DatabaseReference =
        groupMembers(groupId).child(uid)

    fun userGroups(uid: String): DatabaseReference =
        root.child("userGroups").child(uid)

    fun userGroup(uid: String, groupId: String): DatabaseReference =
        userGroups(uid).child(groupId)

    fun groupMessages(groupId: String): DatabaseReference =
        root.child("groupMessages").child(groupId)

    fun groupSharedSubjects(groupId: String): DatabaseReference =
        root.child("groupSharedSubjects").child(groupId)

    fun username(normalizedUsername: String): DatabaseReference =
        root.child("usernames").child(normalizedUsername)
}

fun normalizeUsername(value: String): String =
    value.trim()
        .lowercase()
        .replace("\\s+".toRegex(), "")
        .replace("[^a-z0-9._-]".toRegex(), "")
