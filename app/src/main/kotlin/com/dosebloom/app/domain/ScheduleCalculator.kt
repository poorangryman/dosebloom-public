package com.dosebloom.app.domain

import com.dosebloom.app.Medicine
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

data class ScheduledDose(
    val medicine: Medicine,
    val date: LocalDate,
    val time: LocalTime
)

object ScheduleCalculator {
    fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value, DATE_FORMATTER) }.getOrNull()

    fun parseTime(value: String): LocalTime? = try {
        LocalTime.parse(value, TIME_FORMATTER)
    } catch (_: DateTimeParseException) {
        null
    }

    fun isEligible(medicine: Medicine, date: LocalDate): Boolean {
        val start = parseDate(medicine.startDate) ?: return false
        val end = medicine.endDate.takeIf(String::isNotBlank)?.let(::parseDate)
        return !medicine.asNeeded && !date.isBefore(start) && (end == null || !date.isAfter(end))
    }

    fun events(medicines: List<Medicine>, date: LocalDate): List<ScheduledDose> = medicines
        .asSequence()
        .filter { isEligible(it, date) }
        .flatMap { medicine -> medicine.times.asSequence().mapNotNull { time -> parseTime(time)?.let { medicine to it } } }
        .sortedBy { it.second }
        .map { (medicine, time) -> ScheduledDose(medicine, date, time) }
        .toList()

    fun isValidTime(value: String): Boolean = parseTime(value) != null
}
