# 📊 Predictor de Notas - Implementación Completada

## ¿Qué es?
El **Predictor de Notas** es un feature que responde la pregunta más importante del estudiante:
> **"¿Qué nota necesito en el porcentaje restante para aprobar?"**

Se muestra automáticamente en la pantalla de Notas (NotasActivity) cuando hay:
- ✅ Al menos 1 nota registrada
- ✅ Porcentaje no completado (< 100%)

---

## 🎯 Cómo Funciona

### Versión FREE
- Ve la nota necesaria para **aprobar** (nota mínima por defecto)
- **NO puede cambiar el objetivo** con el slider
- Ve un botón "Upgrade" invitando a Premium
- Lee el resultado pero sin simulación

### Versión PREMIUM
- Ve la nota necesaria para **cualquier objetivo**
- **Slider interactivo** de 1.0 a 10.0
- El resultado se recalcula en **tiempo real** mientras desliza
- Puede simular: "¿Y si quiero un 7? ¿Y un 8? ¿Y un 9?"

---

## 📍 Ubicación en la UI

```
NotasScreen (en InicioActivity → selecciona asignatura)
    ├─ TopAppBar (nombre asignatura)
    ├─ EvolucionSection (gráfico histórico)
    ├─ EncabezadoNotas (media actual + progreso)
    ├─► PredictorCard (NUEVO - aquí va el predictor)
    ├─ ListaNotasPorPeriodo (lista de notas)
    └─ ModalBottomSheet (agregar/editar nota)
```

---

## 🧮 Lógica Detrás del Cálculo

Usa la función `calculateRequiredGradeFromNotes()` de `GradeCalculator.kt`:

```
Fórmula: requiredGrade = (targetAverage × 100 - currentWeightedPoints) / remainingWeight

Ejemplo:
- Tienes 2 notas: Parcial1 (6.0 × 40%) + Parcial2 (5.0 × 30%) = 4.8 puntos
- Porcentaje usado: 70%, Restante: 30%
- Para aprobar (6.0): 
  requiredGrade = (6.0 × 100 - (6×40 + 5×30)) / 30
  requiredGrade = (600 - 390) / 30 = 7.0

Resultado: "Necesitas 7.0 en el 30% restante"
```

---

## 🎨 Estados Visuales

El predictor muestra 4 casos diferentes:

```
1. NECESARIAS (verde/naranja/rojo según dificultad)
   ├─ Verde (tertiary):   g ≤ notaMinima → "Fácil"
   ├─ Naranja (primary):  notaMinima < g ≤ 8.0 → "Moderado"
   └─ Rojo (error):       g > 8.0 → "Difícil"

2. SUFICIENTE (✓ verde)
   "¡Ya tienes suficiente para aprobar!"

3. IMPOSIBLE (✗ rojo)
   "Imposible alcanzar ese objetivo"

4. COMPLETADO (azul)
   "Período completado"
```

---

## 💾 Archivo Modificado

**`NotasActivity.kt`** - Cambios:
1. ➕ 3 imports nuevos (GradeCalculator, PlanManager, Slider, Lock icon)
2. ➕ 5 líneas en NotasScreen (inserción condicional del PredictorCard)
3. ➕ 100 líneas nuevo composable `PredictorCard()`

**Archivos NO modificados** (reutilizados):
- `GradeCalculator.kt` - funciones existentes
- `domain/PlanManager.kt` - verificación FREE/PREMIUM
- Todas las demás pantallas intactas

---

## 🚀 Cómo Usar (como usuario)

### Si eres FREE:
1. Abre una asignatura con notas
2. Ve el Predictor: "Necesitas 6.36 para un 6"
3. Presiona "Upgrade" si quieres simular otros objetivos

### Si eres PREMIUM:
1. Abre una asignatura con notas
2. Ve el Predictor con un slider
3. **Arrastra el slider**: 5 → 6 → 7 → 8 → 9
4. **En tiempo real**: "Para un 7 necesitas 7.45..."
5. Planifica tu estudio según el resultado

---

## ✅ Verificación

### Checklist de Prueba:

- [ ] Abrir asignatura SIN notas: Predictor NO se ve
- [ ] Abrir asignatura con 1 nota, porcentaje < 100: Predictor SI se ve
- [ ] Abrir asignatura con porcentaje = 100%: Predictor NO se ve
- [ ] FREE: Ver "Necesitas X..." + botón Upgrade
- [ ] FREE: Deslizar slider NO funciona (deshabilitado)
- [ ] PREMIUM: Ver "Necesitas X..." + slider visible
- [ ] PREMIUM: Mover slider → resultado cambia en tiempo real
- [ ] Resultado "Necesitas 5.5" → verde (fácil)
- [ ] Resultado "Necesitas 8.5" → rojo (difícil)
- [ ] Ya aprobado → "¡Ya tienes suficiente!" verde
- [ ] Imposible → "Imposible..." rojo

---

## 📈 Por Qué es Killer para Conversión

1. **Responde la pregunta #1 del estudiante** → Relevancia máxima
2. **FREE ve la respuesta pero no puede explorar** → Gancho Natural
3. **PREMIUM simula escenarios** → Valor concreto y evidente
4. **Usuario vuelve cada examen** → Retención automática
5. **Diferenciador claro** → Pocas apps educativas lo hacen bien

---

## 🔧 Código Clave

### Cálculo:
```kotlin
val result = remember(notas, targetGrade) {
    calculateRequiredGradeFromNotes(notas, targetGrade)
}
```

### Mostrar/Ocultar:
```kotlin
if (notasState.value.isNotEmpty() && porcentajeTotal < 100.0) {
    PredictorCard(...)
}
```

### Distinguir FREE/PREMIUM:
```kotlin
if (PlanManager.isPremium(userPlan)) {
    Slider(...)  // PREMIUM: activo
} else {
    Surface(...) { "🔒 Unlock" }  // FREE: bloqueado
}
```

---

## 🎓 Próximos Pasos Opcionales

1. **Análisis de tendencias**: "Mejoraste 0.5 puntos/semana"
2. **Predicción de final**: "Si mantienes ritmo, llegarás a 7.5"
3. **Plan de estudio**: "Estudia estos temas para alcanzar 8"
4. **Notificaciones**: "Te falta 1 semana para el examen de Matemáticas"

---

**Estado**: ✅ **IMPLEMENTADO Y LISTO**  
**Build**: ✅ **BUILD SUCCESSFUL**  
**Testing**: 🔄 **Listo para QA en dispositivo**
