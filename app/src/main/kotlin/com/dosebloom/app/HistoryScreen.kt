package com.dosebloom.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Calendar

@Composable
fun HistoryScreen(
    viewModel: DoseBloomViewModel,
    medicines: List<Medicine>,
    onWidgetRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var month by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by rememberSaveable { mutableStateOf(Schedule.todayKey()) }
    val year = month.get(Calendar.YEAR)
    val monthIndex = month.get(Calendar.MONTH)
    val from = remember(year, monthIndex) {
        Calendar.getInstance().apply {
            set(year, monthIndex, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val to = remember(year, monthIndex) {
        Calendar.getInstance().apply {
            set(year, monthIndex, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
    val records by remember(from.timeInMillis, to.timeInMillis) {
        viewModel.observeIntakes(Schedule.dateKey(from), Schedule.dateKey(to))
    }.collectAsStateWithLifecycle(emptyList())
    val recordDates = remember(records) { records.map { it.date }.toSet() }
    val locale = LocalConfiguration.current.locales[0]

    val takenMonthCount = remember(records) { records.count { it.status == "TAKEN" } }
    val totalMonthRecords = remember(records) { records.size }
    val adherencePercent = if (totalMonthRecords > 0) (takenMonthCount * 100 / totalMonthRecords) else 100

    Column(modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.history), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                month = (month.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                selectedDate = Schedule.dateKey(month)
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = stringResource(R.string.previous_month),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = {
                month = (month.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                selectedDate = Schedule.dateKey(month)
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = stringResource(R.string.next_month),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        val monthName = remember(year, monthIndex, locale) {
            SimpleDateFormat("LLLL yyyy", locale).format(month.time).replaceFirstChar { it.uppercase(locale) }
        }
        Text(monthName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.history_month_summary, takenMonthCount, adherencePercent),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        MonthCalendar(month, selectedDate, recordDates) { selectedDate = it }
        Spacer(Modifier.height(16.dp))
        val selectedRecords = records.filter { it.date == selectedDate }
        Text(selectedDate.replace('-', '.'), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        if (selectedRecords.isEmpty()) {
            InfoCard(stringResource(R.string.no_records_for_day))
        } else {
            selectedRecords.forEach { record ->
                val name = medicines.firstOrNull { it.id == record.medicineId }?.name ?: stringResource(R.string.medicine_fallback)
                Card(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.plannedTime, fontWeight = FontWeight.Bold)
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
        Spacer(Modifier.height(24.dp))
    }
}
