package com.example.edutrack.Inicio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.edutrack.Anio.AnioActivity
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.ui.theme.EduTrackTheme

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CuerpoInicio(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CuerpoInicio(modifier: Modifier = Modifier) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var context = LocalContext.current
    val anios = listOf(
        Anio("2021", "Descripción del año 2021"),
        Anio("2022", "Descripción del año 2022"),
        Anio("2023", "Descripción del año 2023"),
        Anio("2024", "Descripción del año 2024", "ddddd", "02/02/2024"),
    )
    val actions = listOf(
        "Perfil" to Icons.Default.Person,
        "Buscar" to Icons.Default.Search,
        "Añadir" to Icons.Default.Add,
        "Opciones" to Icons.Default.MoreVert
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.051f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))
                )),
    ) {
    }
    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = screenHeight * 0.02f),
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f),
            contentPadding = PaddingValues(horizontal = screenWidth * 0.04f)
        ) { 
            items(actions) { (label, icon) ->
                CircularActionButton(
                    icon = icon,
                    label = label,
                    onClick = { /* TODO: Implement action */ },
                    screenHeight = screenHeight
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = screenHeight * 0.02f)
        ) {

            itemsIndexed(anios) { index, anio ->
                AnioCard(anio, index + 1, screenHeight, screenWidth, function = {
                    var intent = Intent(context, AnioActivity::class.java)
                    intent.putExtra("anio",anio.nombre)
                    intent.putExtra("descripcion",anio.descripcion)
                    intent.putExtra("fecha",anio.fechaInicio)
                    intent.putExtra("imagen",anio.fechaFin)
                    context.startActivity(intent)
                    //Cuando se pulse aqui se entrara en la actividad de anio y se cargaran los datos
                    //del anio correspondiente

                })
            }
        }
    }
}

@Composable
fun CircularActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    screenHeight: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(screenHeight * 0.07f)
                .shadow(elevation = screenHeight * 0.01f, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)

        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(screenHeight * 0.045f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(screenHeight * 0.09f)
        )
    }
}

@Composable
fun AnioCard(anio: Anio, index: Int, screenHeight: Dp, screenWidth: Dp,function : () -> Unit) {
    //Haremos que tod el card sea clikable
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f),
        elevation = CardDefaults.cardElevation(defaultElevation = screenHeight * 0.01f),
        onClick = function,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,) // Added shadow
    ) {
        Row(
            modifier = Modifier.padding(screenHeight * 0.02f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(screenHeight * 0.06f)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = toRoman(index),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(screenWidth * 0.04f))
            Column(modifier = Modifier.weight(1f)) {
                anio.nombre?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) }
                anio.descripcion?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {
        CuerpoInicio()
    }
}
