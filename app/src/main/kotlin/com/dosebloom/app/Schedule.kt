package com.dosebloom.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Schedule {
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.US)
    fun dateKey(date: Date)=fmt.format(date)
    fun dateKey(calendar:Calendar)=fmt.format(calendar.time)
    fun parseDate(key:String):Calendar=Calendar.getInstance().apply{time=fmt.parse(key)?:Date()}
    fun todayKey()=dateKey(Date())
    fun eligible(m:Medicine,calendar:Calendar):Boolean { val d=dateKey(calendar); return d>=m.startDate && (m.endDate.isBlank()||d<=m.endDate) }
    fun events(medicines:List<Medicine>,date:String):List<Pair<Medicine,String>> { val cal=parseDate(date); return medicines.filter{!it.asNeeded&&eligible(it,cal)}.flatMap{m->m.times.map{m to it}}.sortedBy{it.second} }
    fun validTime(s:String)=Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$").matches(s)
    fun monthDays(year:Int,month:Int):List<Calendar>{ val first=Calendar.getInstance().apply{set(year,month,1,0,0,0);set(Calendar.MILLISECOND,0)}; val start=first.clone() as Calendar; val dow=(first.get(Calendar.DAY_OF_WEEK)+5)%7; start.add(Calendar.DAY_OF_MONTH,-dow); return (0 until 42).map{(start.clone() as Calendar).apply{add(Calendar.DAY_OF_MONTH,it)}} }
    fun nowTime()=timeFmt.format(Date())
}
