package com.dosebloom.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dosebloom.app.data.IntakeStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val RefLight = androidx.compose.material3.lightColorScheme(primary = Color(0xFF6B4DB3), onPrimary = Color.White, primaryContainer = Color(0xFFE9DDFF), onPrimaryContainer = Color(0xFF27114E), secondaryContainer = Color(0xFFEDE7F5), background = Color(0xFFF8F7F4), surface = Color(0xFFF8F7F4), surfaceVariant = Color(0xFFE8E1EA))
private val RefDark = androidx.compose.material3.darkColorScheme(primary = Color(0xFFD0BCFF), onPrimary = Color(0xFF39215F), primaryContainer = Color(0xFF513B78), onPrimaryContainer = Color(0xFFEADDFF), secondaryContainer = Color(0xFF49454F), background = Color(0xFF141218), surface = Color(0xFF141218), surfaceVariant = Color(0xFF49454F))

@Composable
fun DoseBloomScreen(
    activity: RefactoredMainActivity,
    viewModel: DoseBloomViewModel,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onWidgetRefresh: () -> Unit
) {
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    MaterialTheme(colorScheme = if (darkMode) RefDark else RefLight) {
        DoseBloomContent(activity, viewModel, darkMode, onExport, onImport, onWidgetRefresh)
    }
}

@Composable
private fun DoseBloomContent(
    activity: RefactoredMainActivity,
    viewModel: DoseBloomViewModel,
    darkMode: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onWidgetRefresh: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var profileDialog by remember { mutableStateOf(false) }
    var settingsDialog by remember { mutableStateOf(false) }
    var editorVisible by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<Medicine?>(null) }
    val profile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("DoseBloom", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { profileDialog = true }) { Text(Localization.profileDisplayName(activity, profile)) }
                    TextButton(onClick = { settingsDialog = true }) { Text("⚙") }
                }
                Text(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()).replaceFirstChar { it.uppercase() })
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, { Text("●") }, label = { Text(activity.getString(R.string.today)) })
                NavigationBarItem(tab == 1, { tab = 1 }, { Text("▦") }, label = { Text(activity.getString(R.string.history)) })
                NavigationBarItem(tab == 2, { tab = 2 }, { Text("+") }, label = { Text(activity.getString(R.string.medicines)) })
            }
        },
        floatingActionButton = { if (tab == 2) FloatingActionButton(onClick = { editorVisible = true }) { Text("+", fontSize = 26.sp) } }
    ) { padding ->
        AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { selected ->
            when (selected) {
                0 -> TodayScreen(viewModel, medicines, onWidgetRefresh, Modifier.fillMaxSize().padding(padding))
                1 -> HistoryScreen(viewModel, medicines, Modifier.fillMaxSize().padding(padding))
                else -> MedicinesScreen(viewModel, medicines, onExport, onImport, onWidgetRefresh, Modifier.fillMaxSize().padding(padding))
            }
        }
    }

    if (editorVisible || editingMedicine != null) {
        MedicineEditor(activity, editingMedicine, profile, onDismiss = { editorVisible = false; editingMedicine = null }) { medicine ->
            viewModel.saveMedicine(medicine)
            Scheduler.rescheduleAll(activity)
            onWidgetRefresh()
            editorVisible = false
            editingMedicine = null
        }
    }
    if (profileDialog) {
        ProfileDialog(activity, profiles, profile, onSelect = { viewModel.selectProfile(it); profileDialog = false }, onAdd = { viewModel.addProfile(it); viewModel.selectProfile(it.trim()); profileDialog = false }, onDelete = { viewModel.removeProfile(it) })
    }
    if (settingsDialog) {
        SettingsDialog(activity, darkMode, language, onDarkModeChanged = viewModel::setDarkMode, onLanguageChanged = { Localization.setLanguage(activity, it); viewModel.setLanguage(it); activity.recreate() }, onDismiss = { settingsDialog = false })
    }
}

@Composable
private fun TodayScreen(viewModel: DoseBloomViewModel, medicines: List<Medicine>, onWidgetRefresh: () -> Unit, modifier: Modifier) {
    val date = Schedule.todayKey()
    val intakeFlow = remember(date) { viewModel.observeIntakes(date) }
    val records by intakeFlow.collectAsStateWithLifecycle()
    val events = remember(medicines, date) { Schedule.events(medicines, date) }
    LazyColumn(modifier.widthIn(max = 760.dp).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(stringResourceCompat(viewModel, R.string.today), style = MaterialTheme.typography.headlineMedium) }
        if (events.isEmpty()) item { InfoCard("Нет плановых приёмов на сегодня.") }
        items(events, key = { "${it.first.id}-${it.second}" }) { (medicine, time) ->
            val record = records.firstOrNull { it.medicineId == medicine.id && it.plannedTime == time }
            DoseCard(medicine, time, record?.status, record?.actualMillis) {
                viewModel.takeDose(medicine.id, date, time)
                onWidgetRefresh()
            }
        }
    }
}

@Composable
private fun DoseCard(medicine: Medicine, time: String, status: String?, actual: Long?, onTake: () -> Unit) {
    Card(Modifier.fillMaxWidth().animateContentSizeCompat(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                StatusPill(status)
            }
            Spacer(Modifier.height(5.dp))
            Text(medicine.name, style = MaterialTheme.typography.titleLarge)
            Text("${medicine.dose} ${medicine.unit}")
            if (medicine.note.isNotBlank()) Text(medicine.note, style = MaterialTheme.typography.bodySmall)
            AnimatedVisibility(status == null) {
                Button(onClick = onTake, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Принять") }
            }
            if (actual != null) Text("Фактически: ${SimpleDateFormat("HH:mm", Locale.US).format(Date(actual))}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun StatusPill(status: String?) {
    val text = when (status) { "TAKEN" -> "Принято"; "SKIPPED" -> "Пропущено"; else -> "План" }
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) { Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) }
}

@Composable
private fun HistoryScreen(viewModel: DoseBloomViewModel, medicines: List<Medicine>, modifier: Modifier) {
    var month by remember { mutableStateOf(Calendar.getInstance()) }
    val from = remember(month.get(Calendar.YEAR), month.get(Calendar.MONTH)) { Calendar.getInstance().apply { set(month.get(Calendar.YEAR), month.get(Calendar.MONTH), 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) } }
    val to = remember(month.get(Calendar.YEAR), month.get(Calendar.MONTH)) { Calendar.getInstance().apply { set(month.get(Calendar.YEAR), month.get(Calendar.MONTH), getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59); set(Calendar.MILLISECOND, 999) } }
    val intakeFlow = remember(from.timeInMillis, to.timeInMillis) { viewModel.observeIntakes(Schedule.dateKey(from), Schedule.dateKey(to)) }
    val records by intakeFlow.collectAsStateWithLifecycle()
    val visible = remember(records, medicines) { records.filter { record -> medicines.any { it.id == record.medicineId } } }
    Column(modifier.widthIn(max = 760.dp).padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
            Text(activityString(R.string.history), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) { Text("‹") }
            TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) { Text("›") }
        }
        Text(SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(month.time).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        if (visible.isEmpty()) InfoCard("За этот месяц записей нет.")
        else LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visible, key = { it.id }) { record ->
                val name = medicines.firstOrNull { it.id == record.medicineId }?.name ?: "Лекарство"
                Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("${record.date} • ${record.plannedTime}", fontWeight = FontWeight.SemiBold); Text(name) }; StatusPill(record.status) } }
            }
        }
    }
}

@Composable
private fun MedicinesScreen(viewModel: DoseBloomViewModel, medicines: List<Medicine>, onExport: () -> Unit, onImport: () -> Unit, onWidgetRefresh: () -> Unit, modifier: Modifier) {
    var editing by remember { mutableStateOf<Medicine?>(null) }
    var deleting by remember { mutableStateOf<Medicine?>(null) }
    LazyColumn(modifier.widthIn(max = 900.dp).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(activityString(R.string.medicines), style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.weight(1f)); TextButton(onClick = onExport) { Text("Экспорт") }; TextButton(onClick = onImport) { Text("Импорт") } } }
        if (medicines.isEmpty()) item { InfoCard("Лекарств пока нет.") }
        items(medicines, key = { it.id }) { medicine ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(medicine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); if (medicine.asNeeded) Text("по необходимости") }; Text("${medicine.dose} ${medicine.unit}"); if (!medicine.asNeeded) Text(medicine.times.joinToString(", ")); Text("Запас: ${medicine.stock} · минимум: ${medicine.lowStock}"); AnimatedVisibility(medicine.stock <= medicine.lowStock) { Text("Запас заканчивается", color = MaterialTheme.colorScheme.error) }; Row { TextButton(onClick = { editing = medicine }) { Text("Изменить") }; TextButton(onClick = { deleting = medicine }) { Text("Удалить") } } } }
        }
    }
    editing?.let { medicine -> MedicineEditor(activity = null, existing = medicine, profile = medicine.profile, onDismiss = { editing = null }) { updated -> viewModel.saveMedicine(updated); Scheduler.rescheduleAll(null); onWidgetRefresh(); editing = null } }
    deleting?.let { medicine -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Удалить ${medicine.name}?") }, text = { Text("Приёмная история этого лекарства также будет удалена.") }, confirmButton = { Button(onClick = { viewModel.deleteMedicine(medicine.id); Scheduler.cancelMedicine(null, medicine); onWidgetRefresh(); deleting = null }) { Text("Удалить") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Отмена") } }) }
}

@Composable
private fun MedicineEditor(activity: RefactoredMainActivity?, existing: Medicine?, profile: String, onDismiss: () -> Unit, onSave: (Medicine) -> Unit) {
    val defaultUnit = "таблетка"
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var dose by remember(existing) { mutableStateOf(existing?.dose ?: "1") }
    var unit by remember(existing) { mutableStateOf(existing?.unit ?: defaultUnit) }
    var times by remember(existing) { mutableStateOf(existing?.times?.joinToString(", ") ?: "08:00") }
    var start by remember(existing) { mutableStateOf(existing?.startDate ?: Schedule.todayKey()) }
    var end by remember(existing) { mutableStateOf(existing?.endDate ?: "") }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
    var stock by remember(existing) { mutableStateOf((existing?.stock ?: 30).toString()) }
    var low by remember(existing) { mutableStateOf((existing?.lowStock ?: 5).toString()) }
    var asNeeded by remember(existing) { mutableStateOf(existing?.asNeeded ?: false) }
    var error by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (existing == null) "Новое лекарство" else "Изменить лекарство") }, text = {
        Column(Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Название") }, singleLine = true)
            OutlinedTextField(dose, { dose = it }, label = { Text("Доза") }, singleLine = true)
            OutlinedTextField(unit, { unit = it }, label = { Text("Единица") }, singleLine = true)
            OutlinedTextField(times, { times = it }, label = { Text("Время: 08:00, 20:00") }, enabled = !asNeeded, singleLine = true)
            OutlinedTextField(start, { start = it }, label = { Text("Дата начала") }, singleLine = true)
            OutlinedTextField(end, { end = it }, label = { Text("Дата окончания") }, singleLine = true)
            OutlinedTextField(stock, { stock = it.filter(Char::isDigit) }, label = { Text("Запас") }, singleLine = true)
            OutlinedTextField(low, { low = it.filter(Char::isDigit) }, label = { Text("Минимальный запас") }, singleLine = true)
            OutlinedTextField(note, { note = it }, label = { Text("Заметка") })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("По необходимости"); Switch(asNeeded, { asNeeded = it }) }
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = {
        Button(enabled = name.isNotBlank(), onClick = {
            val parsed = times.split(",").map(String::trim).filter(Schedule::validTime).distinct().sorted()
            when {
                !asNeeded && parsed.isEmpty() -> error = "Укажите хотя бы одно корректное время"
                ScheduleCalculatorFacade.invalidDate(start) -> error = "Некорректная дата начала"
                end.isNotBlank() && ScheduleCalculatorFacade.invalidDate(end) -> error = "Некорректная дата окончания"
                end.isNotBlank() && end < start -> error = "Дата окончания раньше даты начала"
                else -> onSave(Medicine(existing?.id ?: 0L, name.trim(), dose.trim(), unit.trim(), if (asNeeded) emptyList() else parsed, start.trim(), end.trim(), note.trim(), stock.toIntOrNull() ?: 0, low.toIntOrNull() ?: 0, asNeeded, profile))
            }
        }) { Text("Сохранить") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}

@Composable
private fun ProfileDialog(activity: RefactoredMainActivity, profiles: List<String>, current: String, onSelect: (String) -> Unit, onAdd: (String) -> Unit, onDelete: (String) -> Unit) {
    var newProfile by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = { onSelect(current) }, title = { Text("Профиль") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            profiles.forEach { profile -> Row(verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { onSelect(profile) }, Modifier.weight(1f)) { Text(if (profile == current) "✓ ${Localization.profileDisplayName(activity, profile)}" else Localization.profileDisplayName(activity, profile)) }; if (profile != "Я") TextButton(onClick = { onDelete(profile) }) { Text("Удалить") } } }
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            OutlinedTextField(newProfile, { newProfile = it }, label = { Text("Новый профиль") }, singleLine = true)
            TextButton(enabled = newProfile.isNotBlank(), onClick = { onAdd(newProfile.trim()); newProfile = "" }) { Text("Добавить и выбрать") }
        }
    }, confirmButton = { TextButton(onClick = { onSelect(current) }) { Text("Готово") } })
}

@Composable
private fun SettingsDialog(activity: RefactoredMainActivity, darkMode: Boolean, language: String, onDarkModeChanged: (Boolean) -> Unit, onLanguageChanged: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Настройки") }, text = {
        Column(Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Тёмная тема"); Switch(darkMode, onDarkModeChanged) }
            HorizontalDivider()
            Text("Язык", fontWeight = FontWeight.Medium)
            OutlinedButton(onClick = { onLanguageChanged(Localization.SYSTEM) }, Modifier.fillMaxWidth()) { Text("Системный") }
            OutlinedButton(onClick = { onLanguageChanged(Localization.RUSSIAN) }, Modifier.fillMaxWidth()) { Text("Русский") }
            OutlinedButton(onClick = { onLanguageChanged(Localization.ENGLISH) }, Modifier.fillMaxWidth()) { Text("English") }
            Text("Выбран: ${when (language) { Localization.RUSSIAN -> "Русский"; Localization.ENGLISH -> "English"; else -> "Системный" }}", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            OutlinedButton(onClick = { runCatching { activity.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName) }) } }, Modifier.fillMaxWidth()) { Text("Настройки уведомлений") }
            if (android.os.Build.VERSION.SDK_INT >= 31) OutlinedButton(onClick = { runCatching { activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${activity.packageName}"))) } }, Modifier.fillMaxWidth()) { Text("Точные будильники") }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } })
}

@Composable private fun InfoCard(text: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Text(text, Modifier.padding(18.dp)) } }

private fun stringResourceCompat(viewModel: DoseBloomViewModel, id: Int): String = "Сегодня"
private fun activityString(id: Int): String = when (id) { R.string.history -> "История"; R.string.medicines -> "Лекарства"; else -> "" }
private fun Modifier.animateContentSizeCompat(): Modifier = this
private object ScheduleCalculatorFacade { fun invalidDate(value: String): Boolean = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value) }.isFailure }
