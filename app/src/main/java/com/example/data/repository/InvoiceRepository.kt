package com.example.data.repository

import com.example.data.dao.AccountingDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.PartyDao
import com.example.data.dao.TreasuryDao
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceWithItems
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val inventoryDao: InventoryDao,
    private val partyDao: PartyDao,
    private val treasuryDao: TreasuryDao,
    private val accountingDao: AccountingDao
) {

    val allInvoicesWithItems: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoicesWithItems()
    val totalSalesSum: Flow<Long?> = invoiceDao.getTotalSalesSum()

    fun getTodaySalesSum(todayJalali: String): Flow<Long?> = invoiceDao.getTodaySalesSum(todayJalali)
    fun getTodayInvoiceCount(todayJalali: String): Flow<Int> = invoiceDao.getTodayInvoiceCount(todayJalali)
    fun getInvoicesForParty(partyId: Long): Flow<List<InvoiceEntity>> = invoiceDao.getInvoicesForParty(partyId)

    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems? =
        invoiceDao.getInvoiceWithItemsById(id)

    /**
     * ثبت کامل فاکتور به همراه آپدیت انبارداری، خزانه‌داری، مانده حساب مشتری، و صدور سند اتوماتیک دوبل حسابداری
     */
    suspend fun createInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        val invoiceId = invoiceDao.createInvoiceWithItems(invoice, items)

        // اگر پیش‌فاکتور (PROFORMA) باشد، از انبار کسر نمی‌شود و سند قطعی حسابداری صادر نمی‌گردد
        if (invoice.status == "PROFORMA") {
            return invoiceId
        }

        applyInvoiceEffects(invoice.copy(id = invoiceId), items)
        return invoiceId
    }

    /**
     * اعمال اثرات انبارداری، حسابداری، و خزانه‌داری یک فاکتور قطعی
     */
    private suspend fun applyInvoiceEffects(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        // 1. به روزرسانی کاردکس و موجودی انبار برای هر قلم
        for (item in items) {
            val stockDelta = if (invoice.invoiceType in listOf("SALE", "POS_SALE")) {
                -item.quantityInSecondary // خروج از انبار
            } else {
                item.quantityInSecondary // ورود به انبار
            }

            inventoryDao.insertMovement(
                InventoryMovementEntity(
                    productId = item.productId,
                    warehouseId = invoice.warehouseId,
                    jalaliDate = invoice.jalaliDate,
                    documentType = invoice.invoiceType,
                    referenceNumber = invoice.invoiceNumber,
                    quantitySecondary = stockDelta,
                    unitPrice = item.unitPrice,
                    description = "فاکتور ${invoice.invoiceNumber} - ${item.productName}"
                )
            )
            inventoryDao.adjustStock(item.productId, stockDelta)
        }

        // 2. به روزرسانی مانده حساب طرف حساب
        if (invoice.partyId != null) {
            val debtChange = when (invoice.paymentType) {
                "CREDIT" -> invoice.totalAmount
                "MIXED" -> invoice.creditAmount
                else -> 0L
            }
            if (debtChange > 0) {
                partyDao.updatePartyBalance(invoice.partyId, debtChange)
            }
        }

        // 3. به روزرسانی خزانه‌داری (صندوق و پوز)
        if (invoice.cashAmount > 0) {
            treasuryDao.updateCashBalance(invoice.cashDeskId, invoice.cashAmount)
        }
        if (invoice.posAmount > 0) {
            treasuryDao.updateBankBalance(invoice.bankAccountId, invoice.posAmount)
        }

        // 4. صدور خودکار سند دوبل حسابداری در دفتر روزنامه
        val journalItems = mutableListOf<JournalItemEntity>()

        // ردیف‌های بدهکار
        if (invoice.cashAmount > 0) {
            journalItems.add(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = 10101, // صندوق
                    partyId = null,
                    description = "تسویه نقدی فاکتور شماره ${invoice.invoiceNumber}",
                    debit = invoice.cashAmount,
                    credit = 0L
                )
            )
        }
        if (invoice.posAmount > 0) {
            journalItems.add(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = 10102, // بانک و پوز
                    partyId = null,
                    description = "تسویه پوز فاکتور شماره ${invoice.invoiceNumber}",
                    debit = invoice.posAmount,
                    credit = 0L
                )
            )
        }
        val creditAmount = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
        if (creditAmount > 0) {
            journalItems.add(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 102,
                    subsidiaryCode = 10201, // بدهکاران تجاری
                    partyId = invoice.partyId,
                    description = "فروش نسیه به ${invoice.partyName} فاکتور ${invoice.invoiceNumber}",
                    debit = creditAmount,
                    credit = 0L
                )
            )
        }
        if (invoice.chequeAmount > 0) {
            journalItems.add(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 102,
                    subsidiaryCode = 10202, // اسناد دریافتنی
                    partyId = invoice.partyId,
                    description = "دریافت چک بابت فاکتور ${invoice.invoiceNumber}",
                    debit = invoice.chequeAmount,
                    credit = 0L
                )
            )
        }

        // ردیف بستانکار: فروش کالا و خدمات
        journalItems.add(
            JournalItemEntity(
                voucherId = 0L,
                generalCode = 601,
                subsidiaryCode = 60101, // درآمد فروش
                partyId = invoice.partyId,
                description = "درآمد حاصل از فروش کالا - فاکتور ${invoice.invoiceNumber}",
                debit = 0L,
                credit = invoice.totalAmount
            )
        )

        val totalDebit = journalItems.sumOf { it.debit }
        val totalCredit = journalItems.sumOf { it.credit }
        if (totalDebit == totalCredit && journalItems.isNotEmpty()) {
            val nextVoucherNumber = accountingDao.getMaxVoucherNumber() + 1
            accountingDao.createVoucherWithItems(
                JournalEntryEntity(
                    voucherNumber = nextVoucherNumber,
                    jalaliDate = invoice.jalaliDate,
                    description = "سند اتوماتیک فاکتور فروش شماره ${invoice.invoiceNumber}",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
        }
    }

    /**
     * تبدیل سریع پیش‌فاکتور به فاکتور فروش قطعی
     */
    suspend fun convertProformaToFinal(invoiceId: Long): Boolean {
        val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return false
        val invoice = invoiceWithItems.invoice
        if (invoice.status != "PROFORMA") return false

        val updatedInvoice = invoice.copy(status = "FINAL")
        invoiceDao.updateInvoice(updatedInvoice)
        applyInvoiceEffects(updatedInvoice, invoiceWithItems.items)
        return true
    }

    /**
     * ابطال تمامی اثرات فاکتور و علامت‌گذاری به عنوان باطل شده (VOID)
     */
    suspend fun voidInvoice(invoiceId: Long): Boolean {
        val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return false
        val invoice = invoiceWithItems.invoice
        if (invoice.status == "VOID") return false

        // اگر فاکتور قطعی بود، اثرات انبارداری، مشتری و خزانه‌داری را معکوس می‌کنیم
        if (invoice.status == "FINAL") {
            // 1. برگشت موجودی به انبار
            for (item in invoiceWithItems.items) {
                val reverseStock = item.quantityInSecondary // بازگشت به انبار
                inventoryDao.insertMovement(
                    InventoryMovementEntity(
                        productId = item.productId,
                        warehouseId = invoice.warehouseId,
                        jalaliDate = JalaliDate.now().toStandardString(),
                        documentType = "RETURN_SALE",
                        referenceNumber = "VOID-${invoice.invoiceNumber}",
                        quantitySecondary = reverseStock,
                        unitPrice = item.unitPrice,
                        description = "ابطال فاکتور ${invoice.invoiceNumber} - برگشت ${item.productName} به انبار"
                    )
                )
                inventoryDao.adjustStock(item.productId, reverseStock)
            }

            // 2. برگشت مانده حساب بدهی مشتری
            if (invoice.partyId != null) {
                val debtChange = when (invoice.paymentType) {
                    "CREDIT" -> invoice.totalAmount
                    "MIXED" -> invoice.creditAmount
                    else -> 0L
                }
                if (debtChange > 0) {
                    partyDao.updatePartyBalance(invoice.partyId, -debtChange)
                }
            }

            // 3. کسر از صندوق / بانک بابت برگشت وجه
            if (invoice.cashAmount > 0) {
                treasuryDao.updateCashBalance(invoice.cashDeskId, -invoice.cashAmount)
            }
            if (invoice.posAmount > 0) {
                treasuryDao.updateBankBalance(invoice.bankAccountId, -invoice.posAmount)
            }

            // 4. صدور سند معکوس در حسابداری
            val nextVoucher = accountingDao.getMaxVoucherNumber() + 1
            accountingDao.createVoucherWithItems(
                JournalEntryEntity(
                    voucherNumber = nextVoucher,
                    jalaliDate = JalaliDate.now().toStandardString(),
                    description = "سند ابطال فاکتور فروش شماره ${invoice.invoiceNumber}",
                    isBalanced = true,
                    status = "FINAL"
                ),
                listOf(
                    JournalItemEntity(
                        voucherId = 0L,
                        generalCode = 601,
                        subsidiaryCode = 60101,
                        partyId = invoice.partyId,
                        description = "ابطال درآمد فروش فاکتور ${invoice.invoiceNumber}",
                        debit = invoice.totalAmount,
                        credit = 0L
                    ),
                    JournalItemEntity(
                        voucherId = 0L,
                        generalCode = 101,
                        subsidiaryCode = if (invoice.posAmount > 0) 10102 else 10101,
                        partyId = null,
                        description = "برگشت تسویه فاکتور باطل شده ${invoice.invoiceNumber}",
                        debit = 0L,
                        credit = invoice.totalAmount
                    )
                )
            )
        }

        invoiceDao.setInvoiceStatus(invoiceId, "VOID")
        return true
    }

    suspend fun deleteInvoice(invoiceId: Long): Boolean {
        voidInvoice(invoiceId)
        val invoice = invoiceDao.getInvoiceById(invoiceId)
        if (invoice != null) {
            invoiceDao.deleteInvoice(invoice)
            invoiceDao.deleteInvoiceItemsForInvoice(invoiceId)
            return true
        }
        return false
    }

    suspend fun generateNextInvoiceNumber(): String {
        val maxId = invoiceDao.getMaxInvoiceId()
        return "INV-${1000 + maxId + 1}"
    }
}
