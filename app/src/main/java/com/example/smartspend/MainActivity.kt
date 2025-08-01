package com.example.smartspend

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartspend.databinding.ActivityMainBinding
import com.example.smartspend.databinding.DialogAddTransactionBinding
import com.example.smartspend.databinding.DialogSetBudgetBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private lateinit var sharedPrefs: SharedPreferences
    private var budgetLimit = 1000.0
    private var totalExpenses = 0.0
    private var totalIncome = 0.0
    private var hasExceededBudget = false
    private lateinit var dataBackupManager: DataBackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.budgetProgress.max = 100
        binding.budgetProgress.progress = 0

        sharedPrefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        budgetLimit = sharedPrefs.getFloat("budgetLimit", 1000f).toDouble()
        totalExpenses = sharedPrefs.getFloat("totalExpenses", 0f).toDouble()
        totalIncome = sharedPrefs.getFloat("totalIncome", 0f).toDouble()
        loadTransactionsFromPrefs()

        dataBackupManager = DataBackupManager(this)

        setupRecyclerView()
        setupBottomNavigation()
        updateBudgetUI()

        binding.addTransactionBtn.setOnClickListener {
            showAddTransactionDialog()
        }

        binding.budgetCard.setOnClickListener {
            showSetBudgetDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            transactions,
            onEditClick = { transaction -> showEditTransactionDialog(transaction) },
            onDeleteClick = { transaction -> deleteTransaction(transaction) }
        )
        binding.transactionList.layoutManager = LinearLayoutManager(this)
        binding.transactionList.adapter = adapter
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> {
                    binding.transactionList.visibility = View.VISIBLE
                    binding.addTransactionBtn.visibility = View.VISIBLE
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, Fragment())
                        .commit()
                    true
                }
                R.id.nav_summary -> {
                    binding.transactionList.visibility = View.GONE
                    binding.addTransactionBtn.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TransactionSummaryFragment.newInstance())
                        .commit()
                    true
                }
                R.id.nav_backup -> {
                    showBackupOptionsDialog()
                    false
                }
                else -> false
            }
        }
    }

    private fun updateBudgetUI() {
        binding.budgetAmount.text = "$${String.format("%.2f", totalExpenses)} / $${String.format("%.2f", budgetLimit)}"

        val progress = if (budgetLimit > 0) {
            val percentage = (totalExpenses / budgetLimit) * 100
            percentage.coerceIn(0.0, 100.0).toInt()
        } else {
            0
        }

        binding.budgetProgress.max = 100
        binding.budgetProgress.progress = progress

        when {
            progress >= 90 -> binding.budgetProgress.progressTintList = getColorStateList(android.R.color.holo_red_light)
            progress >= 75 -> binding.budgetProgress.progressTintList = getColorStateList(android.R.color.holo_orange_light)
            else -> binding.budgetProgress.progressTintList = getColorStateList(R.color.accent)
        }

        if (totalExpenses >= budgetLimit && !hasExceededBudget) {
            showBudgetFilledAlert()
            hasExceededBudget = true
        } else if (totalExpenses < budgetLimit) {
            hasExceededBudget = false
        }
    }

    private fun showBudgetFilledAlert() {
        AlertDialog.Builder(this)
            .setTitle("Budget Limit Reached")
            .setMessage("Your total expenses ($${String.format("%.2f", totalExpenses)}) have reached or exceeded your monthly budget limit of $${String.format("%.2f", budgetLimit)}. Consider reviewing your spending.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showAddTransactionDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val categories = resources.getStringArray(R.array.transaction_categories)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.categorySpinner.adapter = spinnerAdapter

        dialogBinding.addButton.setOnClickListener {
            val title = dialogBinding.titleInput.text.toString()
            val amountStr = dialogBinding.amountInput.text.toString()
            val category = dialogBinding.categorySpinner.selectedItem.toString()
            val isExpense = dialogBinding.radioExpense.isChecked

            if (title.isNotEmpty() && amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0

                if (isExpense && (totalExpenses + amount) > budgetLimit) {
                    AlertDialog.Builder(this)
                        .setTitle("Cannot Add Transaction")
                        .setMessage("Adding this expense ($${String.format("%.2f", amount)}) would exceed your budget limit of $${String.format("%.2f", budgetLimit)}. Current expenses: $${String.format("%.2f", totalExpenses)}.")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .show()
                    return@setOnClickListener
                }

                val newId = transactions.size + 1
                val transaction = Transaction(
                    id = newId,
                    title = title,
                    amount = amount,
                    category = category,
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    isExpense = isExpense
                )
                this@MainActivity.adapter.addTransaction(transaction)
                if (isExpense) {
                    totalExpenses += amount
                } else {
                    totalIncome += amount
                }
                saveToPrefs()
                updateBudgetUI()
                dialog.dismiss()
            }
        }

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val widthInDp = 340
        val metrics = resources.displayMetrics
        val widthInPx = (widthInDp * metrics.density).toInt()
        dialog.window?.setLayout(widthInPx, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        val dialog = Dialog(this)
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.dialogTitle.text = "Edit Transaction"
        dialogBinding.titleInput.setText(transaction.title)
        dialogBinding.amountInput.setText(transaction.amount.toString())
        val categories = resources.getStringArray(R.array.transaction_categories)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.categorySpinner.adapter = spinnerAdapter
        dialogBinding.categorySpinner.setSelection(categories.indexOf(transaction.category))
        if (transaction.isExpense) {
            dialogBinding.radioExpense.isChecked = true
        } else {
            dialogBinding.radioIncome.isChecked = true
        }

        dialogBinding.addButton.text = "Save"
        dialogBinding.addButton.setOnClickListener {
            val title = dialogBinding.titleInput.text.toString()
            val amountStr = dialogBinding.amountInput.text.toString()
            val category = dialogBinding.categorySpinner.selectedItem.toString()
            val isExpense = dialogBinding.radioExpense.isChecked

            if (title.isNotEmpty() && amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0

                if (isExpense) {
                    val newTotalExpenses = totalExpenses - transaction.amount + amount
                    if (newTotalExpenses > budgetLimit) {
                        AlertDialog.Builder(this)
                            .setTitle("Cannot Edit Transaction")
                            .setMessage("Editing this expense to $${String.format("%.2f", amount)} would exceed your budget limit of $${String.format("%.2f", budgetLimit)}. Current expenses: $${String.format("%.2f", totalExpenses)}.")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .show()
                        return@setOnClickListener
                    }
                }

                val updatedTransaction = Transaction(
                    id = transaction.id,
                    title = title,
                    amount = amount,
                    category = category,
                    date = transaction.date,
                    isExpense = isExpense
                )

                if (transaction.isExpense) {
                    totalExpenses -= transaction.amount
                } else {
                    totalIncome -= transaction.amount
                }
                if (isExpense) {
                    totalExpenses += amount
                } else {
                    totalIncome += amount
                }

                val index = transactions.indexOfFirst { it.id == transaction.id }
                if (index != -1) {
                    transactions[index] = updatedTransaction
                    adapter.notifyItemChanged(index)
                }

                saveToPrefs()
                updateBudgetUI()
                dialog.dismiss()
            }
        }

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val widthInDp = 340
        val metrics = resources.displayMetrics
        val widthInPx = (widthInDp * metrics.density).toInt()
        dialog.window?.setLayout(widthInPx, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun deleteTransaction(transaction: Transaction) {
        if (transaction.isExpense) {
            totalExpenses -= transaction.amount
        } else {
            totalIncome -= transaction.amount
        }

        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions.removeAt(index)
            adapter.notifyItemRemoved(index)
        }

        saveToPrefs()
        updateBudgetUI()
    }

    private fun showSetBudgetDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogSetBudgetBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.budgetInput.setText(budgetLimit.toString())

        dialogBinding.saveBudgetButton.setOnClickListener {
            val budgetStr = dialogBinding.budgetInput.text.toString()
            if (budgetStr.isNotEmpty()) {
                budgetLimit = budgetStr.toDoubleOrNull() ?: budgetLimit
                saveToPrefs()
                updateBudgetUI()
                dialog.dismiss()
            }
        }

        dialogBinding.cancelBudgetButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBackupOptionsDialog() {
        val options = arrayOf("Export Transactions", "Import Transactions")
        AlertDialog.Builder(this)
            .setTitle("Backup Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportData()
                    1 -> importData()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun exportData() {
        dataBackupManager.exportTransactions(transactions).fold(
            onSuccess = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(this, "Export failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun importData() {
        AlertDialog.Builder(this)
            .setTitle("Restore Data")
            .setMessage("Restoring will overwrite existing transactions. Continue?")
            .setPositiveButton("Yes") { _, _ ->
                dataBackupManager.importTransactions().fold(
                    onSuccess = { importedTransactions ->
                        if (importedTransactions.isNotEmpty()) {
                            transactions.clear()
                            transactions.addAll(importedTransactions)
                            totalExpenses = importedTransactions.filter { it.isExpense }.sumOf { it.amount }
                            totalIncome = importedTransactions.filter { !it.isExpense }.sumOf { it.amount }
                            adapter.notifyDataSetChanged()
                            saveToPrefs()
                            updateBudgetUI()
                            Toast.makeText(this, "Imported ${importedTransactions.size} transactions", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "No transactions found in backup", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        Toast.makeText(this, "Import failed: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadTransactionsFromPrefs() {
        val transactionSet = sharedPrefs.getStringSet("transactions", emptySet()) ?: emptySet()
        transactions.clear()
        totalExpenses = 0.0
        totalIncome = 0.0
        transactionSet.forEach { transactionString ->
            val parts = transactionString.split("|")
            if (parts.size == 6) {
                val transaction = Transaction(
                    id = parts[0].toInt(),
                    title = parts[1],
                    amount = parts[2].toDouble(),
                    category = parts[3],
                    date = parts[4],
                    isExpense = parts[5].toBoolean()
                )
                transactions.add(transaction)
                if (transaction.isExpense) {
                    totalExpenses += transaction.amount
                } else {
                    totalIncome += transaction.amount
                }
            }
        }
    }

    private fun saveToPrefs() {
        val editor = sharedPrefs.edit()
        editor.putFloat("budgetLimit", budgetLimit.toFloat())
        editor.putFloat("totalExpenses", totalExpenses.toFloat())
        editor.putFloat("totalIncome", totalIncome.toFloat())
        val transactionSet = transactions.map { "${it.id}|${it.title}|${it.amount}|${it.category}|${it.date}|${it.isExpense}" }.toSet()
        editor.putStringSet("transactions", transactionSet)
        editor.apply()
    }
}