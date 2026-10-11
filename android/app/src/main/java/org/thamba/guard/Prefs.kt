package org.thamba.guard

import android.content.Context

/** Small settings kept on the phone. Nothing here is ever sent anywhere. */
object Prefs {
    private const val FILE = "thamba"

    private fun store(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** "mr", "hi" or "en". */
    fun lang(context: Context): String = store(context).getString("lang", "mr") ?: "mr"

    fun setLang(context: Context, lang: String) = store(context).edit().putString("lang", lang).apply()

    fun family(context: Context): String = store(context).getString("family", "") ?: ""

    fun setFamily(context: Context, number: String) = store(context).edit().putString("family", number).apply()

    fun onCall(context: Context): Boolean = store(context).getBoolean("on_call", false)

    fun callNumber(context: Context): String? = store(context).getString("call_number", null)

    /**
     * Remember the current call. Android reports a call more than once, sometimes
     * without the number, so a missing number never erases one we already have.
     */
    fun setCall(context: Context, onCall: Boolean, number: String?) {
        val edit = store(context).edit().putBoolean("on_call", onCall)
        if (!onCall) {
            edit.remove("call_number")
        } else if (!number.isNullOrBlank()) {
            edit.putString("call_number", number)
        }
        edit.apply()
    }
}
