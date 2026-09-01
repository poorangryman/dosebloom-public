package com.dosebloom.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    private lateinit var database: DoseBloomDatabase
    private lateinit var repository: DoseBloomRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DoseBloomDatabase::class.java).build()
        repository = DoseBloomRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun takeDoseIsAtomicAndIdempotent() = runBlocking {
        val id = repository.addMedicine(
            com.dosebloom.app.Medicine(
                name = "Test",
                dose = "1",
                unit = "таблетка",
                times = listOf("08:00"),
                startDate = "2026-09-01",
                endDate = "",
                note = "",
                stock = 3,
                lowStock = 1,
                asNeeded = false,
                profile = "Я"
            )
        )

        assertTrue(repository.takeDose(id, "2026-09-01", "08:00"))
        assertFalse(repository.takeDose(id, "2026-09-01", "08:00"))

        val medicine = repository.findMedicine(id)
        assertEquals(2, medicine?.stock)
        val intake = database.intakeDao().forDate("2026-09-01")
        assertEquals(1, intake.size)
        assertEquals("TAKEN", intake.single().status)
    }
}
