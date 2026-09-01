package com.dosebloom.app.data

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DoseBloomMigrationTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DoseBloomDatabase::class.java
    )

    @Test
    fun migrate2To3PreservesDataAndConstraints() {
        helper.createDatabase("dosebloom.db", 2).apply {
            execSQL("INSERT INTO profiles(name) VALUES('Я')")
            execSQL("INSERT INTO profiles(name) VALUES('Мама')")
            execSQL("INSERT INTO medicines(name,dose,unit,times,startDate,endDate,note,stock,lowStock,asNeeded,profile) VALUES('Vitamin D','1','таблетка','08:00','2026-09-01','', '',10,2,0,'Я')")
            execSQL("INSERT INTO intakes(medicineId,date,plannedTime,actualMillis,status) VALUES(1,'2026-09-01','08:00',1000,'TAKEN')")
            close()
        }

        val db = helper.runMigrationsAndValidate("dosebloom.db", 3, true, DoseBloomDatabase.MIGRATION_2_3)
        db.query("SELECT COUNT(*) FROM medicines").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM intakes").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT name FROM profiles ORDER BY id").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Я", cursor.getString(0))
            assertTrue(cursor.moveToNext())
            assertEquals("Мама", cursor.getString(0))
        }
        db.close()
        context.deleteDatabase("dosebloom.db")
    }
}
