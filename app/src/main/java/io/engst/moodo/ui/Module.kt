package io.engst.moodo.ui

import io.engst.moodo.ui.tasks.TaskListViewModel
import io.engst.moodo.ui.tasks.edit.TaskEditViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val moduleUi = module {
    viewModel { TaskListViewModel(get()) }
    viewModel { TaskEditViewModel(get()) }
}