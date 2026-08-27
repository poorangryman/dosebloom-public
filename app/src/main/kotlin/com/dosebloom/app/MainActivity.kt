package com.dosebloom.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val DoseBloomLightColors = lightColorScheme(primary = Color(0xFF6B4DB3), onPrimary = Color.White, primaryContainer = Color(0xFFE9DDFF), onPrimaryContainer = Color(0xFF27114E), secondaryContainer = Color(0xFFEDE7F5), background = Color(0xFFF8F7F4), surface = Color(0xFFF8F7F4), surfaceVariant = Color(0xFFE8E1EA))
private val DoseBloomDarkColors = darkColorScheme(primary = Color(0xFFD0BCFF), onPrimary = Color(0xFF39215F), primaryContainer = Color(0xFF513B78), onPrimaryContainer = Color(0xFFEADDFF), secondaryContainer = Color(0xFF49454F), background = Color(0xFF141218), surface = Color(0xFF141218), surfaceVariant = Color(0xFF49454F))

class MainActivity : AppCompatActivity() {
    private lateinit var db: Db
    private var pendingExport: (Uri) -> Unit = {}
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> uri?.let(pendingExport) }
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) runCatching { ExportImport.import(this, uri) }.onSuccess { Scheduler.rescheduleAll(this); refreshWidget(); render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge(); db = Db(this); createNotificationChannel(); requestNotificationPermission(); requestExactAlarmPermission(); Scheduler.rescheduleAll(this); render()
    }
    private fun render() { setContent { DoseBloomRoot() } }
    private fun isDarkMode(): Boolean = getPreferences(MODE_PRIVATE).getBoolean("dark_mode", false)
    private fun setDarkMode(enabled: Boolean) { getPreferences(MODE_PRIVATE).edit().putBoolean("dark_mode", enabled).apply() }
    private fun requestNotificationPermission() { if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100) }
    private fun requestExactAlarmPermission() { if (Build.VERSION.SDK_INT >= 31) { val alarmManager = getSystemService(AlarmManager::class.java); if (!alarmManager.canScheduleExactAlarms()) runCatching { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) } } }
    private fun createNotificationChannel() { getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("dosebloom_reminders", getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH)) }
    private fun refreshWidget() { val manager = AppWidgetManager.getInstance(this); val component = android.content.ComponentName(this, NextDoseWidget::class.java); manager.getAppWidgetIds(component).forEach { id -> NextDoseWidget.update(this, manager, id) } }
    private fun today(): String = Schedule.todayKey()
    private fun currentLocale(): Locale = if (Build.VERSION.SDK_INT >= 24) resources.configuration.locales[0] ?: Locale.getDefault() else @Suppress("DEPRECATION") resources.configuration.locale
    private fun prettyDate(key: String): String = SimpleDateFormat("EEEE, d MMMM", currentLocale()).format(Schedule.parseDate(key).time).replaceFirstChar { it.uppercase() }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable private fun DoseBloomRoot() { var darkMode by remember { mutableStateOf(isDarkMode()) }; MaterialTheme(colorScheme = if (darkMode) DoseBloomDarkColors else DoseBloomLightColors) { DoseBloomApp(darkMode) { darkMode = it; setDarkMode(it) } } }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable private fun DoseBloomApp(darkMode: Boolean, onDarkModeChanged: (Boolean) -> Unit) {
        var tab by remember { mutableIntStateOf(0) }; var refresh by remember { mutableIntStateOf(0) }; var selectedProfile by remember { mutableStateOf("Я") }; var profileDialog by remember { mutableStateOf(false) }; var settingsDialog by remember { mutableStateOf(false) }; var language by remember { mutableStateOf(Localization.currentLanguage(this@MainActivity)) }; var editorVisible by remember { mutableStateOf(false) }; var editingMedicine by remember { mutableStateOf<Medicine?>(null) }
        val medicines = remember(refresh, selectedProfile) { db.medicines().filter { it.profile == selectedProfile } }
        Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0), topBar = { AppHeader(prettyDate(today()), selectedProfile, { profileDialog = true }, { settingsDialog = true }) }, bottomBar = {
            NavigationBar(tonalElevation = 2.dp) {
                NavigationBarItem(tab == 0, { tab = 0 }, { Text("●", fontSize = 16.sp) }, label = { Text(stringResource(R.string.today)) })
                NavigationBarItem(tab == 1, { tab = 1 }, { Text("▦", fontSize = 20.sp) }, label = { Text(stringResource(R.string.history)) })
                NavigationBarItem(tab == 2, { tab = 2 }, { Text("+", fontSize = 22.sp) }, label = { Text(stringResource(R.string.medicines)) })
            }
        }, floatingActionButton = { if (tab == 2) FloatingActionButton(onClick = { editorVisible = true }) { Text("+", fontSize = 26.sp) } }) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) { AnimatedContent(targetState = tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab_transition") { selectedTab ->
                when (selectedTab) {
                    0 -> Today(medicines, refresh, { refresh++ }, Modifier.fillMaxSize())
                    1 -> History(medicines, refresh, Modifier.fillMaxSize())
                    else -> Medicines(medicines, { refresh++ }, { pendingExport = { uri -> ExportImport.export(this@MainActivity, uri) }; exportLauncher.launch("DoseBloom-${today()}.json") }, { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }, Modifier.fillMaxSize())
                }
            } }
        }
        if (editorVisible || editingMedicine != null) MedicineEditor(editingMedicine, selectedProfile, { editorVisible = false; editingMedicine = null }) { medicine -> if (medicine.id == 0L) db.insertMedicine(medicine) else { editingMedicine?.let { Scheduler.cancelMedicine(this, it) }; db.updateMedicine(medicine) }; Scheduler.rescheduleAll(this); refreshWidget(); refresh++; editorVisible = false; editingMedicine = null }
        if (profileDialog) ProfileDialog(selectedProfile, { selectedProfile = it; profileDialog = false }, { name -> db.addProfile(name); selectedProfile = name.trim(); profileDialog = false }) { name -> db.removeProfile(name); if (selectedProfile == name) selectedProfile = "Я" }
        if (settingsDialog) SettingsDialog(darkMode, onDarkModeChanged, language, { selectedLanguage -> Localization.setLanguage(this@MainActivity, selectedLanguage); language = selectedLanguage; recreate() }) { settingsDialog = false }
    }

    @Composable private fun AppHeader(dateText: String, profile: String, onProfile: () -> Unit, onSettings: () -> Unit) { Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text("DoseBloom", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); TextButton(onClick = onProfile) { Text(Localization.profileDisplayName(this@MainActivity, profile)) }; IconButton(onClick = onSettings, modifier = Modifier.size(44.dp)) { Text("⚙", fontSize = 21.sp) } }; Text(dateText, style = MaterialTheme.typography.bodyMedium) } }

    @Composable private fun Today(medicines: List<Medicine>, refresh: Int, changed: () -> Unit, modifier: Modifier = Modifier) { val date = today(); val events = remember(medicines, refresh, date) { Schedule.events(medicines, date) }; val records = remember(medicines, refresh, date) { db.intakes(date) }; val completed = events.isNotEmpty() && events.all { event -> records.any { it.medicineId == event.first.id && it.plannedTime == event.second } }; LazyColumn(modifier.widthIn(max = 760.dp).padding(horizontal = 16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 10.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Text(stringResource(R.string.today), style = MaterialTheme.typography.headlineMedium) }; if (events.isEmpty()) item { EmptyTodayCard() } else if (completed) item { CompletedTodayCard() }; items(events, key = { "${it.first.id}-${it.second}" }) { event -> val medicine = event.first; val time = event.second; val record = records.firstOrNull { it.medicineId == medicine.id && it.plannedTime == time }; DoseCard(medicine, time, record?.status, record?.actualMillis) { if (!db.hasIntake(medicine.id, date, time)) { db.addIntake(medicine.id, date, time, "TAKEN"); db.decreaseStock(medicine.id); refreshWidget(); changed() } } } } }
    @Composable private fun EmptyTodayCard() { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(20.dp)) { Text(stringResource(R.string.today_calm_title), style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(6.dp)); Text(stringResource(R.string.today_calm_body)) } } }
    @Composable private fun CompletedTodayCard() { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFDDEFE2))) { Column(Modifier.padding(18.dp)) { Text(stringResource(R.string.today_completed_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text(stringResource(R.string.today_completed_body)) } } }
    @Composable private fun DoseCard(medicine: Medicine, time: String, status: String?, actual: Long?, onTake: () -> Unit) { Card(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); StatusPill(status) }; Spacer(Modifier.height(5.dp)); Text(medicine.name, style = MaterialTheme.typography.titleLarge); Text("${medicine.dose} ${medicine.unit}"); if (medicine.note.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text(medicine.note, style = MaterialTheme.typography.bodySmall) }; AnimatedVisibility(status == null) { Button(onClick = onTake, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(stringResource(R.string.take)) } }; AnimatedVisibility(status != null && actual != null) { Text(stringResource(R.string.fact_time, SimpleDateFormat("HH:mm", Locale.US).format(Date(actual ?: 0))), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall) } } } }
    @Composable private fun StatusPill(status: String?) { val text = when (status) { "TAKEN" -> stringResource(R.string.status_taken); "SKIPPED" -> stringResource(R.string.status_skipped); else -> stringResource(R.string.status_pending) }; val background = when (status) { "TAKEN" -> Color(0xFFDDEFE2); "SKIPPED" -> Color(0xFFF8DEDE); else -> MaterialTheme.colorScheme.secondaryContainer }; val color = when (status) { "TAKEN" -> Color(0xFF24623B); "SKIPPED" -> MaterialTheme.colorScheme.error; else -> MaterialTheme.colorScheme.onSurfaceVariant }; Surface(color = background, shape = RoundedCornerShape(50), tonalElevation = 0.dp) { Text(text, color = color, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) } }

    @Composable private fun History(medicines: List<Medicine>, refresh: Int, modifier: Modifier = Modifier) { var month by remember { mutableStateOf(Calendar.getInstance()) }; val from = Calendar.getInstance().apply { set(month.get(Calendar.YEAR), month.get(Calendar.MONTH), 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }; val to = Calendar.getInstance().apply { time = from.time; set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) }; val records = remember(refresh, month.get(Calendar.YEAR), month.get(Calendar.MONTH), medicines) { db.intakesBetween(Schedule.dateKey(from), Schedule.dateKey(to)).filter { record -> medicines.any { it.id == record.medicineId } } }; Column(modifier.widthIn(max = 760.dp).padding(horizontal = 16.dp)) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) { Text(stringResource(R.string.history), style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.weight(1f)); TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) { Text("‹") }; TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) { Text("›") } }; Text(SimpleDateFormat("LLLL yyyy", currentLocale()).format(month.time).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)); CalendarLegend(); Spacer(Modifier.height(8.dp)); CalendarGrid(month, medicines, records); Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.intakes), style = MaterialTheme.typography.titleLarge); if (records.isEmpty()) EmptyCard(stringResource(R.string.history_empty)) else LazyColumn(modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(records, key = { it.id }) { record -> HistoryRecordCard(record, medicines.firstOrNull { it.id == record.medicineId }?.name ?: stringResource(R.string.medicine_fallback)) } } } }
    @Composable private fun CalendarLegend() { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { LegendItem(Color(0xFFDDEFE2), stringResource(R.string.status_taken)); LegendItem(Color(0xFFFFE9B5), stringResource(R.string.planned)); LegendItem(Color(0xFFF8DEDE), stringResource(R.string.status_skipped)) } }
    @Composable private fun LegendItem(color: Color, text: String) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp))); Spacer(Modifier.size(4.dp)); Text(text, style = MaterialTheme.typography.labelSmall) } }
    @Composable private fun HistoryRecordCard(record: Intake, medicineName: String) { Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("${record.date} • ${record.plannedTime}", fontWeight = FontWeight.SemiBold); Text(medicineName) }; StatusPill(record.status) } } }
    @Composable private fun CalendarGrid(month: Calendar, medicines: List<Medicine>, records: List<Intake>) { val days = Schedule.monthDays(month.get(Calendar.YEAR), month.get(Calendar.MONTH)); Column { Row(Modifier.fillMaxWidth()) { listOf(R.string.days_mon, R.string.days_tue, R.string.days_wed, R.string.days_thu, R.string.days_fri, R.string.days_sat, R.string.days_sun).forEach { day -> Text(stringResource(day), Modifier.weight(1f), fontWeight = FontWeight.Bold) } }; days.chunked(7).forEach { week -> Row(Modifier.fillMaxWidth()) { week.forEach { day -> val key = Schedule.dateKey(day); val inMonth = day.get(Calendar.MONTH) == month.get(Calendar.MONTH); val events = Schedule.events(medicines, key); val dayRecords = records.filter { it.date == key }; val background = when { !inMonth -> Color.Transparent; events.isEmpty() -> Color.Transparent; dayRecords.any { it.status == "SKIPPED" } -> Color(0xFFF8DEDE); dayRecords.count { it.status == "TAKEN" } == events.size -> Color(0xFFDDEFE2); else -> Color(0xFFFFE9B5) }; Box(Modifier.weight(1f).padding(2.dp).background(background, RoundedCornerShape(8.dp)).padding(vertical = 8.dp), contentAlignment = Alignment.Center) { Text(day.get(Calendar.DAY_OF_MONTH).toString(), color = if (inMonth) MaterialTheme.colorScheme.onSurface else Color.LightGray) } } } } } }

    @Composable private fun Medicines(medicines: List<Medicine>, changed: () -> Unit, onExport: () -> Unit, onImport: () -> Unit, modifier: Modifier = Modifier) { var editing by remember { mutableStateOf<Medicine?>(null) }; var deleting by remember { mutableStateOf<Medicine?>(null) }; BoxWithConstraints(modifier = modifier) { val horizontal = if (maxWidth >= 600.dp) 28.dp else 16.dp; LazyColumn(Modifier.fillMaxSize().widthIn(max = 900.dp).padding(horizontal = horizontal), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 10.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.medicines), style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.weight(1f)); TextButton(onClick = onExport) { Text(stringResource(R.string.export)) }; TextButton(onClick = onImport) { Text(stringResource(R.string.import_data)) } } }; if (medicines.isEmpty()) item { EmptyCard(stringResource(R.string.medicines_empty)) }; items(medicines, key = { it.id }) { medicine -> MedicineCard(medicine, { editing = medicine }, { deleting = medicine }) } } }; editing?.let { medicine -> MedicineEditor(medicine, medicine.profile, { editing = null }) { updated -> Scheduler.cancelMedicine(this, medicine); db.updateMedicine(updated); Scheduler.rescheduleAll(this); refreshWidget(); changed(); editing = null } }; deleting?.let { medicine -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text(stringResource(R.string.delete_medicine_title, medicine.name)) }, text = { Text(stringResource(R.string.delete_medicine_body)) }, confirmButton = { Button(onClick = { Scheduler.cancelMedicine(this, medicine); db.deleteMedicine(medicine.id); Scheduler.rescheduleAll(this); refreshWidget(); changed(); deleting = null }) { Text(stringResource(R.string.delete)) } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } }) } }

    @Composable private fun MedicineCard(medicine: Medicine, onEdit: () -> Unit, onDelete: () -> Unit) { Card(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(medicine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); if (medicine.asNeeded) Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) { Text(stringResource(R.string.as_needed), Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium) } }; Spacer(Modifier.height(4.dp)); Text("${medicine.dose} ${medicine.unit}"); if (!medicine.asNeeded) Text(stringResource(R.string.time_label, medicine.times.joinToString(", "))); Text(stringResource(R.string.course, medicine.startDate, if (medicine.endDate.isNotBlank()) " — ${medicine.endDate}" else "")); Text(stringResource(R.string.remaining_stock, medicine.stock, medicine.lowStock)); AnimatedVisibility(medicine.stock <= medicine.lowStock) { Text(stringResource(R.string.low_stock), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 2.dp)) }; if (medicine.note.isNotBlank()) Text(medicine.note, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)); Row { TextButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }; TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) } } } } }

    @Composable private fun MedicineEditor(existing: Medicine?, profile: String, onDismiss: () -> Unit, onSave: (Medicine) -> Unit) {
        val defaultUnit = stringResource(R.string.default_unit)
        var name by remember(existing) { mutableStateOf(existing?.name ?: "") }; var dose by remember(existing) { mutableStateOf(existing?.dose ?: "1") }; var unit by remember(existing) { mutableStateOf(existing?.unit ?: defaultUnit) }; var times by remember(existing) { mutableStateOf(existing?.times?.joinToString(", ") ?: "08:00") }; var start by remember(existing) { mutableStateOf(existing?.startDate ?: today()) }; var end by remember(existing) { mutableStateOf(existing?.endDate ?: "") }; var note by remember(existing) { mutableStateOf(existing?.note ?: "") }; var stock by remember(existing) { mutableStateOf((existing?.stock ?: 30).toString()) }; var low by remember(existing) { mutableStateOf((existing?.lowStock ?: 5).toString()) }; var asNeeded by remember(existing) { mutableStateOf(existing?.asNeeded ?: false) }; var error by remember { mutableStateOf("") }
        fun validDate(value: String): Boolean = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value) }.isSuccess
        AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(if (existing == null) R.string.new_medicine else R.string.edit_medicine)) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.name)) }, singleLine = true); OutlinedTextField(dose, { dose = it }, label = { Text(stringResource(R.string.dose)) }, singleLine = true); OutlinedTextField(unit, { unit = it }, label = { Text(stringResource(R.string.unit)) }, singleLine = true); OutlinedTextField(times, { times = it }, label = { Text(stringResource(R.string.time_example)) }, enabled = !asNeeded, singleLine = true); OutlinedTextField(start, { start = it }, label = { Text(stringResource(R.string.start_date)) }, singleLine = true); OutlinedTextField(end, { end = it }, label = { Text(stringResource(R.string.end_date)) }, singleLine = true); OutlinedTextField(stock, { stock = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.stock)) }, singleLine = true); OutlinedTextField(low, { low = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.low_stock_threshold)) }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.note)) }); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.as_needed)); Switch(checked = asNeeded, onCheckedChange = { asNeeded = it }) }; if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        } }, confirmButton = { Button(enabled = name.isNotBlank(), onClick = { val parsedTimes = times.split(",").map(String::trim).filter(Schedule::validTime).distinct().sorted(); when { !asNeeded && parsedTimes.isEmpty() -> error = getString(R.string.invalid_time); !validDate(start) -> error = getString(R.string.invalid_start_date); end.isNotBlank() && !validDate(end) -> error = getString(R.string.invalid_end_date); end.isNotBlank() && end < start -> error = getString(R.string.end_before_start); else -> onSave(Medicine(existing?.id ?: 0L, name.trim(), dose.trim(), unit.trim(), if (asNeeded) emptyList() else parsedTimes, start.trim(), end.trim(), note.trim(), stock.toIntOrNull() ?: 0, low.toIntOrNull() ?: 0, asNeeded, profile)) } }) { Text(stringResource(R.string.save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
    }

    @Composable private fun ProfileDialog(current: String, onSelect: (String) -> Unit, onAdd: (String) -> Unit, onDelete: (String) -> Unit) { var newProfile by remember { mutableStateOf("") }; var deleteTarget by remember { mutableStateOf<String?>(null) }; val profiles = db.profiles(); if (deleteTarget != null) { val target = deleteTarget!!; AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text(stringResource(R.string.delete_profile_title)) }, text = { Text(stringResource(R.string.delete_profile_body, Localization.profileDisplayName(this@MainActivity, target))) }, confirmButton = { Button(onClick = { onDelete(target); deleteTarget = null }) { Text(stringResource(R.string.delete)) } }, dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.cancel)) } }) }; AlertDialog(onDismissRequest = { onSelect(current) }, title = { Text(stringResource(R.string.profile)) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) { profiles.forEach { profile -> Row(verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { onSelect(profile) }, modifier = Modifier.weight(1f)) { val display = Localization.profileDisplayName(this@MainActivity, profile); Text(if (profile == current) "✓ $display" else display) }; if (profile != "Я") TextButton(onClick = { deleteTarget = profile }) { Text(stringResource(R.string.delete)) } } }; HorizontalDivider(Modifier.padding(vertical = 6.dp)); OutlinedTextField(newProfile, { newProfile = it }, label = { Text(stringResource(R.string.new_profile)) }, singleLine = true); TextButton(enabled = newProfile.isNotBlank(), onClick = { onAdd(newProfile.trim()); newProfile = "" }) { Text(stringResource(R.string.add_and_select)) } } }, confirmButton = { TextButton(onClick = { onSelect(current) }) { Text(stringResource(R.string.done)) } }) }

    @Composable private fun SettingsDialog(darkMode: Boolean, onDarkModeChanged: (Boolean) -> Unit, language: String, onLanguageChanged: (String) -> Unit, onDismiss: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.settings)) }, text = { Column(Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(stringResource(R.string.dark_theme), fontWeight = FontWeight.Medium); Text(stringResource(R.string.dark_theme_summary), style = MaterialTheme.typography.bodySmall) }; Switch(checked = darkMode, onCheckedChange = onDarkModeChanged) }; HorizontalDivider(); Text(stringResource(R.string.language), fontWeight = FontWeight.Medium); Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { onLanguageChanged(Localization.SYSTEM) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.system_default)) }; OutlinedButton(onClick = { onLanguageChanged(Localization.RUSSIAN) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.russian)) }; OutlinedButton(onClick = { onLanguageChanged(Localization.ENGLISH) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.english)) } }; Text(when (language) { Localization.RUSSIAN -> stringResource(R.string.russian); Localization.ENGLISH -> stringResource(R.string.english); else -> stringResource(R.string.system_default) }, style = MaterialTheme.typography.bodySmall); HorizontalDivider(); Text(stringResource(R.string.notifications), fontWeight = FontWeight.Medium); Text(stringResource(R.string.notification_settings_summary), style = MaterialTheme.typography.bodySmall); OutlinedButton(onClick = { runCatching { startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, packageName) }) } }) { Text(stringResource(R.string.open_notification_settings)) }; if (Build.VERSION.SDK_INT >= 31) OutlinedButton(onClick = { runCatching { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) } }) { Text(stringResource(R.string.exact_alarm_settings)) }; Text("DoseBloom 1.4.1", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }, confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) } }) }
    @Composable private fun EmptyCard(text: String) { Card(Modifier.fillMaxWidth()) { Text(text, Modifier.padding(18.dp)) } }
}
