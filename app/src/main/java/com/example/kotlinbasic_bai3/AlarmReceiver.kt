package com.example.kotlinbasic_bai3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Alarm is ringing!", Toast.LENGTH_SHORT).show()

        val serviceIntent = Intent(context, AlarmService::class.java)
        context.startService(serviceIntent)
    }
}
