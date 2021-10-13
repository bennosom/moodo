package io.engst.moodo.shared

import android.util.Log

interface Logger {
    fun debug(supplier: () -> String)

    fun info(supplier: () -> String)

    fun warn(supplier: () -> String)

    fun error(tr: Throwable? = null, supplier: () -> String)
}

class LogCatLogger(
    private val tag: String? = "empty",
    private val prefix: String? = ""
) : Logger {
    companion object {
        inline fun <reified T : Any> create(tag: String? = null): Logger {
            return LogCatLogger(tag, logPrefix<T>())
        }
    }

    override fun debug(supplier: () -> String) {
        Log.d(tag, prefix + supplier())
    }

    override fun info(supplier: () -> String) {
        Log.i(tag, prefix + supplier())
    }

    override fun warn(supplier: () -> String) {
        Log.w(tag, prefix + supplier())
    }

    override fun error(tr: Throwable?, supplier: () -> String) {
        Log.e(tag, prefix + supplier(), tr)
    }
}

inline fun <reified T : Any> logPrefix(): String = "[${T::class.java.simpleName}] "
