package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.repository.AccountingRepository
import com.example.data.repository.ExpenseAndIncomeRepository
import com.example.data.repository.TreasuryRepository
import com.example.util.ExportHelper
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * مدل داده‌ای تراکنش نقدینگی و گردش حساب‌های خزانه
 */
data class TreasuryTransaction(
    val id: String,
    val type: TransactionType,
    val title: String,
    val category: String,
    val amount: Long,
    val jalaliDate: String,
    val timestamp: Long,
    val accountName: String,
    val method: String, // "CASH" or "BANK"
    val referenceNumber: String = "",
    val notes: String = ""
)

enum class TransactionType(val persianLabel: String) {
    INFLOW("دریافت / واریز"),
    OUTFLOW("پرداخت / برداشت"),
    TRANSFER("انتقال وجه")
}

enum class TreasuryDateFilter(val title: String) {
    ALL("همه تاریخ‌ها"),
    TODAY("امروز"),
    LAST_7_DAYS("۷ روز اخیر"),
    THIS_MONTH("ماه جاری")
}

class TreasuryViewModel(
    private val repository: TreasuryRepository,
    private val accountingRepo: AccountingRepository,
    private val expenseRepo: ExpenseAndIncomeRepository
) : ViewModel() {

    val allCashDesks: StateFlow<List<CashDeskEntity>> = repository.allCashDesks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBankAccounts: StateFlow<List<BankAccountEntity>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCheques: StateFlow<List<ChequeEntity>> = repository.allCheques
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receivableCheques: StateFlow<List<ChequeEntity>> = repository.receivableCheques
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payableCheques: StateFlow<List<ChequeEntity>> = repository.payableCheques
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val sevenDaysAhead = JalaliDate.now().addDays(7).toEpochMillis()
    val dueAlertCheques: StateFlow<List<ChequeEntity>> = repository.getDueOrOverdueCheques(sevenDaysAhead)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCashBalance: StateFlow<Long?> = repository.totalCashBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalBankBalance: StateFlow<Long?> = repository.totalBankBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // فیلترهای فهرست تراکنش‌ها و گردش حساب
    private val _transactionSearchQuery = MutableStateFlow("")
    val transactionSearchQuery: StateFlow<String> = _transactionSearchQuery.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(TreasuryDateFilter.ALL)
    val selectedDateFilter: StateFlow<TreasuryDateFilter> = _selectedDateFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    /**
     * تجمیع واقعی تمام تراکنش‌های مالی، دریافتی‌ها، پرداختی‌ها و اسناد انتقال از دیتابیس
     */
    val allTransactions: StateFlow<List<TreasuryTransaction>> = combine(
        expenseRepo.allExpenses,
        expenseRepo.allIncomes,
        accountingRepo.allJournalEntries
    ) { expenses, incomes, vouchers ->
        val list = mutableListOf<TreasuryTransaction>()

        incomes.forEach { inc ->
            list.add(
                TreasuryTransaction(
                    id = "INC-${inc.id}",
                    type = TransactionType.INFLOW,
                    title = inc.title.ifBlank { "دریافت وجه / درآمد" },
                    category = inc.categoryTitle.ifBlank { "درآمد عملیاتی" },
                    amount = inc.amount,
                    jalaliDate = inc.jalaliDate,
                    timestamp = inc.timestamp,
                    accountName = inc.accountName.ifBlank { if (inc.depositMethod == "CASH") "صندوق نقدی" else "حساب بانکی" },
                    method = inc.depositMethod,
                    referenceNumber = inc.voucherId?.let { "سند #$it" } ?: "",
                    notes = inc.notes
                )
            )
        }

        expenses.forEach { exp ->
            list.add(
                TreasuryTransaction(
                    id = "EXP-${exp.id}",
                    type = TransactionType.OUTFLOW,
                    title = exp.title.ifBlank { "پرداخت وجه / هزینه" },
                    category = exp.categoryTitle.ifBlank { "هزینه جاری" },
                    amount = exp.amount,
                    jalaliDate = exp.jalaliDate,
                    timestamp = exp.timestamp,
                    accountName = exp.accountName.ifBlank { if (exp.paymentMethod == "CASH") "صندوق نقدی" else "حساب بانکی" },
                    method = exp.paymentMethod,
                    referenceNumber = exp.voucherId?.let { "سند #$it" } ?: "",
                    notes = exp.notes
                )
            )
        }

        vouchers.filter { it.description.contains("انتقال") || it.description.contains("سرمایه") }.forEach { v ->
            val isCapital = v.description.contains("سرمایه")
            list.add(
                TreasuryTransaction(
                    id = "VOUCH-${v.id}",
                    type = if (isCapital) TransactionType.INFLOW else TransactionType.TRANSFER,
                    title = v.description,
                    category = if (isCapital) "سرمایه و آورده" else "انتقال بین حساب‌ها",
                    amount = 0L,
                    jalaliDate = v.jalaliDate,
                    timestamp = v.timestamp,
                    accountName = if (isCapital) "آورده نقدی شرکا" else "حساب‌های خزانه",
                    method = "INTERNAL",
                    referenceNumber = "سند #${v.voucherNumber}",
                    notes = v.description
                )
            )
        }

        list.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * تراکنش‌های فیلتر شده بر اساس جستجو، فیلتر تاریخ شمسی و نوع تراکنش
     */
    val filteredTransactions: StateFlow<List<TreasuryTransaction>> = combine(
        allTransactions,
        _transactionSearchQuery,
        _selectedDateFilter,
        _selectedTypeFilter
    ) { transactions, query, dateFilter, typeFilter ->
        val today = JalaliDate.now()
        val todayStr = today.toStandardString()
        val sevenDaysAgo = today.addDays(-7).toStandardString()
        val currentMonthPrefix = "${today.year}/${today.month.toString().padStart(2, '0')}"

        transactions.filter { tx ->
            // فیلتر نوع
            val matchesType = typeFilter == null || tx.type == typeFilter

            // فیلتر تاریخ شمسی
            val matchesDate = when (dateFilter) {
                TreasuryDateFilter.ALL -> true
                TreasuryDateFilter.TODAY -> tx.jalaliDate == todayStr
                TreasuryDateFilter.LAST_7_DAYS -> tx.jalaliDate >= sevenDaysAgo
                TreasuryDateFilter.THIS_MONTH -> tx.jalaliDate.startsWith(currentMonthPrefix)
            }

            // فیلتر جستجو
            val q = query.trim()
            val matchesQuery = q.isBlank() ||
                    tx.title.contains(q, ignoreCase = true) ||
                    tx.category.contains(q, ignoreCase = true) ||
                    tx.accountName.contains(q, ignoreCase = true) ||
                    tx.referenceNumber.contains(q, ignoreCase = true) ||
                    tx.jalaliDate.contains(q) ||
                    tx.notes.contains(q, ignoreCase = true)

            matchesType && matchesDate && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTransactionSearchQuery(query: String) {
        _transactionSearchQuery.value = query
    }

    fun setDateFilter(filter: TreasuryDateFilter) {
        _selectedDateFilter.value = filter
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    data class ChequeRassResult(
        val totalAmount: Long,
        val chequeCount: Int,
        val weightedDays: Int,
        val rassDateJalali: String,
        val averageAmount: Long
    )

    fun calculateChequeRass(cheques: List<ChequeEntity>): ChequeRassResult? {
        if (cheques.isEmpty()) return null
        val totalAmount = cheques.sumOf { it.amount }
        if (totalAmount <= 0) return null

        var weightedDaysSum = 0.0
        cheques.forEach { ch ->
            val days = ch.daysUntilDue().coerceAtLeast(0)
            weightedDaysSum += (ch.amount.toDouble() * days.toDouble())
        }
        val avgDays = (weightedDaysSum / totalAmount.toDouble()).toInt()
        val rassDate = JalaliDate.now().addDays(avgDays).toStandardString()

        return ChequeRassResult(
            totalAmount = totalAmount,
            chequeCount = cheques.size,
            weightedDays = avgDays,
            rassDateJalali = rassDate,
            averageAmount = totalAmount / cheques.size
        )
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun addCheque(
        chequeNumber: String,
        sayadNumber: String,
        type: String,
        partyName: String,
        bankName: String,
        branch: String,
        amount: Long,
        issueDate: String,
        dueDate: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val dueJalali = JalaliDate.parse(dueDate)
            val cheque = ChequeEntity(
                chequeNumber = chequeNumber,
                sayadNumber = sayadNumber,
                type = type,
                partyName = partyName,
                bankName = bankName,
                branch = branch,
                amount = amount,
                issueDateJalali = issueDate,
                dueDateJalali = dueDate,
                dueDateEpochMillis = dueJalali.toEpochMillis(),
                status = "NORMAL",
                notes = notes
            )
            repository.insertCheque(cheque)
            _statusMessage.value = "چک صیادی با موفقیت در سامانه ثبت شد."
            onSuccess()
        }
    }

    fun updateChequeStatus(cheque: ChequeEntity, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateCheque(cheque.copy(status = newStatus))
                _statusMessage.value = "وضعیت چک به‌روزرسانی شد."
            } catch (e: Exception) {
                _statusMessage.value = "خطا در تغییر وضعیت چک: ${e.localizedMessage}"
            }
        }
    }

    fun updateCheque(cheque: ChequeEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateCheque(cheque)
                _statusMessage.value = "اطلاعات چک با موفقیت ویرایش شد."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "خطا در ویرایش چک: ${e.localizedMessage}"
            }
        }
    }

    fun deleteCheque(cheque: ChequeEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteCheque(cheque)
                _statusMessage.value = "چک با موفقیت حذف گردید."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "امکان حذف این چک وجود ندارد."
            }
        }
    }

    fun addCashDesk(name: String, code: String, keeper: String, initialBalance: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.insertCashDesk(
                    CashDeskEntity(
                        code = code,
                        name = name,
                        keeperName = keeper,
                        currentBalance = initialBalance
                    )
                )
                _statusMessage.value = "صندوق جدید $name با موفقیت تعریف شد."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "خطا در افزودن صندوق: ${e.localizedMessage}"
            }
        }
    }

    fun updateCashDesk(cashDesk: CashDeskEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateCashDesk(cashDesk)
                _statusMessage.value = "اطلاعات صندوق به‌روزرسانی شد."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "خطا در ویرایش صندوق: ${e.localizedMessage}"
            }
        }
    }

    fun deleteCashDesk(cashDesk: CashDeskEntity) {
        viewModelScope.launch {
            try {
                repository.deleteCashDesk(cashDesk)
                _statusMessage.value = "صندوق ${cashDesk.name} حذف شد."
            } catch (e: Exception) {
                _statusMessage.value = "امکان حذف این صندوق به دلیل داشتن تراکنش‌های مالی وجود ندارد."
            }
        }
    }

    fun addBankAccount(
        bankName: String,
        accountNumber: String,
        cardNumber: String,
        sheba: String,
        branch: String,
        initialBalance: Long,
        hasPos: Boolean,
        posTerminalId: String = "",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.insertBankAccount(
                    BankAccountEntity(
                        bankName = bankName,
                        accountNumber = accountNumber,
                        cardNumber = cardNumber,
                        shebaNumber = sheba,
                        branch = branch,
                        currentBalance = initialBalance,
                        hasPos = hasPos,
                        posTerminalId = posTerminalId
                    )
                )
                _statusMessage.value = "حساب بانکی $bankName با موفقیت تعریف شد."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "خطا در ثبت حساب بانکی: ${e.localizedMessage}"
            }
        }
    }

    fun updateBankAccount(account: BankAccountEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateBankAccount(account)
                _statusMessage.value = "اطلاعات حساب بانکی به‌روزرسانی شد."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "خطا در ویرایش حساب بانکی: ${e.localizedMessage}"
            }
        }
    }

    fun deleteBankAccount(account: BankAccountEntity) {
        viewModelScope.launch {
            try {
                repository.deleteBankAccount(account)
                _statusMessage.value = "حساب بانکی ${account.bankName} حذف شد."
            } catch (e: Exception) {
                _statusMessage.value = "امکان حذف این حساب بانکی به دلیل داشتن تراکنش وجود ندارد."
            }
        }
    }

    /**
     * ثبت عملیات دریافت وجه / درآمد واقعی در دیتابیس
     */
    fun registerIncome(
        title: String,
        amount: Long,
        destinationType: String, // "CASH" or "BANK"
        targetId: Long,
        category: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _statusMessage.value = "مبلغ درآمد باید بیشتر از صفر باشد."
                return@launch
            }
            try {
                val accName = if (destinationType == "CASH") {
                    allCashDesks.value.firstOrNull { it.id == targetId }?.name ?: "صندوق نقدی"
                } else {
                    allBankAccounts.value.firstOrNull { it.id == targetId }?.bankName ?: "حساب بانکی"
                }

                val incomeEntity = IncomeEntity(
                    categoryId = 1L,
                    categoryTitle = category.ifBlank { "درآمد متفرقه" },
                    amount = amount,
                    jalaliDate = JalaliDate.now().toStandardString(),
                    timestamp = System.currentTimeMillis(),
                    depositMethod = destinationType,
                    cashDeskId = if (destinationType == "CASH") targetId else null,
                    bankAccountId = if (destinationType == "BANK") targetId else null,
                    accountName = accName,
                    title = title,
                    notes = notes
                )
                expenseRepo.createIncome(incomeEntity)
                _statusMessage.value = "دریافت وجه «$title» به مبلغ ${NumberFormatUtils.formatPrice(amount)} ثبت شد."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "خطا در ثبت دریافت وجه: ${e.localizedMessage}"
            }
        }
    }

    /**
     * ثبت هزینه / پرداخت واقعی در دیتابیس
     */
    fun registerExpense(
        title: String,
        amount: Long,
        sourceType: String, // "CASH" or "BANK"
        sourceId: Long,
        category: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _statusMessage.value = "مبلغ هزینه باید بیشتر از صفر باشد."
                return@launch
            }
            try {
                val accName = if (sourceType == "CASH") {
                    allCashDesks.value.firstOrNull { it.id == sourceId }?.name ?: "صندوق نقدی"
                } else {
                    allBankAccounts.value.firstOrNull { it.id == sourceId }?.bankName ?: "حساب بانکی"
                }

                val expenseEntity = ExpenseEntity(
                    categoryId = 1L,
                    categoryTitle = category.ifBlank { "هزینه عمومی" },
                    amount = amount,
                    jalaliDate = JalaliDate.now().toStandardString(),
                    timestamp = System.currentTimeMillis(),
                    paymentMethod = sourceType,
                    cashDeskId = if (sourceType == "CASH") sourceId else null,
                    bankAccountId = if (sourceType == "BANK") sourceId else null,
                    accountName = accName,
                    title = title,
                    notes = notes
                )
                expenseRepo.createExpense(expenseEntity)
                _statusMessage.value = "پرداخت هزینه «$title» به مبلغ ${NumberFormatUtils.formatPrice(amount)} ثبت شد."
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "خطا در ثبت پرداخت هزینه: ${e.localizedMessage}"
            }
        }
    }

    /**
     * ثبت سرمایه اولیه و آورده نقدی شرکا
     */
    fun registerCapitalInvestment(
        investorName: String,
        amount: Long,
        destinationType: String,
        targetId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            if (destinationType == "CASH") {
                repository.updateCashBalance(targetId, amount)
            } else {
                repository.updateBankBalance(targetId, amount)
            }
            val today = JalaliDate.now()
            val subCode = if (destinationType == "CASH") 10101 else 10102
            val journalItems = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = subCode,
                    description = "آورده نقدی سرمایه توسط $investorName",
                    debit = amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 501,
                    subsidiaryCode = 50101,
                    description = "افزایش سرمایه و سهم‌الشرکه $investorName",
                    debit = 0L,
                    credit = amount
                )
            )
            accountingRepo.createJournalVoucher(
                JournalEntryEntity(
                    voucherNumber = 0L,
                    jalaliDate = today.toStandardString(),
                    description = "سند ثبت سرمایه اولیه / آورده شرکا: $investorName",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
            _statusMessage.value = "ثبت آورده سرمایه «$investorName» با موفقیت انجام شد."
            onSuccess()
        }
    }

    /**
     * انتقال وجه بین صندوق و حساب بانکی
     */
    fun transferFunds(
        fromType: String, // "CASH" or "BANK"
        fromId: Long,
        toType: String, // "CASH" or "BANK"
        toId: Long,
        amount: Long,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _statusMessage.value = "مبلغ انتقال باید بیشتر از صفر باشد."
                return@launch
            }
            if (fromType == "CASH") repository.updateCashBalance(fromId, -amount)
            else repository.updateBankBalance(fromId, -amount)

            if (toType == "CASH") repository.updateCashBalance(toId, amount)
            else repository.updateBankBalance(toId, amount)

            val fromName = if (fromType == "CASH") {
                allCashDesks.value.firstOrNull { it.id == fromId }?.name ?: "صندوق"
            } else {
                allBankAccounts.value.firstOrNull { it.id == fromId }?.bankName ?: "بانک"
            }

            val toName = if (toType == "CASH") {
                allCashDesks.value.firstOrNull { it.id == toId }?.name ?: "صندوق"
            } else {
                allBankAccounts.value.firstOrNull { it.id == toId }?.bankName ?: "بانک"
            }

            val today = JalaliDate.now()
            val fromSub = if (fromType == "CASH") 10101 else 10102
            val toSub = if (toType == "CASH") 10101 else 10102

            val journalItems = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = toSub,
                    description = "انتقال وجه وارده به $toName بابت جابجایی از $fromName",
                    debit = amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = fromSub,
                    description = "خروج وجه از $fromName بابت انتقال به $toName",
                    debit = 0L,
                    credit = amount
                )
            )
            accountingRepo.createJournalVoucher(
                JournalEntryEntity(
                    voucherNumber = 0L,
                    jalaliDate = today.toStandardString(),
                    description = "انتقال وجه بین حساب‌ها: از $fromName به $toName ${notes.ifBlank { "" }}",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
            _statusMessage.value = "انتقال وجه مبلغ ${NumberFormatUtils.formatPrice(amount)} با موفقیت انجام شد."
            onSuccess()
        }
    }

    fun exportTreasuryPdf(context: Context) {
        val headers = listOf("ردیف", "عنوان منبع / حساب", "نوع", "مشخصات / شماره", "موجودی (تومان)")
        val rows = mutableListOf<List<String>>()

        var r = 1
        allCashDesks.value.forEach { c ->
            rows.add(listOf(r++.toString(), c.name, "صندوق نقدی", "مسئول: ${c.keeperName}", NumberFormatUtils.formatPrice(c.currentBalance)))
        }
        allBankAccounts.value.forEach { b ->
            rows.add(listOf(r++.toString(), b.bankName, "حساب بانکی", "کارت: ${b.cardNumber}", NumberFormatUtils.formatPrice(b.currentBalance)))
        }
        val file = ExportHelper.exportReportPdf(context, "گزارش موجودی نقد، بانک و صندوق‌ها", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "گزارش خزانه‌داری PDF")
        }
    }

    fun exportTreasuryCsv(context: Context) {
        val headers = listOf("ردیف", "عنوان", "نوع", "شماره حساب/کارت", "موجودی (تومان)")
        val rows = mutableListOf<List<String>>()
        var r = 1
        allCashDesks.value.forEach { c ->
            rows.add(listOf(r++.toString(), c.name, "صندوق", c.code, c.currentBalance.toString()))
        }
        allBankAccounts.value.forEach { b ->
            rows.add(listOf(r++.toString(), b.bankName, "بانک", b.cardNumber.ifEmpty { b.accountNumber }, b.currentBalance.toString()))
        }
        val file = ExportHelper.exportReportCsv(context, "گزارش نقدینگی خزانه‌داری", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل خزانه‌داری")
        }
    }
}
