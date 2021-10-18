package io.engst.moodo.shared

import android.content.Intent

val Intent.extraAsString: String?
    get() = extras?.keySet()?.joinToString(", ", "[", "]") {
        "$it => ${extras?.get(it)}"
    }