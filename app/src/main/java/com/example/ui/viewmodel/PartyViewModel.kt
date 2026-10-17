package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.InvoiceEntity
import com.example.data.model.PartyEntity
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.PartyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerCategoryReport(
    val wholesaleCount: Int = 0,
    val retailCount: Int = 0,
    val supplierCount: Int = 0,
    val wholesaleSalesVolume: Long = 0L,
    val retailSalesVolume: Long = 0L,
    val wholesaleTotalBalance: Long = 0L,
    val retailTotalBalance: Long = 0L,
    val overCreditCount: Int = 0
)

class PartyViewModel(
    private val partyRepository: PartyRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 0: Wholesale Customers, 1: Retail Customers, 2: Suppliers, 3: Reports & Over Credit
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val allParties: StateFlow<List<PartyEntity>> = partyRepository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredParties: StateFlow<List<PartyEntity>> = combine(
        partyRepository.allParties,
        _searchQuery,
        _selectedTab
    ) { parties, query, tab ->
        parties.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.companyName.contains(query, ignoreCase = true) ||
                    p.mobile.contains(query, ignoreCase = true) ||
                    p.code.contains(query, ignoreCase = true)

            val matchesTab = when (tab) {
                0 -> p.isWholesaleCustomer // مشتریان عمده
                1 -> p.isRetailCustomer // مشتریان تکی / خرده‌فروشی
                2 -> p.partyType in listOf("SUPPLIER", "BOTH") // تامین‌کنندگان
                3 -> p.isCreditLimitExceeded() // سقف اعتبار
                else -> true
            }

            matchesQuery && matchesTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Comprehensive separate report between wholesale and retail customers
    val categoryReport: StateFlow<CustomerCategoryReport> = combine(
        partyRepository.allParties,
        invoiceRepository.allInvoicesWithItems
    ) { parties, invoices ->
        val wholesaleParties = parties.filter { it.isWholesaleCustomer }
        val retailParties = parties.filter { it.isRetailCustomer }
        val supplierParties = parties.filter { it.partyType in listOf("SUPPLIER", "BOTH") }

        val wholesalePartyIds = wholesaleParties.map { it.id }.toSet()
        val retailPartyIds = retailParties.map { it.id }.toSet()

        val finalSalesInvoices = invoices.filter { it.invoice.status == "FINAL" && it.invoice.invoiceType in listOf("SALE", "POS_SALE") }

        val wholesaleSales = finalSalesInvoices
            .filter { it.invoice.partyId in wholesalePartyIds || it.invoice.pricingMode == "WHOLESALE" }
            .sumOf { it.invoice.totalAmount }

        val retailSales = finalSalesInvoices
            .filter { it.invoice.partyId in retailPartyIds || (it.invoice.partyId == null && it.invoice.pricingMode != "WHOLESALE") }
            .sumOf { it.invoice.totalAmount }

        val wholesaleBalance = wholesaleParties.sumOf { if (it.currentBalance > 0) it.currentBalance else 0L }
        val retailBalance = retailParties.sumOf { if (it.currentBalance > 0) it.currentBalance else 0L }

        val overCreditCount = parties.count { it.isCreditLimitExceeded() }

        CustomerCategoryReport(
            wholesaleCount = wholesaleParties.size,
            retailCount = retailParties.size,
            supplierCount = supplierParties.size,
            wholesaleSalesVolume = wholesaleSales,
            retailSalesVolume = retailSales,
            wholesaleTotalBalance = wholesaleBalance,
            retailTotalBalance = retailBalance,
            overCreditCount = overCreditCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomerCategoryReport())

    // Selected party for ledger statement view
    private val _selectedParty = MutableStateFlow<PartyEntity?>(null)
    val selectedParty: StateFlow<PartyEntity?> = _selectedParty.asStateFlow()

    val partyInvoices: StateFlow<List<InvoiceEntity>> = _selectedParty
        .flatMapLatest { party ->
            if (party != null) {
                invoiceRepository.getInvoicesForParty(party.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun selectPartyForStatement(party: PartyEntity?) {
        _selectedParty.value = party
    }

    fun addParty(
        code: String,
        name: String,
        company: String,
        mobile: String,
        phone: String,
        address: String,
        nationalCode: String,
        partyType: String,
        customerCategory: String,
        creditLimit: Long,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val party = PartyEntity(
                code = code.ifBlank { "PRT-${System.currentTimeMillis() % 10000}" },
                name = name,
                companyName = company,
                mobile = mobile,
                phone = phone,
                address = address,
                nationalCode = nationalCode,
                partyType = partyType,
                customerCategory = customerCategory,
                creditLimit = creditLimit,
                notes = notes
            )
            partyRepository.insertParty(party)
            onSuccess()
        }
    }

    fun updateParty(
        party: PartyEntity,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            partyRepository.updateParty(party)
            if (_selectedParty.value?.id == party.id) {
                _selectedParty.value = party
            }
            onSuccess()
        }
    }

    fun deleteParty(
        party: PartyEntity,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            partyRepository.deleteParty(party)
            if (_selectedParty.value?.id == party.id) {
                _selectedParty.value = null
            }
            onSuccess()
        }
    }
}
