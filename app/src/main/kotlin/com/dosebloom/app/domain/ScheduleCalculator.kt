package com.dosebloom.app.domain

import com.dosebloom.app.Medicine
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class ScheduledDose(val medicine: Medicine, val date: LocalDate, val time: LocalTime)

object ScheduleCalculator {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()
    fun parseTime(value: String): LocalTime? = runCatching { LocalTime.parse(value, timeFormatter) }.getOrNull()
    fun isEligible(medicine: Medicine, date: LocalDate): Boolean {
        val start = parseDate(medicine.startDate) ?: return false
        val end = medicine.endDate.takeIf(String::isNotBlank)?.let(::parseDate)
        return !medicine.asNeeded && !date.isBefore(start) && (end == null || !date.isAfter(end))
    }
    fun events(medicines: List<Medicine>, date: LocalDate): List<ScheduledDose> = medicines.asSequence().filter { isEligible(it, date) }.flatMap { medicine -> medicine.times.asSequence().mapNotNull { parseTime(it)?.let { time -> ScheduledDose(medicine, date, time) } } }.sortedBy { it.time }.toList()
    fun isValidTime(value: String): Boolean = parseTime(value) != null
}
