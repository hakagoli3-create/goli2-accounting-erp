package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseCategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeCategoryEntity
import com.example.data.model.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseAndIncomeDao {

    // --- Expense Categories ---
    @Query("SELECT * FROM expense_categories ORDER BY id ASC")
    fun getAllExpenseCategories(): Flow<List<ExpenseCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCategory(category: ExpenseCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCategories(categories: List<ExpenseCategoryEntity>)

    @Update
    suspend fun updateExpenseCategory(category: ExpenseCategoryEntity)

    @Delete
    suspend fun deleteExpenseCategory(category: ExpenseCategoryEntity)

    @Query("SELECT * FROM expense_categories WHERE id = :id LIMIT 1")
    suspend fun getExpenseCategoryById(id: Long): ExpenseCategoryEntity?

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE jalaliDate >= :startDate AND jalaliDate <= :endDate ORDER BY timestamp DESC")
    fun getExpensesBetweenDates(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Long?>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE jalaliDate = :today")
    fun getTodayExpenses(today: String): Flow<Long>

    // --- Income Categories ---
    @Query("SELECT * FROM income_categories ORDER BY id ASC")
    fun getAllIncomeCategories(): Flow<List<IncomeCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomeCategory(category: IncomeCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomeCategories(categories: List<IncomeCategoryEntity>)

    @Update
    suspend fun updateIncomeCategory(category: IncomeCategoryEntity)

    @Delete
    suspend fun deleteIncomeCategory(category: IncomeCategoryEntity)

    @Query("SELECT * FROM income_categories WHERE id = :id LIMIT 1")
    suspend fun getIncomeCategoryById(id: Long): IncomeCategoryEntity?

    // --- Incomes ---
    @Query("SELECT * FROM incomes ORDER BY timestamp DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getIncomesByCategory(categoryId: Long): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE jalaliDate >= :startDate AND jalaliDate <= :endDate ORDER BY timestamp DESC")
    fun getIncomesBetweenDates(startDate: String, endDate: String): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)

    @Query("SELECT * FROM incomes WHERE id = :id LIMIT 1")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Query("SELECT SUM(amount) FROM incomes")
    fun getTotalIncomes(): Flow<Long?>
}
