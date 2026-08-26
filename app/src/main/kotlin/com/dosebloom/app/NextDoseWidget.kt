package com.dosebloom.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.util.Calendar

class NextDoseWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }
    companion object {
        fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val db = Db(context)
            val meds = db.medicines()
            val now = Calendar.getInstance()
            var found: Pair<Medicine, String>? = null
            var foundMillis = Long.MAX_VALUE
            for (offset in 0..7) {
                val day = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
                val date = Schedule.dateKey(day)
                for ((medicine, time) in Schedule.events(meds, date)) {
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
            val views = RemoteViews(context.packageName, R.layout.widget_next_dose)
            if (found == null) {
                views.setTextViewText(R.id.widget_title, "DoseBloom")
                views.setTextViewText(R.id.widget_body, "Нет ближайших приёмов")
            } else {
                views.setTextViewText(R.id.widget_title, "Следующий приём")
                views.setTextViewText(R.id.widget_body, "${found!!.second} • ${found!!.first.name}")
            }
            manager.updateAppWidget(id, views)
        }
    }
}
