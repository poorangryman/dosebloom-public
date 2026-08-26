package com.dosebloom.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Db(context: Context) : SQLiteOpenHelper(context, "dosebloom.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE medicines(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,dose TEXT NOT NULL,unit TEXT NOT NULL,times TEXT NOT NULL,startDate TEXT NOT NULL,endDate TEXT NOT NULL,note TEXT NOT NULL,stock INTEGER NOT NULL,lowStock INTEGER NOT NULL,asNeeded INTEGER NOT NULL,profile TEXT NOT NULL)")
        db.execSQL("CREATE TABLE intakes(id INTEGER PRIMARY KEY AUTOINCREMENT,medicineId INTEGER NOT NULL,date TEXT NOT NULL,plannedTime TEXT NOT NULL,actualMillis INTEGER NOT NULL,status TEXT NOT NULL,UNIQUE(medicineId,date,plannedTime))")
        db.execSQL("CREATE TABLE profiles(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)")
        db.execSQL("INSERT INTO profiles(name) VALUES('Я')")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS profiles(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)")
            db.execSQL("INSERT OR IGNORE INTO profiles(name) VALUES('Я')")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_intake_unique ON intakes(medicineId,date,plannedTime)")
        }
    }
    private fun medicine(c: android.database.Cursor) = Medicine(
        c.getLong(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("name")),
        c.getString(c.getColumnIndexOrThrow("dose")), c.getString(c.getColumnIndexOrThrow("unit")),
        c.getString(c.getColumnIndexOrThrow("times")).split(",").filter(String::isNotBlank),
        c.getString(c.getColumnIndexOrThrow("startDate")), c.getString(c.getColumnIndexOrThrow("endDate")),
        c.getString(c.getColumnIndexOrThrow("note")), c.getInt(c.getColumnIndexOrThrow("stock")),
        c.getInt(c.getColumnIndexOrThrow("lowStock")), c.getInt(c.getColumnIndexOrThrow("asNeeded")) == 1,
        c.getString(c.getColumnIndexOrThrow("profile"))
    )
    fun medicines(): List<Medicine> = buildList {
        readableDatabase.query("medicines", null, null, null, null, null, "name COLLATE NOCASE ASC").use { c -> while(c.moveToNext()) add(medicine(c)) }
    }
    fun insertMedicine(m: Medicine): Long = writableDatabase.insertOrThrow("medicines", null, values(m))
    fun updateMedicine(m: Medicine) { writableDatabase.update("medicines", values(m), "id=?", arrayOf(m.id.toString())) }
    private fun values(m: Medicine) = ContentValues().apply {
        put("name",m.name); put("dose",m.dose); put("unit",m.unit); put("times",m.times.joinToString(",")); put("startDate",m.startDate); put("endDate",m.endDate); put("note",m.note); put("stock",m.stock); put("lowStock",m.lowStock); put("asNeeded",if(m.asNeeded)1 else 0); put("profile",m.profile)
    }
    fun deleteMedicine(id: Long) { writableDatabase.delete("intakes","medicineId=?",arrayOf(id.toString())); writableDatabase.delete("medicines","id=?",arrayOf(id.toString())) }
    fun hasIntake(medicineId: Long,date:String,time:String): Boolean = readableDatabase.rawQuery("SELECT 1 FROM intakes WHERE medicineId=? AND date=? AND plannedTime=? LIMIT 1",arrayOf(medicineId.toString(),date,time)).use{it.moveToFirst()}
    fun addIntake(medicineId:Long,date:String,time:String,status:String="TAKEN",actualMillis:Long=System.currentTimeMillis()) { val v=ContentValues().apply{put("medicineId",medicineId);put("date",date);put("plannedTime",time);put("actualMillis",actualMillis);put("status",status)}; writableDatabase.insertWithOnConflict("intakes",null,v,SQLiteDatabase.CONFLICT_REPLACE) }
    fun intakesBetween(from:String,to:String): List<Intake> = buildList { readableDatabase.query("intakes",null,"date BETWEEN ? AND ?",arrayOf(from,to),null,null,"date DESC, plannedTime DESC").use{c->while(c.moveToNext())add(Intake(c.getLong(c.getColumnIndexOrThrow("id")),c.getLong(c.getColumnIndexOrThrow("medicineId")),c.getString(c.getColumnIndexOrThrow("date")),c.getString(c.getColumnIndexOrThrow("plannedTime")),c.getLong(c.getColumnIndexOrThrow("actualMillis")),c.getString(c.getColumnIndexOrThrow("status"))))} }
    fun intakes(date:String)=intakesBetween(date,date)
    fun decreaseStock(id:Long){writableDatabase.execSQL("UPDATE medicines SET stock=CASE WHEN stock>0 THEN stock-1 ELSE 0 END WHERE id=?",arrayOf(id))}
    fun profiles(): List<String> = buildList { readableDatabase.query("profiles",arrayOf("name"),null,null,null,null,"id ASC").use{c->while(c.moveToNext())add(c.getString(0))} }
    fun addProfile(name:String){if(name.isNotBlank())writableDatabase.insertWithOnConflict("profiles",null,ContentValues().apply{put("name",name.trim())},SQLiteDatabase.CONFLICT_IGNORE)}
    fun removeProfile(name:String){if(name!="Я")writableDatabase.delete("profiles","name=?",arrayOf(name))}
}
