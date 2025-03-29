package com.example.finance

data class Transaction(
    val date: String ="",        // Дата транзакції
    val name: String ="",        // Категорія
    val description: String ="", // Примітка
    val amount: String ="",      // Сума
    val type: String ="",
    val id: String =""
)