package io.engst.moodo.model.service.persistence

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.engst.moodo.ui.MainActivity
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


const val TAG = "TaskDatabase"

@Database(
    version = 2,
    entities = [TaskEntity::class]
)
@TypeConverters(TaskConverter::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao

    companion object {
        private var database: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            if (database == null) {
                synchronized(TaskDatabase::class.java) {
                    database = Room.databaseBuilder(
                        context.applicationContext,
                        TaskDatabase::class.java,
                        "moodoDb"
                    ).build()
                }
            }
            return database!!
        }

        fun backup(context: Context) {
            if (database == null) {
                Log.e(TAG, "database not initialized")
                return
            }

            val dbName = database!!.openHelper.databaseName
            val backupPath = context.filesDir
            //val backupPath = context.getExternalFilesDir("backup")
            val databaseFile = context.getDatabasePath(dbName)

            Log.d(TAG, "Backup database $dbName at $databaseFile")

            database!!.close()

            val fileName = "$dbName-${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            }.sqlite3"

            databaseFile.copyTo(File("$backupPath/$fileName"))

            Log.d(TAG, "Saved $fileName to $backupPath")
            Toast.makeText(context, "Backup successfully", Toast.LENGTH_LONG)
                .show()
            restartApp(context)
        }

        fun showRestoreDialog(context: Context) {
            val backupPath = context.filesDir
            val backupFiles = backupPath.listFiles()

            if (backupFiles.isNullOrEmpty()) {
                Log.w(TAG, "No backups available at $backupPath")
                Toast.makeText(context, "No backups found", Toast.LENGTH_LONG).show()
                return
            }

            val backupFileArray = backupFiles.map { it.name }.toTypedArray()

            MaterialAlertDialogBuilder(context)
                .setTitle("Restore database")
                .setItems(backupFileArray) { _, which ->
                    val fileName = backupFileArray[which]
                    val file = File("$backupPath/$fileName")
                    restore(context, file)
                }
                .setOnCancelListener {
                    Log.d(TAG, "Restore dialog canceled")
                }
                .show()
        }

        private fun restore(context: Context, file: File) {
            Log.d(TAG, "Restore database from $file")

            val dbName = database!!.openHelper.databaseName
            val databaseFile = context.getDatabasePath(dbName)
            database!!.close()


            if (file.extension == "sqlite3") {
                file.copyTo(databaseFile, overwrite = true)
                Log.d(TAG, "Restored database from $file")
                restartApp(context)
            } else {
                Log.e(TAG, "File is no SQLite database: $file")
            }
        }

        private fun restartApp(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            if (context is Activity) {
                context.finish()
            }
            Runtime.getRuntime().exit(0)
        }
    }
}