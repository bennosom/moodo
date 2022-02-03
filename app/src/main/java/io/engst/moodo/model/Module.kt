package io.engst.moodo.model

import io.engst.moodo.model.persistence.TaskDatabase
import io.engst.moodo.shared.getLogger
import org.koin.dsl.module

val moduleModel = module {
    single { TaskDatabase.getInstance(get()).taskDao }
    single { TaskScheduler(getLogger<TaskScheduler>("scheduler"), get()) }
    single { TaskFactory(get()) }
    single { TaskRepository(get(), get(), get(), get()) }
    single { TaskNotifications(getLogger<TaskNotifications>("notification"), get(), get()) }
}