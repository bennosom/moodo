package io.engst.moodo

import android.app.Application
import android.content.Context
import io.engst.moodo.model.moduleModel
import io.engst.moodo.shared.moduleShared
import io.engst.moodo.ui.moduleUi
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.atomic.AtomicBoolean

const val moodo = "moodo"
const val moodoIntentActionPrefix = "$moodo.intent.action"
const val moodoIntentExtraPrefix = "$moodo.intent.extra"

class MoodoApplication : Application() {

    companion object {
        private var initialized = AtomicBoolean(false)

        private fun setupKoin(context: Context) {
            if (initialized.compareAndSet(false, true)) {
                startKoin {
                    androidContext(context)
                    modules(
                        moduleShared,
                        moduleModel,
                        moduleUi
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        setupKoin(this)
    }
}