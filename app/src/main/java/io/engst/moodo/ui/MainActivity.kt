package io.engst.moodo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.engst.moodo.R
import io.engst.moodo.model.persistence.TaskDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val topAppBar =
            findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
        }
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.backup -> {
                    TaskDatabase.backup(applicationContext)
                    true
                }
                R.id.restore -> {
                    TaskDatabase.showRestoreDialog(this)
                    true
                }
                R.id.about -> {
                    false
                }
                else -> false
            }
        }
    }
}
