package com.alialfayed.foreverbackgroundservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RestartServiceReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start your service when the device boots up
            val serviceIntent = Intent(context, ForeverService::class.java)
            context?.startService(serviceIntent)
        }
    }
}