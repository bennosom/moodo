package io.engst.moodo

import android.app.Application
import android.content.Context
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.persistence.TaskDatabase
import io.engst.moodo.shared.NotificationUtils
import io.engst.moodo.ui.tasks.TaskListViewModel
import io.engst.moodo.ui.tasks.edit.TaskEditViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicBoolean

class MoodoApplication : Application() {

    companion object {
        const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"

        private var initialized = AtomicBoolean(false)

        private fun setupKoin(context: Context) {
            if (initialized.compareAndSet(false, true)) {
                startKoin {
                    androidContext(context)
                    modules(
                        module {
                            single { TaskDatabase.getInstance(context).taskDao }
                            single { TaskRepository(get()) }

                            viewModel { TaskListViewModel(get()) }
                            viewModel { TaskEditViewModel(get()) }
                        }
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        NotificationUtils.createNotificationChannel(applicationContext)

        setupKoin(this)
    }
}