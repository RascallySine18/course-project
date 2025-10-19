package com.example.personalfinanceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.RecurringPayment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RecurringPaymentAdapter(
    private val onDeleteClick: (Long) -> Unit,
    private val onEditClick: (Long) -> Unit
) : ListAdapter<RecurringPayment, RecurringPaymentAdapter.RecurringPaymentViewHolder>(RecurringPaymentDiffCallback()) {

    class RecurringPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val amountText: TextView = itemView.findViewById(R.id.amountText)
        private val periodicityText: TextView = itemView.findViewById(R.id.periodicityText)
        private val startDateText: TextView = itemView.findViewById(R.id.startDateText)
        private val timeOfDayText: TextView = itemView.findViewById(R.id.timeOfDayText)
        private val endDateText: TextView = itemView.findViewById(R.id.endDateText)
        private val deleteBtn: Button = itemView.findViewById(R.id.deleteBtn)
        private val editBtn: Button = itemView.findViewById(R.id.editBtn)

        fun bind(payment: RecurringPayment, onDeleteClick: (Long) -> Unit, onEditClick: (Long) -> Unit) {
            titleText.text = payment.title
            val numberFormat = NumberFormat.getNumberInstance(Locale("ru")).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            amountText.text = "${numberFormat.format(payment.amount)} ₽"
            periodicityText.text = payment.periodicity
            startDateText.text = "Начало: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(payment.startDate)}"
            timeOfDayText.text = "Время: ${payment.timeOfDay}"
            endDateText.text = "Окончание: ${payment.endDate?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it) } ?: "Без окончания"}"
            deleteBtn.setOnClickListener { onDeleteClick(payment.id) }
            editBtn.setOnClickListener { onEditClick(payment.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecurringPaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recurring_payment, parent, false)
        return RecurringPaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecurringPaymentViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick, onEditClick)
    }
}

class RecurringPaymentDiffCallback : DiffUtil.ItemCallback<RecurringPayment>() {
    override fun areItemsTheSame(oldItem: RecurringPayment, newItem: RecurringPayment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RecurringPayment, newItem: RecurringPayment): Boolean {
        return oldItem == newItem
    }
}