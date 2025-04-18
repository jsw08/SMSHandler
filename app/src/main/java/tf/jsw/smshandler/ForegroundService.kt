package tf.jsw.smshandler

import android.Manifest
import android.R.drawable
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {
    private lateinit var smsResponder: SmsResponder
    private val smsReceiver = SmsReceiver()
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): ForegroundService = this@ForegroundService
    }
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun onSms(message: SmsMessage) {
        smsResponder.onReceive(message)
    }

    override fun onCreate() {
        super.onCreate()

        smsResponder = SmsResponder(this)
        createNotificationChannel(this)
        registerReceiver(
            smsReceiver,
            IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "sms_listener_channel")
            .setContentTitle("SMS Listener")
            .setContentText("Listening for incoming SMS...")
            .setSmallIcon(drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        return START_STICKY
    }
}