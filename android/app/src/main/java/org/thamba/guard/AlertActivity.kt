package org.thamba.guard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.telecom.TelecomManager
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/** The full-screen warning. One layout serves the code, link and message guards. */
class AlertActivity : AppCompatActivity() {

    private var speaker: Speaker? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Lang.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        Notifier.clearAlert(this)

        val tone = intent.getStringExtra(EXTRA_TONE) ?: "red"
        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_CODE
        val line = intent.getStringExtra(EXTRA_LINE) ?: ""
        val why = intent.getStringExtra(EXTRA_WHY) ?: ""
        val detail = intent.getStringExtra(EXTRA_DETAIL)
        val url = intent.getStringExtra(EXTRA_URL)
        val realSite = intent.getStringExtra(EXTRA_REAL_SITE)

        val title = findViewById<TextView>(R.id.alertTitle)
        val lineView = findViewById<TextView>(R.id.alertLine)
        val whyView = findViewById<TextView>(R.id.alertWhy)
        val detailView = findViewById<TextView>(R.id.alertDetail)
        val primary = findViewById<MaterialButton>(R.id.alertPrimary)
        val secondary = findViewById<MaterialButton>(R.id.alertSecondary)
        val close = findViewById<TextView>(R.id.alertClose)

        title.text = intent.getStringExtra(EXTRA_TITLE)
        lineView.text = line
        whyView.text = why
        detailView.text = detail
        detailView.visibility = if (detail.isNullOrBlank()) View.GONE else View.VISIBLE

        // Colours: red and amber grounds need different text colours to stay readable.
        val ground = getColor(if (tone == "amber") R.color.warn else R.color.stop)
        val ink = if (tone == "amber") getColor(R.color.warn_ink) else Color.WHITE
        findViewById<View>(R.id.alertRoot).setBackgroundColor(ground)
        @Suppress("DEPRECATION")
        window.statusBarColor = ground
        listOf(title, lineView, whyView, detailView, close).forEach { it.setTextColor(ink) }
        primary.backgroundTintList = ColorStateList.valueOf(ink)
        primary.setTextColor(ground)
        secondary.setTextColor(ink)
        secondary.strokeColor = ColorStateList.valueOf(ink)
        close.paintFlags = close.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        when (mode) {
            MODE_CODE -> {
                primary.setText(R.string.btn_cut_call)
                primary.setOnClickListener { cutCall() }
                secondary.setText(R.string.btn_call_family)
                secondary.setOnClickListener { callFamily() }
                // The close option wakes up after three seconds, so the warning is heard once.
                close.isEnabled = false
                close.alpha = 0.4f
                Handler(Looper.getMainLooper()).postDelayed({
                    close.isEnabled = true
                    close.alpha = 1f
                }, 3000)
            }
            MODE_LINK_RED -> {
                primary.setText(R.string.btn_go_back)
                primary.setOnClickListener { finish() }
                if (realSite != null) {
                    secondary.setText(R.string.btn_open_real)
                    secondary.setOnClickListener {
                        Browser.open(this, realSite)
                        finish()
                    }
                } else {
                    secondary.visibility = View.GONE
                }
                close.visibility = View.GONE
            }
            MODE_LINK_AMBER -> {
                primary.setText(R.string.btn_go_back)
                primary.setOnClickListener { finish() }
                secondary.setText(R.string.btn_open_anyway)
                secondary.setOnClickListener {
                    if (url != null) Browser.open(this, url)
                    finish()
                }
                close.visibility = View.GONE
            }
            MODE_PAYMENT -> {
                primary.setText(R.string.btn_family_first)
                primary.setOnClickListener { callFamily() }
                // The payment is never blocked: this simply closes the warning.
                secondary.setText(R.string.btn_paying_myself)
                secondary.setOnClickListener { finish() }
                close.visibility = View.GONE
            }
            else -> { // MODE_SENDER
                primary.setText(R.string.btn_close)
                primary.setOnClickListener { finish() }
                secondary.setText(R.string.btn_call_family)
                secondary.setOnClickListener { callFamily() }
                close.visibility = View.GONE
            }
        }
        close.setOnClickListener { finish() }

        if (tone == "red") vibrate()
        speaker = Speaker(this, Prefs.lang(this)).also {
            it.say(listOfNotNull(line, why, detail).joinToString(". "))
        }
    }

    override fun onDestroy() {
        speaker?.shutdown()
        super.onDestroy()
    }

    private fun vibrate() {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 400), -1))
    }

    /** Ends the call if Android allows it. Either way the warning closes. */
    private fun cutCall() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
            ) {
                @Suppress("DEPRECATION")
                (getSystemService(Context.TELECOM_SERVICE) as TelecomManager).endCall()
            }
        } catch (e: Exception) {
            // The person can still end the call from the phone screen.
        }
        finish()
    }

    /** Opens the dialler with the family number filled in. The person presses call. */
    private fun callFamily() {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Prefs.family(this))))
        finish()
    }

    companion object {
        const val MODE_CODE = "code"
        const val MODE_LINK_RED = "link_red"
        const val MODE_LINK_AMBER = "link_amber"
        const val MODE_SENDER = "sender"
        const val MODE_PAYMENT = "payment"

        private const val EXTRA_TONE = "tone"
        private const val EXTRA_MODE = "mode"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_LINE = "line"
        private const val EXTRA_WHY = "why"
        private const val EXTRA_DETAIL = "detail"
        private const val EXTRA_URL = "url"
        private const val EXTRA_REAL_SITE = "real_site"

        fun intent(context: Context, screen: Screen): Intent =
            Intent(context, AlertActivity::class.java)
                .putExtra(EXTRA_TONE, screen.tone)
                .putExtra(EXTRA_MODE, screen.mode)
                .putExtra(EXTRA_TITLE, screen.title)
                .putExtra(EXTRA_LINE, screen.line)
                .putExtra(EXTRA_WHY, screen.why)
                .putExtra(EXTRA_DETAIL, screen.detail)
                .putExtra(EXTRA_URL, screen.url)
                .putExtra(EXTRA_REAL_SITE, screen.realSite)
    }
}
