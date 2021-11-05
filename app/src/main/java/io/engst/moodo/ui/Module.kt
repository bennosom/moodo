package io.engst.moodo.ui

import io.engst.moodo.ui.tasks.TaskListViewModel
import io.engst.moodo.ui.tasks.edit.TaskEditViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val moduleUi = module {
    viewModel {
        TaskListViewModel(
            androidContext(),
            Dispatchers.Default,
            get(),
            get(),
            get()
        )
    }
    viewModel { TaskEditViewModel(get()) }
}