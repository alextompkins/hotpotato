package me.nubuscu.hotpotato.util

import android.content.Context
import java.lang.ref.WeakReference

class DataHolder {
    companion object {
        val instance = DataHolder()
    }

    lateinit var context: WeakReference<Context>
}