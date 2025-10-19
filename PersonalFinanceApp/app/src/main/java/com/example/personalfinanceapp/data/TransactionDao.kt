package com.example.personalfinanceapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC")
    fun getTransactionsForUser(email: String): Flow<List<Transaction>>

    @Query("SELECT SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END) - SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END) AS balance FROM transactions WHERE userEmail = :email")
    suspend fun getBalance(email: String): Double?
}