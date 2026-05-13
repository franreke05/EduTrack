package com.example.edutrack.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.edutrack.dataclass.Anio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CursoPdfExporter {

    suspend fun exportAndShare(context: Context, anio: Anio) = withContext(Dispatchers.IO) {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#1B5E20")
        }
        val headerPaint = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#2E7D32")
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.DKGRAY
        }
        val smallPaint = Paint().apply {
            textSize = 9f
            color = android.graphics.Color.GRAY
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#C8E6C9")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var y = 60f
        val left = 50f
        val right = 545f

        canvas.drawText(anio.nombre ?: "Curso", left, y, titlePaint)
        y += 10f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 20f

        if (!anio.fechaInicio.isNullOrBlank() && !anio.fechaFin.isNullOrBlank()) {
            canvas.drawText("${anio.fechaInicio} – ${anio.fechaFin}", left, y, smallPaint)
            y += 20f
        }

        val asignaturas = anio.lista_asignaturas?.values?.toList().orEmpty()
        val totalCreditos = asignaturas.sumOf { it.creditos ?: 0 }
        val mediaGlobal = asignaturas
            .filter { (it.numero_notas ?: 0) > 0 }
            .mapNotNull { it.media }
            .takeIf { it.isNotEmpty() }
            ?.average()

        canvas.drawText(
            "Asignaturas: ${asignaturas.size}   Créditos totales: $totalCreditos   Media global: ${if (mediaGlobal != null) String.format("%.2f", mediaGlobal) else "–"}",
            left, y, bodyPaint
        )
        y += 24f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 18f

        canvas.drawText("Asignatura", left, y, headerPaint)
        canvas.drawText("Media", 390f, y, headerPaint)
        canvas.drawText("Créditos", 470f, y, headerPaint)
        canvas.drawText("Notas", 530f, y, headerPaint)
        y += 8f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 16f

        val rowPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.DKGRAY
        }
        val dimPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.LTGRAY
        }

        asignaturas.forEach { asig ->
            if (y > 800f) return@forEach
            val mediaStr = if (asig.media != null && (asig.numero_notas ?: 0) > 0)
                String.format("%.1f", asig.media) else "–"
            canvas.drawText(asig.nombre?.take(40) ?: "–", left, y, rowPaint)
            canvas.drawText(mediaStr, 390f, y, if (asig.media != null) rowPaint else dimPaint)
            canvas.drawText("${asig.creditos ?: 0}", 480f, y, rowPaint)
            canvas.drawText("${asig.numero_notas ?: 0}", 535f, y, dimPaint)
            y += 18f
        }

        y += 12f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 14f
        canvas.drawText("Generado con EduTrack", left, y, smallPaint)

        doc.finishPage(page)

        val safeName = (anio.nombre ?: "Curso").replace(Regex("[^A-Za-z0-9_\\-]"), "_")
        val file = File(context.cacheDir, "EduTrack_$safeName.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        withContext(Dispatchers.Main) {
            context.startActivity(Intent.createChooser(shareIntent, "Exportar PDF"))
        }
    }
}
