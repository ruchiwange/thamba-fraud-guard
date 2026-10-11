package org.thamba.guard

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Shows the app in the language chosen during setup, whatever the phone's own language is. */
object Lang {
    fun wrap(base: Context): Context {
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale(Prefs.lang(base)))
        return base.createConfigurationContext(config)
    }
}
