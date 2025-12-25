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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

private enum class AuthMode { Login, Register }

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
            firebaseAuthWithGoogle(auth, account?.idToken, context, loading) { user ->
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

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))
                    )
                )
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = screenHeight * 0.06f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            color = MaterialTheme.colorScheme.onSurface,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = MaterialTheme.shapes.extraLarge,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            enabled = !isLoading,
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
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
                                        Surface(
                                            modifier = Modifier.size(32.dp),
                                            color = Color.Transparent,
                                            shape = CircleShape,
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "G",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Continuar con Google",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = if (authMode == AuthMode.Login) "Correo o usuario" else "Correo electrónico") },
                            placeholder = {
                                Text(
                                    text = if (authMode == AuthMode.Login) "correo@ejemplo.com o usuario" else "correo@ejemplo.com"
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false,
                                keyboardType = KeyboardType.Email
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
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(autoCorrect = false)
                            )
                        }

                        OutlinedTextField(
                            value = password,
                            onValueChange = onPasswordChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Contraseña") },
                            placeholder = { Text(text = "••••••••") },
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
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Password)
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
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Password)
                            )
                        }

                        Button(
                            onClick = onSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF245D34)),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (authMode == AuthMode.Login) "Iniciar sesión" else "Crear cuenta",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (authMode == AuthMode.Login) "Regístrate aquí" else "Inicia sesión",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .clickable { onToggleMode() }
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tus datos se guardarán en tu cuenta y podrás recuperarlos en cualquier dispositivo. Te enviaremos un correo de verificación cuando registres una nueva cuenta.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Text(
                            text = "Al continuar, aceptas nuestros Términos de Servicio y Política de Privacidad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

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

private fun sendVerificationEmailIfNeeded(user: FirebaseUser?, context: Context) {
    val currentUser = user ?: return
    if (!currentUser.isEmailVerified) {
        currentUser.sendEmailVerification()
            .addOnFailureListener {
                Toast.makeText(context, "No se pudo enviar el correo de verificación.", Toast.LENGTH_LONG).show()
            }
    }
}

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

private fun googleSignInOptions(context: Context): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
}

private fun firebaseAuthWithGoogle(
    auth: FirebaseAuth,
    idToken: String?,
    context: Context,
    loading: MutableState<Boolean>,
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
                val user = auth.currentUser
                persistUserInDatabase(user, context)
                sendVerificationEmailIfNeeded(user, context)
                if (user != null) onSuccess(user)
            } else {
                Toast.makeText(context, "Error autenticando con Google.", Toast.LENGTH_LONG)
                    .show()
            }
        }
}
