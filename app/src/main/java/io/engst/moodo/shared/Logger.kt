package io.engst.moodo.shared

import android.util.Log

interface Logger {
    fun debug(block: () -> String)

    fun info(block: () -> String)

    fun warn(block: () -> String)

    fun error(throwable: Throwable? = null, block: () -> String)
}

class LogCatLogger(
    private val tag: String?,
    private val prefix: String? = ""
) : Logger {
    companion object {
        inline fun <reified T : Any> create(tag: String? = null, prefix: String? = null): Logger {
            return LogCatLogger(tag, prefix?.let { "[$prefix] " } ?: logPrefix<T>())
        }
    }

    override fun debug(block: () -> String) {
        Log.d(tag, prefix + block())
    }

    override fun info(block: () -> String) {
        Log.i(tag, prefix + block())
    }

    override fun warn(block: () -> String) {
        Log.w(tag, prefix + block())
    }

    override fun error(throwable: Throwable?, block: () -> String) {
        Log.e(tag, prefix + block(), throwable)
    }
}

fun logPrefix(prefix: String): String = "[$prefix] "

inline fun <reified T : Any> logPrefix(): String = logPrefix(T::class.java.simpleName)
