package com.example.data.repository

import com.example.data.dao.AccountingDao
import com.example.data.dao.ExpenseAndIncomeDao
import com.example.data.dao.TreasuryDao
import com.example.data.model.ExpenseCategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeCategoryEntity
import com.example.data.model.IncomeEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import kotlinx.coroutines.flow.Flow

class ExpenseAndIncomeRepository(
    private val expenseAndIncomeDao: ExpenseAndIncomeDao,
    private val treasuryDao: TreasuryDao,
    private val accountingDao: AccountingDao
) {

    // --- Expense Categories ---
    val allExpenseCategories: Flow<List<ExpenseCategoryEntity>> =
        expenseAndIncomeDao.getAllExpenseCategories()

    suspend fun insertExpenseCategory(category: ExpenseCategoryEntity): Long =
        expenseAndIncomeDao.insertExpenseCategory(category)

    suspend fun updateExpenseCategory(category: ExpenseCategoryEntity) =
        expenseAndIncomeDao.updateExpenseCategory(category)

    suspend fun deleteExpenseCategory(category: ExpenseCategoryEntity) =
        expenseAndIncomeDao.deleteExpenseCategory(category)

    // --- Expenses ---
    val allExpenses: Flow<List<ExpenseEntity>> =
        expenseAndIncomeDao.getAllExpenses()

    val totalExpenses: Flow<Long?> =
        expenseAndIncomeDao.getTotalExpenses()

    fun getExpensesBetweenDates(startDate: String, endDate: String): Flow<List<ExpenseEntity>> =
        expenseAndIncomeDao.getExpensesBetweenDates(startDate, endDate)

    suspend fun createExpense(expense: ExpenseEntity): Long {
        // 1. Create a double-entry accounting journal voucher
        val voucherId = try {
            val entry = JournalEntryEntity(
                voucherNumber = 0L,
                jalaliDate = expense.jalaliDate,
                timestamp = expense.timestamp,
                description = "هزینه: ${expense.categoryTitle} - ${expense.title}",
                isBalanced = true,
                status = "FINAL"
            )
            // Subsidiary accounts:
            // 80101 (هزینه عمومی) -> Debit (بدهکار)
            // 10101 (صندوق) or 10102 (بانک) -> Credit (بستانکار)
            val creditSubCode = if (expense.paymentMethod == "CASH") 10101 else 10102
            val creditGenCode = 101

            val items = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 801,
                    subsidiaryCode = 80101,
                    description = "هزینه ${expense.categoryTitle} - ${expense.title}",
                    debit = expense.amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = creditGenCode,
                    subsidiaryCode = creditSubCode,
                    description = "پرداخت بابت ${expense.title} از ${expense.accountName}",
                    debit = 0L,
                    credit = expense.amount
                )
            )
            accountingDao.createVoucherWithItems(entry, items)
        } catch (e: Exception) {
            null
        }

        // 2. Adjust Treasury Balance (Subtract amount)
        if (expense.paymentMethod == "CASH" && expense.cashDeskId != null) {
            treasuryDao.updateCashBalance(expense.cashDeskId, -expense.amount)
        } else if (expense.paymentMethod == "BANK" && expense.bankAccountId != null) {
            treasuryDao.updateBankBalance(expense.bankAccountId, -expense.amount)
        }

        // 3. Save Expense Entity
        return expenseAndIncomeDao.insertExpense(expense.copy(voucherId = voucherId))
    }

    suspend fun updateExpense(oldExpense: ExpenseEntity, newExpense: ExpenseEntity) {
        // Revert old treasury balance effect
        if (oldExpense.paymentMethod == "CASH" && oldExpense.cashDeskId != null) {
            treasuryDao.updateCashBalance(oldExpense.cashDeskId, oldExpense.amount)
        } else if (oldExpense.paymentMethod == "BANK" && oldExpense.bankAccountId != null) {
            treasuryDao.updateBankBalance(oldExpense.bankAccountId, oldExpense.amount)
        }

        // Apply new treasury balance effect
        if (newExpense.paymentMethod == "CASH" && newExpense.cashDeskId != null) {
            treasuryDao.updateCashBalance(newExpense.cashDeskId, -newExpense.amount)
        } else if (newExpense.paymentMethod == "BANK" && newExpense.bankAccountId != null) {
            treasuryDao.updateBankBalance(newExpense.bankAccountId, -newExpense.amount)
        }

        // Update connected journal voucher if it exists
        if (oldExpense.voucherId != null) {
            try {
                accountingDao.deleteJournalItemsByVoucher(oldExpense.voucherId)
                val creditSubCode = if (newExpense.paymentMethod == "CASH") 10101 else 10102
                val items = listOf(
                    JournalItemEntity(
                        voucherId = oldExpense.voucherId,
                        generalCode = 801,
                        subsidiaryCode = 80101,
                        description = "هزینه ${newExpense.categoryTitle} - ${newExpense.title}",
                        debit = newExpense.amount,
                        credit = 0L
                    ),
                    JournalItemEntity(
                        voucherId = oldExpense.voucherId,
                        generalCode = 101,
                        subsidiaryCode = creditSubCode,
                        description = "پرداخت بابت ${newExpense.title} از ${newExpense.accountName}",
                        debit = 0L,
                        credit = newExpense.amount
                    )
                )
                accountingDao.insertJournalItems(items)
            } catch (e: Exception) {
                // Ignore if journal update fails
            }
        }

        expenseAndIncomeDao.updateExpense(newExpense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        // Revert treasury balance effect (+amount)
        if (expense.paymentMethod == "CASH" && expense.cashDeskId != null) {
            treasuryDao.updateCashBalance(expense.cashDeskId, expense.amount)
        } else if (expense.paymentMethod == "BANK" && expense.bankAccountId != null) {
            treasuryDao.updateBankBalance(expense.bankAccountId, expense.amount)
        }

        // Void or delete linked journal voucher
        expense.voucherId?.let { vId ->
            try {
                accountingDao.voidJournalEntry(vId)
                accountingDao.deleteJournalItemsByVoucher(vId)
            } catch (e: Exception) {
                // Ignore
            }
        }

        expenseAndIncomeDao.deleteExpense(expense)
    }

    // --- Income Categories ---
    val allIncomeCategories: Flow<List<IncomeCategoryEntity>> =
        expenseAndIncomeDao.getAllIncomeCategories()

    suspend fun insertIncomeCategory(category: IncomeCategoryEntity): Long =
        expenseAndIncomeDao.insertIncomeCategory(category)

    suspend fun updateIncomeCategory(category: IncomeCategoryEntity) =
        expenseAndIncomeDao.updateIncomeCategory(category)

    suspend fun deleteIncomeCategory(category: IncomeCategoryEntity) =
        expenseAndIncomeDao.deleteIncomeCategory(category)

    // --- Incomes ---
    val allIncomes: Flow<List<IncomeEntity>> =
        expenseAndIncomeDao.getAllIncomes()

    val totalIncomes: Flow<Long?> =
        expenseAndIncomeDao.getTotalIncomes()

    fun getIncomesBetweenDates(startDate: String, endDate: String): Flow<List<IncomeEntity>> =
        expenseAndIncomeDao.getIncomesBetweenDates(startDate, endDate)

    suspend fun createIncome(income: IncomeEntity): Long {
        // 1. Create a double-entry accounting journal voucher
        val voucherId = try {
            val entry = JournalEntryEntity(
                voucherNumber = 0L,
                jalaliDate = income.jalaliDate,
                timestamp = income.timestamp,
                description = "درآمد متفرقه: ${income.categoryTitle} - ${income.title}",
                isBalanced = true,
                status = "FINAL"
            )
            // Debit: 10101 (صندوق) or 10102 (بانک)
            // Credit: 60101 (فروش و درآمدهای موسسه)
            val debitSubCode = if (income.depositMethod == "CASH") 10101 else 10102
            val debitGenCode = 101

            val items = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = debitGenCode,
                    subsidiaryCode = debitSubCode,
                    description = "واریز درآمد به ${income.accountName}",
                    debit = income.amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 601,
                    subsidiaryCode = 60101,
                    description = "درآمد ${income.categoryTitle} - ${income.title}",
                    debit = 0L,
                    credit = income.amount
                )
            )
            accountingDao.createVoucherWithItems(entry, items)
        } catch (e: Exception) {
            null
        }

        // 2. Adjust Treasury Balance (Add amount)
        if (income.depositMethod == "CASH" && income.cashDeskId != null) {
            treasuryDao.updateCashBalance(income.cashDeskId, income.amount)
        } else if (income.depositMethod == "BANK" && income.bankAccountId != null) {
            treasuryDao.updateBankBalance(income.bankAccountId, income.amount)
        }

        // 3. Save Income Entity
        return expenseAndIncomeDao.insertIncome(income.copy(voucherId = voucherId))
    }

    suspend fun updateIncome(oldIncome: IncomeEntity, newIncome: IncomeEntity) {
        // Revert old treasury balance effect
        if (oldIncome.depositMethod == "CASH" && oldIncome.cashDeskId != null) {
            treasuryDao.updateCashBalance(oldIncome.cashDeskId, -oldIncome.amount)
        } else if (oldIncome.depositMethod == "BANK" && oldIncome.bankAccountId != null) {
            treasuryDao.updateBankBalance(oldIncome.bankAccountId, -oldIncome.amount)
        }

        // Apply new treasury balance effect
        if (newIncome.depositMethod == "CASH" && newIncome.cashDeskId != null) {
            treasuryDao.updateCashBalance(newIncome.cashDeskId, newIncome.amount)
        } else if (newIncome.depositMethod == "BANK" && newIncome.bankAccountId != null) {
            treasuryDao.updateBankBalance(newIncome.bankAccountId, newIncome.amount)
        }

        // Update connected journal voucher
        if (oldIncome.voucherId != null) {
            try {
                accountingDao.deleteJournalItemsByVoucher(oldIncome.voucherId)
                val debitSubCode = if (newIncome.depositMethod == "CASH") 10101 else 10102
                val items = listOf(
                    JournalItemEntity(
                        voucherId = oldIncome.voucherId,
                        generalCode = 101,
                        subsidiaryCode = debitSubCode,
                        description = "واریز درآمد به ${newIncome.accountName}",
                        debit = newIncome.amount,
                        credit = 0L
                    ),
                    JournalItemEntity(
                        voucherId = oldIncome.voucherId,
                        generalCode = 601,
                        subsidiaryCode = 60101,
                        description = "درآمد ${newIncome.categoryTitle} - ${newIncome.title}",
                        debit = 0L,
                        credit = newIncome.amount
                    )
                )
                accountingDao.insertJournalItems(items)
            } catch (e: Exception) {
                // Ignore
            }
        }

        expenseAndIncomeDao.updateIncome(newIncome)
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        // Revert treasury balance effect (-amount)
        if (income.depositMethod == "CASH" && income.cashDeskId != null) {
            treasuryDao.updateCashBalance(income.cashDeskId, -income.amount)
        } else if (income.depositMethod == "BANK" && income.bankAccountId != null) {
            treasuryDao.updateBankBalance(income.bankAccountId, -income.amount)
        }

        income.voucherId?.let { vId ->
            try {
                accountingDao.voidJournalEntry(vId)
                accountingDao.deleteJournalItemsByVoucher(vId)
            } catch (e: Exception) {
                // Ignore
            }
        }

        expenseAndIncomeDao.deleteIncome(income)
    }
}
