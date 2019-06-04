package me.nubuscu.hotpotato.util

import android.media.AudioManager
import android.media.ToneGenerator

class BeepHelper {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_DTMF, 100)

    fun beep(duration: Int) {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, duration)
    }

    fun release() {
        toneGenerator.stopTone()
        toneGenerator.release()
    }
}
