package tf.jsw.smshandler

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsMessage
import androidx.annotation.RequiresPermission

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val serviceIntent = Intent(context, ForegroundService::class.java)
        val serviceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(arg0: ComponentName) {}

            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val service = (service as ForegroundService.LocalBinder).getService()
                val resolver = context.contentResolver

                for (message in Telephony.Sms.Intents.getMessagesFromIntent(intent))
                    if (
                        isSenderStarred(
                            message,
                            resolver
                        )
                        && message.messageBody.trim() in BroadcastActions.all
                    )
                        service.onSms(message)

                context.unbindService(this)
            }
        }

        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun isSenderStarred(sms: SmsMessage, resolver: ContentResolver): Boolean {
        val rawNumber = sms.displayOriginatingAddress ?: return false
        val normalizedNumber = PhoneNumberUtils.normalizeNumber(rawNumber)

        if (normalizedNumber.isNullOrEmpty()) return false

        return try {
            val uri = ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI
                .buildUpon()
                .appendPath(normalizedNumber)
                .build()

            val projection = arrayOf(
                ContactsContract.Contacts.STARRED,
                ContactsContract.PhoneLookup.NORMALIZED_NUMBER
            )

            resolver.query(
                uri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                cursor.moveToFirst() && cursor.getInt(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)
                ) == 1
            } == true
        } catch (e: Exception) {
            false
        }
    }
};