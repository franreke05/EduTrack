package com.example.edutrack.dataclass

data class Group(
    var id: String? = "",
    val name: String? = "",
    val description: String? = "",
    val creatorId: String? = "",
    val ownerId: String? = creatorId,
    val createdAt: Long? = 0L,
    val updatedAt: Long? = 0L,
    val settings: GroupSettings? = GroupSettings(),
    val members: Map<String, GroupMember>? = emptyMap()
)

data class GroupMember(
    val userId: String? = "",
    val displayName: String? = "",
    val role: String? = "member",
    val joinedAt: Long? = 0L,
    val updatedAt: Long? = 0L
)

data class GroupSettings(
    val gradePrivacy: String? = GroupGradePrivacy.PRIVATE.value,
    val allowSubjectSharing: Boolean? = true,
    val rankingEnabled: Boolean? = true,
    val chatEnabled: Boolean? = false
)

enum class GroupGradePrivacy(val value: String, val label: String) {
    PRIVATE("private", "Notas privadas siempre"),
    ANONYMOUS("anonymous", "Notas visibles anónimas"),
    NAMED("named", "Notas visibles con nombre"),
    SUMMARY("summaryOnly", "Solo medias/resúmenes visibles");

    companion object {
        fun fromValue(value: String?): GroupGradePrivacy =
            entries.firstOrNull { it.value == value } ?: PRIVATE
    }
}
