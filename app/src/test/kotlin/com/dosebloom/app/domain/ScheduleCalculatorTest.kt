package com.dosebloom.app.domain

import com.dosebloom.app.Medicine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScheduleCalculatorTest {
    private fun medicine(start: String = "2026-09-01", end: String = "", times: List<String> = listOf("20:00", "08:00", "14:00"), asNeeded: Boolean = false) = Medicine(0, "Test", "1", "таблетка", times, start, end, "", 10, 2, asNeeded, "Я")

    @Test fun sortsTimes() { assertEquals(listOf("08:00", "14:00", "20:00"), ScheduleCalculator.events(listOf(medicine()), LocalDate.of(2026, 9, 1)).map { it.time.toString() }) }
    @Test fun excludesAsNeeded() { assertTrue(ScheduleCalculator.events(listOf(medicine(asNeeded = true)), LocalDate.of(2026, 9, 1)).isEmpty()) }
    @Test fun respectsDateRange() { val m = medicine(end = "2026-09-02"); assertTrue(ScheduleCalculator.events(listOf(m), LocalDate.of(2026, 8, 31)).isEmpty()); assertFalse(ScheduleCalculator.events(listOf(m), LocalDate.of(2026, 9, 2)).isEmpty()); assertTrue(ScheduleCalculator.events(listOf(m), LocalDate.of(2026, 9, 3)).isEmpty()) }
    @Test fun validatesTime() { assertTrue(ScheduleCalculator.isValidTime("23:59")); assertFalse(ScheduleCalculator.isValidTime("25:00")); assertFalse(ScheduleCalculator.isValidTime("12:60")) }
}
