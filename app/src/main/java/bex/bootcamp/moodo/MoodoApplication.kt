package bex.bootcamp.moodo

import android.app.Application
import android.content.Context
import bex.bootcamp.moodo.model.service.TaskRepository
import bex.bootcamp.moodo.model.service.persistence.TaskDatabase
import bex.bootcamp.moodo.shared.NotificationScheduler
import bex.bootcamp.moodo.ui.tasks.TaskListViewModel
import bex.bootcamp.moodo.ui.tasks.task.TaskViewModel
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
                            viewModel { TaskViewModel(get()) }
                        }
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        NotificationScheduler.createNotificationChannel(applicationContext)

        setupKoin(this)
    }
}