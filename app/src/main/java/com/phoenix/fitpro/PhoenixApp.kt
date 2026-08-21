package com.phoenix.fitpro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhoenixApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Workout reminder channel
            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT_REMINDER,
                getString(R.string.notif_channel_workout),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels pour tes séances de sport planifiées"
                enableVibration(true)
            }

            // Achievement channel
            val achievementChannel = NotificationChannel(
                CHANNEL_ACHIEVEMENT,
                getString(R.string.notif_channel_achievement),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications de badges débloqués"
            }

            manager.createNotificationChannels(listOf(workoutChannel, achievementChannel))
        }
    }

    companion object {
        const val CHANNEL_WORKOUT_REMINDER = "workout_reminders"
        const val CHANNEL_ACHIEVEMENT = "achievements"
    }
}
