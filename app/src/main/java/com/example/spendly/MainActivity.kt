package com.example.spendly

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
class MainActivity : AppCompatActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvTotalSpent = findViewById<TextView>(R.id.tvTotalSpent)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        val btnAddExpense = findViewById<Button>(R.id.btnAddExpense)
        val btnScanReceipt = findViewById<Button>(R.id.btnScanReceipt)
        btnScanReceipt.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java).apply {
                putExtra("AUTO_SCAN", true)
            }
            startActivity(intent)
        }
        val adapter = ExpenseAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Observe total spent
        viewModel.totalSpent.observe(this) { total ->
            val sum = total ?: 0.0
            tvTotalSpent.text = "₹$sum"
        }

        // Observe all expenses list
        viewModel.allExpenses.observe(this) { expenses ->
            adapter.submitList(expenses)
        }

        // Temporary click listener to test inserting dummy data
        btnAddExpense.setOnClickListener {
            val sampleExpense = Expense(
                title = "Groceries",
                amount = 1240.0,
                category = "Food",
                date = "31 Oct 2026",
                note = "Weekly shopping"
            )
            viewModel.insert(sampleExpense)
        }
        btnAddExpense.setOnClickListener {
            val intent = android.content.Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }
        val btnViewReports = findViewById<Button>(R.id.btnViewReports)
        btnViewReports.setOnClickListener {
            val intent = android.content.Intent(this, ReportsActivity::class.java)
            startActivity(intent)
        }
    }

}