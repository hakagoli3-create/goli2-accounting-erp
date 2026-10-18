package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AccountingRepository
import com.example.data.repository.ExpenseAndIncomeRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.PartyRepository
import com.example.data.repository.TreasuryRepository
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val todaySalesAmount: Long = 0L,
    val todayInvoicesCount: Int = 0,
    val totalSalesAmount: Long = 0L,
    val totalCashBalance: Long = 0L,
    val totalBankBalance: Long = 0L,
    val totalReceivables: Long = 0L,
    val totalPayables: Long = 0L,
    val todayExpenses: Long = 0L,
    val totalExpenses: Long = 0L,
    val lowStockCount: Int = 0,
    val dueChequesAlertCount: Int = 0,
    val overCreditPartiesCount: Int = 0,
    val currentShamsiDate: String = JalaliDate.now().format(),
    val currentShamsiFullDate: String = JalaliDate.now().getFormattedFullDate(),
    val isLoading: Boolean = false
)

private data class SalesStats(val todaySales: Long, val todayInvoices: Int, val totalSales: Long)
private data class TreasuryStats(val cash: Long, val bank: Long)
private data class AlertStats(val lowStock: Int, val dueCheques: Int, val overCredit: Int)
private data class PartyStats(val receivables: Long, val payables: Long)
private data class ExpenseStats(val todayExpense: Long, val totalExpense: Long)

class DashboardViewModel(
    private val invoiceRepo: InvoiceRepository,
    private val treasuryRepo: TreasuryRepository,
    private val inventoryRepo: InventoryRepository,
    private val partyRepo: PartyRepository,
    private val accountingRepo: AccountingRepository,
    private val expenseRepo: ExpenseAndIncomeRepository
) : ViewModel() {

    private val todayStr = JalaliDate.now().toStandardString()

    private val salesStats = combine(
        invoiceRepo.getTodaySalesSum(todayStr),
        invoiceRepo.getTodayInvoiceCount(todayStr),
        invoiceRepo.totalSalesSum
    ) { salesToday, invCount, totalSales ->
        SalesStats(salesToday ?: 0L, invCount, totalSales ?: 0L)
    }

    private val treasuryStats = combine(
        treasuryRepo.totalCashBalance,
        treasuryRepo.totalBankBalance
    ) { cash, bank ->
        TreasuryStats(cash ?: 0L, bank ?: 0L)
    }

    private val alertStats = combine(
        inventoryRepo.lowStockCount,
        treasuryRepo.getDueAlertsCount(),
        partyRepo.overCreditLimitCount
    ) { lowStock, dueCheques, overCredit ->
        AlertStats(lowStock, dueCheques, overCredit)
    }

    private val partyStats = combine(
        partyRepo.totalReceivables,
        partyRepo.totalPayables
    ) { receivables, payables ->
        PartyStats(receivables, payables)
    }

    private val expenseStats = combine(
        expenseRepo.getTodayExpenses(todayStr),
        expenseRepo.totalExpenses
    ) { todayExp, totalExp ->
        ExpenseStats(todayExp, totalExp ?: 0L)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        salesStats,
        treasuryStats,
        alertStats,
        partyStats,
        expenseStats
    ) { sales, treasury, alerts, party, expenses ->
        DashboardUiState(
            todaySalesAmount = sales.todaySales,
            todayInvoicesCount = sales.todayInvoices,
            totalSalesAmount = sales.totalSales,
            totalCashBalance = treasury.cash,
            totalBankBalance = treasury.bank,
            totalReceivables = party.receivables,
            totalPayables = party.payables,
            todayExpenses = expenses.todayExpense,
            totalExpenses = expenses.totalExpense,
            lowStockCount = alerts.lowStock,
            dueChequesAlertCount = alerts.dueCheques,
            overCreditPartiesCount = alerts.overCredit,
            currentShamsiDate = JalaliDate.now().format(),
            currentShamsiFullDate = JalaliDate.now().getFormattedFullDate(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
}
