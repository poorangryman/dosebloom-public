package com.dosebloom.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleTest {

    @Test
    fun validTimeAcceptsStandardFormats() {
        assertTrue(Schedule.validTime("00:00"))
        assertTrue(Schedule.validTime("08:30"))
        assertTrue(Schedule.validTime("14:45"))
        assertTrue(Schedule.validTime("23:59"))
    }

    @Test
    fun validTimeRejectsInvalidFormats() {
        assertFalse(Schedule.validTime("24:00"))
        assertFalse(Schedule.validTime("12:60"))
        assertFalse(Schedule.validTime("8:30"))
        assertFalse(Schedule.validTime("invalid"))
    }

    @Test
    fun normalizeTimePadsSingleDigitHour() {
        assertEquals("08:30", Schedule.normalizeTime("8:30"))
        assertEquals("09:00", Schedule.normalizeTime("9:00"))
        assertEquals("12:00", Schedule.normalizeTime("12:00"))
    }

    @Test
    fun eligibleRespectsStartAndEndDates() {
        val medicine = Medicine(
            id = 1,
            name = "Aspirin",
            dose = "100",
            unit = "mg",
            times = listOf("08:00"),
            startDate = "2026-10-01",
            endDate = "2026-10-10",
            note = "",
            stock = 10,
            lowStock = 2,
            asNeeded = false,
            profile = "Я"
        )

        val beforeStart = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 30) }
        val atStart = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 1) }
        val withinRange = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 5) }
        val atEnd = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10) }
        val afterEnd = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 11) }

        assertFalse(Schedule.eligible(medicine, beforeStart))
        assertTrue(Schedule.eligible(medicine, atStart))
        assertTrue(Schedule.eligible(medicine, withinRange))
        assertTrue(Schedule.eligible(medicine, atEnd))
        assertFalse(Schedule.eligible(medicine, afterEnd))
    }

    @Test
    fun monthDaysGeneratesCompleteGrid() {
        val days = Schedule.monthDays(2026, Calendar.OCTOBER)
        assertEquals(42, days.size)
    }
}
