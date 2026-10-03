package com.dosebloom.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseBloomUiStateTest {

    @Test
    fun defaultUiStateInitializesCorrectly() {
        val state = DoseBloomUiState()
        assertEquals("Я", state.selectedProfile)
        assertFalse(state.darkMode)
        assertEquals("system", state.language)
        assertTrue(state.medicines.isEmpty())
        assertEquals(listOf("Я"), state.profiles)
    }

    @Test
    fun medicineModelPreservesProperties() {
        val medicine = Medicine(
            id = 42L,
            name = "Paracetamol",
            dose = "500",
            unit = "mg",
            times = listOf("08:00", "20:00"),
            startDate = "2026-10-01",
            endDate = "2026-10-05",
            note = "After meals",
            stock = 20,
            lowStock = 4,
            asNeeded = false,
            profile = "Family"
        )

        assertEquals(42L, medicine.id)
        assertEquals("Paracetamol", medicine.name)
        assertEquals(2, medicine.times.size)
        assertEquals(20, medicine.stock)
        assertFalse(medicine.asNeeded)
    }

    @Test
    fun intakeModelPreservesProperties() {
        val intake = Intake(
            id = 100L,
            medicineId = 42L,
            date = "2026-10-04",
            plannedTime = "08:00",
            actualMillis = 1728000000000L,
            status = "TAKEN"
        )

        assertEquals(100L, intake.id)
        assertEquals(42L, intake.medicineId)
        assertEquals("TAKEN", intake.status)
    }
}
