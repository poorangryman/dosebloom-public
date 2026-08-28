package com.dosebloom.app

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

object ExportImport {
    fun export(context: Context, uri: Uri) {
        val db = Db(context)
        val root = JSONObject()
        root.put("profiles", JSONArray(db.profiles()))
        root.put("medicines", JSONArray().apply {
            db.medicines().forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("dose", m.dose)
                    put("unit", m.unit)
                    put("times", JSONArray(m.times))
                    put("startDate", m.startDate)
                    put("endDate", m.endDate)
                    put("note", m.note)
                    put("stock", m.stock)
                    put("lowStock", m.lowStock)
                    put("asNeeded", m.asNeeded)
                    put("profile", m.profile)
                })
            }
        })
        root.put("intakes", JSONArray().apply {
            db.intakesBetween("0000-01-01", "9999-12-31").forEach { intake ->
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
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(root.toString(2)) }
    }

    fun import(context: Context, uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return
        val root = JSONObject(text)
        val db = Db(context)
        val profiles = root.optJSONArray("profiles") ?: JSONArray()
        for (i in 0 until profiles.length()) db.addProfile(profiles.getString(i))
        val map = HashMap<Long, Long>()
        val meds = root.getJSONArray("medicines")
        for (i in 0 until meds.length()) {
            val o = meds.getJSONObject(i)
            val old = o.optLong("id", 0)
            val m = Medicine(
                0,
                o.getString("name"),
                o.optString("dose", "1"),
                o.optString("unit", context.getString(R.string.default_unit)),
                o.getJSONArray("times").let { a -> (0 until a.length()).map { a.getString(it) } },
                o.optString("startDate", Schedule.todayKey()),
                o.optString("endDate", ""),
                o.optString("note", ""),
                o.optInt("stock", 0),
                o.optInt("lowStock", 0),
                o.optBoolean("asNeeded", false),
                o.optString("profile", context.getString(R.string.default_profile))
            )
            val existing = db.medicines().firstOrNull { it.name == m.name && it.profile == m.profile }
            val id = if (existing == null) db.insertMedicine(m) else {
                db.updateMedicine(m.copy(id = existing.id))
                existing.id
            }
            map[old] = id
        }
        val ints = root.optJSONArray("intakes") ?: JSONArray()
        for (i in 0 until ints.length()) {
            val o = ints.getJSONObject(i)
            val id = map[o.optLong("medicineId", -1)] ?: continue
            db.addIntake(id, o.getString("date"), o.getString("plannedTime"), o.optString("status", "TAKEN"), o.optLong("actualMillis", System.currentTimeMillis()))
        }
    }
}
