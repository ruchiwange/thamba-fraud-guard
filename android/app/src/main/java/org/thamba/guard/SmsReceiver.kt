package org.thamba.guard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Telephony

/**
 * Runs when an SMS arrives. It hands the message and the call situation to the
 * engine and lets Notifier act on the verdict. The message is not stored or sent.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (parts.isEmpty()) return

        val sender = parts[0].originatingAddress ?: ""
        val text = parts.joinToString("") { it.messageBody ?: "" }
        val app = context.applicationContext

        // The check runs on the engine's background thread. goAsync() tells
        // Android to keep the app alive until finish() is called.
        val pending = goAsync()
        Engine.worker.execute {
            try {
                val onCall = Prefs.onCall(app) || audioSaysInCall(app)
                val callerKnown = Contacts.isKnown(app, Prefs.callNumber(app))
                Notifier.handleSms(app, Engine.onSms(app, sender, text, onCall, callerKnown))
            } catch (e: Exception) {
                // If the check fails, the message is simply delivered as usual.
            } finally {
                pending.finish()
            }
        }
    }

    /** A second check, in case the call broadcast was missed. */
    private fun audioSaysInCall(context: Context): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audio.mode == AudioManager.MODE_IN_CALL
    }
}
