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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.saveable.rememberSaveable
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
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
fun DoseBloomScreen(activity: RefactoredMainActivity, viewModel: DoseBloomViewModel, onExport: () -> Unit, onImport: () -> Unit, onWidgetRefresh: () -> Unit) {
    val dark by viewModel.darkMode.collectAsStateWithLifecycle()
    MaterialTheme(colorScheme = if (dark) DoseDark else DoseLight) { DoseBloomContent(activity, viewModel, onExport, onImport, onWidgetRefresh) }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
private fun DoseBloomContent(activity: RefactoredMainActivity, viewModel: DoseBloomViewModel, onExport: () -> Unit, onImport: () -> Unit, onWidgetRefresh: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editorId by rememberSaveable { mutableLongStateOf(-1L) }
    var addMedicine by rememberSaveable { mutableStateOf(false) }
    var profileDialog by rememberSaveable { mutableStateOf(false) }
    var settingsDialog by rememberSaveable { mutableStateOf(false) }
    val profile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val editor = medicines.firstOrNull { it.id == editorId }

    NavigationSuiteScaffold(
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteItems = {
            item(selected = tab == 0, onClick = { tab = 0 }, icon = { Text("●") }, label = { Text("Сегодня") })
            item(selected = tab == 1, onClick = { tab = 1 }, icon = { Text("▦") }, label = { Text("История") })
            item(selected = tab == 2, onClick = { tab = 2 }, icon = { Text("+") }, label = { Text("Лекарства") })
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("DoseBloom", fontWeight = FontWeight.SemiBold)
                            Text(profile, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    actions = {
                        TextButton(onClick = { profileDialog = true }) { Text("Профиль") }
                        TextButton(onClick = { settingsDialog = true }) { Text("⚙", style = MaterialTheme.typography.titleLarge) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                    AnimatedContent(targetState = tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { selectedTab ->
                        when (selectedTab) {
                            0 -> TodayScreen(viewModel, Modifier.fillMaxSize().padding(horizontal = 16.dp))
                            1 -> HistoryScreen(viewModel, Modifier.fillMaxSize().padding(horizontal = 16.dp))
                            else -> MedicinesScreen(viewModel, Modifier.fillMaxSize().padding(horizontal = 16.dp), onAdd = { addMedicine = true }, onEdit = { editorId = it })
                        }
                    }
                }
            }
        }
    }

    if (profileDialog) ProfileDialog(viewModel, onDismiss = { profileDialog = false })
    if (settingsDialog) SettingsDialog(viewModel, onDismiss = { settingsDialog = false })
    if (addMedicine) MedicineEditor(viewModel, medicine = null, onDismiss = { addMedicine = false })
    if (editor != null) MedicineEditor(viewModel, medicine = editor, onDismiss = { editorId = -1L })
}
