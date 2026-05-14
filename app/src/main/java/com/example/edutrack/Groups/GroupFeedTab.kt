package com.example.edutrack.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.edutrack.dataclass.GroupFeedEvent
import com.example.edutrack.dataclass.GroupFeedEventType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun GroupFeedTab(
    groupId: String,
    modifier: Modifier = Modifier
) {
    val events by rememberGroupFeedState(groupId)

    if (events.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "Aquí aparecerán las novedades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cada vez que alguien comparte una asignatura, se une o la importa, lo verás aquí.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(events, key = { _, e -> e.id ?: e.hashCode().toString() }) { index, event ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(event.id) {
                delay(index * 40L + 20L)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(250)) + slideInVertically { it / 4 }
            ) {
                FeedEventRow(event = event)
            }
        }
    }
}

@Composable
private fun FeedEventRow(event: GroupFeedEvent) {
    val type = runCatching { event.type?.let { GroupFeedEventType.valueOf(it) } }.getOrNull()
    val tint = colorForType(type)
    val icon = iconForType(type)
    val message = messageForEvent(event, type)
    val timestamp = event.createdAt?.let { relativeTimeSpanish(it) } ?: ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(40.dp)) {
                if (!event.actorPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = event.actorPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = (event.actorName ?: "?").take(1).uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (timestamp.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier.size(32.dp).clip(CircleShape),
                color = tint.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun colorForType(type: GroupFeedEventType?) = when (type) {
    GroupFeedEventType.JOIN -> MaterialTheme.colorScheme.primary
    GroupFeedEventType.LEAVE -> MaterialTheme.colorScheme.onSurfaceVariant
    GroupFeedEventType.SHARE -> MaterialTheme.colorScheme.tertiary
    GroupFeedEventType.UNSHARE -> MaterialTheme.colorScheme.error
    GroupFeedEventType.IMPORT -> MaterialTheme.colorScheme.secondary
    null -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun iconForType(type: GroupFeedEventType?): ImageVector = when (type) {
    GroupFeedEventType.JOIN -> Icons.AutoMirrored.Filled.Login
    GroupFeedEventType.LEAVE -> Icons.AutoMirrored.Filled.ExitToApp
    GroupFeedEventType.SHARE -> Icons.Default.Share
    GroupFeedEventType.UNSHARE -> Icons.Default.RemoveCircleOutline
    GroupFeedEventType.IMPORT -> Icons.Default.Download
    null -> Icons.Default.History
}

private fun messageForEvent(event: GroupFeedEvent, type: GroupFeedEventType?): String {
    val name = event.actorName?.takeIf { it.isNotBlank() } ?: "Alguien"
    val target = event.targetLabel?.takeIf { it.isNotBlank() }
    return when (type) {
        GroupFeedEventType.JOIN -> "$name se unió al grupo"
        GroupFeedEventType.LEAVE -> "$name salió del grupo"
        GroupFeedEventType.SHARE -> target?.let { "$name compartió $it" } ?: "$name compartió una asignatura"
        GroupFeedEventType.UNSHARE -> target?.let { "$name quitó $it" } ?: "$name eliminó una asignatura compartida"
        GroupFeedEventType.IMPORT -> target?.let { "$name importó $it" } ?: "$name importó una asignatura"
        null -> "Actividad del grupo"
    }
}

private fun relativeTimeSpanish(ts: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - ts).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L
    return when {
        minutes < 1 -> "Justo ahora"
        minutes < 60 -> "hace ${minutes}min"
        hours < 24 -> "hace ${hours}h"
        days == 1L -> "ayer"
        days < 7 -> "hace ${days} días"
        else -> {
            val nowCal = Calendar.getInstance()
            val eventCal = Calendar.getInstance().apply { timeInMillis = ts }
            val pattern = if (nowCal.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR)) "dd MMM" else "dd MMM yyyy"
            SimpleDateFormat(pattern, Locale("es", "ES")).format(Date(ts))
        }
    }
}
