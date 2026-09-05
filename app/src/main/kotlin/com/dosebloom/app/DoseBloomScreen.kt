package com.dosebloom.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val DoseLight = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF2A6B55),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6EFE3),
    onPrimaryContainer = Color(0xFF04281B),
    secondary = Color(0xFF4C6357),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9D9),
    onSecondaryContainer = Color(0xFF092016),
    tertiary = Color(0xFF3B6470),
    background = Color(0xFFF7F9F8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFEDF2EE),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFDCE3DF)
)

private val DoseDark = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF7FD1A7),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF124E38),
    onPrimaryContainer = Color(0xFFA1F2C5),
    secondary = Color(0xFFB3CCBE),
    onSecondary = Color(0xFF1E352A),
    secondaryContainer = Color(0xFF354B40),
    onSecondaryContainer = Color(0xFFCEE9D9),
    tertiary = Color(0xFFA2CDE0),
    background = Color(0xFF111513),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF181E1B),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF232B27),
    onSurfaceVariant = Color(0xFFC0C9C2),
    outline = Color(0xFF8A938D),
    outlineVariant = Color(0xFF343D37)
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
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_nav_today),
                        contentDescription = stringResource(R.string.today),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(stringResource(R.string.today)) }
            )
            item(
                selected = tab == 1,
                onClick = { tab = 1 },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_nav_history),
                        contentDescription = stringResource(R.string.history),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(stringResource(R.string.history)) }
            )
            item(
                selected = tab == 2,
                onClick = { tab = 2 },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_nav_medicines),
                        contentDescription = stringResource(R.string.medicines),
                        modifier = Modifier.size(24.dp)
                    )
                },
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
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        Surface(
                            onClick = { profileDialog = true },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_profile),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    Localization.profileDisplayName(activity, profile),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        IconButton(onClick = { settingsDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = stringResource(R.string.settings),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                    contentDescription = null,
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
                    contentDescription = null,
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    val first = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val days = month.getActualMaximum(Calendar.DAY_OF_MONTH)
    val total = ((offset + days + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (weekStart in 0 until total step 7) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            shape = RoundedCornerShape(12.dp),
                            border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    day.toString(),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    if (hasRecord) "•" else " ",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
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
                                Text("×", style = MaterialTheme.typography.titleLarge)
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
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        low,
                        { low = it.filter(Char::isDigit) },
                        label = { Text(stringResource(R.string.low_stock_threshold)) },
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
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.profile), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                profiles.forEach { p ->
                    val displayName = Localization.profileDisplayName(activity, p)
                    Surface(
                        onClick = { onSelect(p) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (p == current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_profile),
                                contentDescription = null,
                                tint = if (p == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                            )
                            Text(
                                displayName,
                                Modifier.weight(1f),
                                fontWeight = if (p == current) FontWeight.Bold else FontWeight.Normal
                            )
                            if (p != "Я") {
                                IconButton(onClick = { onDelete(p) }, modifier = Modifier.size(28.dp)) {
                                    Text("×", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text(stringResource(R.string.new_profile)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    enabled = name.isNotBlank(),
                    onClick = { onAdd(name); name = "" },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
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
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.dark_theme), fontWeight = FontWeight.Medium)
                        Switch(dark, onDarkMode)
                    }
                }
                HorizontalDivider()
                val currentLangLabel = when (language) {
                    Localization.RUSSIAN -> stringResource(R.string.russian)
                    Localization.ENGLISH -> stringResource(R.string.english)
                    else -> stringResource(R.string.system_default)
                }
                Text(
                    "${stringResource(R.string.language)}: $currentLangLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(
                    onClick = { onLanguage(Localization.SYSTEM) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.system_default))
                }
                OutlinedButton(
                    onClick = { onLanguage(Localization.RUSSIAN) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.russian))
                }
                OutlinedButton(
                    onClick = { onLanguage(Localization.ENGLISH) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
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
    val isTaken = status == "TAKEN"
    val isSkipped = status == "SKIPPED"
    val bgColor = when {
        isTaken -> if (MaterialTheme.colorScheme.background.red < 0.5f) Color(0xFF1B3824) else Color(0xFFE8F5E9)
        isSkipped -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    }
    val contentColor = when {
        isTaken -> if (MaterialTheme.colorScheme.background.red < 0.5f) Color(0xFFA5D6A7) else Color(0xFF1B5E20)
        isSkipped -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val label = when {
        isTaken -> stringResource(R.string.status_taken)
        isSkipped -> stringResource(R.string.status_skipped)
        else -> stringResource(R.string.planned)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isTaken) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(text, Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun validDate(value: String): Boolean = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)
}.isSuccess
