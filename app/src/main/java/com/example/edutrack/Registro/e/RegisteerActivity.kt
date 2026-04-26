package com.example.edutrack.Registro.e

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.edutrack.feature.auth.AuthScreen
import com.example.edutrack.ui.theme.EduTrackTheme

class RegisteerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                RegisteerScreen(onAuthSuccess = { finish() })
            }
        }
    }
}

@Composable
fun RegisteerScreen(
    modifier: Modifier = Modifier,
    onAuthSuccess: (String) -> Unit = {},
    isLoadingOverride: Boolean? = null
) {
    AuthScreen(
        modifier = modifier,
        onAuthSuccess = onAuthSuccess
    )
}
