package org.thamba.guard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** Shows the dangerous apps found on the phone, each with a reason and a remove button. */
class AppCheckActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Lang.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)
    }

    /** Scan again each time the screen returns, so a removed app disappears from the list. */
    override fun onResume() {
        super.onResume()
        val list = findViewById<LinearLayout>(R.id.appsList)
        val allClear = findViewById<TextView>(R.id.appsNone)
        list.removeAllViews()

        val checking = findViewById<TextView>(R.id.appsChecking)
        allClear.visibility = View.GONE
        checking.visibility = View.VISIBLE

        Engine.worker.execute {
            val findings = AppScanner.scan(this)
            runOnUiThread {
                checking.visibility = View.GONE
                allClear.visibility = if (findings.isEmpty()) View.VISIBLE else View.GONE
                showFindings(list, findings)
            }
        }
    }

    private fun showFindings(list: LinearLayout, findings: List<AppScanner.Finding>) {
        list.removeAllViews()
        for (finding in findings) {
            val row = LayoutInflater.from(this).inflate(R.layout.row_app, list, false)
            row.findViewById<TextView>(R.id.appName).text = finding.label
            row.findViewById<TextView>(R.id.appWhy).setText(
                if (finding.reason == "app_remote_access") R.string.app_why_remote else R.string.app_why_sms
            )
            row.setBackgroundColor(getColor(if (finding.level == "red") R.color.stop_soft else R.color.warn_soft))
            row.findViewById<Button>(R.id.appRemove).setOnClickListener {
                // Opens Android's own "uninstall this app?" question.
                startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:" + finding.packageName)))
            }
            list.addView(row)
        }
    }
}
