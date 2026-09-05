package com.dosebloom.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Schedule {
    private val fmt = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    private val timeFmt = ThreadLocal.withInitial { SimpleDateFormat("HH:mm", Locale.US) }

    fun dateKey(date: Date): String = fmt.get().format(date)
    fun dateKey(calendar: Calendar): String = fmt.get().format(calendar.time)
    fun parseDate(key: String): Calendar = Calendar.getInstance().apply {
        time = runCatching { fmt.get().parse(key) }.getOrNull() ?: Date()
    }
    fun todayKey(): String = dateKey(Date())
    fun eligible(m: Medicine, calendar: Calendar): Boolean {
        val d = dateKey(calendar)
        return d >= m.startDate && (m.endDate.isBlank() || d <= m.endDate)
    }
    fun events(medicines: List<Medicine>, date: String): List<Pair<Medicine, String>> {
        val cal = parseDate(date)
        return medicines.filter { !it.asNeeded && eligible(it, cal) }
            .flatMap { m -> m.times.map { m to it } }
            .sortedBy { it.second }
    }
    fun validTime(s: String): Boolean = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$").matches(s)
    fun normalizeTime(s: String): String {
        val trimmed = s.trim()
        val parts = trimmed.split(":")
        if (parts.size == 2 && parts[0].length == 1 && parts[0].all(Char::isDigit) && parts[1].length == 2 && parts[1].all(Char::isDigit)) {
            return "0${parts[0]}:${parts[1]}"
        }
        return trimmed
    }
    fun monthDays(year: Int, month: Int): List<Calendar> {
        val first = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val start = first.clone() as Calendar
        val dow = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
        start.add(Calendar.DAY_OF_MONTH, -dow)
        return (0 until 42).map { (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, it) } }
    }
    fun nowTime(): String = timeFmt.get().format(Date())
}
