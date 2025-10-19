package com.example.personalfinanceapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.NumberFormat
import java.util.Locale

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TITLE") ?: "Напоминание"
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        val numberFormat = NumberFormat.getNumberInstance(Locale("ru")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val message = "Сумма: ${numberFormat.format(amount)} ₽"

        val notification = NotificationCompat.Builder(context, "recurring_payments_channel")
            .setSmallIcon(R.drawable.ic_add)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }
}