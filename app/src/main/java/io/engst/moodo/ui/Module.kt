package io.engst.moodo.ui

import android.os.Vibrator
import androidx.core.content.getSystemService
import io.engst.moodo.ui.tasks.TaskListViewModel
import io.engst.moodo.ui.tasks.edit.TaskEditViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val moduleUi = module {
    single { androidContext().getSystemService<Vibrator>() }

    viewModel { TaskListViewModel(androidContext(), Dispatchers.Default, get(), get(), get()) }
    viewModel { TaskEditViewModel(get(), get(), get(), get()) }
}