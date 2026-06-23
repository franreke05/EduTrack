package com.example.edutrack.Groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edutrack.R
import com.example.edutrack.dataclass.GroupGrade

@Composable
fun GroupGradesStudentTab(
    grades: List<GroupGrade>,
    modifier: Modifier = Modifier
) {
    if (grades.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.group_grades_empty_student),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Agrupadas por asignatura para mejor lectura
    val grouped = grades.groupBy { it.subjectName?.ifBlank { null } ?: "General" }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped.forEach { (subject, subjectGrades) ->
            item {
                Text(
                    subject,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(subjectGrades) { grade ->
                StudentGradeCard(grade)
            }
        }
    }
}

@Composable
private fun StudentGradeCard(grade: GroupGrade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        ListItem(
            headlineContent = {
                Text(grade.nombreNota ?: "Nota", fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                val sub = buildString {
                    grade.fecha?.let { append(it).append(" · ") }
                    append("Peso ${grade.peso?.toInt() ?: 0}%")
                }
                Text(sub, style = MaterialTheme.typography.bodySmall)
            },
            trailingContent = {
                val valor = grade.valor ?: 0.0
                val color = when {
                    valor >= 9.0 -> MaterialTheme.colorScheme.primary
                    valor >= 5.0 -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.error
                }
                Text(
                    "%.1f".format(valor),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        )
    }
}
