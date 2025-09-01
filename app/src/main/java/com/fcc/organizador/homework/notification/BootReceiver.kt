package com.fcc.organizador.homework.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fcc.organizador.homework.Homework

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            //val homeworkList = homeworkDao.getAllHomeworks()
            val homeworkList = mutableListOf<Homework>()
            for (homework in homeworkList) {
                scheduleExactNotification(context, homework)
            }
        }
    }
}