package io.engst.moodo.shared

import android.util.Log

class Logger(
    private val tag: String,
    private val prefix: String = ""
) {
    companion object {
        inline fun <reified T> create(tag: String? = null): Logger {
            return Logger(tag ?: "empty", "[${T::class.java.simpleName}] ")
        }
    }

    fun debug(supplier: () -> String) {
        Log.d(tag, prefix + supplier())
    }

    fun info(supplier: () -> String) {
        Log.i(tag, prefix + supplier())
    }

    fun warn(supplier: () -> String) {
        Log.w(tag, prefix + supplier())
    }

    fun error(tr: Throwable? = null, supplier: () -> String) {
        Log.e(tag, prefix + supplier(), tr)
    }
}


