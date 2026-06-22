package com.example.edutrack.ui

import androidx.compose.runtime.compositionLocalOf
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.UserGroup
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.domain.PremiumCache
import com.example.edutrack.domain.UserPlan

val LocalUserPlan     = compositionLocalOf<UserPlan> { UserPlan.FREE }
val LocalAnios        = compositionLocalOf<List<Anio>> { emptyList() }
val LocalUsuario      = compositionLocalOf<Usuario?> { null }
val LocalPremiumCache = compositionLocalOf { PremiumCache() }
val LocalUserGroups   = compositionLocalOf<List<UserGroup>> { emptyList() }
