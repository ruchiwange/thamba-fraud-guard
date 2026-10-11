package org.thamba.guard

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/** Hands a link that passed the check to the phone's real browser. */
object Browser {

    fun open(context: Context, url: String) {
        val uri = Uri.parse(if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url")

        // Find a browser that is not this app, otherwise the link would come straight back here.
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val browsers = context.packageManager
            .queryIntentActivities(probe, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != context.packageName }
            .distinct()
        val target = browsers.firstOrNull { it == "com.android.chrome" } ?: browsers.firstOrNull()

        if (target == null) {
            Toast.makeText(context, Lang.wrap(context).getString(R.string.no_browser), Toast.LENGTH_LONG).show()
            return
        }
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).setPackage(target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Toast.makeText(context, Lang.wrap(context).getString(R.string.no_browser), Toast.LENGTH_LONG).show()
        }
    }
}
