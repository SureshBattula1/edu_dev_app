package com.example.myeduapp.core.sound

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import com.example.myeduapp.MyEduApp

private const val CHANNEL_ID = "school_alerts_assignment_notify_v1"
private const val SOUND_NAME = "assignment_notify"

actual fun playNotificationSound() {
    val context = runCatching { MyEduApp.appContext }.getOrNull() ?: return
    val uri = assignmentNotifyUri(context)
    try {
        MediaPlayer().apply {
            setDataSource(context, uri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setOnCompletionListener { release() }
            prepare()
            start()
        }
    } catch (_: Exception) {
        try {
            RingtoneManager.getRingtone(context, uri)?.play()
        } catch (_: Exception) {
        }
    }
}

actual fun showAppNotification(title: String, message: String) {
    val context = runCatching { MyEduApp.appContext }.getOrNull() ?: return
    playNotificationSound()
    postSystemNotification(context, title, message)
}

private fun postSystemNotification(context: Context, title: String, message: String) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    val soundUri = assignmentNotifyUri(context)
    ensureChannel(manager, soundUri)

    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val pending = PendingIntent.getActivity(
        context,
        0,
        launch,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        android.app.Notification.Builder(context, CHANNEL_ID)
    } else {
        @Suppress("DEPRECATION")
        android.app.Notification.Builder(context).setSound(soundUri)
    }

    val notification = builder
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(android.app.Notification.BigTextStyle().bigText(message))
        .setAutoCancel(true)
        .setContentIntent(pending)
        .build()

    manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
}

private fun ensureChannel(manager: NotificationManager, soundUri: Uri) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    manager.deleteNotificationChannel("school_alerts")
    manager.deleteNotificationChannel("school_alerts_custom")
    if (manager.getNotificationChannel(CHANNEL_ID) != null) return
    val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, "School alerts", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Assignment and school notifications"
            enableVibration(true)
            setSound(soundUri, attrs)
        }
    )
}

private fun assignmentNotifyUri(context: Context): Uri {
    val resId = context.resources.getIdentifier(SOUND_NAME, "raw", context.packageName)
    if (resId != 0) {
        return Uri.parse("android.resource://${context.packageName}/$resId")
    }
    return Uri.parse("android.resource://${context.packageName}/raw/$SOUND_NAME")
}
