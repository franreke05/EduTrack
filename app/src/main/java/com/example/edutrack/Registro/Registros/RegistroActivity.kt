package com.example.edutrack.Registro.Registros

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.R
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.Registro.signup.RegistrarUsuarioActivity
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
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
    val googleAuthDisabled = remember { true } // Deshabilitado temporalmente
    val webClientId = remember { context.getString(R.string.default_web_client_id) }
    val resolvedWebClientId = remember(webClientId) {
        webClientId
            .takeIf { it.isNotBlank() }
            ?.takeUnless { it.contains("REPLACE_WITH_WEB_CLIENT_ID", ignoreCase = true) }
    }
    val googleSignInClient = remember(resolvedWebClientId, googleAuthDisabled) {
        if (googleAuthDisabled) null else resolvedWebClientId?.let { GoogleSignIn.getClient(context, googleSignInOptions(it)) }
    }

    val launcher =
        if (googleAuthDisabled) null
        else rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(auth, account?.idToken, context, loading) { user ->
                    onLoginSuccess(user.uid)
                }
            } catch (e: ApiException) {
                loading.value = false
                Toast.makeText(context, "No se pudo iniciar sesion con Google.", Toast.LENGTH_LONG).show()
            }
        }

    val isLoading = isLoadingOverride ?: loading.value

    Scaffold { innerPadding ->
        LoginContent(
            modifier = modifier.padding(innerPadding),
            isLoading = isLoading,
            onGoogleSignIn = {
                if (loading.value) return@LoginContent
                if (googleAuthDisabled) {
                    Toast.makeText(context, "Autenticacion con Google deshabilitada temporalmente.", Toast.LENGTH_LONG).show()
                    return@LoginContent
                }
                if (googleSignInClient == null) {
                    Toast.makeText(context, "Configura el ID de cliente web de Firebase antes de iniciar sesion.", Toast.LENGTH_LONG).show()
                    return@LoginContent
                }
                loading.value = true
                launcher?.launch(googleSignInClient.signInIntent)
            },
            onRegister = {
                val intent = Intent(context, RegistrarUsuarioActivity::class.java)
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun LoginContent(
    modifier: Modifier,
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onRegister: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF6A00FF))
                )
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(screenHeight * 0.06f))

        Text(
            text = "EduTrack",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = screenHeight * 0.01f)
        )

        Text(
            text = "Tu estudio, sin friccion",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(screenHeight * 0.03f))
                .shadow(screenHeight * 0.03f),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(screenHeight * 0.025f),
                verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.agendita),
                    contentDescription = "Logo",
                    modifier = Modifier.size(screenHeight * 0.15f)
                )
                Text(
                    text = "Autenticate con Google para continuar",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(screenHeight * 0.03f))

        Button(
            onClick = { onGoogleSignIn() },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(54.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Continuar con Google", color = Color.White)
            }
        }

        Button(
            onClick = onRegister,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0x1A007AFF)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Registrarse", color = Color(0xFF007AFF))
        }

        Spacer(modifier = Modifier.height(screenHeight * 0.02f))

        Text(
            text = "Usamos tu cuenta de Google para autenticacion segura.\nSe enviara un correo de verificacion si tu email no esta verificado.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(screenHeight * 0.18f))

        Text(
            text = "Tus datos se guardaran en tu cuenta y podras recuperarlos en cualquier dispositivo.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

private fun googleSignInOptions(webClientId: String): GoogleSignInOptions {
    // Autenticacion con Google deshabilitada: devolvemos opciones minimas sin token.
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
    // Autenticacion con Google deshabilitada temporalmente.
    loading.value = false
    Toast.makeText(context, "Autenticacion con Google deshabilitada temporalmente.", Toast.LENGTH_LONG).show()
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

private fun sendVerificationEmailIfNeeded(user: FirebaseUser?, context: Context) {
    val currentUser = user ?: return
    if (!currentUser.isEmailVerified) {
        currentUser.sendEmailVerification()
            .addOnCompleteListener { task ->
                val message = if (task.isSuccessful) {
                    "Hemos reenviado un correo de verificacion a ${currentUser.email}"
                } else {
                    "No se pudo enviar el correo de verificacion. Intenta mas tarde."
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    EduTrackTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
