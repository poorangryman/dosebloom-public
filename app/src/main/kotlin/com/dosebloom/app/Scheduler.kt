package com.dosebloom.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object Scheduler {
    private const val DAYS=30
    fun cancelMedicine(context:Context, medicine:Medicine){
        val am=context.getSystemService(AlarmManager::class.java)
        val start=Calendar.getInstance()
        for(day in 0..DAYS){
            val c=(start.clone() as Calendar).apply{add(Calendar.DAY_OF_YEAR,day)}
            val date=Schedule.dateKey(c)
            for(time in medicine.times) am.cancel(pending(context,medicine.id,date,time))
        }
    }
    private fun request(id:Long,date:String,time:String)=(("$id|$date|$time").hashCode())
    fun cancelAll(context:Context, medicines:List<Medicine>) { val am=context.getSystemService(AlarmManager::class.java); val now=Calendar.getInstance(); for(m in medicines) for(day in 0..DAYS){ val c=(now.clone() as Calendar).apply{add(Calendar.DAY_OF_YEAR,day)}; val date=Schedule.dateKey(c); for(time in m.times){val pi=pending(context,m.id,date,time); am.cancel(pi)}} }
    fun rescheduleAll(context:Context){ val db=Db(context); val medicines=db.medicines(); cancelAll(context,medicines); val am=context.getSystemService(AlarmManager::class.java); val now=System.currentTimeMillis(); val start=Calendar.getInstance(); for(m in medicines){if(m.asNeeded)continue; for(day in 0..DAYS){val c=(start.clone() as Calendar).apply{add(Calendar.DAY_OF_YEAR,day)}; if(!Schedule.eligible(m,c))continue; val date=Schedule.dateKey(c); for(time in m.times){if(!Schedule.validTime(time))continue; val p=time.split(":"); val alarm=(c.clone() as Calendar).apply{set(Calendar.HOUR_OF_DAY,p[0].toInt());set(Calendar.MINUTE,p[1].toInt());set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}; if(alarm.timeInMillis<=now)continue; try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarm.timeInMillis,pending(context,m.id,date,time))}catch(_:SecurityException){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarm.timeInMillis,pending(context,m.id,date,time))}}}}}
    private fun pending(context:Context,id:Long,date:String,time:String):PendingIntent=PendingIntent.getBroadcast(context,request(id,date,time),Intent(context,ReminderReceiver::class.java).apply{putExtra("medicineId",id);putExtra("time",time);putExtra("date",date)},PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun postpone(context:Context,id:Long,time:String,date:String){ val am=context.getSystemService(AlarmManager::class.java); val pi=PendingIntent.getBroadcast(context,request(id,date,"$time+10"),Intent(context,ReminderReceiver::class.java).apply{putExtra("medicineId",id);putExtra("time",time);putExtra("date",date)},PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+600000,pi)}catch(_:SecurityException){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+600000,pi)} }
}
