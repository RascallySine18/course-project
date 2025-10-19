package com.example.personalfinanceapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "recurring_payments")
data class RecurringPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val title: String,
    val periodicity: String, // "Ежедневно", "Еженедельно", "Ежемесячно", "Ежегодно"
    val startDate: Date,
    val timeOfDay: String, // Формат "HH:mm", например, "14:30"
    val endDate: Date?,
    val amount: Double
)