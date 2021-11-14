package io.engst.moodo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import io.engst.moodo.R
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.TaskScheduler
import io.engst.moodo.model.persistence.TaskDatabase
import io.engst.moodo.model.types.extraTaskId
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.koinGet
import io.engst.moodo.ui.tasks.TaskListViewModel
import io.engst.moodo.ui.tasks.edit.TaskEditFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: TaskListViewModel by viewModel()
    private val repository: TaskRepository by inject()
    private val taskScheduler: TaskScheduler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskScheduler.updateReminder()

        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar).apply {
            setNavigationOnClickListener {
                viewModel.scrollToToday()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.backup -> {
                        TaskDatabase.backup(applicationContext)
                        true
                    }
                    R.id.restore -> {
                        TaskDatabase.showRestoreDialog(context)
                        true
                    }
                    R.id.about -> {
                        Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
        }

        if (intent.action == Intent.ACTION_VIEW) {
            intent.extras?.getLong(extraTaskId)?.let { taskId ->
                lifecycle.coroutineScope.launchWhenCreated {
                    val task = repository.getTask(taskId)
                    val sheet = TaskEditFragment(task)
                    sheet.show(supportFragmentManager, "taskEdit")
                }
            }
        }
    }
}