package com.dosebloom.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale

class RefactoredMainActivity : ComponentActivity() {
    private val viewModel: DoseBloomViewModel by viewModels()
    private var pendingExport: (Uri) -> Unit = {}
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> uri?.let(pendingExport) }
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching { ExportImport.import(this, uri) }.onSuccess { Scheduler.rescheduleAll(this); refreshWidget() }
    }

    override fun attachBaseContext(newBase: Context) {
        val language = newBase.getSharedPreferences("dosebloom_localization", Context.MODE_PRIVATE).getString("language", "system") ?: "system"
        if (language == "system") super.attachBaseContext(newBase) else {
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(Locale.forLanguageTag(language))
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        requestNotificationPermission()
        Scheduler.rescheduleAll(this)
        setContent { DoseBloomScreen(this, viewModel, ::exportData, ::importData, ::refreshWidget) }
    }

    private fun exportData() {
        pendingExport = { uri -> ExportImport.export(this, uri) }
        exportLauncher.launch("DoseBloom-${Schedule.todayKey()}.json")
    }

    private fun importData() = importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))

    private fun refreshWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val component = android.content.ComponentName(this, NextDoseWidget::class.java)
        manager.getAppWidgetIds(component).forEach { id -> NextDoseWidget.update(this, manager, id) }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("dosebloom_reminders", getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH)
        )
    }
}
