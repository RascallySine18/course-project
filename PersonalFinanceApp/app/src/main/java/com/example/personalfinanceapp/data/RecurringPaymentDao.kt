package com.example.personalfinanceapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringPaymentDao {
    @Insert
    suspend fun insertRecurringPayment(payment: RecurringPayment)

    @Update
    suspend fun updateRecurringPayment(payment: RecurringPayment)

    @Query("SELECT * FROM recurring_payments WHERE userEmail = :email ORDER BY startDate DESC")
    fun getRecurringPaymentsForUser(email: String): Flow<List<RecurringPayment>>

    @Query("DELETE FROM recurring_payments WHERE id = :id")
    suspend fun deleteRecurringPayment(id: Long)
}