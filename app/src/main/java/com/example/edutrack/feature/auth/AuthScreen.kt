package com.example.edutrack.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edutrack.R
import com.example.edutrack.ui.components.EdutrackButton
import com.example.edutrack.ui.components.EdutrackCard

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onAuthSuccess: (String) -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.successUserId) {
        val userId = state.successUserId
        if (userId != null) {
            onAuthSuccess(userId)
            viewModel.clearNavigationEvent()
        }
    }

    AuthContent(
        modifier = modifier,
        state = state,
        onEmailChange = viewModel::onEmailOrUsernameChange,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onSubmit = viewModel::submit,
        onGoogleSignIn = { viewModel.signInWithGoogle(context) },
        onToggleMode = viewModel::toggleMode
    )
}

@Composable
private fun AuthContent(
    modifier: Modifier,
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onToggleMode: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier.size(82.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.agendita),
                        contentDescription = "Edutrack",
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(CircleShape)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Edutrack", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (state.mode == AuthMode.Login) "Accede a tu espacio académico." else "Crea tu cuenta para sincronizar tus datos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            if (state.mode == AuthMode.Login) "Iniciar sesión" else "Crear cuenta",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        GoogleButton(
                            isLoading = state.isLoading,
                            onClick = onGoogleSignIn
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text("o", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        AuthTextField(
                            value = state.emailOrUsername,
                            onValueChange = onEmailChange,
                            label = "Correo electrónico",
                            keyboardType = KeyboardType.Email,
                            leadingIcon = Icons.Default.Email
                        )

                        if (state.mode == AuthMode.Register) {
                            AuthTextField(
                                value = state.displayName,
                                onValueChange = onDisplayNameChange,
                                label = "Nombre visible",
                                keyboardType = KeyboardType.Text,
                                leadingIcon = Icons.Default.Person
                            )
                        }

                        AuthTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = "Contraseña",
                            keyboardType = KeyboardType.Password,
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility) {
                                    Icon(
                                        if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (state.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            }
                        )

                        if (state.mode == AuthMode.Register) {
                            AuthTextField(
                                value = state.confirmPassword,
                                onValueChange = onConfirmPasswordChange,
                                label = "Confirmar contraseña",
                                keyboardType = KeyboardType.Password,
                                leadingIcon = Icons.Default.Lock,
                                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                            )
                        }

                        state.errorMessage?.let {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    it,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        EdutrackButton(
                            text = if (state.mode == AuthMode.Login) "Entrar" else "Crear cuenta",
                            onClick = onSubmit,
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (state.mode == AuthMode.Login) "¿No tienes cuenta?" else "¿Ya tienes cuenta?",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onToggleMode, enabled = !state.isLoading) {
                                Text(if (state.mode == AuthMode.Login) "Regístrate" else "Inicia sesión")
                            }
                        }
                    }
                }

                Text(
                    "Al continuar aceptas los términos y la política de privacidad de Edutrack.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GoogleButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_google_logo),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(10.dp))
                Text("Continuar con Google", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, autoCorrect = false),
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
