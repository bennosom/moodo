package io.engst.moodo.model

import io.engst.moodo.model.persistence.TaskDatabase
import io.engst.moodo.shared.logPrefix
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

const val TAG = "model"

val moduleModel = module {
    single { TaskDatabase.getInstance(get()).taskDao }
    single { TaskScheduler(get { parametersOf(TAG, logPrefix<TaskScheduler>()) }, get()) }
    single { TaskRepository(get(), get()) }
    single { NotificationHelper(get { parametersOf(TAG, logPrefix<NotificationHelper>()) }, get()) }
}