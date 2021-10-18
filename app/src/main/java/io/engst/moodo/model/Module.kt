package io.engst.moodo.model

import io.engst.moodo.model.persistence.TaskDatabase
import io.engst.moodo.shared.logPrefix
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

const val TAG = "model"

val moduleModel = module {
    single { TaskDatabase.getInstance(get()).taskDao }
    single { ReminderScheduler(get { parametersOf(TAG, logPrefix<ReminderScheduler>()) }, get()) }
    single { TaskRepository(get(), get()) }
    single { NotificationHelper(get { parametersOf(TAG, logPrefix<NotificationHelper>()) }, get()) }
}