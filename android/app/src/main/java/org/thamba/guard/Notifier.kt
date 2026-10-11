package org.thamba.guard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.json.JSONObject

/** Decides how loudly to tell the person: a full-screen warning or a small notification. */
object Notifier {
    private const val CHANNEL_ALERTS = "thamba_alerts"
    private const val CHANNEL_NOTICES = "thamba_notices"
    private const val ID_ALERT = 1
    private const val ID_NOTICE = 2
    private const val ID_MESSAGE = 3

    private fun manager(context: Context) =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun ensureChannels(context: Context, l: Context) {
        val m = manager(context)
        m.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, l.getString(R.string.channel_alerts), NotificationManager.IMPORTANCE_HIGH)
        )
        m.createNotificationChannel(
            NotificationChannel(CHANNEL_NOTICES, l.getString(R.string.channel_notices), NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    /** The warning screen is showing, so its stand-in notification is no longer needed. */
    fun clearAlert(context: Context) = manager(context).cancel(ID_ALERT)

    /** Act on the engine's verdict for an SMS that has just arrived. */
    fun handleSms(context: Context, verdict: JSONObject) {
        val l = Lang.wrap(context)
        val code = verdict.getJSONObject("code")
        val sender = verdict.getJSONObject("sender")
        val link = verdict.getJSONObject("link")

        when (code.optString("level")) {
            "alert" -> {
                fullScreen(context, l, Texts.codeAlert(l, code))
                return
            }
            "notice" -> Texts.codeNotice(l, code)?.let { small(context, l, it) }
        }

        // A bad link or a fake "official" sender does not take over the screen,
        // because nothing is at risk until the person acts on the message.
        if (link.optString("level") == "red") {
            tappable(context, l, Texts.linkScreen(l, link, null))
        } else if (sender.optString("level") == "amber") {
            tappable(context, l, Texts.senderScreen(l))
        }
    }

    private fun pending(context: Context, screen: Screen, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            AlertActivity.intent(context, screen),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** Take over the screen now. The notification is the fallback if Android refuses. */
    private fun fullScreen(context: Context, l: Context, screen: Screen) {
        ensureChannels(context, l)
        val open = pending(context, screen, ID_ALERT)
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(screen.title + " " + screen.line)
            .setContentText(screen.why)
            .setStyle(NotificationCompat.BigTextStyle().bigText(screen.why))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(open, true)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        post(context, ID_ALERT, notification)
        try {
            context.startActivity(AlertActivity.intent(context, screen).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            // Android blocked the direct launch; the full-screen notification above remains.
        }
    }

    private fun tappable(context: Context, l: Context, screen: Screen) {
        ensureChannels(context, l)
        val open = pending(context, screen, ID_MESSAGE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(screen.title + " " + screen.line)
            .setContentText(screen.why)
            .setStyle(NotificationCompat.BigTextStyle().bigText(screen.why))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        post(context, ID_MESSAGE, notification)
    }

    private fun small(context: Context, l: Context, text: String) {
        ensureChannels(context, l)
        val notification = NotificationCompat.Builder(context, CHANNEL_NOTICES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(l.getString(R.string.notice_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        post(context, ID_NOTICE, notification)
    }

    private fun post(context: Context, id: Int, notification: android.app.Notification) {
        try {
            manager(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; the full-screen path does not depend on it.
        }
    }
}
