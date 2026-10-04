package com.dosebloom.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    DoseBloomTheme(darkTheme = dark) {
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
