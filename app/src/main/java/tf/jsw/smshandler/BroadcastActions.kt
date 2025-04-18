package tf.jsw.smshandler

import android.content.IntentFilter

object BroadcastActions {
    const val GPS = "GPS"
    const val LOUD = "LOUD"

    val all = arrayOf(GPS, LOUD)
    val filter =  IntentFilter().apply {
        for (intent in all) {
            addAction(intent)
        }
    }
}
