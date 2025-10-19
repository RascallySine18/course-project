package com.example.personalfinanceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeText: TextView = itemView.findViewById(R.id.typeText)
        private val amountText: TextView = itemView.findViewById(R.id.amountText)
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)

        fun bind(transaction: Transaction) {
            typeText.text = if (transaction.type == "income") "Доход" else "Расход"
            val numberFormat = NumberFormat.getNumberInstance(Locale("ru"))
            val formattedAmount = numberFormat.format(transaction.amount)
            amountText.text = "${if (transaction.type == "expense") "-" else ""}$formattedAmount ₽"
            categoryText.text = transaction.category
            dateText.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(transaction.date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}