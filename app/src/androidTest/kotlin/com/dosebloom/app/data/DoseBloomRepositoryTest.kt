package com.dosebloom.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dosebloom.app.Medicine
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DoseBloomRepositoryTest {
    private lateinit var db: DoseBloomDatabase
    private lateinit var repository: DoseBloomRepository
    @Before fun setUp() { val context = ApplicationProvider.getApplicationContext<Context>(); db = Room.inMemoryDatabaseBuilder(context, DoseBloomDatabase::class.java).build(); repository = DoseBloomRepository(db) }
    @After fun tearDown() { db.close() }
    @Test fun takeDoseIsIdempotentAndDecrementsOnce() = runBlocking {
        val id = repository.addMedicine(Medicine(0, "Test", "1", "таблетка", listOf("08:00"), "2026-09-01", "", "", 3, 1, false, "Я"))
        assertTrue(repository.takeDose(id, "2026-09-01", "08:00"))
        assertFalse(repository.takeDose(id, "2026-09-01", "08:00"))
        assertEquals(2, repository.findMedicine(id)?.stock)
        assertEquals(1, db.intakeDao().forDate("2026-09-01").size)
    }

    @Test fun undoDoseRestoresStockAndRemovesIntake() = runBlocking {
        val id = repository.addMedicine(Medicine(0, "Test", "1", "таблетка", listOf("08:00"), "2026-09-01", "", "", 3, 1, false, "Я"))
        repository.takeDose(id, "2026-09-01", "08:00")
        assertEquals(2, repository.findMedicine(id)?.stock)
        
        assertTrue(repository.undoDose(id, "2026-09-01", "08:00"))
        assertEquals(3, repository.findMedicine(id)?.stock)
        assertEquals(0, db.intakeDao().forDate("2026-09-01").size)
    }

    @Test fun takeAsNeededSupportsMultipleInSameMinute() = runBlocking {
        val id = repository.addMedicine(Medicine(0, "Test PRN", "1", "таблетка", listOf(), "2026-09-01", "", "", 10, 1, true, "Я"))
        assertTrue(repository.takeAsNeeded(id, "2026-09-01", "14:30"))
        assertTrue(repository.takeAsNeeded(id, "2026-09-01", "14:30"))
        
        assertEquals(8, repository.findMedicine(id)?.stock)
        val intakes = db.intakeDao().forDate("2026-09-01")
        assertEquals(2, intakes.size)
        assertTrue(intakes.any { it.plannedTime == "14:30" })
        assertTrue(intakes.any { it.plannedTime == "14:30 (2)" })
    }

    @Test fun restockIncrementsStock() = runBlocking {
        val id = repository.addMedicine(Medicine(0, "Test", "1", "таблетка", listOf("08:00"), "2026-09-01", "", "", 3, 1, false, "Я"))
        repository.restock(id, 10)
        assertEquals(13, repository.findMedicine(id)?.stock)
    }
}
