package org.thamba.guard

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The only place where Kotlin talks to the Python decision engine.
 *
 * Starting Python takes a few seconds the first time, so nothing here may be
 * called on the main thread. Callers run their work on [worker].
 */
object Engine {

    /** One background thread for all engine work, so calls never overlap. */
    val worker: ExecutorService = Executors.newSingleThreadExecutor()

    @Synchronized
    private fun bridge(context: Context): PyObject {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context.applicationContext))
        }
        return Python.getInstance().getModule("thamba_engine.bridge")
    }

    /** Start Python ahead of time, for example when the phone rings, so the first warning is not delayed. */
    fun warmUp(context: Context) {
        val app = context.applicationContext
        worker.execute {
            try {
                bridge(app)
            } catch (e: Exception) {
                // The guards will try again when they need the engine.
            }
        }
    }

    fun onSms(context: Context, sender: String, text: String, onCall: Boolean, callerKnown: Boolean): JSONObject =
        JSONObject(bridge(context).callAttr("on_sms_json", sender, text, onCall, callerKnown).toString())

    fun onLink(context: Context, url: String): JSONObject =
        JSONObject(bridge(context).callAttr("on_link_json", url).toString())

    /** permissions is the app's requested permissions joined with commas. */
    fun onApp(context: Context, packageName: String, installer: String, permissions: String, isSystem: Boolean): JSONObject =
        JSONObject(bridge(context).callAttr("on_app_json", packageName, installer, permissions, isSystem).toString())

    fun onPayment(context: Context, foregroundPackage: String, onCall: Boolean, callerKnown: Boolean): JSONObject =
        JSONObject(bridge(context).callAttr("on_payment_json", foregroundPackage, onCall, callerKnown).toString())
}
