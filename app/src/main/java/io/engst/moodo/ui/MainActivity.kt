package io.engst.moodo.ui

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
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.tasks.TaskListViewModel
import io.engst.moodo.ui.tasks.edit.TaskEditFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val logger: Logger by injectLogger("view")
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

    init {
        lifecycle.addObserver(LifecycleEventLogger("MainActivity"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskScheduler.updateReminder()

        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar).apply {
            setNavigationOnClickListener {
                logger.debug { "clicked toolbar navigation button" }
                viewModel.scrollToday()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.backup -> {
                        logger.debug { "clicked toolbar menu export button" }
                        dbExportLauncher.launch(CreateFileParams(suggestedName = TaskDatabase.getSuggestedExportName()))
                        true
                    }
                    R.id.restore -> {
                        logger.debug { "clicked toolbar menu import button" }
                        dbImportLauncher.launch(SelectFileParams())
                        true
                    }
                    R.id.about -> {
                        logger.debug { "clicked toolbar menu info button" }
                        showAboutDialog()
                        true
                    }
                    else -> false
                }
            }
        }

        if (intent.hasExtra(extraTaskId)) {
            intent.getLongExtra(extraTaskId, -1L).takeIf { it > -1 }?.let { taskId ->
                logger.debug { "show task edit fragment for #$taskId" }
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
            .setNeutralButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}