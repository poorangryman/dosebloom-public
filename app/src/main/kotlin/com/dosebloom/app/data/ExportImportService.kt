package com.dosebloom.app.data

import android.content.Context
import android.net.Uri
import com.dosebloom.app.Medicine
import com.dosebloom.app.R
import com.dosebloom.app.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

class ExportImportService(private val context: Context) {
    private val database = DoseBloomDatabase.get(context)
    private val medicineDao = database.medicineDao()
    private val intakeDao = database.intakeDao()
    private val profileDao = database.profileDao()

    suspend fun export(uri: Uri) = withContext(Dispatchers.IO) {
        val root = JSONObject().apply {
            put("schemaVersion", 2)
            put("appVersion", runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "1.4.8")
            put("exportedAt", System.currentTimeMillis())
            put("profiles", JSONArray(profileDao.allNames()))
            put("medicines", JSONArray().apply {
                medicineDao.all().forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("name", m.name)
                        put("dose", m.dose)
                        put("unit", m.unit)
                        put("times", JSONArray(m.times.split(",").filter(String::isNotBlank)))
                        put("startDate", m.startDate)
                        put("endDate", m.endDate)
                        put("note", m.note)
                        put("stock", m.stock)
                        put("lowStock", m.lowStock)
                        put("asNeeded", m.asNeeded == 1)
                        put("profile", m.profile)
                    })
                }
            })
            put("intakes", JSONArray().apply {
                intakeDao.between("0000-01-01", "9999-12-31").forEach { intake ->
                    put(JSONObject().apply {
                        put("id", intake.id)
                        put("medicineId", intake.medicineId)
                        put("date", intake.date)
                        put("plannedTime", intake.plannedTime)
                        put("actualMillis", intake.actualMillis)
                        put("status", intake.status)
                    })
                }
            })
        }
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(root.toString(2)) }
            ?: error("Unable to open export destination")
    }

    suspend fun import(uri: Uri) = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Unable to open import source")
        val root = JSONObject(text)
        val schemaVersion = root.optInt("schemaVersion", 1)
        require(schemaVersion in 1..2) { "Unsupported DoseBloom export schema: $schemaVersion" }
        val profiles = root.optJSONArray("profiles") ?: JSONArray()
        val medicines = root.optJSONArray("medicines") ?: error("Missing medicines array")
        val intakes = root.optJSONArray("intakes") ?: JSONArray()

        database.withTransaction {
            for (i in 0 until profiles.length()) {
                val name = profiles.optString(i).trim()
                if (name.isNotEmpty()) profileDao.insert(ProfileEntity(name = name))
            }

            val idMap = HashMap<Long, Long>()
            val existing = medicineDao.all()
            for (i in 0 until medicines.length()) {
                val o = medicines.optJSONObject(i) ?: error("Invalid medicine at index $i")
                val medicine = MedicineEntity(
                    id = 0,
                    name = o.optString("name").trim().also { require(it.isNotEmpty()) { "Medicine name is empty" } },
                    dose = o.optString("dose", "1"),
                    unit = o.optString("unit", context.getString(R.string.default_unit)),
                    times = (0 until (o.optJSONArray("times")?.length() ?: 0)).joinToString(",") { index -> o.getJSONArray("times").getString(index).trim() },
                    startDate = o.optString("startDate", Schedule.todayKey()),
                    endDate = o.optString("endDate", ""),
                    note = o.optString("note", ""),
                    stock = o.optInt("stock", 0),
                    lowStock = o.optInt("lowStock", 0),
                    asNeeded = if (o.optBoolean("asNeeded", false)) 1 else 0,
                    profile = o.optString("profile", context.getString(R.string.default_profile))
                )
                val oldId = o.optLong("id", 0)
                val current = existing.firstOrNull { it.name == medicine.name && it.profile == medicine.profile }
                val newId = if (current == null) medicineDao.insert(medicine) else {
                    medicineDao.update(medicine.copy(id = current.id))
                    current.id
                }
                idMap[oldId] = newId
            }

            for (i in 0 until intakes.length()) {
                val o = intakes.optJSONObject(i) ?: continue
                val medicineId = idMap[o.optLong("medicineId", -1)] ?: continue
                val date = o.optString("date")
                val time = o.optString("plannedTime")
                if (date.isBlank() || !Schedule.validTime(time)) continue
                intakeDao.insert(
                    IntakeEntity(
                        id = 0,
                        medicineId = medicineId,
                        date = date,
                        plannedTime = time,
                        actualMillis = o.optLong("actualMillis", System.currentTimeMillis()),
                        status = o.optString("status", IntakeStatus.TAKEN.storageValue)
                    )
                )
            }
        }
    }
}
