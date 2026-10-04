package com.dosebloom.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun MedicineEditor(
    existing: Medicine?,
    profile: String,
    onDismiss: () -> Unit,
    onSave: (Medicine) -> Unit
) {
    val defaultUnit = stringResource(R.string.default_unit)
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var dose by rememberSaveable(existing?.id) { mutableStateOf(existing?.dose ?: "1") }
    var unit by rememberSaveable(existing?.id) { mutableStateOf(existing?.unit ?: defaultUnit) }
    var times by rememberSaveable(existing?.id) { mutableStateOf(existing?.times?.joinToString(", ") ?: "08:00") }
    var start by rememberSaveable(existing?.id) { mutableStateOf(existing?.startDate ?: Schedule.todayKey()) }
    var end by rememberSaveable(existing?.id) { mutableStateOf(existing?.endDate ?: "") }
    var stock by rememberSaveable(existing?.id) { mutableStateOf((existing?.stock ?: 30).toString()) }
    var low by rememberSaveable(existing?.id) { mutableStateOf((existing?.lowStock ?: 5).toString()) }
    var note by rememberSaveable(existing?.id) { mutableStateOf(existing?.note ?: "") }
    var asNeeded by rememberSaveable(existing?.id) { mutableStateOf(existing?.asNeeded ?: false) }
    var error by rememberSaveable(existing?.id) { mutableStateOf("") }

    val errInvalidTime = stringResource(R.string.invalid_time)
    val errInvalidStart = stringResource(R.string.invalid_start_date)
    val errInvalidEnd = stringResource(R.string.invalid_end_date)
    val errEndBeforeStart = stringResource(R.string.end_before_start)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (existing == null) stringResource(R.string.new_medicine) else stringResource(R.string.edit_medicine),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        dose,
                        { dose = it },
                        label = { Text(stringResource(R.string.dose)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        unit,
                        { unit = it },
                        label = { Text(stringResource(R.string.unit)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    times,
                    { times = it },
                    enabled = !asNeeded,
                    label = { Text(stringResource(R.string.time_example)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        start,
                        { start = it },
                        label = { Text(stringResource(R.string.start_date)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        end,
                        { end = it },
                        label = { Text(stringResource(R.string.end_date)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        stock,
                        { stock = it.filter(Char::isDigit) },
                        label = { Text(stringResource(R.string.stock)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        low,
                        { low = it.filter(Char::isDigit) },
                        label = { Text(stringResource(R.string.low_stock_threshold)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    note,
                    { note = it },
                    label = { Text(stringResource(R.string.note)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.as_needed), fontWeight = FontWeight.Medium)
                    Switch(asNeeded, { asNeeded = it })
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    val parsed = times.split(",")
                        .map(String::trim)
                        .map { Schedule.normalizeTime(it) }
                        .filter(Schedule::validTime)
                        .distinct()
                        .sorted()
                    when {
                        !asNeeded && parsed.isEmpty() -> error = errInvalidTime
                        !validDate(start) -> error = errInvalidStart
                        end.isNotBlank() && !validDate(end) -> error = errInvalidEnd
                        end.isNotBlank() && end < start -> error = errEndBeforeStart
                        else -> onSave(
                            Medicine(
                                id = existing?.id ?: 0L,
                                name = name.trim(),
                                dose = dose.trim(),
                                unit = unit.trim(),
                                times = if (asNeeded) emptyList() else parsed,
                                startDate = start.trim(),
                                endDate = end.trim(),
                                note = note.trim(),
                                stock = stock.toIntOrNull() ?: 0,
                                lowStock = low.toIntOrNull() ?: 0,
                                asNeeded = asNeeded,
                                profile = profile
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.save), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
