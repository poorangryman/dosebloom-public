package com.dosebloom.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE profile = :profile ORDER BY name COLLATE NOCASE ASC")
    fun observeByProfile(profile: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun find(id: Long): MedicineEntity?

    @Query("SELECT * FROM medicines ORDER BY name COLLATE NOCASE ASC")
    suspend fun all(): List<MedicineEntity>

    @Insert
    suspend fun insert(entity: MedicineEntity): Long

    @Update
    suspend fun update(entity: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE medicines SET stock = CASE WHEN stock > 0 THEN stock - 1 ELSE 0 END WHERE id = :id")
    suspend fun decreaseStock(id: Long)
}

@Dao
interface IntakeDao {
    @Query("SELECT * FROM intakes WHERE date = :date ORDER BY plannedTime DESC")
    fun observeForDate(date: String): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE date BETWEEN :from AND :to ORDER BY date DESC, plannedTime DESC")
    fun observeBetween(from: String, to: String): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE date = :date ORDER BY plannedTime DESC")
    suspend fun forDate(date: String): List<IntakeEntity>

    @Query("SELECT * FROM intakes WHERE date BETWEEN :from AND :to ORDER BY date DESC, plannedTime DESC")
    suspend fun between(from: String, to: String): List<IntakeEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM intakes WHERE medicineId = :medicineId AND date = :date AND plannedTime = :time)")
    suspend fun exists(medicineId: Long, date: String, time: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IntakeEntity): Long

    @Query("DELETE FROM intakes WHERE medicineId = :medicineId")
    suspend fun deleteForMedicine(medicineId: Long)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT name FROM profiles ORDER BY id ASC")
    suspend fun allNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ProfileEntity): Long

    @Query("DELETE FROM profiles WHERE name = :name")
    suspend fun delete(name: String): Int
}
