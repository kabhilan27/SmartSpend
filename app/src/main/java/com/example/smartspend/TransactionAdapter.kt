package com.example.smartspend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.transaction_title)
        val amount: TextView = itemView.findViewById(R.id.transaction_amount)
        val category: TextView = itemView.findViewById(R.id.transaction_category)
        val date: TextView = itemView.findViewById(R.id.transaction_date)
        val editButton: Button = itemView.findViewById(R.id.edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.title.text = transaction.title
        holder.amount.text = if (transaction.isExpense) "-$${String.format("%.2f", transaction.amount)}" else "+$${String.format("%.2f", transaction.amount)}"
        holder.amount.setTextColor(
            if (transaction.isExpense) 0xFFFF6F61.toInt() else 0xFF26A69A.toInt()
        )
        holder.category.text = transaction.category
        holder.date.text = transaction.date

        // Set click listeners
        holder.editButton.setOnClickListener {
            onEditClick(transaction)
        }
        holder.deleteButton.setOnClickListener {
            onDeleteClick(transaction)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
        notifyItemInserted(transactions.size - 1)
    }
}