package org.thamba.guard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

/** Keeps track of whether a phone call is on right now, and with which number. */
class CallReceiver : BroadcastReceiver() {

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                    TelephonyManager.EXTRA_STATE_IDLE -> Prefs.setCall(context, false, null)
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        Prefs.setCall(context, true, number)
                        // Get the engine ready while the phone is still ringing.
                        Engine.warmUp(context)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        Prefs.setCall(context, true, number)
                        Engine.warmUp(context)
                        // The call has been answered: start the payment guard for its duration.
                        CallWatchService.start(context)
                    }
                }
            }
            Intent.ACTION_NEW_OUTGOING_CALL ->
                Prefs.setCall(context, true, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER))
        }
    }
}
