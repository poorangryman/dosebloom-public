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
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val DoseBloomLightColors = lightColorScheme(
    primary = Color(0xFF6B4DB3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF27114E),
    secondaryContainer = Color(0xFFEDE7F5),
    background = Color(0xFFF8F7F4),
    surface = Color(0xFFF8F7F4),
    surfaceVariant = Color(0xFFE8E1EA)
)

private val DoseBloomDarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF39215F),
    primaryContainer = Color(0xFF513B78),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondaryContainer = Color(0xFF49454F),
    background = Color(0xFF141218),
    surface = Color(0xFF141218),
    surfaceVariant = Color(0xFF49454F)
)

class MainActivity : ComponentActivity() {
    private lateinit var db: Db
    private var pendingExport: (Uri) -> Unit = {}

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(pendingExport) }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching { ExportImport.import(this, uri) }
                .onSuccess {
                    Scheduler.rescheduleAll(this)
                    refreshWidget()
                    render()
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        db = Db(this)
        createNotificationChannel()
        requestNotificationPermission()
        requestExactAlarmPermission()
        Scheduler.rescheduleAll(this)
        render()
    }

    private fun render() {
        setContent { DoseBloomRoot() }
    }

    private fun isDarkMode(): Boolean = getPreferences(MODE_PRIVATE).getBoolean("dark_mode", false)

    private fun setDarkMode(enabled: Boolean) {
        getPreferences(MODE_PRIVATE).edit().putBoolean("dark_mode", enabled).apply()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                runCatching {
                    startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:$packageName")
                        )
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "dosebloom_reminders",
            "Напоминания DoseBloom",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun refreshWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val component = android.content.ComponentName(this, NextDoseWidget::class.java)
        manager.getAppWidgetIds(component).forEach { id -> NextDoseWidget.update(this, manager, id) }
    }

    private fun today(): String = Schedule.todayKey()

    private fun prettyDate(key: String): String =
        SimpleDateFormat("EEEE, d MMMM", Locale("ru"))
            .format(Schedule.parseDate(key).time)
            .replaceFirstChar { it.uppercase() }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DoseBloomRoot() {
        var darkMode by remember { mutableStateOf(isDarkMode()) }

        MaterialTheme(colorScheme = if (darkMode) DoseBloomDarkColors else DoseBloomLightColors) {
            DoseBloomApp(
                darkMode = darkMode,
                onDarkModeChanged = {
                    darkMode = it
                    setDarkMode(it)
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DoseBloomApp(
        darkMode: Boolean,
        onDarkModeChanged: (Boolean) -> Unit
    ) {
        var tab by remember { mutableIntStateOf(0) }
        var refresh by remember { mutableIntStateOf(0) }
        var selectedProfile by remember { mutableStateOf("Я") }
        var profileDialog by remember { mutableStateOf(false) }
        var settingsDialog by remember { mutableStateOf(false) }
        var editorVisible by remember { mutableStateOf(false) }
        var editingMedicine by remember { mutableStateOf<Medicine?>(null) }

        val medicines = remember(refresh, selectedProfile) {
            db.medicines().filter { it.profile == selectedProfile }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            topBar = {
                AppHeader(
                    dateText = prettyDate(today()),
                    profile = selectedProfile,
                    onProfile = { profileDialog = true },
                    onSettings = { settingsDialog = true }
                )
            },
            bottomBar = {
                NavigationBar(
                    tonalElevation = 2.dp
                ) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Text("●", fontSize = 16.sp) },
                        label = { Text("Сегодня") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Text("▦", fontSize = 20.sp) },
                        label = { Text("История") }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Text("+", fontSize = 22.sp) },
                        label = { Text("Лекарства") }
                    )
                }
            },
            floatingActionButton = {
                if (tab == 2) {
                    FloatingActionButton(onClick = { editorVisible = true }) {
                        Text("+", fontSize = 26.sp)
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { selectedTab ->
                    when (selectedTab) {
                        0 -> Today(
                            medicines = medicines,
                            refresh = refresh,
                            changed = { refresh++ },
                            modifier = Modifier.fillMaxSize()
                        )
                        1 -> History(
                            medicines = medicines,
                            refresh = refresh,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Medicines(
                            medicines = medicines,
                            changed = { refresh++ },
                            onExport = {
                                pendingExport = { uri -> ExportImport.export(this@MainActivity, uri) }
                                exportLauncher.launch("DoseBloom-${today()}.json")
                            },
                            onImport = {
                                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (editorVisible || editingMedicine != null) {
            MedicineEditor(
                existing = editingMedicine,
                profile = selectedProfile,
                onDismiss = {
                    editorVisible = false
                    editingMedicine = null
                },
                onSave = { medicine ->
                    if (medicine.id == 0L) {
                        db.insertMedicine(medicine)
                    } else {
                        editingMedicine?.let { Scheduler.cancelMedicine(this, it) }
                        db.updateMedicine(medicine)
                    }
                    Scheduler.rescheduleAll(this)
                    refreshWidget()
                    refresh++
                    editorVisible = false
                    editingMedicine = null
                }
            )
        }

        if (profileDialog) {
            ProfileDialog(
                current = selectedProfile,
                onSelect = {
                    selectedProfile = it
                    profileDialog = false
                },
                onAdd = { name ->
                    db.addProfile(name)
                    selectedProfile = name.trim()
                    profileDialog = false
                },
                onDelete = { name ->
                    db.removeProfile(name)
                    if (selectedProfile == name) selectedProfile = "Я"
                }
            )
        }

        if (settingsDialog) {
            SettingsDialog(
                darkMode = darkMode,
                onDarkModeChanged = onDarkModeChanged,
                onDismiss = { settingsDialog = false }
            )
        }
    }

    @Composable
    private fun AppHeader(
        dateText: String,
        profile: String,
        onProfile: () -> Unit,
        onSettings: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DoseBloom",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onProfile) { Text(profile) }
                IconButton(onClick = onSettings, modifier = Modifier.size(44.dp)) {
                    Text("⚙", fontSize = 21.sp)
                }
            }
            Text(text = dateText, style = MaterialTheme.typography.bodyMedium)
        }
    }

    @Composable
    private fun Today(
        medicines: List<Medicine>,
        refresh: Int,
        changed: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val date = today()
        val events = remember(medicines, refresh, date) { Schedule.events(medicines, date) }
        val records = remember(medicines, refresh, date) { db.intakes(date) }
        val completed = events.isNotEmpty() && events.all { event ->
            records.any { it.medicineId == event.first.id && it.plannedTime == event.second }
        }

        LazyColumn(
            modifier = modifier
                .widthIn(max = 760.dp)
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Сегодня", style = MaterialTheme.typography.headlineMedium)
            }
            if (events.isEmpty()) {
                item { EmptyTodayCard() }
            } else if (completed) {
                item { CompletedTodayCard() }
            }
            items(events, key = { "${it.first.id}-${it.second}" }) { event ->
                val medicine = event.first
                val time = event.second
                val record = records.firstOrNull {
                    it.medicineId == medicine.id && it.plannedTime == time
                }
                DoseCard(
                    medicine = medicine,
                    time = time,
                    status = record?.status,
                    actual = record?.actualMillis,
                    onTake = {
                        if (!db.hasIntake(medicine.id, date, time)) {
                            db.addIntake(medicine.id, date, time, "TAKEN")
                            db.decreaseStock(medicine.id)
                            refreshWidget()
                            changed()
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun EmptyTodayCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("На сегодня всё спокойно", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("Нет запланированных приёмов. Если лекарство принимается по необходимости, его можно отметить на экране «Лекарства».")
            }
        }
    }

    @Composable
    private fun CompletedTodayCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDEFE2))
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Все приёмы выполнены", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("На сегодня больше ничего не запланировано.")
            }
        }
    }

    @Composable
    private fun DoseCard(
        medicine: Medicine,
        time: String,
        status: String?,
        actual: Long?,
        onTake: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    StatusPill(status)
                }
                Spacer(Modifier.height(5.dp))
                Text(medicine.name, style = MaterialTheme.typography.titleLarge)
                Text("${medicine.dose} ${medicine.unit}")
                if (medicine.note.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(medicine.note, style = MaterialTheme.typography.bodySmall)
                }
                AnimatedVisibility(visible = status == null) {
                    Button(onClick = onTake, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text("ПРИНЯТЬ")
                    }
                }
                AnimatedVisibility(visible = status != null && actual != null) {
                    Text(
                        "Факт: ${SimpleDateFormat("HH:mm", Locale.US).format(Date(actual ?: 0))}",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    @Composable
    private fun StatusPill(status: String?) {
        val text = when (status) {
            "TAKEN" -> "Принято"
            "SKIPPED" -> "Пропущено"
            else -> "Ожидает"
        }
        val background = when (status) {
            "TAKEN" -> Color(0xFFDDEFE2)
            "SKIPPED" -> Color(0xFFF8DEDE)
            else -> MaterialTheme.colorScheme.secondaryContainer
        }
        val color = when (status) {
            "TAKEN" -> Color(0xFF24623B)
            "SKIPPED" -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Surface(
            color = background,
            shape = RoundedCornerShape(50),
            tonalElevation = 0.dp
        ) {
            Text(text, color = color, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
        }
    }

    @Composable
    private fun History(
        medicines: List<Medicine>,
        refresh: Int,
        modifier: Modifier = Modifier
    ) {
        var month by remember { mutableStateOf(Calendar.getInstance()) }
        val from = Calendar.getInstance().apply {
            set(month.get(Calendar.YEAR), month.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val to = Calendar.getInstance().apply {
            time = from.time
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        val records = remember(refresh, month.get(Calendar.YEAR), month.get(Calendar.MONTH), medicines) {
            db.intakesBetween(Schedule.dateKey(from), Schedule.dateKey(to))
                .filter { record -> medicines.any { it.id == record.medicineId } }
        }

        Column(
            modifier = modifier
                .widthIn(max = 760.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                Text("История", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) { Text("‹") }
                TextButton(onClick = { month = (month.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) { Text("›") }
            }
            Text(
                SimpleDateFormat("LLLL yyyy", Locale("ru")).format(month.time).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            CalendarLegend()
            Spacer(Modifier.height(8.dp))
            CalendarGrid(month, medicines, records)
            Spacer(Modifier.height(12.dp))
            Text("Приёмы", style = MaterialTheme.typography.titleLarge)
            if (records.isEmpty()) {
                EmptyCard("За выбранный месяц нет отмеченных приёмов.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        val medicine = medicines.firstOrNull { it.id == record.medicineId }
                        HistoryRecordCard(record, medicine?.name ?: "Лекарство")
                    }
                }
            }
        }
    }

    @Composable
    private fun CalendarLegend() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(Color(0xFFDDEFE2), "Принято")
            LegendItem(Color(0xFFFFE9B5), "Запланировано")
            LegendItem(Color(0xFFF8DEDE), "Пропущено")
        }
    }

    @Composable
    private fun LegendItem(color: Color, text: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
            Spacer(Modifier.size(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }

    @Composable
    private fun HistoryRecordCard(record: Intake, medicineName: String) {
        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("${record.date} • ${record.plannedTime}", fontWeight = FontWeight.SemiBold)
                    Text(medicineName)
                }
                StatusPill(record.status)
            }
        }
    }

    @Composable
    private fun CalendarGrid(
        month: Calendar,
        medicines: List<Medicine>,
        records: List<Intake>
    ) {
        val days = Schedule.monthDays(month.get(Calendar.YEAR), month.get(Calendar.MONTH))
        Column {
            Row(Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                    Text(day, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
            }
            days.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        val key = Schedule.dateKey(day)
                        val inMonth = day.get(Calendar.MONTH) == month.get(Calendar.MONTH)
                        val events = Schedule.events(medicines, key)
                        val dayRecords = records.filter { it.date == key }
                        val background = when {
                            !inMonth -> Color.Transparent
                            events.isEmpty() -> Color.Transparent
                            dayRecords.any { it.status == "SKIPPED" } -> Color(0xFFF8DEDE)
                            dayRecords.count { it.status == "TAKEN" } == events.size -> Color(0xFFDDEFE2)
                            else -> Color(0xFFFFE9B5)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .background(background, RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                day.get(Calendar.DAY_OF_MONTH).toString(),
                                color = if (inMonth) MaterialTheme.colorScheme.onSurface else Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Medicines(
        medicines: List<Medicine>,
        changed: () -> Unit,
        onExport: () -> Unit,
        onImport: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var editing by remember { mutableStateOf<Medicine?>(null) }
        var deleting by remember { mutableStateOf<Medicine?>(null) }

        BoxWithConstraints(modifier = modifier) {
            val horizontal = if (maxWidth >= 600.dp) 28.dp else 16.dp
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 900.dp)
                    .padding(horizontal = horizontal),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Лекарства", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onExport) { Text("Экспорт") }
                        TextButton(onClick = onImport) { Text("Импорт") }
                    }
                }
                if (medicines.isEmpty()) {
                    item { EmptyCard("Добавьте первое лекарство кнопкой +") }
                }
                items(medicines, key = { it.id }) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        onEdit = { editing = medicine },
                        onDelete = { deleting = medicine }
                    )
                }
            }
        }

        editing?.let { medicine ->
            MedicineEditor(
                existing = medicine,
                profile = medicine.profile,
                onDismiss = { editing = null },
                onSave = { updated ->
                    Scheduler.cancelMedicine(this, medicine)
                    db.updateMedicine(updated)
                    Scheduler.rescheduleAll(this)
                    refreshWidget()
                    changed()
                    editing = null
                }
            )
        }

        deleting?.let { medicine ->
            AlertDialog(
                onDismissRequest = { deleting = null },
                title = { Text("Удалить ${medicine.name}?") },
                text = { Text("История этого лекарства тоже будет удалена.") },
                confirmButton = {
                    Button(onClick = {
                        Scheduler.cancelMedicine(this, medicine)
                        db.deleteMedicine(medicine.id)
                        Scheduler.rescheduleAll(this)
                        refreshWidget()
                        changed()
                        deleting = null
                    }) { Text("Удалить") }
                },
                dismissButton = { TextButton(onClick = { deleting = null }) { Text("Отмена") } }
            )
        }
    }

    @Composable
    private fun MedicineCard(
        medicine: Medicine,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(medicine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    if (medicine.asNeeded) {
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) {
                            Text("По необходимости", modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("${medicine.dose} ${medicine.unit}")
                if (!medicine.asNeeded) Text("Время: ${medicine.times.joinToString(", ")}")
                Text(
                    "Курс: ${medicine.startDate}" + if (medicine.endDate.isNotBlank()) " — ${medicine.endDate}" else ""
                )
                Text("Осталось: ${medicine.stock} • порог: ${medicine.lowStock}")
                AnimatedVisibility(medicine.stock <= medicine.lowStock) {
                    Text("⚠ Осталось мало", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 2.dp))
                }
                if (medicine.note.isNotBlank()) {
                    Text(medicine.note, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
                Row {
                    TextButton(onClick = onEdit) { Text("Изменить") }
                    TextButton(onClick = onDelete) { Text("Удалить") }
                }
            }
        }
    }

    @Composable
    private fun MedicineEditor(
        existing: Medicine?,
        profile: String,
        onDismiss: () -> Unit,
        onSave: (Medicine) -> Unit
    ) {
        var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
        var dose by remember(existing) { mutableStateOf(existing?.dose ?: "1") }
        var unit by remember(existing) { mutableStateOf(existing?.unit ?: "таблетка") }
        var times by remember(existing) { mutableStateOf(existing?.times?.joinToString(", ") ?: "08:00") }
        var start by remember(existing) { mutableStateOf(existing?.startDate ?: today()) }
        var end by remember(existing) { mutableStateOf(existing?.endDate ?: "") }
        var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
        var stock by remember(existing) { mutableStateOf((existing?.stock ?: 30).toString()) }
        var low by remember(existing) { mutableStateOf((existing?.lowStock ?: 5).toString()) }
        var asNeeded by remember(existing) { mutableStateOf(existing?.asNeeded ?: false) }
        var error by remember { mutableStateOf("") }

        fun validDate(value: String): Boolean = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)
        }.isSuccess

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (existing == null) "Новое лекарство" else "Изменить лекарство") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(name, { name = it }, label = { Text("Название") }, singleLine = true)
                    OutlinedTextField(dose, { dose = it }, label = { Text("Доза") }, singleLine = true)
                    OutlinedTextField(unit, { unit = it }, label = { Text("Единица") }, singleLine = true)
                    OutlinedTextField(
                        times,
                        { times = it },
                        label = { Text("Время: 08:00, 20:00") },
                        enabled = !asNeeded,
                        singleLine = true
                    )
                    OutlinedTextField(start, { start = it }, label = { Text("Начало YYYY-MM-DD") }, singleLine = true)
                    OutlinedTextField(end, { end = it }, label = { Text("Конец YYYY-MM-DD") }, singleLine = true)
                    OutlinedTextField(stock, { stock = it.filter(Char::isDigit) }, label = { Text("Запас") }, singleLine = true)
                    OutlinedTextField(low, { low = it.filter(Char::isDigit) }, label = { Text("Порог") }, singleLine = true)
                    OutlinedTextField(note, { note = it }, label = { Text("Заметка") })
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("По необходимости")
                        Switch(checked = asNeeded, onCheckedChange = { asNeeded = it })
                    }
                    if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(enabled = name.isNotBlank(), onClick = {
                    val parsedTimes = times.split(",").map(String::trim).filter(Schedule::validTime).distinct().sorted()
                    when {
                        !asNeeded && parsedTimes.isEmpty() -> error = "Введите хотя бы одно время в формате ЧЧ:ММ"
                        !validDate(start) -> error = "Дата начала: YYYY-MM-DD"
                        end.isNotBlank() && !validDate(end) -> error = "Дата окончания: YYYY-MM-DD"
                        end.isNotBlank() && end < start -> error = "Дата окончания раньше даты начала"
                        else -> onSave(
                            Medicine(
                                id = existing?.id ?: 0L,
                                name = name.trim(),
                                dose = dose.trim(),
                                unit = unit.trim(),
                                times = if (asNeeded) emptyList() else parsedTimes,
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
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
        )
    }

    @Composable
    private fun ProfileDialog(
        current: String,
        onSelect: (String) -> Unit,
        onAdd: (String) -> Unit,
        onDelete: (String) -> Unit
    ) {
        var newProfile by remember { mutableStateOf("") }
        var deleteTarget by remember { mutableStateOf<String?>(null) }
        val profiles = db.profiles()

        if (deleteTarget != null) {
            val target = deleteTarget!!
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Удалить профиль?") },
                text = { Text("Профиль «$target» и его выбор будут удалены. Лекарства этого профиля останутся в базе данных.") },
                confirmButton = {
                    Button(onClick = { onDelete(target); deleteTarget = null }) { Text("Удалить") }
                },
                dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } }
            )
        }

        AlertDialog(
            onDismissRequest = { onSelect(current) },
            title = { Text("Профиль") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    profiles.forEach { profile ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { onSelect(profile) }, modifier = Modifier.weight(1f)) {
                                Text(if (profile == current) "✓ $profile" else profile)
                            }
                            if (profile != "Я") {
                                TextButton(onClick = { deleteTarget = profile }) { Text("Удалить") }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    OutlinedTextField(newProfile, { newProfile = it }, label = { Text("Новый профиль") }, singleLine = true)
                    TextButton(
                        enabled = newProfile.isNotBlank(),
                        onClick = {
                            onAdd(newProfile.trim())
                            newProfile = ""
                        }
                    ) { Text("Добавить и выбрать") }
                }
            },
            confirmButton = { TextButton(onClick = { onSelect(current) }) { Text("Готово") } }
        )
    }

    @Composable
    private fun SettingsDialog(
        darkMode: Boolean,
        onDarkModeChanged: (Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Настройки") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Тёмная тема", fontWeight = FontWeight.Medium)
                            Text("Запоминается между запусками", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = darkMode, onCheckedChange = onDarkModeChanged)
                    }
                    HorizontalDivider()
                    Text("Уведомления", fontWeight = FontWeight.Medium)
                    Text("Напоминания и точное расписание управляются системными разрешениями Android.", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(
                        onClick = {
                            runCatching { startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, packageName) }) }
                        }
                    ) { Text("Открыть настройки уведомлений") }
                    if (Build.VERSION.SDK_INT >= 31) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                                }
                            }
                        ) { Text("Настроить точные будильники") }
                    }
                    Text("DoseBloom 1.3.0", style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } }
        )
    }

    @Composable
    private fun EmptyCard(text: String) {
        Card(Modifier.fillMaxWidth()) { Text(text, Modifier.padding(18.dp)) }
    }
}
