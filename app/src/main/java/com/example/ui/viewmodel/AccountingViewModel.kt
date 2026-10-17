package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountGroupEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.TrialBalanceRow
import com.example.data.repository.AccountingRepository
import com.example.util.ExportHelper
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.data.model.InvoiceWithItems
import com.example.data.model.PartyEntity
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.PartyRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VoucherItemDraft(
    val subsidiaryCode: Int,
    val subsidiaryTitle: String,
    val generalCode: Int,
    val description: String,
    val debit: Long,
    val credit: Long
)

data class ProfitLossStatement(
    val totalRevenue: Long,
    val costOfGoodsSold: Long,
    val grossProfit: Long,
    val operatingExpenses: Long,
    val netProfit: Long
)

class AccountingViewModel(
    private val repository: AccountingRepository,
    private val partyRepo: PartyRepository,
    private val invoiceRepo: InvoiceRepository
) : ViewModel() {

    val allGroups: StateFlow<List<AccountGroupEntity>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenerals: StateFlow<List<GeneralAccountEntity>> = repository.allGenerals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubsidiaries: StateFlow<List<SubsidiaryAccountEntity>> = repository.allSubsidiaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJournalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParties: StateFlow<List<PartyEntity>> = partyRepo.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<InvoiceWithItems>> = invoiceRepo.allInvoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trialBalance: StateFlow<List<TrialBalanceRow>> = repository.trialBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // محاسبه لحظه‌ای صورت سود و زیان بر اساس کدینگ استاندارد
    val profitLoss: StateFlow<ProfitLossStatement> = trialBalance.map { rows ->
        var revenue = 0L
        var cogs = 0L
        var expenses = 0L

        for (row in rows) {
            val groupCode = row.subsidiaryCode / 10000 // مثلا 60101 -> گروه ۶
            when (groupCode) {
                6 -> revenue += (row.totalCredit - row.totalDebit)
                7 -> cogs += (row.totalDebit - row.totalCredit)
                8 -> expenses += (row.totalDebit - row.totalCredit)
            }
        }
        val gross = revenue - cogs
        val net = gross - expenses
        ProfitLossStatement(
            totalRevenue = if (revenue > 0) revenue else 0L,
            costOfGoodsSold = if (cogs > 0) cogs else 0L,
            grossProfit = gross,
            operatingExpenses = if (expenses > 0) expenses else 0L,
            netProfit = net
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfitLossStatement(0, 0, 0, 0, 0))

    // State for Party Subsidiary Ledger (دفتر معین اشخاص)
    private val _selectedPartyIdForLedger = MutableStateFlow<Long?>(null)
    val selectedPartyIdForLedger: StateFlow<Long?> = _selectedPartyIdForLedger.asStateFlow()

    val selectedPartyForLedger: StateFlow<PartyEntity?> = combine(allParties, _selectedPartyIdForLedger) { parties, id ->
        if (id != null) parties.find { it.id == id } else parties.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val partyLedgerEntries: StateFlow<List<JournalItemEntity>> = _selectedPartyIdForLedger.flatMapLatest { id ->
        val effectiveId = id ?: allParties.value.firstOrNull()?.id
        if (effectiveId != null) repository.getJournalItemsByParty(effectiveId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State for creating a new Journal Voucher
    private val _draftVoucherDescription = MutableStateFlow("")
    val draftVoucherDescription: StateFlow<String> = _draftVoucherDescription.asStateFlow()

    private val _draftVoucherDate = MutableStateFlow(JalaliDate.now().toStandardString())
    val draftVoucherDate: StateFlow<String> = _draftVoucherDate.asStateFlow()

    private val _draftItems = MutableStateFlow<List<VoucherItemDraft>>(emptyList())
    val draftItems: StateFlow<List<VoucherItemDraft>> = _draftItems.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    fun selectPartyForLedger(partyId: Long?) {
        _selectedPartyIdForLedger.value = partyId
    }

    fun addPartyTransaction(
        partyId: Long,
        description: String,
        debit: Long,
        credit: Long,
        jalaliDate: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addPartyTransaction(partyId, description, debit, credit, jalaliDate)
            val netEffect = debit - credit
            partyRepo.updatePartyBalance(partyId, netEffect)
            _operationMessage.value = "گردش حساب با موفقیت در دفتر معین ثبت شد."
            onSuccess()
        }
    }

    fun updatePartyTransaction(
        item: JournalItemEntity,
        newDescription: String,
        newDebit: Long,
        newCredit: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val oldNet = item.debit - item.credit
            val newNet = newDebit - newCredit
            val diff = newNet - oldNet
            repository.updateJournalItem(item.copy(description = newDescription, debit = newDebit, credit = newCredit))
            if (item.partyId != null && diff != 0L) {
                partyRepo.updatePartyBalance(item.partyId, diff)
            }
            _operationMessage.value = "ردیف دفتر معین با موفقیت ویرایش گردید."
            onSuccess()
        }
    }

    fun deletePartyTransaction(
        item: JournalItemEntity,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.deleteJournalItem(item.id)
            if (item.partyId != null) {
                val reverse = -(item.debit - item.credit)
                partyRepo.updatePartyBalance(item.partyId, reverse)
            }
            _operationMessage.value = "ردیف دفتر معین حذف شد."
            onSuccess()
        }
    }

    fun voidInvoice(invoiceId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = invoiceRepo.voidInvoice(invoiceId)
            if (res) {
                _operationMessage.value = "فاکتور با موفقیت باطل شد و اثرات آن معکوس گردید."
                onSuccess()
            }
        }
    }

    fun deleteInvoice(invoiceId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = invoiceRepo.deleteInvoice(invoiceId)
            if (res) {
                _operationMessage.value = "فاکتور با موفقیت حذف گردید."
                onSuccess()
            }
        }
    }

    fun exportPartyLedgerPdf(context: Context, party: PartyEntity, items: List<JournalItemEntity>) {
        val headers = listOf("ردیف", "شماره سند", "شرح گردش حساب", "بدهکار (تومان)", "بستانکار (تومان)", "مانده")
        var running = 0L
        val rows = items.mapIndexed { idx, itm ->
            running += (itm.debit - itm.credit)
            listOf(
                (idx + 1).toString(),
                "سند ${itm.voucherId}",
                itm.description,
                NumberFormatUtils.formatPrice(itm.debit),
                NumberFormatUtils.formatPrice(itm.credit),
                NumberFormatUtils.formatPrice(running)
            )
        }
        val f = ExportHelper.exportReportPdf(context, "صورت‌حساب دفتر معین ${party.name}", headers, rows)
        if (f != null) ExportHelper.shareFile(context, f, "application/pdf", "دفتر معین ${party.name} PDF")
    }

    fun exportPartyLedgerCsv(context: Context, party: PartyEntity, items: List<JournalItemEntity>) {
        val headers = listOf("ردیف", "شماره سند", "شرح گردش", "بدهکار", "بستانکار", "مانده")
        var running = 0L
        val rows = items.mapIndexed { idx, itm ->
            running += (itm.debit - itm.credit)
            listOf(
                (idx + 1).toString(),
                itm.voucherId.toString(),
                itm.description,
                itm.debit.toString(),
                itm.credit.toString(),
                running.toString()
            )
        }
        val f = ExportHelper.exportReportCsv(context, "معین_${party.name}", headers, rows)
        if (f != null) ExportHelper.shareFile(context, f, "text/csv", "خروجی اکسل معین ${party.name}")
    }

    fun voidVoucher(voucher: JournalEntryEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.voidVoucher(voucher.id)
            _operationMessage.value = "سند شماره ${voucher.voucherNumber} با موفقیت باطل شد."
            onSuccess()
        }
    }

    fun updateVoucher(voucher: JournalEntryEntity, newDesc: String, newDate: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateVoucher(voucher.copy(description = newDesc, jalaliDate = newDate))
            _operationMessage.value = "سند شماره ${voucher.voucherNumber} ویرایش شد."
            onSuccess()
        }
    }

    fun deleteVoucher(voucherId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteVoucher(voucherId)
            _operationMessage.value = "سند مالی با موفقیت حذف گردید."
            onSuccess()
        }
    }

    fun updateDraftDescription(desc: String) {
        _draftVoucherDescription.value = desc
    }

    fun updateDraftDate(date: String) {
        _draftVoucherDate.value = date
    }

    fun addDraftItem(item: VoucherItemDraft) {
        _draftItems.value = _draftItems.value + item
    }

    fun removeDraftItem(index: Int) {
        val current = _draftItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _draftItems.value = current
        }
    }

    fun clearDraft() {
        _draftItems.value = emptyList()
        _draftVoucherDescription.value = ""
        _draftVoucherDate.value = JalaliDate.now().toStandardString()
    }

    fun clearMessage() {
        _operationMessage.value = null
    }

    fun submitJournalVoucher(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val items = _draftItems.value
            val totalDebit = items.sumOf { it.debit }
            val totalCredit = items.sumOf { it.credit }

            if (items.size < 2) {
                _operationMessage.value = "سند حسابداری باید حداقل دارای ۲ سطر باشد."
                return@launch
            }

            if (totalDebit != totalCredit) {
                _operationMessage.value = "سند تراز نیست! اختلاف تراز: ${Math.abs(totalDebit - totalCredit)} تومان"
                return@launch
            }

            val entry = JournalEntryEntity(
                voucherNumber = 0L,
                jalaliDate = _draftVoucherDate.value,
                description = _draftVoucherDescription.value.ifBlank { "سند حسابداری دستی" },
                isBalanced = true,
                status = "FINAL"
            )

            val journalItems = items.map {
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = it.generalCode,
                    subsidiaryCode = it.subsidiaryCode,
                    description = it.description,
                    debit = it.debit,
                    credit = it.credit
                )
            }

            val result = repository.createJournalVoucher(entry, journalItems)
            if (result.isSuccess) {
                _operationMessage.value = "سند حسابداری با موفقیت ثبت قطعی شد."
                clearDraft()
                onSuccess()
            } else {
                _operationMessage.value = result.exceptionOrNull()?.message ?: "خطا در ثبت سند"
            }
        }
    }

    // --- Exports for Trial Balance, Journal, and Profit & Loss ---

    fun exportTrialBalancePdf(context: Context) {
        val headers = listOf("کد معین", "عنوان حساب معین", "حساب کل", "گردش بدهکار", "گردش بستانکار", "مانده بدهکار", "مانده بستانکار")
        val rows = trialBalance.value.map { r ->
            listOf(
                r.subsidiaryCode.toString(),
                r.title,
                r.generalTitle,
                NumberFormatUtils.formatPrice(r.totalDebit),
                NumberFormatUtils.formatPrice(r.totalCredit),
                NumberFormatUtils.formatPrice(r.balanceDebit),
                NumberFormatUtils.formatPrice(r.balanceCredit)
            )
        }
        val file = ExportHelper.exportReportPdf(context, "تراز آزمایشی چهارستونی معین", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "تراز آزمایشی PDF")
        }
    }

    fun exportTrialBalanceCsv(context: Context) {
        val headers = listOf("کد معین", "عنوان معین", "حساب کل", "گردش بدهکار", "گردش بستانکار", "مانده بدهکار", "مانده بستانکار")
        val rows = trialBalance.value.map { r ->
            listOf(
                r.subsidiaryCode.toString(),
                r.title,
                r.generalTitle,
                r.totalDebit.toString(),
                r.totalCredit.toString(),
                r.balanceDebit.toString(),
                r.balanceCredit.toString()
            )
        }
        val file = ExportHelper.exportReportCsv(context, "تراز آزمایشی معین", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل تراز آزمایشی")
        }
    }

    fun exportProfitLossPdf(context: Context) {
        val pl = profitLoss.value
        val headers = listOf("ردیف", "عنوان قلم صورت مالی", "مبلغ (تومان)")
        val rows = listOf(
            listOf("۱", "درآمدهای عملیاتی و حاصل از فروش", NumberFormatUtils.formatPrice(pl.totalRevenue)),
            listOf("۲", "بهای تمام شده کالای فروش رفته (COGS)", "(${NumberFormatUtils.formatPrice(pl.costOfGoodsSold)})"),
            listOf("۳", "سود (زیان) ناخالص", NumberFormatUtils.formatPrice(pl.grossProfit)),
            listOf("۴", "هزینه‌های عمومی، اداری و توزیع", "(${NumberFormatUtils.formatPrice(pl.operatingExpenses)})"),
            listOf("۵", "سود (زیان) خالص نهایی دوره", NumberFormatUtils.formatPrice(pl.netProfit))
        )
        val file = ExportHelper.exportReportPdf(context, "صورت سود و زیان رسمی دوره مالی", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "صورت سود و زیان PDF")
        }
    }

    fun exportProfitLossCsv(context: Context) {
        val pl = profitLoss.value
        val headers = listOf("ردیف", "عنوان", "مبلغ به تومان")
        val rows = listOf(
            listOf("۱", "درآمد فروش", pl.totalRevenue.toString()),
            listOf("۲", "بهای تمام شده کالای فروش رفته", pl.costOfGoodsSold.toString()),
            listOf("۳", "سود ناخالص", pl.grossProfit.toString()),
            listOf("۴", "هزینه‌های عمومی و اداری", pl.operatingExpenses.toString()),
            listOf("۵", "سود خالص دوره", pl.netProfit.toString())
        )
        val file = ExportHelper.exportReportCsv(context, "صورت سود و زیان", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل سود و زیان")
        }
    }
}
