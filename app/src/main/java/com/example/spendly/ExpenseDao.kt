package com.example.spendly

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Returns a live stream of all expenses ordered by latest first
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    // Get total sum for monthly dashboard summary
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalSpent(): Flow<Double?>
}