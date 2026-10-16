package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DatabaseBackupHelper {

    private const val DB_NAME = "goli_accounting_erp.db"

    /**
     * Create a backup .db file on device storage and return the File.
     */
    suspend fun createDatabaseBackup(context: Context): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Checkpoint SQLite WAL database so memory/log is flushed to main file
            val appDb = AppDatabase.getInstance(context)
            try {
                val db = appDb.openHelper.writableDatabase
                val cursor = db.query("PRAGMA wal_checkpoint(FULL)")
                cursor.moveToFirst()
                cursor.close()
            } catch (_: Exception) {
            }

            val currentDbPath = context.getDatabasePath(DB_NAME)
            if (!currentDbPath.exists()) {
                return@withContext Result.failure(Exception("پایگاه داده یافت نشد."))
            }

            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = JalaliDate.now().toStandardString().replace("/", "-")
            val backupFile = File(backupDir, "Goli_Accounting_Backup_$timestamp.db")

            FileInputStream(currentDbPath).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share backup file with external apps (Telegram, WhatsApp, Drive, Files)
     */
    fun shareBackupFile(context: Context, backupFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            backupFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "پشتیبان دیتابیس حسابداری گلی")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "اشتراک‌گذاری فایل پشتیبان (Backup)")
        context.startActivity(chooser)
    }

    /**
     * Restore database from an existing backup file or input stream.
     */
    suspend fun restoreDatabaseFromFile(context: Context, backupUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentDbPath = context.getDatabasePath(DB_NAME)
            val walFile = File(currentDbPath.parent, "$DB_NAME-wal")
            val shmFile = File(currentDbPath.parent, "$DB_NAME-shm")

            context.contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(currentDbPath).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("امکان بازگشایی فایل پشتیبان وجود ندارد."))

            // Delete obsolete WAL/SHM files to let the restored DB open cleanly
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
