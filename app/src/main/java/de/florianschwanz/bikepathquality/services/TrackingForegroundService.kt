package de.florianschwanz.bikepathquality.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.ui.main.MainActivity

class TrackingForegroundService : LifecycleService() {

    private lateinit var logEntryViewModel: LogEntryViewModel

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate() {
        super.onCreate()
        logEntryViewModel =
            LogEntryViewModel((this.application as BikePathQualityApplication).logEntryRepository)
    }

    /**
     * Handles on-start lifecycle phase
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val notification = createNotification(
            title = R.string.action_tracking_bike_activity_idle,
            text = R.string.action_tracking_bike_activity_idle_description,
            icon = R.drawable.ic_baseline_pause_24
        )
        startForeground(1, notification)

        log("Start tracking service")

        return START_STICKY
    }

    //
    // Helpers
    //

    /**
     * Creates notification channel with a given id and name
     *
     * @param channelId channel ID
     * @param channelName channel name
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String = CHANNEL_ID,
        channelName: String = CHANNEL_NAME
    ): String {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)
        return channelId
    }

    /**
     * Creates notification
     */
    private fun createNotification(
        notificationChannelId: String = createNotificationChannel(),
        title: Int,
        text: Int,
        icon: Int
    ): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(getString(title))
            .setContentText(getString(text))
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * Logs message
     */
    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }

    companion object {
        const val CHANNEL_ID = "channel.TRACKING"
        const val CHANNEL_NAME = "channel.TRACKING"
    }
}