package com.dosebloom.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dosebloom.app.Medicine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileRepositoryTest {
    private lateinit var repository: DoseBloomRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, DoseBloomDatabase::class.java).build()
        repository = DoseBloomRepository(database)
    }

    @Test
    fun removingProfileMovesMedicinesToDefault() = runBlocking {
        repository.addProfile("Мама")
        repository.addMedicine(Medicine(name = "Test", dose = "1", unit = "таблетка", times = listOf("08:00"), startDate = "2026-09-01", endDate = "", note = "", stock = 1, lowStock = 0, asNeeded = false, profile = "Мама"))
        repository.removeProfile("Мама")
        assertEquals(listOf("Я"), repository.observeProfiles().let { itValue -> kotlinx.coroutines.flow.first(itValue) })
        assertEquals("Я", repository.observeAllMedicines().let { kotlinx.coroutines.flow.first(it) }.single().profile)
    }
}
