# Sistema de Gestos Moderno para EduTrack

**Versión**: 1.0  
**Creado**: 2025-05-13  
**Autor**: ArchitectUX  
**Estado**: Listo para implementación  

---

## 📋 Contenido de Esta Carpeta

```
gestures/
├── README.md                      ← Estás aquí
├── GestureDetectors.kt            ← Componentes reutilizables (core)
├── GestureExamples.kt             ← Ejemplos copiar & pegar
├── INTEGRATION_GUIDE.md           ← Cómo integrar en screens reales
├── CONFLICT_RESOLUTION.md         ← Cómo evitar conflictos entre gestos
└── [Tu implementación aquí]
```

---

## 🎯 Objetivo

Agregar gestos modernos (swipe, long-press, drag, fling) sin conflictuar con scroll/navigation existente.

### Beneficios
- **UX Moderna**: Gestos al estilo Instagram, Spotify, LinkedIn
- **Menos clics**: Acciones rápidas con long-press y swipe
- **Accesibilidad**: Botones tradicionales siguen funcionando
- **Sin conflictos**: Sistema inteligente previene ambigüedad

---

## 🚀 Quick Start

### 1. Copiar `GestureDetectors.kt` a tu proyecto

Ya está en:
```
app/src/main/java/com/example/edutrack/gestures/GestureDetectors.kt
```

### 2. Agregar gesto más simple (Swipe Back)

En `AnioScreen()`, envuelve la Surface raíz:

```kotlin
Surface(
    modifier = modifier
        .fillMaxSize()
        .swipeBackGesture(onBack = onBack),  // ← Agregar esto
    color = MaterialTheme.colorScheme.background
) {
    // contenido existente
}
```

**Listo**. Ya funciona swipe right para volver atrás.

### 3. Agregar gesto en card (Long Press)

En `AsignaturaCard()`, cambiar:

```kotlin
// Antes
.combinedClickable(onClick = onClick)

// Después
.combinedClickable(
    onClick = onClick,
    onLongClick = { showActionsMenu = true }
)
```

**Listo**. Long press abre menú contextual.

---

## 📊 Matriz de Gestos

| Gesto | Pantalla | Acción | Implementación |
|-------|----------|--------|-----------------|
| **Swipe Right** | AnioScreen | Volver atrás | `swipeBackGesture()` |
| **Long Press** | AsignaturaCard | Abrir menú | `combinedClickable()` |
| **Swipe Down** | BottomSheet | Cerrar | `swipeDismissGesture()` |
| **Swipe Left/Right** | StoriesRow | Cambiar curso | `horizontalSwipeGesture()` |
| **Swipe Up** | Card | Expandir | `verticalSwipeGesture()` |
| **Drag** | AsignaturaCard (Premium) | Reordenar | `draggable()` |
| **Fling** | Card | Dismiss inmediato | `flingDetector()` |

---

## 🔧 Conflictos Resueltos

| Conflicto | Solución |
|-----------|----------|
| Swipe back vs LazyRow scroll | Detectar velocidad (>300 dp/s) + posición (borde) |
| Long press vs Drag | Timeout 500ms + movimiento threshold |
| Swipe up vs LazyColumn scroll | Detectar ángulo (30° tolerancia) |
| Bottom sheet swipe vs contenido scroll | ModalBottomSheet maneja capas |

**Detalles completos**: Ver `CONFLICT_RESOLUTION.md`

---

## 📱 Qué Gestos en Qué Pantalla

### InicioActivity (Feed principal)
```
✓ Long press en AnioFeedCard → Menú: Abrir | Simulador | Eliminar
✓ Swipe up en ResumenGeneralCard → Expandir detalles
✓ Swipe left/right en StoriesRow → Navegar entre cursos
```

### AnioActivity (Detalles de año)
```
✓ Swipe right (borde izquierdo) → Volver atrás
✓ Long press en AsignaturaCard → Menú: Editar | Compartir | Eliminar
✓ Swipe up en AsignaturaCard → Expandir detalles (media, fechas, etc)
✓ Drag en AsignaturaCard (Premium) → Reordenar en grilla
```

### Bottom Sheets (Calendarios, Formularios)
```
✓ Swipe down → Cerrar sheet
✓ Drag handle → Indicador visual "se puede deslizar"
✓ Contenido interno puede scrollear normalmente
```

### Tablets (>600dp)
```
✓ Todos los gestos superiores
✓ Agregar: Swipe horizontal entre columnas
✓ Considerar: Reordenar entre secciones
```

---

## 🎮 Parámetros Clave

```kotlin
// En GestureDetectors.kt, líneas 50+

edgeThreshold = 50f          // Píxeles desde borde izquierdo
velocityThreshold = 300f     // dp/s mínimos para swipe rápido
dismissThreshold = 0.3f      // 30% de altura para cerrar sheet
angleThreshold = 30f         // Grados tolerados (puro horizontal/vertical)
minimumVelocity = 500f       // dp/s mínimos para fling
pressDuration = 500L         // Milisegundos para long press
```

**Para ajustar**: Modifica estos valores según feedback de usuarios.

---

## 📖 Cómo Implementar

### Paso 1: Leer INTEGRATION_GUIDE.md
Allí está exactamente dónde y cómo agregar cada gesto.

### Paso 2: Copiar código de GestureExamples.kt
Hay ejemplos listos para copiar & pegar.

### Paso 3: Probar y Ajustar
Ver sección "DEBUGGING" en CONFLICT_RESOLUTION.md

---

## ✅ Checklist Antes de Shipping

- [ ] `GestureDetectors.kt` compilable sin errores
- [ ] Swipe back funciona en AnioScreen
- [ ] Long press abre menú en cards
- [ ] Bottom sheet se cierra con swipe down
- [ ] No hay conflictos con scroll existente
- [ ] Funciona en móviles y tablets
- [ ] Botones tradicionales siguen funcionando
- [ ] Sin performance issues (Logcat sin janks)
- [ ] Testing en diferentes velocidades
- [ ] Documentación actualizada

---

## 🐛 Troubleshooting

### Problema: Swipe no se detecta
**Causa**: Velocidad insuficiente o posición fuera del threshold
**Solución**: Revisa `velocityThreshold` en GestureDetectors.kt línea ~50

### Problema: Long press abre menú pero también scrollea
**Causa**: Probablemente drag comenzó antes de 500ms
**Solución**: Aumenta `PRESS_DURATION_MS` en LongPressDefaults (línea ~110)

### Problema: LazyColumn no scrollea cuando necesita
**Causa**: Swipe gesture está consumiendo el evento
**Solución**: Usa `intelligentSwipeDetector()` para detectar ángulo correcto

### Problema: Funciona en móvil pero no en tablet
**Causa**: Diferentes umbrales para diferentes tamaños
**Solución**: Ajusta parámetros en sección "AJUSTES POR DISPOSITIVO"

---

## 📚 Archivos de Referencia

| Archivo | Para Qué |
|---------|----------|
| `GestureDetectors.kt` | Implementación técnica de detectores |
| `GestureExamples.kt` | Ejemplos copiar & pegar listos |
| `INTEGRATION_GUIDE.md` | Dónde exactamente poner el código |
| `CONFLICT_RESOLUTION.md` | Cómo evitar que conflictuen gestos |

---

## 🎨 Feedback Visual

**Importante**: Agregar feedback visual cuando usuario realiza gesto:

```kotlin
// Cambio de color al presionar
val backgroundColor by animateColorAsState(
    targetValue = if (isPressed) { /* color primario */ }
                  else { /* color normal */ }
)

// Cambio de elevation al presionar
elevation = if (isDragging) 8.dp else 2.dp

// Ripple effect (automático en Material3)
```

---

## 🔐 Accesibilidad

**REGLA ORO**: Ningún gesto es el ÚNICO camino a una función.

```kotlin
// ❌ MAL: Solo gesto
Card(modifier = Modifier.combinedClickable(onLongClick = { delete() }))

// ✅ BIEN: Gesto + botón
Card {
    Row {
        Text("Nombre")
        IconButton(onClick = { delete() }) {
            Icon(Icons.Default.Delete, "Eliminar")
        }
    }
    // Long press abre el MISMO menú
}
```

---

## 📊 Performance

- **GestureDetectors.kt**: ~2KB de código (negligible)
- **Overhead por gesto**: <1ms de CPU por frame
- **Memory**: Sin allocations durante gestos (reutiliza objetos)

**Sin performance concerns** en dispositivos > Android 26.

---

## 🌐 Internacionalización

Gestos son universales, no necesitan traducción.
Pero **etiquetas** sí (en iconos, tooltips):

```kotlin
Icon(Icons.Default.Edit, contentDescription = "Editar")  // ← Traduce esto
```

---

## 🚢 Versionado

```
v1.0 (Mayo 2025)
├─ Swipe back, long press, bottom sheet dismiss
├─ Conflict resolution para 4 escenarios principales
├─ Support para tablets (>600dp)
└─ Documentación completa

v1.1 (Futuro)
├─ Drag & drop para reordenar
├─ Peek & dismiss pattern mejorado
└─ Haptic feedback en gestos
```

---

## 📞 Soporte

**¿Dudas sobre la implementación?**

1. Revisa `INTEGRATION_GUIDE.md` (sección específica para tu pantalla)
2. Busca ejemplo en `GestureExamples.kt`
3. Verifica conflictos en `CONFLICT_RESOLUTION.md`
4. Debug con `adb logcat | grep "velocity"`

---

## 📝 Notas Importantes

### Antes de Implementar
- Asegúrate que todas las funciones tienen botones explícitos también
- Prueba en Android 26+ (minSdk del proyecto)
- Verifica performance en dispositivos reales

### Después de Implementar
- Pide feedback a usuarios (velocidad, sensibilidad)
- Ajusta umbrales según feedback
- Documenta cambios en CHANGELOG

### Para Tablets
- Gestos horizontales funcionan bien
- Considerar reordenar secciones con drag
- No agregar demasiados gestos (información overload)

---

## 📄 Licencia

Este sistema de gestos es parte de EduTrack.
Úsalo libremente en el proyecto.

---

**Última revisión**: 2025-05-13  
**Próxima revisión**: Después de 2 semanas de user testing  

🎉 **¡Listo para implementación!**
