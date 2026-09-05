package com.dosebloom.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val DoseLight = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF6B4DB3),
    primaryContainer = Color(0xFFE9DDFF),
    background = Color(0xFFF8F7F4),
    surface = Color(0xFFF8F7F4)
)
private val DoseDark = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFD0BCFF),
    primaryContainer = Color(0xFF513B78),
    background = Color(0xFF141218),
    surface = Color(0xFF141218)
)

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalMaterial3Api::class)
fun DoseBloomScreen(
    activity: RefactoredMainActivity,
    viewModel: DoseBloomViewModel,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onWidgetRefresh: () -> Unit
) {
    val dark by viewModel.darkMode.collectAsStateWithLifecycle()
    MaterialTheme(colorScheme = if (dark) DoseDark else DoseLight) {
        DoseBloomContent(activity, viewModel, onExport, onImport, onWidgetRefresh)
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalMaterial3Api::class)
private fun DoseBloomContent(
    activity: RefactoredMainActivity,
    viewModel: DoseBloomViewModel,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onWidgetRefresh: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editorId by rememberSaveable { mutableLongStateOf(-1L) }
    var addMedicine by rememberSaveable { mutableStateOf(false) }
    var profileDialog by rememberSaveable { mutableStateOf(false) }
    var settingsDialog by rememberSaveable { mutableStateOf(false) }

    val profile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val dark by viewModel.darkMode.collectAsStateWithLifecycle()
    val editor = medicines.firstOrNull { it.id == editorId }

    NavigationSuiteScaffold(
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteItems = {
            item(
                selected = tab == 0,
                onClick = { tab = 0 },
                icon = { Text("●") },
                label = { Text(stringResource(R.string.today)) }
            )
            item(
                selected = tab == 1,
                onClick = { tab = 1 },
                icon = { Text("▦") },
                label = { Text(stringResource(R.string.history)) }
            )
            item(
                selected = tab == 2,
                onClick = { tab = 2 },
                icon = { Text("+") },
                label = { Text(stringResource(R.string.medicines)) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                Localization.profileDisplayName(activity, profile),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { profileDialog = true }) {
                            Text(stringResource(R.string.profile))
                        }
                        TextButton(onClick = { settingsDialog = true }) {
                            Text("⚙", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> TodayScreen(viewModel, medicines, onWidgetRefresh, Modifier.fillMaxSize())
                            1 -> HistoryScreen(viewModel, medicines, onWidgetRefresh, Modifier.fillMaxSize())
                            else -> MedicinesScreen(
                                viewModel,
                                medicines,
                                onExport,
                                onImport,
                                onWidgetRefresh,
                                activity,
                                Modifier.fillMaxSize(),
                                onAdd = { addMedicine = true },
                                onEdit = { editorId = it.id }
                            )
                        }
                    }
                }
            }
        }
    }

    if (addMedicine || editor != null) {
        MedicineEditor(
            existing = editor,
            profile = profile,
            onDismiss = {
                addMedicine = false
                editorId = -1L
            },
            onSave = { medicine ->
                viewModel.saveMedicine(medicine)
                Scheduler.rescheduleAll(activity)
                onWidgetRefresh()
                addMedicine = false
                editorId = -1L
            }
        )
    }

    if (profileDialog) {
        ProfileDialog(
            activity = activity,
            profiles = profiles,
            current = profile,
            onSelect = {
                viewModel.selectProfile(it)
                profileDialog = false
            },
            onAdd = {
                viewModel.addProfile(it)
                viewModel.selectProfile(it.trim())
                profileDialog = false
            },
            onDelete = viewModel::removeProfile
        )
    }

    if (settingsDialog) {
        SettingsDialog(
            dark = dark,
            language = language,
            onDarkMode = viewModel::setDarkMode,
            onLanguage = {
                viewModel.setLanguage(it)
                Localization.setLanguage(activity, it)
                activity.recreate()
            },
            onDismiss = { settingsDialog = false }
        )
    }
}

@Composable
private fun TodayScreen(
    viewModel: DoseBloomViewModel,
    medicines: List<Medicine>,
    onWidgetRefresh: () -> Unit,
    modifier: Modifier
) {
    val date = Schedule.todayKey()
    val records by remember(date) { viewModel.observeIntakes(date) }.collectAsStateWithLifecycle()
    val events = remember(medicines, date) { Schedule.events(medicines, date) }
    val asNeededMedicines = remember(medicines) { medicines.filter { it.asNeeded } }
    val asNeededRecords = remember(records, events) {
        val eventKeys = events.map { "${it.first.id}-${it.second}" }.toSet()
        records.filter { "${it.medicineId}-${it.plannedTime}" !in eventKeys }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(stringResource(R.string.today), style = MaterialTheme.typography.headlineMedium)
        }
        if (events.isEmpty() && asNeededMedicines.isEmpty() && asNeededRecords.isEmpty()) {
            item { InfoCard(stringResource(R.string.today_calm_body)) }
        }
        items(events, key = { "${it.first.id}-${it.second}" }) { (medicine, time) ->
            val record = records.firstOrNull { it.medicineId == medicine.id && it.plannedTime == time }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        StatusPill(record?.status)
                    }
                    Text(medicine.name, style = MaterialTheme.typography.titleLarge)
                    Text("${medicine.dose} ${medicine.unit}")
                    if (medicine.note.isNotBlank()) {
                        Text(medicine.note, style = MaterialTheme.typography.bodySmall)
                    }
                    if (record == null) {
                        Button(
                            onClick = {
                                viewModel.takeDose(medicine.id, date, time)
                                onWidgetRefresh()
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        ) {
                            Text(stringResource(R.string.take))
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
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(medicine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("${medicine.dose} ${medicine.unit} · ${stringResource(R.string.remaining_stock, medicine.stock, medicine.lowStock)}")
                        }
                        FilledTonalButton(
                            onClick = {
                                viewModel.takeAsNeeded(medicine.id)
                                onWidgetRefresh()
                            }
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
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.plannedTime, fontWeight = FontWeight.SemiBold)
                            Text(name)
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

@Composable
private fun HistoryScreen(
    viewModel: DoseBloomViewModel,
    medicines: List<Medicine>,
    onWidgetRefresh: () -> Unit,
    modifier: Modifier
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
    }.collectAsStateWithLifecycle()
    val recordDates = remember(records) { records.map { it.date }.toSet() }
    val locale = LocalConfiguration.current.locales[0]

    Column(modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.history), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                month = (month.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                selectedDate = Schedule.dateKey(month)
            }) {
                Text("‹", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = {
                month = (month.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                selectedDate = Schedule.dateKey(month)
            }) {
                Text("›", style = MaterialTheme.typography.titleLarge)
            }
        }
        val monthName = remember(year, monthIndex, locale) {
            SimpleDateFormat("LLLL yyyy", locale).format(month.time).replaceFirstChar { it.uppercase(locale) }
        }
        Text(monthName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        MonthCalendar(month, selectedDate, recordDates) { selectedDate = it }
        Spacer(Modifier.height(12.dp))
        val selectedRecords = records.filter { it.date == selectedDate }
        Text(selectedDate.replace('-', '.'), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (selectedRecords.isEmpty()) {
            InfoCard(stringResource(R.string.no_records_for_day))
        } else {
            selectedRecords.forEach { record ->
                val name = medicines.firstOrNull { it.id == record.medicineId }?.name ?: stringResource(R.string.medicine_fallback)
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.plannedTime, fontWeight = FontWeight.SemiBold)
                            Text(name)
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

@Composable
private fun MonthCalendar(
    month: Calendar,
    selectedDate: String,
    recordDates: Set<String>,
    onDateSelected: (String) -> Unit
) {
    val weekdays = listOf(
        R.string.days_mon, R.string.days_tue, R.string.days_wed,
        R.string.days_thu, R.string.days_fri, R.string.days_sat, R.string.days_sun
    ).map { stringResource(it) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        weekdays.forEach {
            Text(
                it,
                Modifier.weight(1f).padding(vertical = 4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
    val first = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val days = month.getActualMaximum(Calendar.DAY_OF_MONTH)
    val total = ((offset + days + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (weekStart in 0 until total step 7) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (cell in weekStart until weekStart + 7) {
                    val day = cell - offset + 1
                    if (day !in 1..days) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        val key = Schedule.dateKey(date)
                        val selected = key == selectedDate
                        val hasRecord = key in recordDates
                        Surface(
                            onClick = { onDateSelected(key) },
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(day.toString(), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                Text(
                                    if (hasRecord) "•" else " ",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicinesScreen(
    viewModel: DoseBloomViewModel,
    medicines: List<Medicine>,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onWidgetRefresh: () -> Unit,
    activity: RefactoredMainActivity,
    modifier: Modifier,
    onAdd: () -> Unit,
    onEdit: (Medicine) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.medicines), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onExport, Modifier.weight(1f)) {
                        Text(stringResource(R.string.export))
                    }
                    OutlinedButton(onClick = onImport, Modifier.weight(1f)) {
                        Text(stringResource(R.string.import_data))
                    }
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = onAdd, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.add_medicine))
                }
            }
        }
        if (medicines.isEmpty()) {
            item { InfoCard(stringResource(R.string.medicines_empty)) }
        }
        items(medicines, key = { it.id }) { medicine ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(medicine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("${medicine.dose} ${medicine.unit}")
                    if (!medicine.asNeeded) {
                        Text(medicine.times.joinToString(", "))
                    }
                    Text(stringResource(R.string.remaining_stock, medicine.stock, medicine.lowStock))
                    if (medicine.stock <= medicine.lowStock) {
                        Text(stringResource(R.string.low_stock), color = MaterialTheme.colorScheme.error)
                    }
                    if (medicine.asNeeded) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.takeAsNeeded(medicine.id)
                                onWidgetRefresh()
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(stringResource(R.string.take_as_needed))
                        }
                    }
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        TextButton(onClick = { onEdit(medicine) }) {
                            Text(stringResource(R.string.edit))
                        }
                        TextButton(onClick = {
                            viewModel.deleteMedicine(medicine.id)
                            Scheduler.rescheduleAll(activity)
                            onWidgetRefresh()
                        }) {
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
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
        title = {
            Text(if (existing == null) stringResource(R.string.new_medicine) else stringResource(R.string.edit_medicine))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    dose,
                    { dose = it },
                    label = { Text(stringResource(R.string.dose)) },
                    singleLine = true
                )
                OutlinedTextField(
                    unit,
                    { unit = it },
                    label = { Text(stringResource(R.string.unit)) },
                    singleLine = true
                )
                OutlinedTextField(
                    times,
                    { times = it },
                    enabled = !asNeeded,
                    label = { Text(stringResource(R.string.time_example)) },
                    singleLine = true
                )
                OutlinedTextField(
                    start,
                    { start = it },
                    label = { Text(stringResource(R.string.start_date)) },
                    singleLine = true
                )
                OutlinedTextField(
                    end,
                    { end = it },
                    label = { Text(stringResource(R.string.end_date)) },
                    singleLine = true
                )
                OutlinedTextField(
                    stock,
                    { stock = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.stock)) },
                    singleLine = true
                )
                OutlinedTextField(
                    low,
                    { low = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.low_stock_threshold)) },
                    singleLine = true
                )
                OutlinedTextField(
                    note,
                    { note = it },
                    label = { Text(stringResource(R.string.note)) }
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.as_needed))
                    Switch(asNeeded, { asNeeded = it })
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ProfileDialog(
    activity: RefactoredMainActivity,
    profiles: List<String>,
    current: String,
    onSelect: (String) -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onSelect(current) },
        title = { Text(stringResource(R.string.profile)) },
        text = {
            Column {
                profiles.forEach { p ->
                    val displayName = Localization.profileDisplayName(activity, p)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onSelect(p) }, Modifier.weight(1f)) {
                            Text(if (p == current) "✓ $displayName" else displayName)
                        }
                        if (p != "Я") {
                            TextButton(onClick = { onDelete(p) }) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                    }
                }
                HorizontalDivider()
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text(stringResource(R.string.new_profile)) },
                    singleLine = true
                )
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = { onAdd(name); name = "" }
                ) {
                    Text(stringResource(R.string.add_and_select))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(current) }) {
                Text(stringResource(R.string.done))
            }
        }
    )
}

@Composable
private fun SettingsDialog(
    dark: Boolean,
    language: String,
    onDarkMode: (Boolean) -> Unit,
    onLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.dark_theme))
                    Switch(dark, onDarkMode)
                }
                HorizontalDivider()
                val currentLangLabel = when (language) {
                    Localization.RUSSIAN -> stringResource(R.string.russian)
                    Localization.ENGLISH -> stringResource(R.string.english)
                    else -> stringResource(R.string.system_default)
                }
                Text("${stringResource(R.string.language)}: $currentLangLabel")
                OutlinedButton(onClick = { onLanguage(Localization.SYSTEM) }, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.system_default))
                }
                OutlinedButton(onClick = { onLanguage(Localization.RUSSIAN) }, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.russian))
                }
                OutlinedButton(onClick = { onLanguage(Localization.ENGLISH) }, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.english))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        }
    )
}

@Composable
private fun StatusPill(status: String?) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(50)
    ) {
        val label = when (status) {
            "TAKEN" -> stringResource(R.string.status_taken)
            "SKIPPED" -> stringResource(R.string.status_skipped)
            else -> stringResource(R.string.planned)
        }
        Text(
            label,
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(Modifier.fillMaxWidth()) {
        Text(text, Modifier.padding(18.dp))
    }
}

private fun validDate(value: String): Boolean = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)
}.isSuccess
