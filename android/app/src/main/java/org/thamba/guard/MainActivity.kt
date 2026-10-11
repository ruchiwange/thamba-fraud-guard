package org.thamba.guard

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** Home and one-time setup. A family member uses this once; after that the guards run by themselves. */
class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Lang.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Engine.warmUp(this)

        mapOf(R.id.langMr to "mr", R.id.langHi to "hi", R.id.langEn to "en").forEach { (id, lang) ->
            findViewById<Button>(id).setOnClickListener {
                Prefs.setLang(this, lang)
                recreate()
            }
        }

        val family = findViewById<EditText>(R.id.familyNumber)
        family.setText(Prefs.family(this))
        findViewById<Button>(R.id.saveFamily).setOnClickListener {
            Prefs.setFamily(this, family.text.toString().trim())
            Toast.makeText(this, R.string.setup_saved, Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.allowPermissions).setOnClickListener {
            requestPermissions(neededPermissions(), 1)
        }
        findViewById<Button>(R.id.allowOverlay).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.checkLinks).setOnClickListener { askToOpenLinks() }
        findViewById<Button>(R.id.allowUsage).setOnClickListener {
            // Android shows a list of apps here; the person turns Thamba on in it.
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        findViewById<Button>(R.id.checkApps).setOnClickListener {
            startActivity(Intent(this, AppCheckActivity::class.java))
        }
        findViewById<Button>(R.id.moneyGone).setOnClickListener {
            startActivity(Intent(this, ComplaintActivity::class.java))
        }
        findViewById<Button>(R.id.testEngine).setOnClickListener {
            startActivity(Intent(this, TestActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        showStatus()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        showStatus()
    }

    private fun neededPermissions(): Array<String> {
        val list = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            @Suppress("DEPRECATION") Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ANSWER_PHONE_CALLS,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list += Manifest.permission.POST_NOTIFICATIONS
        }
        return list.toTypedArray()
    }

    /** Protection counts as on only when the code guard has everything it needs. */
    private fun showStatus() {
        val core = listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
        ).all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
        val ready = core && Settings.canDrawOverlays(this)

        val status = findViewById<TextView>(R.id.status)
        status.setText(if (ready) R.string.home_on else R.string.home_off)
        status.setBackgroundColor(getColor(if (ready) R.color.safe else R.color.warn))
        status.setTextColor(getColor(if (ready) R.color.on_strong else R.color.warn_ink))
    }

    /** Asks Android to send every tapped link to Thamba first. */
    private fun askToOpenLinks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_BROWSER) && !roles.isRoleHeld(RoleManager.ROLE_BROWSER)) {
                @Suppress("DEPRECATION")
                startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_BROWSER), 2)
                return
            }
        }
        startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }
}
