package com.example.edutrack.Registro.Registros

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit = {},
    isLoadingOverride: Boolean? = null
) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val loading: MutableState<Boolean> = remember { mutableStateOf(false) }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions(context)) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(auth, account?.idToken, context, loading) { user ->
                onLoginSuccess(user.uid)
            }
        } catch (e: ApiException) {
            loading.value = false
            Toast.makeText(context, "No se pudo iniciar sesión con Google.", Toast.LENGTH_LONG).show()
        }
    }

    val isLoading = isLoadingOverride ?: loading.value

    Scaffold { innerPadding ->
        LoginContent(
            modifier = modifier.padding(innerPadding),
            isLoading = isLoading,
            onGoogleSignIn = {
                if (loading.value) return@LoginContent
                loading.value = true
                launcher.launch(googleSignInClient.signInIntent)
            }
        )
    }
}

@Composable
private fun LoginContent(modifier: Modifier, isLoading: Boolean, onGoogleSignIn: () -> Unit) {
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
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = screenHeight * 0.04f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.agendita),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(screenHeight * 0.12f)
                            .clip(CircleShape)
                    )

                    Text(
                        text = "Inicia sesión",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Continúa con tu cuenta para sincronizar tus datos en cualquier dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { onGoogleSignIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
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
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¿No tienes cuenta?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Regístrate con Google para comenzar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

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
        Toast.makeText(context, "No se recibió el token de Google.", Toast.LENGTH_LONG).show()
        return
    }

    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            loading.value = false
            if (task.isSuccessful) {
                val user = auth.currentUser
                persistUserInDatabase(user, context)
                sendVerificationEmailIfNeeded(user)
                if (user != null) onSuccess(user)
            } else {
                Toast.makeText(context, "Error autenticando con Google.", Toast.LENGTH_LONG).show()
            }
        }
}

private fun persistUserInDatabase(user: FirebaseUser?, context: Context) {
    val currentUser = user ?: return
    val dbRef = Firebase.database.reference
    val userRef = dbRef.child("Edutrack").child("Usuario").child(currentUser.uid)

    userRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
            val nombreCalculado = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Usuario"
            val usuario = Usuario(
                id = currentUser.uid,
                nombre = nombreCalculado,
                email = currentUser.email ?: "",
                password = null
            )
            userRef.setValue(usuario)
        }
    }
}

private fun sendVerificationEmailIfNeeded(user: FirebaseUser?) {
    val currentUser = user ?: return
    if (!currentUser.isEmailVerified) {
        currentUser.sendEmailVerification()
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    EduTrackTheme {
        LoginContent(modifier = Modifier, isLoading = false, onGoogleSignIn = {})
    }
}
