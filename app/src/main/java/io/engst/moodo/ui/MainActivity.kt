package io.engst.moodo.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.engst.moodo.BuildConfig
import io.engst.moodo.R
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.TaskScheduler
import io.engst.moodo.model.persistence.TaskDatabase
import io.engst.moodo.model.types.extraTaskId
import io.engst.moodo.shared.inject
import io.engst.moodo.ui.tasks.TaskListViewModel
import io.engst.moodo.ui.tasks.edit.TaskEditFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: TaskListViewModel by viewModel()
    private val repository: TaskRepository by inject()
    private val taskScheduler: TaskScheduler by inject()

    private val dbExportLauncher =
        registerForActivityResult(CreateFileResultContract()) { fileUri ->
            fileUri?.let { TaskDatabase.export(this, it) }
        }
    private val dbImportLauncher =
        registerForActivityResult(SelectFileResultContract()) { fileUri ->
            fileUri?.let { TaskDatabase.import(this, it) }
        }

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
                        dbExportLauncher.launch(
                            CreateFileParams(
                                suggestedName = TaskDatabase.getSuggestedExportName(),
                            )
                        )
                        true
                    }
                    R.id.restore -> {
                        dbImportLauncher.launch(
                            SelectFileParams()
                        )
                        true
                    }
                    R.id.about -> {
                        showAboutDialog()
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
                    TaskEditFragment.show(supportFragmentManager, task)
                }
            }
        }
    }

    private fun showAboutDialog() {
        val customView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_about, null, false).also {
                it.findViewById<TextView>(R.id.app_version)?.apply {
                    text = "Version ${BuildConfig.VERSION_NAME}"
                }
            }

        MaterialAlertDialogBuilder(this)
            .setView(customView)
            .setTitle("About ${getString(R.string.app_name)}")
            .setIcon(R.drawable.ic_launcher_foreground)
            .setNeutralButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}