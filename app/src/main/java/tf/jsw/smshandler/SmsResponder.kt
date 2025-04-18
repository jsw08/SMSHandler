package tf.jsw.smshandler

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.telephony.SmsManager
import android.telephony.SmsMessage
import androidx.annotation.RequiresPermission

class SmsResponder(private val context: Context) {
    private val locationManager: LocationManager = context.getSystemService(LocationManager::class.java)
    private val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
    private val audioManager: AudioManager = context.getSystemService(AudioManager::class.java)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun onReceive(message: SmsMessage) {
        val phoneNumber = message.originatingAddress
        val msgBody = message.messageBody.trim()
        if (phoneNumber == null) return

        sendSms(phoneNumber, "OK")
        when (msgBody) {
            BroadcastActions.GPS -> commandGps(phoneNumber)
            BroadcastActions.LOUD -> commandLoud()
            else -> return
        }
    }

    private fun commandLoud() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        for (stream in arrayOf(AudioManager.STREAM_RING, AudioManager.STREAM_NOTIFICATION))
            audioManager.setStreamVolume(stream, audioManager.getStreamMaxVolume(stream), 0)
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun commandGps(phoneNumber: String) {
        locationManager.getCurrentLocation(
            LocationManager.FUSED_PROVIDER,
            null,
            context.mainExecutor
        ) { location: Location? ->
            if (location == null) {
                sendSms(phoneNumber, "Error getting location. Probably no gps.")
                return@getCurrentLocation
            }

            val lat = location.latitude
            val lon = location.longitude
            val acc = location.accuracy

            sendSms(phoneNumber, "https://gps.jsw.tf/?x=$lon&y=$lat&a=$acc")
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        smsManager.sendTextMessage(
            phoneNumber,
            null,
            message,
            null,
            null
        )
    }
}
