package org.thamba.guard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * "Money gone?" helper. Four questions become one clear statement the person can
 * read to the 1930 operator. Nothing typed here is saved or sent by the app.
 */
class ComplaintActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Lang.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint)

        val amount = findViewById<EditText>(R.id.amount)
        val whenField = findViewById<EditText>(R.id.whenHappened)
        val how = findViewById<EditText>(R.id.how)
        val txn = findViewById<EditText>(R.id.txn)
        val output = findViewById<View>(R.id.output)
        val statement = findViewById<TextView>(R.id.statement)

        findViewById<Button>(R.id.makeComplaint).setOnClickListener {
            statement.text = getString(
                R.string.complaint_text,
                whenField.text.toString().trim().ifEmpty { "-" },
                amount.text.toString().trim().ifEmpty { "-" },
                how.text.toString().trim().ifEmpty { "-" },
                txn.text.toString().trim().ifEmpty { "-" },
            )
            output.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.call1930).setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930")))
        }

        findViewById<Button>(R.id.copyStatement).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("complaint", statement.text))
            Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
        }
    }
}
