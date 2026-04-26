package com.example.edutrack.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.edutrack.Inicio.calcularMediaAnio
import com.example.edutrack.Inicio.formatMedia
import com.example.edutrack.dataclass.Anio
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcademicPdfExporter(private val context: Context) {
    fun exportSummary(courses: List<Anio>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply { textSize = 12f }
        var y = 42f
        canvas.drawText("Resumen académico - Edutrack", 40f, y, titlePaint)
        y += 24f
        canvas.drawText("Fecha de exportación: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}", 40f, y, bodyPaint)
        y += 28f

        courses.forEach { course ->
            if (y > 790f) return@forEach
            canvas.drawText(course.nombre ?: "Curso", 40f, y, titlePaint)
            y += 18f
            canvas.drawText("Media: ${calcularMediaAnio(course)?.let { formatMedia(it) } ?: "--"}", 40f, y, bodyPaint)
            y += 18f
            course.lista_asignaturas?.values.orEmpty().forEach { subject ->
                if (y > 810f) return@forEach
                canvas.drawText("- ${subject.nombre ?: "Asignatura"} · Créditos: ${subject.creditos ?: 0} · Media: ${subject.media ?: "--"}", 54f, y, bodyPaint)
                y += 16f
            }
            y += 12f
        }

        document.finishPage(page)
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(dir, "edutrack_resumen_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { output -> document.writeTo(output) }
        document.close()
        return file
    }
}
