package com.example.personalfinanceapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val type: String, // "income" или "expense"
    val amount: Double,
    val category: String,
    val date: Date
)