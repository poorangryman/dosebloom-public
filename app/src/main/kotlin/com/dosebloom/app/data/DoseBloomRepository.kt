package com.dosebloom.app.data

import androidx.room.withTransaction
import com.dosebloom.app.Intake
import com.dosebloom.app.Medicine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DoseBloomRepository(private val database: DoseBloomDatabase) {
    private val medicines = database.medicineDao()
    private val intakes = database.intakeDao()
    private val profiles = database.profileDao()

    fun observeMedicines(profile: String): Flow<List<Medicine>> = medicines.observeByProfile(profile).map { list -> list.map { it.toDomain() } }
    fun observeAllMedicines(): Flow<List<Medicine>> = medicines.observeAll().map { list -> list.map { it.toDomain() } }
    fun observeIntakes(date: String): Flow<List<Intake>> = intakes.observeForDate(date).map { list -> list.map { it.toDomain() } }
    fun observeIntakes(from: String, to: String): Flow<List<Intake>> = intakes.observeBetween(from, to).map { list -> list.map { it.toDomain() } }
    fun observeProfiles(): Flow<List<String>> = profiles.observeAll().map { it.map(ProfileEntity::name) }

    suspend fun findMedicine(id: Long): Medicine? = medicines.find(id)?.toDomain()
    suspend fun addMedicine(medicine: Medicine): Long = medicines.insert(medicine.toEntity())
    suspend fun updateMedicine(medicine: Medicine) = medicines.update(medicine.toEntity())

    suspend fun deleteMedicine(id: Long) = database.withTransaction {
        intakes.deleteForMedicine(id)
        medicines.delete(id)
    }

    suspend fun addProfile(name: String) {
        val normalized = name.trim()
        if (normalized.isNotEmpty()) profiles.insert(ProfileEntity(name = normalized))
    }

    suspend fun removeProfile(name: String) = database.withTransaction {
        if (name != "Я") {
            medicines.moveProfile(name, "Я")
            profiles.delete(name)
        }
    }

    suspend fun hasIntake(medicineId: Long, date: String, time: String): Boolean =
        intakes.exists(medicineId, date, time)

    suspend fun takeDose(medicineId: Long, date: String, time: String, actualMillis: Long = System.currentTimeMillis()): Boolean =
        database.withTransaction {
            if (intakes.exists(medicineId, date, time)) return@withTransaction false
            intakes.insert(IntakeEntity(medicineId = medicineId, date = date, plannedTime = time, actualMillis = actualMillis, status = IntakeStatus.TAKEN.storageValue))
            medicines.decreaseStock(medicineId)
            true
        }

    suspend fun recordIntake(medicineId: Long, date: String, time: String, status: IntakeStatus, actualMillis: Long = System.currentTimeMillis()): Boolean =
        database.withTransaction {
            if (intakes.exists(medicineId, date, time)) return@withTransaction false
            intakes.insert(IntakeEntity(medicineId = medicineId, date = date, plannedTime = time, actualMillis = actualMillis, status = status.storageValue))
            if (status == IntakeStatus.TAKEN) medicines.decreaseStock(medicineId)
            true
        }

    suspend fun undoDose(medicineId: Long, date: String, time: String): Boolean =
        database.withTransaction {
            val existing = intakes.find(medicineId, date, time) ?: return@withTransaction false
            intakes.delete(medicineId, date, time)
            if (existing.status == IntakeStatus.TAKEN.storageValue) {
                medicines.increaseStock(medicineId)
            }
            true
        }

    suspend fun takeAsNeeded(medicineId: Long, date: String = com.dosebloom.app.Schedule.todayKey(), time: String = com.dosebloom.app.Schedule.nowTime()): Boolean =
        takeDose(medicineId, date, time)
}

enum class IntakeStatus(val storageValue: String) {
    TAKEN("TAKEN"),
    SKIPPED("SKIPPED");
    companion object { fun fromStorage(value: String): IntakeStatus = entries.firstOrNull { it.storageValue == value } ?: SKIPPED }
}

private fun MedicineEntity.toDomain() = Medicine(id, name, dose, unit, times.split(',').filter(String::isNotBlank), startDate, endDate, note, stock, lowStock, asNeeded == 1, profile)
private fun Medicine.toEntity() = MedicineEntity(id, name, dose, unit, times.joinToString(","), startDate, endDate, note, stock, lowStock, if (asNeeded) 1 else 0, profile)
private fun IntakeEntity.toDomain() = Intake(id, medicineId, date, plannedTime, actualMillis, status)
