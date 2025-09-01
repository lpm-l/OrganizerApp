package com.fcc.organizador.homework.notification

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.fcc.organizador.homework.Homework
import java.util.Calendar

fun scheduleExactNotification(context: Context, homework: Homework) {
    val timeMillis = homework.dueDateMillis

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", homework.title)
        putExtra("description", "haz tu tarea")
        putExtra("notificationId", homework.id)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        homework.id,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
    } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
    }
}

fun cancelNotification(context: Context, homeworkId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        homeworkId,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    pendingIntent?.let {
        alarmManager.cancel(it)
        it.cancel()
    }
}

fun rescheduleNotification(context: Context, oldHomeworkId: Int, newHomework: Homework) {
    cancelNotification(context, oldHomeworkId)
    scheduleExactNotification(context, newHomework)
}



