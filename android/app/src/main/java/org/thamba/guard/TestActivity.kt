package org.thamba.guard

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Test bench for developers and demos: type a message or a link and see the raw
 * verdict from the engine. It reads nothing from the phone.
 */
class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val sender = findViewById<EditText>(R.id.sender)
        val message = findViewById<EditText>(R.id.message)
        val onCall = findViewById<CheckBox>(R.id.onCall)
        val callerKnown = findViewById<CheckBox>(R.id.callerKnown)
        val link = findViewById<EditText>(R.id.link)
        val result = findViewById<TextView>(R.id.result)

        findViewById<Button>(R.id.checkSms).setOnClickListener {
            val from = sender.text.toString()
            val text = message.text.toString()
            val call = onCall.isChecked
            val known = callerKnown.isChecked
            show(result) { Engine.onSms(this, from, text, call, known).toString(2) }
        }
        findViewById<Button>(R.id.checkLink).setOnClickListener {
            val url = link.text.toString()
            show(result) { Engine.onLink(this, url).toString(2) }
        }
    }

    /** Run the engine in the background and show its answer, or the error text if the Python side fails. */
    private fun show(result: TextView, call: () -> String) {
        Engine.worker.execute {
            val text = try {
                call()
            } catch (e: Exception) {
                "Engine error: ${e.message}"
            }
            runOnUiThread { result.text = text }
        }
    }
}
