package com.dosebloom.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    dark: Boolean,
    language: String,
    onDarkMode: (Boolean) -> Unit,
    onLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var availableUpdate by remember { mutableStateOf<UpdateCheckResult.UpdateAvailable?>(null) }
    val downloadState by UpdateManager.downloadState.collectAsStateWithLifecycle()

    var isBatteryIgnoring by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    val currentVersionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "2.0.1"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                HorizontalDivider()

                // Battery Optimization Section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.battery_optimization_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isBatteryIgnoring) {
                                stringResource(R.string.battery_optimization_disabled)
                            } else {
                                stringResource(R.string.battery_optimization_enabled)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isBatteryIgnoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        if (!isBatteryIgnoring) {
                            OutlinedButton(
                                onClick = {
                                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                                    isBatteryIgnoring = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(stringResource(R.string.disable_battery_optimization))
                            }
                        }
                    }
                }

                HorizontalDivider()

                // In-App Updates Section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = stringResource(R.string.app_version),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "v$currentVersionName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isChecking = true
                                        statusText = null
                                        when (val result = UpdateManager.checkForUpdates(currentVersionName)) {
                                            is UpdateCheckResult.UpdateAvailable -> {
                                                availableUpdate = result
                                                statusText = null
                                            }
                                            is UpdateCheckResult.UpToDate -> {
                                                availableUpdate = null
                                                statusText = context.getString(R.string.up_to_date)
                                            }
                                            is UpdateCheckResult.Error -> {
                                                availableUpdate = null
                                                statusText = "${context.getString(R.string.update_failed)}: ${result.message}"
                                            }
                                        }
                                        isChecking = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isChecking,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isChecking) stringResource(R.string.checking_updates) else stringResource(R.string.check_updates),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        if (statusText != null) {
                            Text(
                                statusText!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (availableUpdate != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "${stringResource(R.string.update_available)}: v${availableUpdate!!.version}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (availableUpdate!!.notes.isNotEmpty()) {
                                        Text(
                                            availableUpdate!!.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                            maxLines = 4
                                        )
                                    }

                                    when (val state = downloadState) {
                                        is DownloadState.Downloading -> {
                                            Text(
                                                stringResource(R.string.update_downloading),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        is DownloadState.ReadyToInstall -> {
                                            Button(
                                                onClick = {
                                                    UpdateManager.installApk(context, state.file)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(stringResource(R.string.install_update))
                                            }
                                        }
                                        else -> {
                                            Button(
                                                onClick = {
                                                    UpdateManager.startDownloadAndInstall(
                                                        context,
                                                        availableUpdate!!.downloadUrl,
                                                        availableUpdate!!.version
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(stringResource(R.string.download_and_install))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
