package com.dosebloom.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.dosebloom.app.data.DoseBloomDatabase
import com.dosebloom.app.data.DoseBloomRepository
import com.dosebloom.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class NextDoseWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }

    companion object {
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val component = android.content.ComponentName(app, NextDoseWidget::class.java)
            manager.getAppWidgetIds(component).forEach { update(app, manager, it) }
        }

        fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val app = context.applicationContext
            widgetScope.launch {
                val repository = DoseBloomRepository(DoseBloomDatabase.get(app))
                val profile = SettingsRepository(app).selectedProfileOnce()
                val medicineList = repository.observeAllMedicines().first().filter { it.profile == profile }
                val now = Calendar.getInstance()
                var found: Pair<Medicine, String>? = null
                var foundMillis = Long.MAX_VALUE
                for (offset in 0..7) {
                    val day = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
                    val date = Schedule.dateKey(day)
                    for ((medicine, time) in Schedule.events(medicineList, date)) {
                        val p = time.split(":")
                        val alarm = (day.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, p[0].toInt())
                            set(Calendar.MINUTE, p[1].toInt())
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (alarm.timeInMillis >= System.currentTimeMillis() && alarm.timeInMillis < foundMillis) {
                            foundMillis = alarm.timeInMillis
                            found = medicine to time
                        }
                    }
                }
                val views = RemoteViews(app.packageName, R.layout.widget_next_dose)
                val item = found
                if (item == null) {
                    views.setTextViewText(R.id.widget_title, app.getString(R.string.app_name))
                    views.setTextViewText(R.id.widget_body, app.getString(R.string.no_upcoming_doses))
                } else {
                    views.setTextViewText(R.id.widget_title, app.getString(R.string.next_dose))
                    views.setTextViewText(R.id.widget_body, "${item.second} • ${item.first.name}")
                }
                manager.updateAppWidget(id, views)
            }
        }
    }
}
