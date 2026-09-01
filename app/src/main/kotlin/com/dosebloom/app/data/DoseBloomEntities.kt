package com.dosebloom.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dose: String,
    val unit: String,
    val times: String,
    val startDate: String,
    val endDate: String,
    val note: String,
    val stock: Int,
    val lowStock: Int,
    val asNeeded: Int,
    val profile: String
)

@Entity(tableName = "intakes", indices = [Index(value = ["medicineId", "date", "plannedTime"], unique = true)])
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val date: String,
    val plannedTime: String,
    val actualMillis: Long,
    val status: String
)

@Entity(tableName = "profiles", indices = [Index(value = ["name"], unique = true)])
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
