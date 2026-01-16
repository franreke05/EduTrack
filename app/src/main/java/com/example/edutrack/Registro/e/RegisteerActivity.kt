package com.example.edutrack.Registro.e

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.R
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// Modos de autenticacion disponibles.
private enum class AuthMode { Login, Register }

// Activity de acceso y registro.
class RegisteerActivity : ComponentActivity() {
    // Configura la UI de login/registro.
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

// Pantalla principal para login o registro.
@Composable
fun RegisteerScreen(
    modifier: Modifier = Modifier,
    onAuthSuccess: (String) -> Unit = {},
    isLoadingOverride: Boolean? = null
) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val loading: MutableState<Boolean> = remember { mutableStateOf(false) }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions(context)) }
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(auth, account?.idToken, context, loading, authMode) { user ->
                onAuthSuccess(user.uid)
            }
        } catch (e: ApiException) {
            loading.value = false
            Toast.makeText(context, "No se pudo iniciar sesión con Google.", Toast.LENGTH_LONG).show()
        }
    }

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
        onSubmit = {
            if (loading.value) return@RegisteerContent
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Completa el correo/usuario y la contraseña.", Toast.LENGTH_LONG).show()
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
                                Toast.makeText(context, "Credenciales inválidas o usuario no registrado.", Toast.LENGTH_LONG).show()
                            }
                        }
                }
                if (loginInput.contains("@")) {
                    signInWithEmail(loginInput)
                } else {
                    resolveEmailForUsername(loginInput) { resolvedEmail ->
                        if (resolvedEmail == null) {
                            loading.value = false
                            Toast.makeText(context, "Usuario no encontrado.", Toast.LENGTH_LONG).show()
                        } else {
                            signInWithEmail(resolvedEmail)
                        }
                    }
                }
            } else {
                if (confirmPassword.isBlank()) {
                    loading.value = false
                    Toast.makeText(context, "Confirma tu contraseña.", Toast.LENGTH_LONG).show()
                    return@RegisteerContent
                }
                if (password != confirmPassword) {
                    loading.value = false
                    Toast.makeText(context, "Las contraseñas no coinciden.", Toast.LENGTH_LONG).show()
                    return@RegisteerContent
                }
                if (password.length < 6) {
                    loading.value = false
                    Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_LONG).show()
                    return@RegisteerContent
                }

                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        loading.value = false
                        if (task.isSuccessful) {
                            auth.currentUser?.let { user ->
                                persistUserInDatabase(user, context, displayName.trim().ifBlank { null }, password)
                                sendVerificationEmailIfNeeded(user, context)
                                Toast.makeText(context, "Cuenta creada. Revisa tu correo para verificarla.", Toast.LENGTH_LONG).show()
                                onAuthSuccess(user.uid)
                            }
                        } else {
                            Toast.makeText(context, "No se pudo registrar el usuario.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        },
        onGoogleSignIn = {
            if (loading.value) return@RegisteerContent
            loading.value = true
            launcher.launch(googleSignInClient.signInIntent)
        },
        onToggleMode = {
            authMode = if (authMode == AuthMode.Login) AuthMode.Register else AuthMode.Login
            confirmPassword = ""
            displayName = ""
        }
    )
}
// Contenido visual del formulario de autenticacion.
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
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onToggleMode: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val colorScheme = MaterialTheme.colorScheme

    Surface(modifier = modifier.fillMaxSize(), color = colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primaryContainer.copy(alpha = 0.95f),
                            colorScheme.surface,
                            colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = screenHeight * 0.06f, bottom = screenHeight * 0.04f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.agendita),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(screenHeight * 0.12f)
                                .clip(CircleShape)
                        )

                        Text(
                            text = if (authMode == AuthMode.Login) "Inicia sesión" else "Crear cuenta",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (authMode == AuthMode.Login) {
                                "Continúa con tu cuenta para sincronizar tus datos."
                            } else {
                                "Regístrate con tu correo electrónico para continuar."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, colorScheme.outlineVariant),
                            enabled = !isLoading,
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = colorScheme.primary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_google_logo),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = if (authMode == AuthMode.Login) {
                                                "Iniciar sesión con Google"
                                            } else {
                                                "Crear cuenta con Google"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Divider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = if (authMode == AuthMode.Login) "Correo o usuario" else "Correo electrónico") },
                            placeholder = {
                                Text(
                                    text = if (authMode == AuthMode.Login) {
                                        "correo@ejemplo.com o usuario"
                                    } else {
                                        "correo@ejemplo.com"
                                    }
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outlineVariant,
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                cursorColor = colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        if (authMode == AuthMode.Register) {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = onDisplayNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(text = "Nombre o usuario") },
                                placeholder = { Text(text = "Tu nombre visible") },
                                singleLine = true,
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.primary,
                                    unfocusedBorderColor = colorScheme.outlineVariant,
                                    focusedContainerColor = colorScheme.surface,
                                    unfocusedContainerColor = colorScheme.surface,
                                    cursorColor = colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    autoCorrect = false,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }

                        OutlinedTextField(
                            value = password,
                            onValueChange = onPasswordChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Contraseña") },
                            placeholder = { Text(text = "********") },
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outlineVariant,
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                cursorColor = colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false,
                                keyboardType = KeyboardType.Password,
                                imeAction = if (authMode == AuthMode.Register) ImeAction.Next else ImeAction.Done
                            )
                        )

                        if (authMode == AuthMode.Register) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = onConfirmPasswordChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(text = "Confirmar contraseña") },
                                placeholder = { Text(text = "Repite tu contraseña") },
                                singleLine = true,
                                shape = MaterialTheme.shapes.extraLarge,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.primary,
                                    unfocusedBorderColor = colorScheme.outlineVariant,
                                    focusedContainerColor = colorScheme.surface,
                                    unfocusedContainerColor = colorScheme.surface,
                                    cursorColor = colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    autoCorrect = false,
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }

                        Button(
                            onClick = onSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (authMode == AuthMode.Login) "Iniciar sesión" else "Crear cuenta",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (authMode == AuthMode.Login) "¿No tienes cuenta?" else "¿Ya tienes cuenta?",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onToggleMode, enabled = !isLoading) {
                                Text(
                                    text = if (authMode == AuthMode.Login) "Regístrate aquí" else "Inicia sesión",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EduTrack",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tus datos se guardarán en tu cuenta y podrás recuperarlos en cualquier dispositivo. Te enviaremos un correo de verificación cuando registres una nueva cuenta.",
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Divider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Text(
                            text = "Al continuar, aceptas nuestros Términos de Servicio y Política de Privacidad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


// Guarda o actualiza el usuario en Firebase.
private fun persistUserInDatabase(
    user: FirebaseUser?,
    context: Context,
    displayName: String? = null,
    password: String? = null
) {
    val currentUser = user ?: return
    val dbRef = Firebase.database.reference
    val userRef = dbRef.child("Edutrack").child("Usuario").child(currentUser.uid)

    userRef.get().addOnSuccessListener { snapshot ->
        val nombreCalculado = displayName?.takeIf { it.isNotBlank() }
            ?: currentUser.displayName
            ?: currentUser.email?.substringBefore("@")
            ?: "Usuario"
        val usuario = Usuario(
            id = currentUser.uid,
            nombre = nombreCalculado,
            email = currentUser.email ?: "",
            password = password
        )
        if (snapshot.exists()) {
            userRef.updateChildren(
                mapOf(
                    "nombre" to usuario.nombre,
                    "email" to usuario.email
                )
            )
        } else {
            userRef.setValue(usuario)
        }
    }.addOnFailureListener {
        Toast.makeText(context, "No se pudo guardar el usuario en la base de datos.", Toast.LENGTH_LONG).show()
    }
}

// Envia correo de verificacion si hace falta.
private fun sendVerificationEmailIfNeeded(user: FirebaseUser?, context: Context) {
    val currentUser = user ?: return
    if (!currentUser.isEmailVerified) {
        currentUser.sendEmailVerification()
            .addOnFailureListener {
                Toast.makeText(context, "No se pudo enviar el correo de verificación.", Toast.LENGTH_LONG).show()
            }
    }
}


// Resuelve el email asociado a un nombre de usuario.
private fun resolveEmailForUsername(username: String, onResolved: (String?) -> Unit) {
    val usersRef = Firebase.database.reference.child("Edutrack").child("Usuario")
    usersRef.orderByChild("nombre").equalTo(username)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userSnapshot = snapshot.children.firstOrNull()
                val email = userSnapshot?.child("email")?.getValue(String::class.java)
                onResolved(email)
            }

            override fun onCancelled(error: DatabaseError) {
                onResolved(null)
            }
        })
}

// Configura Google Sign-In.
private fun googleSignInOptions(context: Context): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
}

// Autentica con Google y persiste el usuario si es valido.
private fun firebaseAuthWithGoogle(
    auth: FirebaseAuth,
    idToken: String?,
    context: Context,
    loading: MutableState<Boolean>,
    authMode: AuthMode,
    onSuccess: (FirebaseUser) -> Unit
) {
    if (idToken.isNullOrEmpty()) {
        loading.value = false
        Toast.makeText(context, "No se recibió el token de Google.", Toast.LENGTH_LONG)
            .show()
        return
    }

    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            loading.value = false
            if (task.isSuccessful) {
                val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                val user = auth.currentUser
                persistUserInDatabase(user, context)
                sendVerificationEmailIfNeeded(user, context)
                val message = when {
                    isNewUser -> "Cuenta creada con Google."
                    authMode == AuthMode.Register -> "Esta cuenta ya existe. Iniciando sesión."
                    else -> "Sesión iniciada con Google."
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                if (user != null) onSuccess(user)
            } else {
                Toast.makeText(context, "Error autenticando con Google.", Toast.LENGTH_LONG)
                    .show()
            }
        }
}

