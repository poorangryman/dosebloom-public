package com.dosebloom.app.domain

import com.dosebloom.app.Medicine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScheduleCalculatorTest {
    private fun medicine(
        startDate: String = "2026-09-01",
        endDate: String = "",
        times: List<String> = listOf("20:00", "08:00", "14:00"),
        asNeeded: Boolean = false
    ) = Medicine(
        name = "Test",
        dose = "1",
        unit = "таблетка",
        times = times,
        startDate = startDate,
        endDate = endDate,
        note = "",
        stock = 10,
        lowStock = 2,
        asNeeded = asNeeded,
        profile = "Я"
    )

    @Test fun sortsValidTimes() {
        val events = ScheduleCalculator.events(listOf(medicine()), LocalDate.of(2026, 9, 1))
        assertEquals(listOf("08:00", "14:00", "20:00"), events.map { it.time.toString() })
    }

    @Test fun excludesAsNeededMedicines() {
        assertTrue(ScheduleCalculator.events(listOf(medicine(asNeeded = true)), LocalDate.of(2026, 9, 1)).isEmpty())
    }

    @Test fun excludesBeforeStart() {
        assertTrue(ScheduleCalculator.events(listOf(medicine(startDate = "2026-09-02")), LocalDate.of(2026, 9, 1)).isEmpty())
    }

    @Test fun excludesAfterEnd() {
        assertTrue(ScheduleCalculator.events(listOf(medicine(endDate = "2026-09-01")), LocalDate.of(2026, 9, 2)).isEmpty())
    }

    @Test fun acceptsBoundaryDates() {
        val m = medicine(startDate = "2026-09-01", endDate = "2026-09-02")
        assertFalse(ScheduleCalculator.events(listOf(m), LocalDate.of(2026, 9, 1)).isEmpty())
        assertFalse(ScheduleCalculator.events(listOf(m), LocalDate.of(2026, 9, 2)).isEmpty())
    }

    @Test fun rejectsInvalidTime() {
        assertFalse(ScheduleCalculator.isValidTime("25:00"))
        assertFalse(ScheduleCalculator.isValidTime("12:60"))
        assertTrue(ScheduleCalculator.isValidTime("23:59"))
    }
}
