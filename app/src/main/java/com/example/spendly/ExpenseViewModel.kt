package com.example.spendly

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).expenseDao()

    // Live stream of all expenses
    val allExpenses = dao.getAllExpenses().asLiveData()

    // Total spent sum
    val totalSpent = dao.getTotalSpent().asLiveData()

    fun insert(expense: Expense) = viewModelScope.launch {
        dao.insertExpense(expense)
    }

    fun delete(expense: Expense) = viewModelScope.launch {
        dao.deleteExpense(expense)
    }
}