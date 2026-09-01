package com.dosebloom.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MedicineEntity::class, IntakeEntity::class, ProfileEntity::class], version = 3, exportSchema = false)
abstract class DoseBloomDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun intakeDao(): IntakeDao
    abstract fun profileDao(): ProfileDao

    companion object {
        private const val DATABASE_NAME = "dosebloom.db"
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE medicines_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, dose TEXT NOT NULL, unit TEXT NOT NULL, times TEXT NOT NULL, startDate TEXT NOT NULL, endDate TEXT NOT NULL, note TEXT NOT NULL, stock INTEGER NOT NULL, lowStock INTEGER NOT NULL, asNeeded INTEGER NOT NULL, profile TEXT NOT NULL)")
                db.execSQL("INSERT INTO medicines_new SELECT id,name,dose,unit,times,startDate,endDate,note,stock,lowStock,asNeeded,profile FROM medicines")
                db.execSQL("DROP TABLE medicines")
                db.execSQL("ALTER TABLE medicines_new RENAME TO medicines")
                db.execSQL("CREATE TABLE intakes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, medicineId INTEGER NOT NULL, date TEXT NOT NULL, plannedTime TEXT NOT NULL, actualMillis INTEGER NOT NULL, status TEXT NOT NULL)")
                db.execSQL("INSERT INTO intakes_new SELECT id,medicineId,date,plannedTime,actualMillis,status FROM intakes")
                db.execSQL("DROP TABLE intakes")
                db.execSQL("ALTER TABLE intakes_new RENAME TO intakes")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_intakes_medicineId_date_plannedTime ON intakes(medicineId,date,plannedTime)")
                db.execSQL("CREATE TABLE profiles_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
                db.execSQL("INSERT INTO profiles_new SELECT id,name FROM profiles")
                db.execSQL("DROP TABLE profiles")
                db.execSQL("ALTER TABLE profiles_new RENAME TO profiles")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_profiles_name ON profiles(name)")
            }
        }

        @Volatile private var instance: DoseBloomDatabase? = null
        fun get(context: Context): DoseBloomDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, DoseBloomDatabase::class.java, DATABASE_NAME).addMigrations(MIGRATION_2_3).build().also { instance = it }
        }
    }
}
