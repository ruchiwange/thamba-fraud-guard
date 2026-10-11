package org.thamba.guard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

/**
 * Payment guard. Runs only while a phone call is on. Every two seconds it looks
 * at which app is on screen; if a payment app opens during a call with someone
 * who is not a saved contact, it asks one question, once per call.
 */
class CallWatchService : Service() {

    // Its own thread: the checks call the Python engine and must not block the screen.
    private val thread = HandlerThread("thamba-watch").apply { start() }
    private val handler = Handler(thread.looper)
    private var asked = false
    @Volatile private var running = true
    private var onScreen: String? = null
    private var lastChecked = 0L

    private val tick = object : Runnable {
        override fun run() {
            check()
            if (running) handler.postDelayed(this, 2000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val l = Lang.wrap(this)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, l.getString(R.string.channel_watch), NotificationManager.IMPORTANCE_LOW)
        )
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle(l.getString(R.string.watch_title))
            .setOngoing(true)
            .build()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        running = true
        lastChecked = System.currentTimeMillis() - 5000
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(tick)
        thread.quitSafely()
        super.onDestroy()
    }

    private fun check() {
        if (!Prefs.onCall(this)) {
            running = false
            stopSelf()
            return
        }
        if (asked) return

        val app = foregroundApp() ?: return
        val callerKnown = Contacts.isKnown(this, Prefs.callNumber(this))
        val verdict = try {
            Engine.onPayment(this, app, true, callerKnown)
        } catch (e: Exception) {
            return
        }
        if (verdict.optString("level") == "ask") {
            asked = true
            try {
                startActivity(
                    AlertActivity.intent(this, Texts.paymentScreen(Lang.wrap(this)))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: Exception) {
                // Android refused to open the screen; nothing more we can do for this call.
            }
        }
    }

    /** The app most recently brought to the front, or null if we cannot tell. */
    private fun foregroundApp(): String? {
        val usage = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usage.queryEvents(lastChecked, now)
        lastChecked = now
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == MOVED_TO_FRONT) onScreen = event.packageName
        }
        return onScreen
    }

    companion object {
        private const val CHANNEL = "thamba_watch"
        private const val NOTIFICATION_ID = 10

        // UsageEvents.Event.ACTIVITY_RESUMED, which has had the value 1 under two names.
        private const val MOVED_TO_FRONT = 1

        fun start(context: Context) {
            if (!Access.canSeeForegroundApp(context)) return
            try {
                context.startForegroundService(Intent(context, CallWatchService::class.java))
            } catch (e: Exception) {
                // Android did not allow the service to start from the background.
            }
        }
    }
}
