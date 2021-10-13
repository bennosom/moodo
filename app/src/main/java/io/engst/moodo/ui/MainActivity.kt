package io.engst.moodo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.engst.moodo.R
import io.engst.moodo.model.persistence.TaskDatabase
import io.engst.moodo.ui.tasks.TaskListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: TaskListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }
}
