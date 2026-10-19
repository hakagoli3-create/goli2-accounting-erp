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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PartyViewModel(
    private val partyRepository: PartyRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Customers, 1: Suppliers, 2: Debtors, 3: Over Credit Limit
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val allParties: StateFlow<List<PartyEntity>> = partyRepository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReceivables: StateFlow<Long> = partyRepository.totalReceivables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalPayables: StateFlow<Long> = partyRepository.totalPayables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val filteredParties: StateFlow<List<PartyEntity>> = combine(
        partyRepository.allParties,
        _searchQuery,
        _selectedTab
    ) { parties, query, tab ->
        val q = query.trim()
        parties.filter { p ->
            val matchesQuery = q.isBlank() ||
                    p.name.contains(q, ignoreCase = true) ||
                    p.companyName.contains(q, ignoreCase = true) ||
                    p.mobile.contains(q, ignoreCase = true) ||
                    p.phone.contains(q, ignoreCase = true) ||
                    p.code.contains(q, ignoreCase = true) ||
                    p.nationalCode.contains(q, ignoreCase = true) ||
                    com.example.util.NumberFormatUtils.toPersianDigits(p.mobile).contains(q) ||
                    com.example.util.NumberFormatUtils.toPersianDigits(p.code).contains(q)

            val matchesTab = when (tab) {
                0 -> p.partyType in listOf("CUSTOMER", "BOTH")
                1 -> p.partyType in listOf("SUPPLIER", "BOTH")
                2 -> p.currentBalance > 0
                3 -> p.isCreditLimitExceeded()
                else -> true
            }

            matchesQuery && matchesTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
