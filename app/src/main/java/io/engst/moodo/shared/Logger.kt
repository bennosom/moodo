package io.engst.moodo.shared

import android.util.Log

class Logger(val tag: String) {
    inline fun debug(crossinline msg: () -> String) {
        Log.d(tag, msg(), null)
    }

    inline fun info(crossinline msg: () -> String) {
        Log.i(tag, msg(), null)
    }

    inline fun warn(crossinline msg: () -> String) {
        Log.w(tag, msg(), null)
    }

    inline fun error(t: Throwable? = null, crossinline msg: () -> String) {
        Log.e(tag, msg(), t)
    }
}

inline fun <reified T> createLogger(): Logger {
    return Logger(T::class.java.simpleName)
}
