package com.dosebloom.app

import android.content.Context
import com.dosebloom.app.data.DoseBloomDatabase
import com.dosebloom.app.data.DoseBloomRepository
import com.dosebloom.app.data.IntakeEntity
import com.dosebloom.app.data.MedicineEntity
import com.dosebloom.app.data.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Compatibility facade for the current UI/background entry points.
 * The persistence implementation is now Room; this facade is intentionally
 * temporary while callers are migrated to DoseBloomRepository/ViewModel.
 */
class Db(context: Context) {
    private val database = DoseBloomDatabase.get(context)
    private val repository = DoseBloomRepository(database)
    private val medicineDao = database.medicineDao()
    private val intakeDao = database.intakeDao()
    private val profileDao = database.profileDao()

    fun medicines(): List<Medicine> = runBlocking(Dispatchers.IO) {
        medicineDao.all().map { it.toMedicine() }
    }

    fun insertMedicine(m: Medicine): Long = runBlocking(Dispatchers.IO) {
        medicineDao.insert(m.toEntity())
    }

    fun updateMedicine(m: Medicine) = runBlocking(Dispatchers.IO) {
        medicineDao.update(m.toEntity())
    }

    fun deleteMedicine(id: Long) = runBlocking(Dispatchers.IO) {
        repository.deleteMedicine(id)
    }

    fun hasIntake(medicineId: Long, date: String, time: String): Boolean = runBlocking(Dispatchers.IO) {
        intakeDao.exists(medicineId, date, time)
    }

    fun addIntake(
        medicineId: Long,
        date: String,
        time: String,
        status: String = "TAKEN",
        actualMillis: Long = System.currentTimeMillis()
    ) = runBlocking(Dispatchers.IO) {
        intakeDao.insert(
            IntakeEntity(
                medicineId = medicineId,
                date = date,
                plannedTime = time,
                actualMillis = actualMillis,
                status = status
            )
        )
    }

    fun intakesBetween(from: String, to: String): List<Intake> = runBlocking(Dispatchers.IO) {
        intakeDao.between(from, to).map { it.toIntake() }
    }

    fun intakes(date: String): List<Intake> = intakesBetween(date, date)

    fun decreaseStock(id: Long) = runBlocking(Dispatchers.IO) {
        medicineDao.decreaseStock(id)
    }

    fun profiles(): List<String> = runBlocking(Dispatchers.IO) { profileDao.allNames() }

    fun addProfile(name: String) = runBlocking(Dispatchers.IO) {
        val normalized = name.trim()
        if (normalized.isNotEmpty()) profileDao.insert(ProfileEntity(name = normalized))
    }

    fun removeProfile(name: String) = runBlocking(Dispatchers.IO) {
        if (name != "Я") profileDao.delete(name)
    }

    private fun MedicineEntity.toMedicine() = Medicine(
        id = id,
        name = name,
        dose = dose,
        unit = unit,
        times = times.split(",").filter(String::isNotBlank),
        startDate = startDate,
        endDate = endDate,
        note = note,
        stock = stock,
        lowStock = lowStock,
        asNeeded = asNeeded == 1,
        profile = profile
    )

    private fun Medicine.toEntity() = MedicineEntity(
        id = id,
        name = name,
        dose = dose,
        unit = unit,
        times = times.joinToString(","),
        startDate = startDate,
        endDate = endDate,
        note = note,
        stock = stock,
        lowStock = lowStock,
        asNeeded = if (asNeeded) 1 else 0,
        profile = profile
    )

    private fun IntakeEntity.toIntake() = Intake(id, medicineId, date, plannedTime, actualMillis, status)
}
