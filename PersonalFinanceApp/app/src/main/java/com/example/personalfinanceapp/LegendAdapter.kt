package com.example.personalfinanceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

data class LegendItem(val category: String, val amount: Double, val color: Int, val percentage: Int)

class LegendAdapter : ListAdapter<LegendItem, LegendAdapter.LegendViewHolder>(LegendDiffCallback()) {

    class LegendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.colorView)
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val amountText: TextView = itemView.findViewById(R.id.amountText)
        private val percentageText: TextView = itemView.findViewById(R.id.percentageText)

        fun bind(item: LegendItem) {
            colorView.setBackgroundColor(item.color)
            categoryText.text = item.category
            val numberFormat = NumberFormat.getNumberInstance(Locale("ru")).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            amountText.text = "${numberFormat.format(item.amount)} ₽"
            percentageText.text = "(${item.percentage}%)"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_legend, parent, false)
        return LegendViewHolder(view)
    }

    override fun onBindViewHolder(holder: LegendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LegendDiffCallback : DiffUtil.ItemCallback<LegendItem>() {
    override fun areItemsTheSame(oldItem: LegendItem, newItem: LegendItem): Boolean {
        return oldItem.category == newItem.category
    }

    override fun areContentsTheSame(oldItem: LegendItem, newItem: LegendItem): Boolean {
        return oldItem == newItem
    }
}