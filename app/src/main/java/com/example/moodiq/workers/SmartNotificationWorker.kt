package com.example.moodiq.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moodiq.MainActivity
import com.example.moodiq.R
import com.example.moodiq.core.MoodiqApp

class SmartNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as MoodiqApp).appContainer
        val recommendation = container.getRecommendationsUseCase()
        val message = when (recommendation.moodTag) {
            "Chill Night" -> "You usually listen to chill songs now 🎧"
            else -> "Play your favorite songs again?"
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Moodiq Smart", NotificationManager.IMPORTANCE_DEFAULT)
        )

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            11,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Moodiq Suggestions")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(901, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "smart_music_channel"
    }
}
