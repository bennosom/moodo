package io.engst.moodo.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger

class LifecycleEventLogger(lifecycleOwnerName: String) : LifecycleEventObserver {

    private val logger: Logger by injectLogger(tag = "lifecycle", prefix = lifecycleOwnerName)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        logger.debug { "onStateChanged=${event.name}" }
    }
}