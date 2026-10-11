package org.thamba.guard

import android.content.Context
import org.json.JSONObject
import java.util.Locale

/** Everything one warning screen needs to show. */
data class Screen(
    val tone: String,          // "red" or "amber"
    val mode: String,          // which buttons to show: see AlertActivity
    val title: String,
    val line: String,
    val why: String,
    val detail: String? = null,
    val url: String? = null,
    val realSite: String? = null,
)

/**
 * Turns an engine verdict into words in the chosen language.
 * `l` must be a context from Lang.wrap so the strings come out in that language.
 */
object Texts {

    private fun rupees(amount: Double): String = String.format(Locale("en", "IN"), "%,.0f", amount)

    private fun amountOf(code: JSONObject): Double? =
        if (code.isNull("amount")) null else code.optDouble("amount")

    fun codeAlert(l: Context, code: JSONObject): Screen {
        val amount = amountOf(code)
        val (why, does) = when (code.optString("kind")) {
            "payment" -> l.getString(R.string.code_why_payment) to
                if (amount != null) l.getString(R.string.code_does_payment_amount, rupees(amount))
                else l.getString(R.string.code_does_payment)
            "identity" -> l.getString(R.string.code_why_identity) to l.getString(R.string.code_does_identity)
            else -> l.getString(R.string.code_why_login) to l.getString(R.string.code_does_login)
        }
        return Screen(
            tone = "red",
            mode = AlertActivity.MODE_CODE,
            title = l.getString(R.string.alert_stop),
            line = l.getString(R.string.code_line),
            why = why,
            detail = does,
        )
    }

    /** One line for the small notification, or null when the code needs no comment. */
    fun codeNotice(l: Context, code: JSONObject): String? {
        val amount = amountOf(code)
        return when (code.optString("kind")) {
            "delivery" -> l.getString(R.string.notice_delivery)
            "payment" ->
                if (amount != null) l.getString(R.string.notice_payment_amount, rupees(amount))
                else l.getString(R.string.notice_payment)
            "login" -> l.getString(R.string.notice_login)
            "identity" -> l.getString(R.string.notice_identity)
            else -> null
        }
    }

    fun linkScreen(l: Context, link: JSONObject, url: String?): Screen {
        val host = link.optString("host")
        val realSite = if (link.isNull("real_site")) null else link.optString("real_site")
        if (link.optString("level") == "red") {
            val why = when (link.optString("reason")) {
                "link_lookalike" -> l.getString(R.string.link_why_lookalike)
                "link_app_file" -> l.getString(R.string.link_why_app_file)
                "link_ip_address" -> l.getString(R.string.link_why_ip)
                else -> l.getString(R.string.link_why_bait)
            }
            return Screen(
                tone = "red",
                mode = AlertActivity.MODE_LINK_RED,
                title = l.getString(R.string.link_danger),
                line = l.getString(R.string.link_line),
                why = why,
                detail = if (realSite != null) l.getString(R.string.link_real, realSite) else host,
                url = url,
                realSite = realSite,
            )
        }
        val why = if (link.optString("reason") == "link_shortened") l.getString(R.string.link_why_shortened)
        else l.getString(R.string.link_why_risky)
        return Screen(
            tone = "amber",
            mode = AlertActivity.MODE_LINK_AMBER,
            title = l.getString(R.string.careful),
            line = l.getString(R.string.link_amber_line),
            why = why,
            detail = host,
            url = url,
        )
    }

    fun paymentScreen(l: Context): Screen = Screen(
        tone = "amber",
        mode = AlertActivity.MODE_PAYMENT,
        title = l.getString(R.string.alert_stop),
        line = l.getString(R.string.payment_line),
        why = l.getString(R.string.payment_why),
    )

    fun senderScreen(l: Context): Screen = Screen(
        tone = "amber",
        mode = AlertActivity.MODE_SENDER,
        title = l.getString(R.string.careful),
        line = l.getString(R.string.sender_line),
        why = l.getString(R.string.sender_why),
    )
}
