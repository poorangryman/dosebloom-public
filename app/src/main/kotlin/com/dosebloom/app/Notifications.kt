package com.dosebloom.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("medicineId", -1)
        val time = intent.getStringExtra("time") ?: return
        val date = intent.getStringExtra("date") ?: return
        val medicine = Db(context).medicines().firstOrNull { it.id == id } ?: return
        val nm = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel("dosebloom_reminders", context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH))
        }
        val n = NotificationCompat.Builder(context, "dosebloom_reminders")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText("${medicine.name} — ${medicine.dose} ${medicine.unit}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.notification_taken), action(context, "TAKE", id, time, date, 1))
            .addAction(0, context.getString(R.string.notification_postpone), action(context, "POSTPONE", id, time, date, 2))
            .addAction(0, context.getString(R.string.notification_skip), action(context, "SKIP", id, time, date, 3))
            .build()
        nm.notify(("$id|$date|$time").hashCode(), n)
        Scheduler.rescheduleAll(context)
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
        val id = intent.getLongExtra("medicineId", -1)
        val time = intent.getStringExtra("time") ?: return
        val date = intent.getStringExtra("date") ?: return
        val db = Db(context)
        if (!db.medicines().any { it.id == id }) return
        when (intent.action) {
            "TAKE" -> if (!db.hasIntake(id, date, time)) {
                db.addIntake(id, date, time, "TAKEN")
                db.decreaseStock(id)
            }
            "SKIP" -> if (!db.hasIntake(id, date, time)) db.addIntake(id, date, time, "SKIPPED")
            "POSTPONE" -> Scheduler.postpone(context, id, time, date)
        }
        context.getSystemService(NotificationManager::class.java).cancel(("$id|$date|$time").hashCode())
        if (intent.action != "POSTPONE") Scheduler.rescheduleAll(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            Scheduler.rescheduleAll(context)
        }
    }
}
