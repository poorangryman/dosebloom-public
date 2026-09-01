package com.dosebloom.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val DoseLight = androidx.compose.material3.lightColorScheme(primary = Color(0xFF6B4DB3), primaryContainer = Color(0xFFE9DDFF), background = Color(0xFFF8F7F4), surface = Color(0xFFF8F7F4))
private val DoseDark = androidx.compose.material3.darkColorScheme(primary = Color(0xFFD0BCFF), primaryContainer = Color(0xFF513B78), background = Color(0xFF141218), surface = Color(0xFF141218))

@Composable
fun DoseBloomScreen(activity: RefactoredMainActivity, viewModel: DoseBloomViewModel, onExport: () -> Unit, onImport: () -> Unit, onWidgetRefresh: () -> Unit) {
    val dark by viewModel.darkMode.collectAsStateWithLifecycle()
    MaterialTheme(colorScheme = if (dark) DoseDark else DoseLight) { DoseBloomContent(activity, viewModel, onExport, onImport, onWidgetRefresh) }
}

@Composable
private fun DoseBloomContent(activity: RefactoredMainActivity, viewModel: DoseBloomViewModel, onExport: () -> Unit, onImport: () -> Unit, onWidgetRefresh: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }; var editor by remember { mutableStateOf<Medicine?>(null) }; var addMedicine by remember { mutableStateOf(false) }; var profileDialog by remember { mutableStateOf(false) }; var settingsDialog by remember { mutableStateOf(false) }
    val profile by viewModel.selectedProfile.collectAsStateWithLifecycle(); val medicines by viewModel.medicines.collectAsStateWithLifecycle(); val profiles by viewModel.profiles.collectAsStateWithLifecycle(); val language by viewModel.language.collectAsStateWithLifecycle(); val dark by viewModel.darkMode.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("DoseBloom", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Text(profile) }; TextButton(onClick = { profileDialog = true }) { Text("Профиль") }; TextButton(onClick = { settingsDialog = true }) { Text("⚙", style = MaterialTheme.typography.titleLarge) } } },
        bottomBar = { NavigationBar { NavigationBarItem(tab == 0, { tab = 0 }, icon = { Text("●") }, label = { Text("Сегодня") }); NavigationBarItem(tab == 1, { tab = 1 }, icon = { Text("▦") }, label = { Text("История") }); NavigationBarItem(tab == 2, { tab = 2 }, icon = { Text("+") }, label = { Text("Лекарства") }) } }
    ) { padding ->
        AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "screen") { when (it) {
            0 -> TodayScreen(viewModel, medicines, onWidgetRefresh, Modifier.fillMaxSize().padding(padding))
            1 -> HistoryScreen(viewModel, medicines, Modifier.fillMaxSize().padding(padding))
            else -> MedicinesScreen(viewModel, medicines, onExport, onImport, onWidgetRefresh, activity, Modifier.fillMaxSize().padding(padding), { addMedicine = true }, { editor = it })
        } }
    }
    if (addMedicine || editor != null) MedicineEditor(editor, profile, onDismiss = { addMedicine = false; editor = null }) { medicine -> viewModel.saveMedicine(medicine); Scheduler.rescheduleAll(activity); onWidgetRefresh(); addMedicine = false; editor = null }
    if (profileDialog) ProfileDialog(profiles, profile, { viewModel.selectProfile(it); profileDialog = false }, { viewModel.addProfile(it); viewModel.selectProfile(it.trim()); profileDialog = false }, viewModel::removeProfile)
    if (settingsDialog) SettingsDialog(dark, language, viewModel::setDarkMode, { viewModel.setLanguage(it); Localization.setLanguage(activity, it); activity.recreate() }, { settingsDialog = false })
}

@Composable
private fun TodayScreen(viewModel: DoseBloomViewModel, medicines: List<Medicine>, onWidgetRefresh: () -> Unit, modifier: Modifier) {
    val date = Schedule.todayKey(); val records by remember(date) { viewModel.observeIntakes(date) }.collectAsStateWithLifecycle(); val events = remember(medicines, date) { Schedule.events(medicines, date) }
    LazyColumn(modifier.widthLimited().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Text("Сегодня", style = MaterialTheme.typography.headlineMedium) }; if (events.isEmpty()) item { InfoCard("Нет плановых приёмов на сегодня.") }; items(events, key = { "${it.first.id}-${it.second}" }) { (medicine, time) -> val record = records.firstOrNull { it.medicineId == medicine.id && it.plannedTime == time }; Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); StatusPill(record?.status) }; Text(medicine.name, style = MaterialTheme.typography.titleLarge); Text("${medicine.dose} ${medicine.unit}"); if (medicine.note.isNotBlank()) Text(medicine.note, style = MaterialTheme.typography.bodySmall); if (record == null) Button(onClick = { viewModel.takeDose(medicine.id, date, time); onWidgetRefresh() }, Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Принять") } } } } }
}

@Composable private fun HistoryScreen(viewModel: DoseBloomViewModel, medicines: List<Medicine>, modifier: Modifier) {
    var month by remember { mutableStateOf(Calendar.getInstance()) }; var selectedDate by remember { mutableStateOf(Schedule.todayKey()) }; val year = month.get(Calendar.YEAR); val monthIndex = month.get(Calendar.MONTH)
    val from = remember(year, monthIndex) { Calendar.getInstance().apply { set(year, monthIndex, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) } }; val to = remember(year, monthIndex) { Calendar.getInstance().apply { set(year, monthIndex, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59); set(Calendar.MILLISECOND, 999) } }
    val records by remember(from.timeInMillis, to.timeInMillis) { viewModel.observeIntakes(Schedule.dateKey(from), Schedule.dateKey(to)) }.collectAsStateWithLifecycle(); val recordDates = remember(records) { records.map { it.date }.toSet() }
    Column(modifier.widthLimited().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text("История", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.weight(1f)); TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, -1) }; selectedDate = Schedule.dateKey(month) }) { Text("‹") }; TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, 1) }; selectedDate = Schedule.dateKey(month) }) { Text("›") } }
        Text(SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(month.time).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(10.dp)); MonthCalendar(month, selectedDate, recordDates) { selectedDate = it }; Spacer(Modifier.height(12.dp))
        val selectedRecords = records.filter { it.date == selectedDate }; Text(selectedDate.replace('-', '.'), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (selectedRecords.isEmpty()) InfoCard("За этот день записей нет.") else selectedRecords.forEach { record -> val name = medicines.firstOrNull { it.id == record.medicineId }?.name ?: "Лекарство"; Card(Modifier.fillMaxWidth().padding(top = 8.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(record.plannedTime, fontWeight = FontWeight.SemiBold); Text(name) }; StatusPill(record.status) } } }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun MonthCalendar(month: Calendar, selectedDate: String, recordDates: Set<String>, onDateSelected: (String) -> Unit) {
    val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) { weekdays.forEach { Text(it, Modifier.weight(1f).padding(vertical = 4.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) } }
    val first = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }; val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7; val days = month.getActualMaximum(Calendar.DAY_OF_MONTH); val total = ((offset + days + 6) / 7) * 7
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { for (weekStart in 0 until total step 7) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) { for (cell in weekStart until weekStart + 7) { val day = cell - offset + 1; if (day !in 1..days) Spacer(Modifier.weight(1f).aspectRatio(1f)) else { val date = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }; val key = Schedule.dateKey(date); val selected = key == selectedDate; val hasRecord = key in recordDates; Surface(onClick = { onDateSelected(key) }, modifier = Modifier.weight(1f).aspectRatio(1f), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) { Column(Modifier.fillMaxSize().padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(day.toString(), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal); Text(if (hasRecord) "•" else " ", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) } } } } } }
}

@Composable private fun MedicinesScreen(viewModel: DoseBloomViewModel, medicines: List<Medicine>, onExport: () -> Unit, onImport: () -> Unit, onWidgetRefresh: () -> Unit, activity: RefactoredMainActivity, modifier: Modifier, onAdd: () -> Unit, onEdit: (Medicine) -> Unit) {
    LazyColumn(modifier.widthLimited().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Column(Modifier.fillMaxWidth()) { Text("Лекарства", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = onExport, Modifier.weight(1f)) { Text("Экспорт") }; OutlinedButton(onClick = onImport, Modifier.weight(1f)) { Text("Импорт") } }; Spacer(Modifier.height(8.dp)); FilledTonalButton(onClick = onAdd, Modifier.fillMaxWidth()) { Text("＋  Добавить лекарство") } } }; if (medicines.isEmpty()) item { InfoCard("Лекарств пока нет.") }; items(medicines, key = { it.id }) { medicine -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(medicine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("${medicine.dose} ${medicine.unit}"); if (!medicine.asNeeded) Text(medicine.times.joinToString(", ")); Text("Запас: ${medicine.stock} · минимум: ${medicine.lowStock}"); if (medicine.stock <= medicine.lowStock) Text("Запас заканчивается", color = MaterialTheme.colorScheme.error); Row { TextButton(onClick = { onEdit(medicine) }) { Text("Изменить") }; TextButton(onClick = { viewModel.deleteMedicine(medicine.id); Scheduler.rescheduleAll(activity); onWidgetRefresh() }) { Text("Удалить") } } } } } }
}

@Composable private fun MedicineEditor(existing: Medicine?, profile: String, onDismiss: () -> Unit, onSave: (Medicine) -> Unit) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }; var dose by remember(existing) { mutableStateOf(existing?.dose ?: "1") }; var unit by remember(existing) { mutableStateOf(existing?.unit ?: "таблетка") }; var times by remember(existing) { mutableStateOf(existing?.times?.joinToString(", ") ?: "08:00") }; var start by remember(existing) { mutableStateOf(existing?.startDate ?: Schedule.todayKey()) }; var end by remember(existing) { mutableStateOf(existing?.endDate ?: "") }; var stock by remember(existing) { mutableStateOf((existing?.stock ?: 30).toString()) }; var low by remember(existing) { mutableStateOf((existing?.lowStock ?: 5).toString()) }; var note by remember(existing) { mutableStateOf(existing?.note ?: "") }; var asNeeded by remember(existing) { mutableStateOf(existing?.asNeeded ?: false) }; var error by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (existing == null) "Новое лекарство" else "Изменить лекарство") }, text = { Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) { OutlinedTextField(name, { name = it }, label = { Text("Название") }); OutlinedTextField(dose, { dose = it }, label = { Text("Доза") }); OutlinedTextField(unit, { unit = it }, label = { Text("Единица") }); OutlinedTextField(times, { times = it }, enabled = !asNeeded, label = { Text("Время: 08:00, 20:00") }); OutlinedTextField(start, { start = it }, label = { Text("Дата начала") }); OutlinedTextField(end, { end = it }, label = { Text("Дата окончания") }); OutlinedTextField(stock, { stock = it.filter(Char::isDigit) }, label = { Text("Запас") }); OutlinedTextField(low, { low = it.filter(Char::isDigit) }, label = { Text("Минимум") }); OutlinedTextField(note, { note = it }, label = { Text("Заметка") }); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("По необходимости"); Switch(asNeeded, { asNeeded = it }) }; if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error) } }, confirmButton = { Button(enabled = name.isNotBlank(), onClick = { val parsed = times.split(",").map(String::trim).filter(Schedule::validTime).distinct().sorted(); when { !asNeeded && parsed.isEmpty() -> error = "Укажите корректное время"; !validDate(start) -> error = "Некорректная дата начала"; end.isNotBlank() && !validDate(end) -> error = "Некорректная дата окончания"; end.isNotBlank() && end < start -> error = "Дата окончания раньше даты начала"; else -> onSave(Medicine(existing?.id ?: 0L, name.trim(), dose.trim(), unit.trim(), if (asNeeded) emptyList() else parsed, start.trim(), end.trim(), note.trim(), stock.toIntOrNull() ?: 0, low.toIntOrNull() ?: 0, asNeeded, profile)) } }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}

@Composable private fun ProfileDialog(profiles: List<String>, current: String, onSelect: (String) -> Unit, onAdd: (String) -> Unit, onDelete: (String) -> Unit) { var name by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = { onSelect(current) }, title = { Text("Профиль") }, text = { Column { profiles.forEach { p -> Row(verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { onSelect(p) }, Modifier.weight(1f)) { Text(if (p == current) "✓ $p" else p) }; if (p != "Я") TextButton(onClick = { onDelete(p) }) { Text("Удалить") } } }; HorizontalDivider(); OutlinedTextField(name, { name = it }, label = { Text("Новый профиль") }); TextButton(enabled = name.isNotBlank(), onClick = { onAdd(name); name = "" }) { Text("Добавить") } } }, confirmButton = { TextButton(onClick = { onSelect(current) }) { Text("Готово") } }) }
@Composable private fun SettingsDialog(dark: Boolean, language: String, onDarkMode: (Boolean) -> Unit, onLanguage: (String) -> Unit, onDismiss: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Настройки") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Тёмная тема"); Switch(dark, onDarkMode) }; HorizontalDivider(); Text("Язык: $language"); OutlinedButton(onClick = { onLanguage(Localization.SYSTEM) }, Modifier.fillMaxWidth()) { Text("Системный") }; OutlinedButton(onClick = { onLanguage(Localization.RUSSIAN) }, Modifier.fillMaxWidth()) { Text("Русский") }; OutlinedButton(onClick = { onLanguage(Localization.ENGLISH) }, Modifier.fillMaxWidth()) { Text("English") } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } }) }
@Composable private fun StatusPill(status: String?) { Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) { Text(when (status) { "TAKEN" -> "Принято"; "SKIPPED" -> "Пропущено"; else -> "План" }, Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) } }
@Composable private fun InfoCard(text: String) { Card(Modifier.fillMaxWidth()) { Text(text, Modifier.padding(18.dp)) } }
private fun Modifier.widthLimited(): Modifier = this.fillMaxWidth()
private fun validDate(value: String): Boolean = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value) }.isSuccess
