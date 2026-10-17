package com.example.data.repository

import com.example.data.dao.AccountingDao
import com.example.data.model.AccountGroupEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.TrialBalanceRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AccountingRepository(private val dao: AccountingDao) {

    val allGroups: Flow<List<AccountGroupEntity>> = dao.getAllAccountGroups()
    val allGenerals: Flow<List<GeneralAccountEntity>> = dao.getAllGeneralAccounts()
    val allSubsidiaries: Flow<List<SubsidiaryAccountEntity>> = dao.getAllSubsidiaryAccounts()
    val allJournalEntries: Flow<List<JournalEntryEntity>> = dao.getAllJournalEntries()
    val totalDebitSum: Flow<Long?> = dao.getTotalDebitSum()
    val totalCreditSum: Flow<Long?> = dao.getTotalCreditSum()

    fun getGeneralAccountsByGroup(groupCode: Int): Flow<List<GeneralAccountEntity>> =
        dao.getGeneralAccountsByGroup(groupCode)

    fun getSubsidiariesByGeneral(generalCode: Int): Flow<List<SubsidiaryAccountEntity>> =
        dao.getSubsidiaryAccountsByGeneral(generalCode)

    fun getJournalItems(voucherId: Long): Flow<List<JournalItemEntity>> =
        dao.getJournalItemsByVoucher(voucherId)

    suspend fun getJournalItemsSync(voucherId: Long): List<JournalItemEntity> =
        dao.getJournalItemsByVoucherSync(voucherId)

    /**
     * ثبت سند دوبل حسابداری با اعتبارسنجی تراز بودن (مجموع بدهکار = مجموع بستانکار)
     */
    suspend fun createJournalVoucher(
        entry: JournalEntryEntity,
        items: List<JournalItemEntity>
    ): Result<Long> {
        val totalDebit = items.sumOf { it.debit }
        val totalCredit = items.sumOf { it.credit }

        if (totalDebit != totalCredit) {
            return Result.failure(
                IllegalArgumentException("سند تراز نیست! مجموع بدهکار ($totalDebit) با مجموع بستانکار ($totalCredit) برابر نمی‌باشد.")
            )
        }

        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("سند باید حداقل شامل ۲ سطر آرتیکل باشد."))
        }

        val voucherId = dao.createVoucherWithItems(entry.copy(isBalanced = true), items)
        return Result.success(voucherId)
    }

    /**
     * گزارش تراز آزمایشی کل/معین با محاسبه مانده بدهکار و بستانکار
     */
    val trialBalance: Flow<List<TrialBalanceRow>> = combine(
        dao.getAllSubsidiaryAccounts(),
        dao.getAllGeneralAccounts(),
        dao.getAllAccountGroups(),
        dao.getAllJournalItems()
    ) { subs, generals, groups, items ->
        val generalMap = generals.associateBy { it.code }
        val groupMap = groups.associateBy { it.code }

        subs.map { sub ->
            val gen = generalMap[sub.generalCode]
            val grp = gen?.let { groupMap[it.groupCode] }

            val subItems = items.filter { it.subsidiaryCode == sub.code }
            val totalDebit = subItems.sumOf { it.debit }
            val totalCredit = subItems.sumOf { it.credit }

            val balance = totalDebit - totalCredit
            val balanceDebit = if (balance > 0) balance else 0L
            val balanceCredit = if (balance < 0) -balance else 0L

            TrialBalanceRow(
                subsidiaryCode = sub.code,
                title = sub.title,
                generalTitle = gen?.title ?: "نامشخص",
                groupTitle = grp?.title ?: "نامشخص",
                totalDebit = totalDebit,
                totalCredit = totalCredit,
                balanceDebit = balanceDebit,
                balanceCredit = balanceCredit
            )
        }.filter { it.totalDebit > 0 || it.totalCredit > 0 }
    }

    suspend fun voidVoucher(voucherId: Long) {
        dao.voidJournalEntry(voucherId)
    }

    suspend fun updateVoucher(entry: JournalEntryEntity) {
        dao.updateJournalEntry(entry)
    }

    suspend fun deleteVoucher(voucherId: Long) {
        val entry = dao.getJournalEntryById(voucherId)
        if (entry != null) {
            dao.deleteJournalItemsByVoucher(voucherId)
            dao.deleteJournalEntry(entry)
        }
    }

    suspend fun updateJournalItem(item: JournalItemEntity) {
        dao.updateJournalItem(item)
    }

    suspend fun deleteJournalItem(itemId: Long) {
        val item = dao.getJournalItemById(itemId)
        if (item != null) {
            dao.deleteJournalItem(item)
        }
    }

    suspend fun addPartyTransaction(
        partyId: Long,
        description: String,
        debit: Long,
        credit: Long,
        jalaliDate: String
    ): Long {
        val nextNumber = dao.getMaxVoucherNumber() + 1
        val voucher = JournalEntryEntity(
            voucherNumber = nextNumber,
            jalaliDate = jalaliDate,
            description = "سند معین طرف حساب: $description",
            isBalanced = true,
            status = "FINAL"
        )
        val voucherId = dao.insertJournalEntry(voucher)
        val items = mutableListOf<JournalItemEntity>()

        // ردیف معین شخص
        items.add(
            JournalItemEntity(
                voucherId = voucherId,
                generalCode = 102,
                subsidiaryCode = 10201,
                partyId = partyId,
                description = description,
                debit = debit,
                credit = credit
            )
        )

        // ردیف مقابل برای حفظ توازن دوبل (صندوق یا حساب جاری)
        items.add(
            JournalItemEntity(
                voucherId = voucherId,
                generalCode = 101,
                subsidiaryCode = 10101,
                partyId = null,
                description = "طرف مقابل سند معین: $description",
                debit = credit,
                credit = debit
            )
        )

        dao.insertJournalItems(items)
        return voucherId
    }

    fun getJournalItemsByParty(partyId: Long): Flow<List<JournalItemEntity>> =
        dao.getJournalItemsByParty(partyId)
}
