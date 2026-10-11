package org.thamba.guard

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.concurrent.thread

/** Reads a warning aloud in the chosen language, if the phone has a voice for it. */
class Speaker(context: Context, private val lang: String) : TextToSpeech.OnInitListener {

    private val lock = Any()
    private var ready = false
    private var closed = false
    private var waiting: String? = null
    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        // Choosing the language loads the phone's list of voices, which can take
        // seconds. Doing it on the main thread would freeze the warning screen.
        thread(name = "thamba-voice") {
            try {
                val result = tts.setLanguage(Locale(lang, "IN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // No voice for this language on this phone. Stay silent; the screen still warns.
                    return@thread
                }
                val text = synchronized(lock) {
                    ready = true
                    waiting.also { waiting = null }
                }
                if (text != null) say(text)
            } catch (e: Exception) {
                // Speech is a help, not a requirement.
            }
        }
    }

    fun say(text: String) {
        synchronized(lock) {
            if (closed) return
            if (!ready) {
                waiting = text
                return
            }
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "thamba-warning")
    }

    fun shutdown() {
        synchronized(lock) { closed = true }
        tts.stop()
        tts.shutdown()
    }
}
