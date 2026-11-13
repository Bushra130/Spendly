package com.example.spendly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategoryDate: TextView = itemView.findViewById(R.id.tvCategoryDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(expense: Expense) {
            tvTitle.text = expense.title
            tvCategoryDate.text = "${expense.category} • ${expense.date}"
            tvAmount.text = "₹${expense.amount}"
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean = oldItem == newItem
    }
}