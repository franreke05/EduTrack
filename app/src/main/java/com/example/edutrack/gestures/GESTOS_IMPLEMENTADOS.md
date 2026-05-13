# 🎯 Gestos Implementados en EduTrack

## ✅ Gestos Activos

### 1. **Swipe Back (AnioActivity)**
- **Cómo funciona**: Desliza desde el borde izquierdo hacia la derecha
- **Resultado**: Vuelve a la pantalla anterior
- **Threshold**: Mínimo 300 dp/s de velocidad, comenzar en primeros 50dp del borde
- **Dónde**: En la pantalla de Asignaturas (AnioActivity)

```
[Pantalla de Asignaturas]
Desliza desde aquí ← ───────── Vuelve atrás
```

---

### 2. **Long Press en AsignaturaCard**
- **Cómo funciona**: Mantén presionada una tarjeta de asignatura ~500ms
- **Resultado**: Si tienes Grupos, abre menú para compartir la asignatura
- **Dónde**: En cada tarjeta de asignatura en la pantalla de cursos

```
┌─────────────────┐
│  Matemáticas    │  ← Long press aquí
│  Media: 8.5     │     (500ms)
└─────────────────┘
         ↓
    [Compartir en Grupo]
```

---

### 3. **Swipe Down en Bottom Sheets**
- **Cómo funciona**: Desliza hacia abajo en el drag handle del ModalBottomSheet
- **Resultado**: Cierra la hoja
- **Dónde**: En cualquier diálogo emergente (crear asignatura, etc)

```
    ___________
   |___ (drag handle)  ← Desliza hacia abajo
   |           |
   | Contenido |
   |___________|
        ↓
    Cierra automáticamente
```

---

## 🔧 Cómo Usar en tu Código

### Agregar Swipe Back a una pantalla:

```kotlin
import com.example.edutrack.gestures.swipeBackGesture

@Composable
fun MiPantalla(onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .swipeBackGesture(onBack = onBack)  // ← Agregar esto
    ) {
        // Contenido de tu pantalla
    }
}
```

### Agregar Long Press a un Card:

```kotlin
import androidx.compose.foundation.combinedClickable

Card(
    modifier = Modifier.combinedClickable(
        onClick = { /* click normal */ },
        onLongClick = { showMenu = true }  // ← Agregar esto
    )
) {
    // Contenido del card
}
```

### Swipe Down en BottomSheet (ya está automático):

```kotlin
ModalBottomSheet(
    onDismissRequest = { onDismiss() },
    sheetState = sheetState  // ← ModalBottomSheet ya maneja swipe down
) {
    // Contenido del sheet
}
```

---

## 📱 Pantallas con Gestos Activos

| Pantalla | Gesto | Estado |
|----------|-------|--------|
| AnioActivity | Swipe Back | ✅ Activo |
| AsignaturaCard | Long Press | ✅ Activo |
| ModalBottomSheet | Swipe Down | ✅ Automático |
| InicioActivity | - | ⏳ Próximo |
| PerfilActivity | - | ⏳ Próximo |

---

## 🎓 Accesibilidad

✅ **Todos los gestos tienen botones alternativos**:
- Swipe back: Botón atrás en la AppBar
- Long press compartir: Botón de menú contextual disponible
- Swipe down: Botón X en el BottomSheet

No hay funcionalidad SOLO accesible por gesto.

---

## 🚀 Próximos Gestos (Fase 2)

- [ ] Swipe L/R entre cursos (InicioActivity)
- [ ] Swipe up para expandir cards
- [ ] Drag & drop para reordenar asignaturas (Premium)
- [ ] Fling para descartar

---

## 🐛 Debugging

Si un gesto no funciona:

1. Verifica que el modifier esté en el lugar correcto (Surface, Card, etc)
2. Asegúrate de que no haya otro detector de gestos conflictivo
3. Prueba en dispositivo físico (a veces el emulador es problemático)
4. Revisa logcat: `filter: swipe|gesture|drag`

---

**Código**: `app/src/main/java/com/example/edutrack/gestures/GestureDetectors.kt`
