package com.example.smartspend

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val isExpense: Boolean
)