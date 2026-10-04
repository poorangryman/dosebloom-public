package com.dosebloom.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Calendar

@Composable
fun TodayScreen(
    viewModel: DoseBloomViewModel,
    medicines: List<Medicine>,
    onWidgetRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val date = Schedule.todayKey()
    val records by remember(date) { viewModel.observeIntakes(date) }.collectAsStateWithLifecycle(emptyList())
    val events = remember(medicines, date) { Schedule.events(medicines, date) }
    val asNeededMedicines = remember(medicines) { medicines.filter { it.asNeeded } }
    val asNeededRecords = remember(records, events) {
        val eventKeys = events.map { "${it.first.id}-${it.second}" }.toSet()
        records.filter { "${it.medicineId}-${it.plannedTime}" !in eventKeys }
    }

    val totalDoses = events.size
    val takenDoses = events.count { (med, time) ->
        records.any { it.medicineId == med.id && it.plannedTime == time && it.status == "TAKEN" }
    }
    val progressPercent = if (totalDoses > 0) (takenDoses * 100 / totalDoses) else 100

    val locale = LocalConfiguration.current.locales[0]
    val formattedToday = remember(date, locale) {
        SimpleDateFormat("EEEE, d MMMM", locale).format(Calendar.getInstance().time)
            .replaceFirstChar { it.uppercase(locale) }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                Text(
                    formattedToday,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.today),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (totalDoses > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.daily_progress_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$progressPercent%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (totalDoses > 0) takenDoses.toFloat() / totalDoses.toFloat() else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (takenDoses == totalDoses) stringResource(R.string.daily_progress_all_done)
                            else stringResource(R.string.daily_progress_summary, takenDoses, totalDoses, progressPercent),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (events.isEmpty() && asNeededMedicines.isEmpty() && asNeededRecords.isEmpty()) {
            item { InfoCard(stringResource(R.string.today_calm_body)) }
        }

        items(events, key = { "${it.first.id}-${it.second}" }) { (medicine, time) ->
            val record = records.firstOrNull { it.medicineId == medicine.id && it.plannedTime == time }
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_pill),
                                contentDescription = null,
                                modifier = Modifier.padding(6.dp).size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        StatusPill(record?.status)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(medicine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${medicine.dose} ${medicine.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (medicine.note.isNotBlank()) {
                        Text(
                            medicine.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (record == null) {
                        Button(
                            onClick = {
                                viewModel.takeDose(medicine.id, date, time)
                                onWidgetRefresh()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Text(stringResource(R.string.take), fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                viewModel.undoDose(medicine.id, date, time)
                                onWidgetRefresh()
                            }) {
                                Text(stringResource(R.string.undo))
                            }
                        }
                    }
                }
            }
        }

        if (asNeededMedicines.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.as_needed),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(asNeededMedicines, key = { "asneeded-${it.id}" }) { medicine ->
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(medicine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${medicine.dose} ${medicine.unit} · ${stringResource(R.string.remaining_stock, medicine.stock, medicine.lowStock)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                viewModel.takeAsNeeded(medicine.id)
                                onWidgetRefresh()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.take_as_needed))
                        }
                    }
                }
            }
        }

        if (asNeededRecords.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.intakes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(asNeededRecords, key = { "extra-${it.id}" }) { record ->
                val name = medicines.firstOrNull { it.id == record.medicineId }?.name ?: stringResource(R.string.medicine_fallback)
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.plannedTime, fontWeight = FontWeight.SemiBold)
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                        StatusPill(record.status)
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            viewModel.undoDose(record.medicineId, record.date, record.plannedTime)
                            onWidgetRefresh()
                        }) {
                            Text(stringResource(R.string.undo))
                        }
                    }
                }
            }
        }
    }
}
