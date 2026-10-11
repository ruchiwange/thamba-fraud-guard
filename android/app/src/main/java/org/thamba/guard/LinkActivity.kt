package org.thamba.guard

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Link guard. When Thamba is set as the phone's link opener, every tapped link
 * arrives here. Safe links go straight on to the real browser; risky ones get a
 * warning screen first. This screen itself is transparent.
 */
class LinkActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent?.dataString
        if (url.isNullOrBlank()) {
            finish()
            return
        }

        Engine.worker.execute {
            val verdict = try {
                Engine.onLink(this, url)
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                val l = Lang.wrap(this)
                when (verdict?.optString("level")) {
                    "red", "amber" -> startActivity(AlertActivity.intent(this, Texts.linkScreen(l, verdict, url)))
                    "green" -> {
                        Toast.makeText(this, l.getString(R.string.link_official), Toast.LENGTH_LONG).show()
                        Browser.open(this, url)
                    }
                    // Neutral, or the check itself failed: do not trap the person, open the link as usual.
                    else -> Browser.open(this, url)
                }
                finish()
            }
        }
    }
}
