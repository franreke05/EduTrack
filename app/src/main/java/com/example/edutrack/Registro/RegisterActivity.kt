package com.example.edutrack.Registro

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.edutrack.R
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.profileRef
import com.example.edutrack.ui.theme.EduTrackTheme
import com.example.edutrack.utils.wrapWithLocale
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

private enum class AuthMode { Login, Register }

class RegisterActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(newBase.wrapWithLocale())
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
    onForgotPassword: (() -> Unit)? = null,
    isLoadingOverride: Boolean? = null
) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val loading: MutableState<Boolean> = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = isLoadingOverride ?: loading.value

    RegisteerContent(
        modifier = modifier,
        isLoading = isLoading,
        authMode = authMode,
        email = email,
        displayName = displayName,
        password = password,
        confirmPassword = confirmPassword,
        passwordVisible = passwordVisible,
        onEmailChange = { email = it },
        onDisplayNameChange = { displayName = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
        onForgotPassword = onForgotPassword,
        onSubmit = {
            if (loading.value) return@RegisteerContent
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, context.getString(R.string.registro_fill_email_password), Toast.LENGTH_LONG).show()
                return@RegisteerContent
            }
            loading.value = true
            if (authMode == AuthMode.Login) {
                val loginInput = email.trim()
                val signInWithEmail: (String) -> Unit = { resolvedEmail ->
                    auth.signInWithEmailAndPassword(resolvedEmail, password)
                        .addOnCompleteListener { task ->
                            loading.value = false
                            if (task.isSuccessful) {
                                auth.currentUser?.let { user ->
                                    persistUserInDatabase(user, context)
                                    onAuthSuccess(user.uid)
                                }
                            } else {
                                val loginError = when {
                                    task.exception?.message?.contains("no user record") == true ->
                                        context.getString(R.string.registro_error_no_account)
                                    task.exception?.message?.contains("password is invalid") == true ||
                                    task.exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                                        context.getString(R.string.registro_error_wrong_password)
                                    task.exception?.message?.contains("too many") == true ->
                                        context.getString(R.string.registro_error_too_many_attempts)
                                    task.exception?.message?.contains("Chain validation failed") == true ||
                                    task.exception?.message?.contains("SSL") == true ->
                                        context.getString(R.string.registro_error_connection)
                                    else -> context.getString(R.string.registro_error_prefix, task.exception?.message ?: context.getString(R.string.registro_error_unknown))
                                }
                                Toast.makeText(context, loginError, Toast.LENGTH_LONG).show()
                            }
                        }
                }
                if (loginInput.contains("@")) {
                    signInWithEmail(loginInput)
                } else {
                    resolveEmailForUsername(loginInput) { resolvedEmail ->
                        if (resolvedEmail == null) {
                            loading.value = false
                            Toast.makeText(context, context.getString(R.string.registro_error_user_not_found), Toast.LENGTH_LONG).show()
                        } else {
                            signInWithEmail(resolvedEmail)
                        }
                    }
                }
            } else {
                if (confirmPassword.isBlank()) {
                    loading.value = false
                    Toast.makeText(context, context.getString(R.string.registro_confirm_password), Toast.LENGTH_LONG).show()
                    return@RegisteerContent
                }
                if (password != confirmPassword) {
                    loading.value = false
                    Toast.makeText(context, context.getString(R.string.registro_passwords_no_match), Toast.LENGTH_LONG).show()
                    return@RegisteerContent
                }
                if (password.length < 6) {
                    loading.value = false
                    Toast.makeText(context, context.getString(R.string.registro_password_min_length), Toast.LENGTH_LONG).show()
                    return@RegisteerContent
                }
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        loading.value = false
                        if (task.isSuccessful) {
                            auth.currentUser?.let { user ->
                                persistUserInDatabase(user, context, displayName.trim().ifBlank { null })
                                sendVerificationEmailIfNeeded(user, context)
                                Toast.makeText(context, context.getString(R.string.registro_account_created), Toast.LENGTH_LONG).show()
                                onAuthSuccess(user.uid)
                            }
                        } else {
                            val errorMsg = when {
                                task.exception?.message?.contains("email address is already in use") == true ->
                                    context.getString(R.string.registro_error_email_in_use)
                                task.exception?.message?.contains("badly formatted") == true ->
                                    context.getString(R.string.registro_error_bad_email)
                                task.exception?.message?.contains("weak-password") == true ->
                                    context.getString(R.string.registro_error_weak_password)
                                else -> context.getString(R.string.registro_error_prefix, task.exception?.message ?: context.getString(R.string.registro_error_unknown))
                            }
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
            }
        },
        onGoogleSignIn = {
            if (loading.value) return@RegisteerContent
            loading.value = true
            scope.launch {
                try {
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        .setAutoSelectEnabled(false)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val result = credentialManager.getCredential(context = context, request = request)
                    val credential = result.credential
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(auth, tokenCredential.idToken, context, loading, authMode) { user ->
                            onAuthSuccess(user.uid)
                        }
                    } else {
                        loading.value = false
                        Toast.makeText(context, context.getString(R.string.registro_error_credential_type), Toast.LENGTH_LONG).show()
                    }
                } catch (e: GetCredentialCancellationException) {
                    loading.value = false
                } catch (e: GetCredentialException) {
                    loading.value = false
                    val msg = e.message?.take(120) ?: "Error desconocido"
                    Toast.makeText(context, context.getString(R.string.registro_error_google_signin, msg), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    loading.value = false
                    Toast.makeText(context, context.getString(R.string.registro_error_unexpected, e.message?.take(100) ?: ""), Toast.LENGTH_LONG).show()
                }
            }
        },
        onToggleMode = {
            authMode = if (authMode == AuthMode.Login) AuthMode.Register else AuthMode.Login
            confirmPassword = ""
            displayName = ""
        }
    )
}

@Composable
private fun RegisteerContent(
    modifier: Modifier,
    isLoading: Boolean,
    authMode: AuthMode,
    email: String,
    displayName: String,
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onForgotPassword: (() -> Unit)?,
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onToggleMode: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isRegister = authMode == AuthMode.Register

    Surface(modifier = modifier.fillMaxSize(), color = cs.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val hPad = if (maxWidth > 600.dp) (maxWidth - 460.dp) / 2f else 20.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = hPad),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(52.dp))

                // ── Logo + marca ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(cs.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.agendita),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "EduTrack",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isRegister) stringResource(R.string.registro_register_subtitle)
                    else stringResource(R.string.registro_login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                // ── Tarjeta principal ────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // Google
                        OutlinedButton(
                            onClick = onGoogleSignIn,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, cs.outlineVariant),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onSurface),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = cs.primary)
                            } else {
                                Icon(painterResource(R.drawable.ic_google_logo), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.registro_google_btn), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // OR divider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = cs.outlineVariant)
                            Text("o", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = cs.outlineVariant)
                        }

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (isRegister) stringResource(R.string.registro_email_label) else stringResource(R.string.registro_email_or_user_label)) },
                            placeholder = { Text(if (isRegister) stringResource(R.string.registro_email_placeholder) else stringResource(R.string.registro_email_or_user_placeholder)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )

                        // Nombre (solo registro)
                        AnimatedVisibility(visible = isRegister) {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = onDisplayNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.registro_name_label)) },
                                placeholder = { Text(stringResource(R.string.registro_name_placeholder)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Next)
                            )
                        }

                        // Contraseña
                        OutlinedTextField(
                            value = password,
                            onValueChange = onPasswordChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.registro_password_label)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) stringResource(R.string.registro_hide_password_cd) else stringResource(R.string.registro_show_password_cd)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Password, imeAction = if (isRegister) ImeAction.Next else ImeAction.Done)
                        )

                        // Olvidé contraseña (solo login)
                        AnimatedVisibility(visible = !isRegister) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                TextButton(
                                    onClick = { onForgotPassword?.invoke() },
                                    enabled = !isLoading && onForgotPassword != null,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.auth_forgot_password),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = cs.primary
                                    )
                                }
                            }
                        }

                        // Confirmar contraseña (solo registro)
                        AnimatedVisibility(visible = isRegister) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = onConfirmPasswordChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.registro_confirm_password_label)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                            )
                        }

                        // Botón principal
                        Button(
                            onClick = onSubmit,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = cs.onPrimary)
                            } else {
                                Text(
                                    if (isRegister) stringResource(R.string.registro_create_account_btn) else stringResource(R.string.registro_login_btn),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Toggle login / registro
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (isRegister) stringResource(R.string.registro_already_account) else stringResource(R.string.registro_no_account),
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    TextButton(onClick = onToggleMode, enabled = !isLoading) {
                        Text(
                            if (isRegister) stringResource(R.string.registro_login_link) else stringResource(R.string.registro_register_link),
                            fontWeight = FontWeight.SemiBold,
                            color = cs.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.registro_legal_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Pantalla de recuperación de contraseña ───────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { inner ->
        val cs = MaterialTheme.colorScheme

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding()
        ) {
            val hPad = if (maxWidth > 600.dp) (maxWidth - 460.dp) / 2f else 20.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPad),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // Icono
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (sent) cs.tertiaryContainer else cs.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (sent) Icons.Default.CheckCircle else Icons.Default.Email,
                        contentDescription = null,
                        tint = if (sent) cs.onTertiaryContainer else cs.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    if (sent) "Correo enviado" else stringResource(R.string.auth_forgot_password),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = cs.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    if (sent)
                        "Hemos enviado las instrucciones a $email. Revisa tu bandeja de entrada (y la carpeta de spam)."
                    else
                        "Introduce tu correo y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(32.dp))

                if (!sent) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cs.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.registro_email_label)) },
                                placeholder = { Text(stringResource(R.string.registro_email_placeholder)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(
                                    autoCorrect = false,
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                enabled = !isLoading
                            )

                            Button(
                                onClick = {
                                    val trimmed = email.trim()
                                    if (trimmed.isBlank()) {
                                        Toast.makeText(context, context.getString(R.string.registro_fill_email_password), Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    auth.sendPasswordResetEmail(trimmed)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                sent = true
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.registro_error_bad_email), Toast.LENGTH_LONG).show()
                                            }
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isLoading && email.isNotBlank()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = cs.onPrimary)
                                } else {
                                    Text("Enviar instrucciones", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Volver al inicio de sesión", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Helpers de Firebase (sin cambios) ───────────────────────────────────────

private fun persistUserInDatabase(user: FirebaseUser?, context: Context, displayName: String? = null) {
    val currentUser = user ?: return
    val userRef = profileRef(currentUser.uid)
    userRef.get().addOnSuccessListener { snapshot ->
        val nombreCalculado = displayName?.takeIf { it.isNotBlank() }
            ?: currentUser.displayName
            ?: currentUser.email?.substringBefore("@")
            ?: "Usuario"
        val authPhotoUrl = currentUser.photoUrl?.toString()
        if (snapshot.exists()) {
            val updates = mutableMapOf<String, Any>("nombre" to nombreCalculado, "email" to (currentUser.email ?: ""))
            val existingPhoto = snapshot.child("photoUrl").getValue(String::class.java)
            if (existingPhoto.isNullOrBlank() && !authPhotoUrl.isNullOrBlank()) updates["photoUrl"] = authPhotoUrl
            userRef.updateChildren(updates)
        } else {
            userRef.setValue(Usuario(id = currentUser.uid, nombre = nombreCalculado, email = currentUser.email ?: "", photoUrl = authPhotoUrl))
        }
    }.addOnFailureListener {
        Toast.makeText(context, context.getString(R.string.registro_error_save_user), Toast.LENGTH_LONG).show()
    }
}

private fun sendVerificationEmailIfNeeded(user: FirebaseUser?, context: Context) {
    user?.takeIf { !it.isEmailVerified }?.sendEmailVerification()
        ?.addOnFailureListener {
            Toast.makeText(context, context.getString(R.string.registro_error_verification_email), Toast.LENGTH_LONG).show()
        }
}

private fun resolveEmailForUsername(username: String, onResolved: (String?) -> Unit) {
    onResolved(null)
}

private fun firebaseAuthWithGoogle(
    auth: FirebaseAuth, idToken: String?, context: Context,
    loading: MutableState<Boolean>, authMode: AuthMode, onSuccess: (FirebaseUser) -> Unit
) {
    if (idToken.isNullOrEmpty()) {
        loading.value = false
        Toast.makeText(context, context.getString(R.string.registro_error_no_google_token), Toast.LENGTH_LONG).show()
        return
    }
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential).addOnCompleteListener { task ->
        loading.value = false
        if (task.isSuccessful) {
            val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
            val user = auth.currentUser
            persistUserInDatabase(user, context)
            sendVerificationEmailIfNeeded(user, context)
            val message = when {
                isNewUser -> context.getString(R.string.registro_google_account_created)
                authMode == AuthMode.Register -> context.getString(R.string.registro_google_already_exists)
                else -> context.getString(R.string.registro_google_signed_in)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if (user != null) onSuccess(user)
        } else {
            Toast.makeText(context, context.getString(R.string.registro_error_google_auth), Toast.LENGTH_LONG).show()
        }
    }
}
