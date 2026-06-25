package com.example.edutrack.Configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.edutrack.gestures.swipeBackGesture

enum class LegalType(val assetPath: String, @androidx.annotation.StringRes val titleRes: Int, val icon: ImageVector) {
    PRIVACY("legal/privacy", com.example.edutrack.R.string.legal_privacy_title, Icons.Rounded.Lock),
    TERMS("legal/terms", com.example.edutrack.R.string.legal_terms_title, Icons.Rounded.Info)
}

private sealed class LegalBlock {
    data class Title(val text: String) : LegalBlock()
    data class SectionHeader(val text: String) : LegalBlock()
    data class Body(val raw: String) : LegalBlock()
    data class Bullet(val text: String) : LegalBlock()
    data class Divider(val nothing: Unit = Unit) : LegalBlock()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalViewerScreen(type: LegalType, onBack: () -> Unit) {
    val context = LocalContext.current
    val blocks = remember(type) { parseLegalAsset(context, type.assetPath) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(9.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                type.icon, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(androidx.compose.ui.res.stringResource(type.titleRes), fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .swipeBackGesture(onBack),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(blocks) { block ->
                    when (block) {
                        is LegalBlock.Title -> {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                block.text,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        is LegalBlock.SectionHeader -> {
                            Spacer(Modifier.height(20.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Text(
                                    block.text,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        is LegalBlock.Body -> {
                            Text(
                                renderInlineBold(block.raw),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                        is LegalBlock.Bullet -> {
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                )
                                Text(
                                    renderInlineBold(block.text),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        is LegalBlock.Divider -> {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    ContactButton()
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ContactButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:franreke506@gmail.com")
                putExtra(android.content.Intent.EXTRA_SUBJECT, "EduTrack — Consulta privacidad")
            }
            runCatching {
                context.startActivity(android.content.Intent.createChooser(intent, "Contactar"))
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Rounded.Email, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(androidx.compose.ui.res.stringResource(com.example.edutrack.R.string.legal_contact_btn), fontWeight = FontWeight.SemiBold)
    }
}

private fun parseLegalAsset(context: android.content.Context, basePath: String): List<LegalBlock> {
    val lang = context.resources.configuration.locales[0].language
    val path = try {
        val localizedPath = "${basePath}_${lang}.md"
        context.assets.open(localizedPath)
        localizedPath
    } catch (_: Exception) {
        "${basePath}.md"
    }
    val lines = try {
        context.assets.open(path).bufferedReader().readLines()
    } catch (_: Exception) {
        return listOf(LegalBlock.Body("Content not available."))
    }

    val blocks = mutableListOf<LegalBlock>()
    for (raw in lines) {
        val line = raw.trim()
        when {
            line.startsWith("# ") -> blocks.add(LegalBlock.Title(line.removePrefix("# ").trim()))
            line.startsWith("## ") -> blocks.add(LegalBlock.SectionHeader(line.removePrefix("## ").trim()))
            line.startsWith("### ") -> blocks.add(LegalBlock.SectionHeader(line.removePrefix("### ").trim()))
            line == "---" -> blocks.add(LegalBlock.Divider())
            line.startsWith("- ") -> blocks.add(LegalBlock.Bullet(line.removePrefix("- ").trim()))
            line.startsWith("* ") -> blocks.add(LegalBlock.Bullet(line.removePrefix("* ").trim()))
            line.isBlank() -> { /* skip */ }
            else -> blocks.add(LegalBlock.Body(line))
        }
    }
    return blocks
}

// Renders **bold** inline markers using AnnotatedString.
@Composable
private fun renderInlineBold(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
        var i = 0
        while (i < text.length) {
            if (text.startsWith("**", i)) {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(boldStyle) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else {
                    append(text[i]); i++
                }
            } else {
                append(text[i]); i++
            }
        }
    }
}
