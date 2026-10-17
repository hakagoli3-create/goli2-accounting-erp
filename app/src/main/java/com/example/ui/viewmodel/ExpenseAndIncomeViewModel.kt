package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ExpenseCategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeCategoryEntity
import com.example.data.model.IncomeEntity
import com.example.data.repository.ExpenseAndIncomeRepository
import com.example.data.repository.TreasuryRepository
import com.example.util.ExportHelper
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategorySummary(
    val categoryId: Long,
    val title: String,
    val totalAmount: Long,
    val count: Int,
    val percentage: Float
)

class ExpenseAndIncomeViewModel(
    private val repository: ExpenseAndIncomeRepository,
    private val treasuryRepo: TreasuryRepository
) : ViewModel() {

    // --- Data Streams ---
    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<ExpenseCategoryEntity>> = repository.allExpenseCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIncomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<IncomeCategoryEntity>> = repository.allIncomeCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashDesks: StateFlow<List<CashDeskEntity>> = treasuryRepo.allCashDesks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccountEntity>> = treasuryRepo.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- User Feedback Alerts ---
    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    fun clearAlertMessage() {
        _alertMessage.value = null
    }

    // ==========================================
    // EXPENSE MANAGEMENT
    // ==========================================

    private val _editingExpense = MutableStateFlow<ExpenseEntity?>(null)
    val editingExpense: StateFlow<ExpenseEntity?> = _editingExpense.asStateFlow()

    fun setEditingExpense(expense: ExpenseEntity?) {
        _editingExpense.value = expense
    }

    fun saveExpense(
        title: String,
        amount: Long,
        category: ExpenseCategoryEntity,
        paymentMethod: String,
        cashDesk: CashDeskEntity?,
        bankAccount: BankAccountEntity?,
        jalaliDate: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank()) {
            _alertMessage.value = "لطفاً بابت/عنوان هزینه را وارد نمایید."
            return
        }
        if (amount <= 0) {
            _alertMessage.value = "مبلغ هزینه باید بزرگتر از صفر باشد."
            return
        }

        val accountName = if (paymentMethod == "CASH") {
            cashDesk?.name ?: "صندوق نقدی"
        } else {
            "${bankAccount?.bankName ?: "بانک"} - ${bankAccount?.accountNumber ?: ""}"
        }

        viewModelScope.launch {
            val currentEditing = _editingExpense.value
            if (currentEditing != null) {
                // Update existing expense
                val updated = currentEditing.copy(
                    title = title.trim(),
                    amount = amount,
                    categoryId = category.id,
                    categoryTitle = category.title,
                    paymentMethod = paymentMethod,
                    cashDeskId = if (paymentMethod == "CASH") cashDesk?.id else null,
                    bankAccountId = if (paymentMethod == "BANK") bankAccount?.id else null,
                    accountName = accountName,
                    jalaliDate = jalaliDate.ifBlank { JalaliDate.now().toStandardString() },
                    notes = notes.trim()
                )
                repository.updateExpense(currentEditing, updated)
                _alertMessage.value = "سند هزینه با موفقیت ویرایش گردید."
            } else {
                // Create new expense
                val newExpense = ExpenseEntity(
                    categoryId = category.id,
                    categoryTitle = category.title,
                    amount = amount,
                    jalaliDate = jalaliDate.ifBlank { JalaliDate.now().toStandardString() },
                    paymentMethod = paymentMethod,
                    cashDeskId = if (paymentMethod == "CASH") cashDesk?.id else null,
                    bankAccountId = if (paymentMethod == "BANK") bankAccount?.id else null,
                    accountName = accountName,
                    title = title.trim(),
                    notes = notes.trim()
                )
                repository.createExpense(newExpense)
                _alertMessage.value = "سند هزینه با موفقیت ثبت و از حساب کسر گردید."
            }
            _editingExpense.value = null
            onSuccess()
        }
    }

    fun deleteExpense(expense: ExpenseEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _alertMessage.value = "سند هزینه حذف و موجودی حساب برگشت داده شد."
            onSuccess()
        }
    }

    // Expense Categories CRUD
    fun addExpenseCategory(title: String, code: String, desc: String, onSuccess: () -> Unit = {}) {
        if (title.isBlank()) {
            _alertMessage.value = "عنوان سرفصل هزینه نمی‌تواند خالی باشد."
            return
        }
        viewModelScope.launch {
            val count = expenseCategories.value.size + 1
            val nextCode = code.ifBlank { "EXP-${String.format("%02d", count)}" }
            val cat = ExpenseCategoryEntity(code = nextCode, title = title.trim(), description = desc.trim())
            repository.insertExpenseCategory(cat)
            _alertMessage.value = "سرفصل هزینه جدید ایجاد شد."
            onSuccess()
        }
    }

    fun updateExpenseCategory(category: ExpenseCategoryEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateExpenseCategory(category)
            _alertMessage.value = "سرفصل هزینه ویرایش گردید."
            onSuccess()
        }
    }

    fun deleteExpenseCategory(category: ExpenseCategoryEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteExpenseCategory(category)
            _alertMessage.value = "سرفصل هزینه حذف شد."
            onSuccess()
        }
    }

    // ==========================================
    // INCOME MANAGEMENT
    // ==========================================

    private val _editingIncome = MutableStateFlow<IncomeEntity?>(null)
    val editingIncome: StateFlow<IncomeEntity?> = _editingIncome.asStateFlow()

    fun setEditingIncome(income: IncomeEntity?) {
        _editingIncome.value = income
    }

    fun saveIncome(
        title: String,
        amount: Long,
        category: IncomeCategoryEntity,
        depositMethod: String,
        cashDesk: CashDeskEntity?,
        bankAccount: BankAccountEntity?,
        jalaliDate: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank()) {
            _alertMessage.value = "لطفاً بابت/عنوان درآمد را وارد نمایید."
            return
        }
        if (amount <= 0) {
            _alertMessage.value = "مبلغ درآمد باید بزرگتر از صفر باشد."
            return
        }

        val accountName = if (depositMethod == "CASH") {
            cashDesk?.name ?: "صندوق نقدی"
        } else {
            "${bankAccount?.bankName ?: "بانک"} - ${bankAccount?.accountNumber ?: ""}"
        }

        viewModelScope.launch {
            val currentEditing = _editingIncome.value
            if (currentEditing != null) {
                val updated = currentEditing.copy(
                    title = title.trim(),
                    amount = amount,
                    categoryId = category.id,
                    categoryTitle = category.title,
                    depositMethod = depositMethod,
                    cashDeskId = if (depositMethod == "CASH") cashDesk?.id else null,
                    bankAccountId = if (depositMethod == "BANK") bankAccount?.id else null,
                    accountName = accountName,
                    jalaliDate = jalaliDate.ifBlank { JalaliDate.now().toStandardString() },
                    notes = notes.trim()
                )
                repository.updateIncome(currentEditing, updated)
                _alertMessage.value = "سند درآمد با موفقیت ویرایش گردید."
            } else {
                val newIncome = IncomeEntity(
                    categoryId = category.id,
                    categoryTitle = category.title,
                    amount = amount,
                    jalaliDate = jalaliDate.ifBlank { JalaliDate.now().toStandardString() },
                    depositMethod = depositMethod,
                    cashDeskId = if (depositMethod == "CASH") cashDesk?.id else null,
                    bankAccountId = if (depositMethod == "BANK") bankAccount?.id else null,
                    accountName = accountName,
                    title = title.trim(),
                    notes = notes.trim()
                )
                repository.createIncome(newIncome)
                _alertMessage.value = "سند درآمد با موفقیت ثبت و به حساب واریز شد."
            }
            _editingIncome.value = null
            onSuccess()
        }
    }

    fun deleteIncome(income: IncomeEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteIncome(income)
            _alertMessage.value = "سند درآمد حذف و اثر آن بر حساب معکوس گردید."
            onSuccess()
        }
    }

    // Income Categories CRUD
    fun addIncomeCategory(title: String, code: String, desc: String, onSuccess: () -> Unit = {}) {
        if (title.isBlank()) {
            _alertMessage.value = "عنوان دسته‌بندی درآمد نمی‌تواند خالی باشد."
            return
        }
        viewModelScope.launch {
            val count = incomeCategories.value.size + 1
            val nextCode = code.ifBlank { "INC-${String.format("%02d", count)}" }
            val cat = IncomeCategoryEntity(code = nextCode, title = title.trim(), description = desc.trim())
            repository.insertIncomeCategory(cat)
            _alertMessage.value = "دسته‌بندی درآمد جدید ایجاد شد."
            onSuccess()
        }
    }

    fun updateIncomeCategory(category: IncomeCategoryEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateIncomeCategory(category)
            _alertMessage.value = "دسته‌بندی درآمد ویرایش گردید."
            onSuccess()
        }
    }

    fun deleteIncomeCategory(category: IncomeCategoryEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteIncomeCategory(category)
            _alertMessage.value = "دسته‌بندی درآمد حذف شد."
            onSuccess()
        }
    }

    // ==========================================
    // REPORTS & ANALYTICS
    // ==========================================

    fun getExpenseSummariesByCategory(expensesList: List<ExpenseEntity>): List<CategorySummary> {
        val total = expensesList.sumOf { it.amount }
        if (total == 0L) return emptyList()

        return expensesList.groupBy { it.categoryId }
            .map { (catId, items) ->
                val sum = items.sumOf { it.amount }
                val title = items.firstOrNull()?.categoryTitle ?: "نامشخص"
                val pct = (sum.toFloat() / total.toFloat()) * 100f
                CategorySummary(catId, title, sum, items.size, pct)
            }
            .sortedByDescending { it.totalAmount }
    }

    fun getIncomeSummariesByCategory(incomesList: List<IncomeEntity>): List<CategorySummary> {
        val total = incomesList.sumOf { it.amount }
        if (total == 0L) return emptyList()

        return incomesList.groupBy { it.categoryId }
            .map { (catId, items) ->
                val sum = items.sumOf { it.amount }
                val title = items.firstOrNull()?.categoryTitle ?: "نامشخص"
                val pct = (sum.toFloat() / total.toFloat()) * 100f
                CategorySummary(catId, title, sum, items.size, pct)
            }
            .sortedByDescending { it.totalAmount }
    }

    // Export Reports
    fun exportExpensesCsv(context: Context, expensesList: List<ExpenseEntity>) {
        val headers = listOf("ردیف", "کد سرفصل", "سرفصل هزینه", "بابت / شرح", "مبلغ (تومان)", "تاریخ", "روش پرداخت", "حساب واریزی", "توضیحات")
        val rows = expensesList.mapIndexed { idx, it ->
            listOf(
                (idx + 1).toString(),
                it.categoryId.toString(),
                it.categoryTitle,
                it.title,
                it.amount.toString(),
                it.jalaliDate,
                if (it.paymentMethod == "CASH") "صندوق نقدی" else "حساب بانکی",
                it.accountName,
                it.notes
            )
        }
        val file = ExportHelper.exportReportCsv(context, "گزارش جامع ریز هزینه‌ها", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "اشتراک گزارش هزینه‌ها")
        }
    }

    fun exportExpensesPdf(context: Context, expensesList: List<ExpenseEntity>) {
        val headers = listOf("ردیف", "سرفصل", "عنوان هزینه", "مبلغ (تومان)", "تاریخ", "پرداخت")
        val rows = expensesList.mapIndexed { idx, it ->
            listOf(
                (idx + 1).toString(),
                it.categoryTitle,
                it.title,
                NumberFormatUtils.formatPrice(it.amount),
                it.jalaliDate,
                it.accountName
            )
        }
        val file = ExportHelper.exportReportPdf(context, "گزارش رسمی اسناد هزینه شرکت گلی", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "اشتراک PDF هزینه‌ها")
        }
    }

    fun exportIncomesCsv(context: Context, incomesList: List<IncomeEntity>) {
        val headers = listOf("ردیف", "سرفصل درآمد", "بابت / شرح", "مبلغ (تومان)", "تاریخ", "حساب واریزی", "توضیحات")
        val rows = incomesList.mapIndexed { idx, it ->
            listOf(
                (idx + 1).toString(),
                it.categoryTitle,
                it.title,
                it.amount.toString(),
                it.jalaliDate,
                it.accountName,
                it.notes
            )
        }
        val file = ExportHelper.exportReportCsv(context, "گزارش درآمدهای متفرقه و غیرعملیاتی", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "اشتراک اکسل درآمدها")
        }
    }

    fun exportIncomesPdf(context: Context, incomesList: List<IncomeEntity>) {
        val headers = listOf("ردیف", "سرفصل درآمد", "شرح درآمد", "مبلغ (تومان)", "تاریخ", "واریز به")
        val rows = incomesList.mapIndexed { idx, it ->
            listOf(
                (idx + 1).toString(),
                it.categoryTitle,
                it.title,
                NumberFormatUtils.formatPrice(it.amount),
                it.jalaliDate,
                it.accountName
            )
        }
        val file = ExportHelper.exportReportPdf(context, "گزارش رسمی درآمدهای متفرقه شرکت گلی", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "اشتراک PDF درآمدها")
        }
    }
}
