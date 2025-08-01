package com.example.smartspend

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class DataBackupManager(private val context: Context) {

    companion object {
        private const val BACKUP_FILE_NAME = "smartspend_backup.json"
        private const val TAG = "DataBackupManager"
    }

    fun exportTransactions(transactions: List<Transaction>): Result<String> {
        try {
            val jsonArray = JSONArray()
            transactions.forEach { transaction ->
                val jsonObject = JSONObject().apply {
                    put("id", transaction.id)
                    put("title", transaction.title)
                    put("amount", transaction.amount)
                    put("category", transaction.category)
                    put("date", transaction.date)
                    put("isExpense", transaction.isExpense)
                }
                jsonArray.put(jsonObject)
            }

            context.openFileOutput(BACKUP_FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write(jsonArray.toString().toByteArray())
            }
            Log.d(TAG, "Backup successful to ${context.filesDir}/$BACKUP_FILE_NAME")
            return Result.success("Transactions exported successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Backup failed due to IO error: ${e.message}")
            return Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed: ${e.message}")
            return Result.failure(e)
        }
    }

    fun importTransactions(): Result<List<Transaction>> {
        val transactions = mutableListOf<Transaction>()
        try {
            val file = File(context.filesDir, BACKUP_FILE_NAME)
            if (!file.exists()) {
                Log.d(TAG, "No backup file found at ${file.absolutePath}")
                return Result.success(emptyList())
            }

            context.openFileInput(BACKUP_FILE_NAME).use { input ->
                val jsonString = input.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val transaction = Transaction(
                        id = jsonObject.getInt("id"),
                        title = jsonObject.getString("title"),
                        amount = jsonObject.getDouble("amount"),
                        category = jsonObject.getString("category"),
                        date = jsonObject.getString("date"),
                        isExpense = jsonObject.getBoolean("isExpense")
                    )
                    transactions.add(transaction)
                }
            }
            Log.d(TAG, "Restore successful, imported ${transactions.size} transactions")
            return Result.success(transactions)
        } catch (e: IOException) {
            Log.e(TAG, "Restore failed due to IO error: ${e.message}")
            return Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed: ${e.message}")
            return Result.failure(e)
        }
    }
}