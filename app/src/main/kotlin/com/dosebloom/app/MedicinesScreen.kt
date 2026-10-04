package com.dosebloom.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MedicinesScreen(
    viewModel: DoseBloomViewModel,
    medicines: List<Medicine>,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onWidgetRefresh: () -> Unit,
    activity: RefactoredMainActivity,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    onEdit: (Medicine) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterType by rememberSaveable { mutableIntStateOf(0) }

    val filteredMedicines = remember(medicines, searchQuery, filterType) {
        medicines.filter { med ->
            val matchesQuery = searchQuery.isBlank() ||
                med.name.contains(searchQuery, ignoreCase = true) ||
                med.note.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterType) {
                1 -> !med.asNeeded
                2 -> med.asNeeded
                3 -> med.stock <= med.lowStock
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.medicines), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_medicines_hint)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.clear_search),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == 0,
                        onClick = { filterType = 0 },
                        label = { Text(stringResource(R.string.filter_all)) },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = filterType == 1,
                        onClick = { filterType = 1 },
                        label = { Text(stringResource(R.string.filter_scheduled)) },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = filterType == 2,
                        onClick = { filterType = 2 },
                        label = { Text(stringResource(R.string.filter_as_needed)) },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = filterType == 3,
                        onClick = { filterType = 3 },
                        label = { Text(stringResource(R.string.filter_low_stock)) },
                        shape = RoundedCornerShape(50)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onExport, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.export))
                    }
                    OutlinedButton(onClick = onImport, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.import_data))
                    }
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = onAdd, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.add_medicine), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (filteredMedicines.isEmpty()) {
            item {
                InfoCard(if (searchQuery.isNotBlank() || filterType != 0) stringResource(R.string.no_records_for_day) else stringResource(R.string.medicines_empty))
            }
        }

        items(filteredMedicines, key = { it.id }) { medicine ->
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
                        Text(medicine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${medicine.dose} ${medicine.unit}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!medicine.asNeeded && medicine.times.isNotEmpty()) {
                        Text(medicine.times.joinToString(", "), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.remaining_stock, medicine.stock, medicine.lowStock),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (medicine.stock <= medicine.lowStock) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.low_stock),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.restock) + ":",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf(10, 30, 50).forEach { amount ->
                            Surface(
                                onClick = {
                                    viewModel.restock(medicine.id, amount)
                                    onWidgetRefresh()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    stringResource(R.string.restock_amount, amount),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (medicine.asNeeded) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.takeAsNeeded(medicine.id)
                                onWidgetRefresh()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        ) {
                            Text(stringResource(R.string.take_as_needed))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onEdit(medicine) }) {
                            Text(stringResource(R.string.edit))
                        }
                        TextButton(onClick = {
                            viewModel.deleteMedicine(medicine.id)
                            Scheduler.rescheduleAll(activity)
                            onWidgetRefresh()
                        }) {
                            Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
