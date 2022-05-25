package io.engst.moodo.model.persistence

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.engst.moodo.R
import io.engst.moodo.model.persistence.entity.TagEntity
import io.engst.moodo.model.persistence.entity.TagTaskEntity
import io.engst.moodo.model.persistence.entity.TaskEntity
import io.engst.moodo.model.persistence.entity.TaskListOrderEntity
import io.engst.moodo.ui.MainActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

const val LOG_TAG = "TaskDatabase"

@Database(
    version = 5,
    entities = [
        TaskEntity::class,
        TaskListOrderEntity::class,
        TagEntity::class,
        TagTaskEntity::class
    ]
)
@TypeConverters(TaskConverter::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao

    companion object {
        private var database: TaskDatabase? = null

        private val dbMigration1to2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // create temporary table
                db.execSQL("CREATE TABLE IF NOT EXISTS `task-v2` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `description` TEXT NOT NULL, `createdDate` TEXT NOT NULL, `dueDate` TEXT, `doneDate` TEXT, `redoCount` INTEGER NOT NULL, `shiftCount` INTEGER NOT NULL)")

                // copy data
                db.execSQL("INSERT INTO `task-v2` (`id`,`description`,`createdDate`,`dueDate`,`doneDate`,`redoCount`,`shiftCount`) SELECT id, description, createdDate, dueDate, doneDate, redoCount, shiftCount FROM `task`")

                // remove old table
                db.execSQL("DROP TABLE `task`")

                // change table name to the old one
                db.execSQL("ALTER TABLE `task-v2` RENAME TO `task`")
            }
        }

        private val dbMigration2to3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // add new table column
                db.execSQL("ALTER TABLE `task` ADD COLUMN `priority` INTEGER")
            }
        }

        private val dbMigration3to4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // create new table
                db.execSQL("CREATE TABLE IF NOT EXISTS `task_list_order` (`list_id` INTEGER PRIMARY KEY NOT NULL, `list_order` TEXT NOT NULL)")

                // add default entity
                val defaultOrder: List<Long> = emptyList() // no default order
                val a = Json.encodeToString(defaultOrder)
                db.execSQL("INSERT INTO `task_list_order` (`list_id`,`list_order`) VALUES (0, '$a')")
            }
        }

        private val dbMigration4to5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // create table: tag
                db.execSQL("CREATE TABLE IF NOT EXISTS `tag` (`tag_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` TEXT NOT NULL)")

                db.addDefaultTags()

                // modify table: task
                db.execSQL("CREATE TABLE IF NOT EXISTS `task-v5` (`task_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL, `createdDate` TEXT NOT NULL, `dueDate` TEXT, `doneDate` TEXT, `priority` INTEGER)")
                db.execSQL("INSERT INTO `task-v5` (`task_id`,`description`,`createdDate`,`dueDate`,`doneDate`,`priority`) SELECT id, description, createdDate, dueDate, doneDate, priority FROM `task`")
                db.execSQL("DROP TABLE `task`")
                db.execSQL("ALTER TABLE `task-v5` RENAME TO `task`")

                // create table: tag_task
                db.execSQL("CREATE TABLE IF NOT EXISTS `tag_task` (`ref_tag_id` INTEGER NOT NULL, `ref_task_id` INTEGER NOT NULL, PRIMARY KEY(`ref_tag_id`, `ref_task_id`), FOREIGN KEY(`ref_tag_id`) REFERENCES `tag`(`tag_id`) ON UPDATE CASCADE ON DELETE CASCADE , FOREIGN KEY(`ref_task_id`) REFERENCES `task`(`task_id`) ON UPDATE CASCADE ON DELETE CASCADE )")
            }
        }

        private fun SupportSQLiteDatabase.addDefaultTags() {
            execSQL("INSERT INTO `tag` (`tag_id`,`name`,`color`) VALUES(0, 'My Project', '#80cbc4')")
            execSQL("INSERT INTO `tag` (`tag_id`,`name`,`color`) VALUES(1, 'Family', '#b39ddb')")
            execSQL("INSERT INTO `tag` (`tag_id`,`name`,`color`) VALUES(2, 'Work', '#ffe082')")
        }

        fun getInstance(context: Context): TaskDatabase {
            if (database == null) {
                synchronized(TaskDatabase::class.java) {
                    database = Room
                        .databaseBuilder(
                            context.applicationContext,
                            TaskDatabase::class.java,
                            "moodoDb"
                        )
                        .setQueryCallback(
                            { sqlQuery, _ ->
                                Log.d(LOG_TAG, "queryCallback: $sqlQuery")
                            },
                            Executors.newFixedThreadPool(4)
                        )
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                // prepopulate some database contents on first run
                                db.addDefaultTags()
                            }
                        })
                        .addMigrations(dbMigration1to2)
                        .addMigrations(dbMigration2to3)
                        .addMigrations(dbMigration3to4)
                        .addMigrations(dbMigration4to5)
                        .build()
                }
            }
            return database!!
        }

        fun getSuggestedExportName(): String {
            if (database == null) {
                Log.e(LOG_TAG, "database not initialized")
                return "backup.db"
            }

            val dbName = database!!.openHelper.databaseName
            val dbSchemaVersion = database!!.openHelper.readableDatabase.version.toString()
            return "$dbName-v$dbSchemaVersion-${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            }.db"
        }

        @Throws(IOException::class)
        fun copy(src: InputStream, dest: OutputStream) {
            src.use { `in` ->
                dest.use { out ->
                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                }
            }
        }

        fun export(context: Context, fileUri: Uri) {
            if (database == null) {
                Log.e(LOG_TAG, "database not initialized")
                return
            }

            val dbName = database!!.openHelper.databaseName
            val dbFile = context.getDatabasePath(dbName)

            Log.d(LOG_TAG, "Export database $dbName")

            database!!.close()

            try {
                context.contentResolver
                    .openFileDescriptor(fileUri, "w")?.fileDescriptor?.let {
                        copy(FileInputStream(dbFile), FileOutputStream(it))
                    }

                Log.d(LOG_TAG, "Successfully exported database")

                Toast.makeText(
                    context,
                    context.getString(R.string.dialog_backup_success),
                    Toast.LENGTH_LONG
                ).show()
            } catch (ex: Exception) {
                Log.e(LOG_TAG, "Failed to export database", ex)
            }

            restartApp(context)
        }


        fun import(context: Context, fileUri: Uri) {
            if (database == null) {
                Log.e(LOG_TAG, "database not initialized")
                return
            }

            val dbName = database!!.openHelper.databaseName
            val dbFile = context.getDatabasePath(dbName)
            database!!.close()

            Log.d(LOG_TAG, "Import database $dbName")

            try {
                context.contentResolver
                    .openFileDescriptor(fileUri, "r")?.fileDescriptor?.let {
                        copy(FileInputStream(it), FileOutputStream(dbFile))
                        Log.d(LOG_TAG, "Successfully imported database")
                        restartApp(context)
                    }
            } catch (ex: Exception) {
                Log.e(LOG_TAG, "Failed to import database", ex)
            }
        }

        fun backup(context: Context) {
            if (database == null) {
                Log.e(LOG_TAG, "database not initialized")
                return
            }

            val dbName = database!!.openHelper.databaseName
            val dbSchemaVersion = database!!.openHelper.readableDatabase.version.toString()
            val fileName = "$dbName-v$dbSchemaVersion-${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            }.db"

            val databaseFile = context.getDatabasePath(dbName)
            val backupPath = context.filesDir
            val backupFile = "$backupPath/$fileName"

            Log.d(LOG_TAG, "Backup database $dbName at $databaseFile")

            database!!.close()
            databaseFile.copyTo(File(backupFile))

            Log.d(LOG_TAG, "Exported database to $backupFile")
            Toast.makeText(
                context,
                "${context.getString(R.string.dialog_backup_success)}: $backupFile",
                Toast.LENGTH_LONG
            ).show()

            restartApp(context)
        }

        fun showRestoreDialog(context: Context) {
            val backupPath = context.filesDir
            val backupFiles = backupPath.listFiles()

            if (backupFiles.isNullOrEmpty()) {
                Log.w(LOG_TAG, "No backups available at $backupPath")
                Toast.makeText(
                    context,
                    context.getString(R.string.dialog_backup_not_available),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val backupFileArray = backupFiles.map { it.name }.toTypedArray()

            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.dialog_restore_database))
                .setItems(backupFileArray) { _, which ->
                    val fileName = backupFileArray[which]
                    val file = File("$backupPath/$fileName")
                    restore(context, file)
                }
                .setOnCancelListener {
                    Log.d(LOG_TAG, "Restore dialog canceled")
                }
                .show()
        }

        private fun restore(context: Context, file: File) {
            Log.d(LOG_TAG, "Restore database from $file")

            val dbName = database!!.openHelper.databaseName
            val databaseFile = context.getDatabasePath(dbName)
            database!!.close()

            if (file.extension == "sqlite3" || file.extension == "db") {
                file.copyTo(databaseFile, overwrite = true)
                Log.d(LOG_TAG, "Restored database from $file")
                restartApp(context)
            } else {
                Log.e(LOG_TAG, "Invalid database file: $file")
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