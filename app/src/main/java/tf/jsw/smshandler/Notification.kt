package tf.jsw.smshandler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "sms_listener_channel",
        "SMS Listener",
        NotificationManager.IMPORTANCE_LOW
    )
    val manager = getSystemService(context, NotificationManager::class.java)
    manager?.createNotificationChannel(channel)
}