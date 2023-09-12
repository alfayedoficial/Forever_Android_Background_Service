package com.alialfayed.foreverbackgroundservice

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.Date


class ForeverService : Service() {

    companion object {
        const val ACTION_STOP_SERVICE = "com.alialfayed.foreverbackgroundservice.STOP_SERVICE"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_PERMISSION_CODE = 5
        const val TAG_DEBUG = "ForeverService"
    }

    /**
     *
     *
     * Example:
     * <pre>{@code
     * see {@link #onStartCommand()} method - Called when the service is started.
     * see {@link #onBind()} method- Called when a component wants to bind to the service (optional if you don't need binding).
     * see {@link #onDestroy()} method - Called when the service is stopped or destroyed.
     * }</pre>
     *
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    /**
     * On start command
     *
     * method, you can check if the service is running as a normal background service or in the foreground:
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_SERVICE) {
            Log.i(TAG_DEBUG , "ACTION_STOP_SERVICE time = ${Date()}")

            // The service was stopped by the user
            stopSelf()
            return START_NOT_STICKY
        }

        // Check if the service is running as a foreground service or a normal background service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Running as a foreground service
            initNotification()
            Log.i(TAG_DEBUG , "initNotification time = ${Date()}")

        } else {
            // Running as a normal background service
            // You can do any background work here
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, ForeverService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
        Log.i(TAG_DEBUG , "onTaskRemoved time = ${Date()}")

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG_DEBUG , "onDestroy time = ${Date()}")
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
    }

    private fun initNotification() {
        val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
        val channelId = "YourChannelId"
        val channelName = "Your Channel Name"

        val requestID = System.currentTimeMillis().toInt()
        val pendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            PendingIntent.getActivity(this, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT  or PendingIntent.FLAG_IMMUTABLE )
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Your service is running")
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setTicker("Running")
            .setOngoing(true)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 100, 200, 300)
            channel.enableLights(true)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
            notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        } else {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                notificationBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE).priority = NotificationCompat.PRIORITY_DEFAULT
            }
        }

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }



}