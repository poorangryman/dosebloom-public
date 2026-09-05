package com.dosebloom.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dosebloom.app.data.DoseBloomDatabase
import com.dosebloom.app.data.DoseBloomRepository
import com.dosebloom.app.data.IntakeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val id = intent.getLongExtra("medicineId", -1)
        val time = intent.getStringExtra("time") ?: run { pendingResult.finish(); return }
        val date = intent.getStringExtra("date") ?: run { pendingResult.finish(); return }
        val appContext = context.applicationContext
        receiverScope.launch {
            try {
                val repository = DoseBloomRepository(DoseBloomDatabase.get(appContext))
                if (repository.hasIntake(id, date, time)) return@launch
                val medicine = repository.findMedicine(id) ?: return@launch
                val nm = appContext.getSystemService(NotificationManager::class.java)
                if (Build.VERSION.SDK_INT >= 26) {
                    nm.createNotificationChannel(NotificationChannel("dosebloom_reminders", appContext.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH))
                }
                val contentIntent = PendingIntent.getActivity(
                    appContext,
                    0,
                    Intent(appContext, RefactoredMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val notification = NotificationCompat.Builder(appContext, "dosebloom_reminders")
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(appContext.getString(R.string.notification_title))
                    .setContentText("${medicine.name} — ${medicine.dose} ${medicine.unit}")
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .addAction(0, appContext.getString(R.string.notification_taken), action(appContext, "TAKE", id, time, date, 1))
                    .addAction(0, appContext.getString(R.string.notification_postpone), action(appContext, "POSTPONE", id, time, date, 2))
                    .addAction(0, appContext.getString(R.string.notification_skip), action(appContext, "SKIP", id, time, date, 3))
                    .build()
                nm.notify(("$id|$date|$time").hashCode(), notification)
                Scheduler.rescheduleAll(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun action(context: Context, action: String, id: Long, time: String, date: String, request: Int) =
        PendingIntent.getBroadcast(context, ("$id|$date|$time|$request").hashCode(), Intent(context, ActionReceiver::class.java).apply {
            this.action = action
            putExtra("medicineId", id)
            putExtra("time", time)
            putExtra("date", date)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val id = intent.getLongExtra("medicineId", -1)
        val time = intent.getStringExtra("time") ?: run { pendingResult.finish(); return }
        val date = intent.getStringExtra("date") ?: run { pendingResult.finish(); return }
        val appContext = context.applicationContext
        receiverScope.launch {
            try {
                val repository = DoseBloomRepository(DoseBloomDatabase.get(appContext))
                when (intent.action) {
                    "TAKE" -> repository.takeDose(id, date, time)
                    "SKIP" -> repository.recordIntake(id, date, time, IntakeStatus.SKIPPED)
                    "POSTPONE" -> Scheduler.postpone(appContext, id, time, date)
                }
                appContext.getSystemService(NotificationManager::class.java).cancel(("$id|$date|$time").hashCode())
                if (intent.action != "POSTPONE") Scheduler.rescheduleAll(appContext)
                NextDoseWidget.refreshAll(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            Scheduler.rescheduleAll(context.applicationContext)
            NextDoseWidget.refreshAll(context.applicationContext)
        }
    }
}
