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
     * ثبت کامل فاکتور (فروش، خرید، مرجوعی فروش، مرجوعی خرید) به همراه کاردکس انبارداری، خزانه‌داری، مانده حساب طرف‌حساب و صدور سند اتوماتیک دوبل
     */
    suspend fun createInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        val invoiceId = invoiceDao.createInvoiceWithItems(invoice, items)

        // اگر پیش‌فاکتور (PROFORMA) باشد، از انبار کسر نمی‌شود و سند قطعی صادر نمی‌گردد
        if (invoice.status == "PROFORMA") {
            return invoiceId
        }

        applyInvoiceEffects(invoice.copy(id = invoiceId), items)
        return invoiceId
    }

    /**
     * اعمال اثرات انبارداری، حسابداری، و خزانه‌داری انواع فاکتورها
     */
    private suspend fun applyInvoiceEffects(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        // 1. کاردکس و موجودی انبار برای هر قلم
        val isDeductingFromStock = invoice.invoiceType in listOf("SALE", "POS_SALE", "RETURN_PURCHASE")
        for (item in items) {
            val stockDelta = if (isDeductingFromStock) -item.quantityInSecondary else item.quantityInSecondary

            inventoryDao.insertMovement(
                InventoryMovementEntity(
                    productId = item.productId,
                    warehouseId = invoice.warehouseId,
                    jalaliDate = invoice.jalaliDate,
                    documentType = invoice.invoiceType,
                    referenceNumber = invoice.invoiceNumber,
                    quantitySecondary = stockDelta,
                    unitPrice = item.unitPrice,
                    description = "فاکتور ${invoice.invoiceType} ${invoice.invoiceNumber} - ${item.productName}"
                )
            )
            inventoryDao.adjustStock(item.productId, stockDelta)
        }

        // 2. مانده حساب طرف‌حساب
        if (invoice.partyId != null) {
            val creditAmount = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
            if (creditAmount > 0) {
                val balanceDelta = when (invoice.invoiceType) {
                    "SALE", "POS_SALE" -> creditAmount // بدهی مشتری زیاد می‌شود
                    "PURCHASE" -> -creditAmount // ما به تامین‌کننده بدهکار می‌شویم
                    "RETURN_SALE" -> -creditAmount // بدهی مشتری کم می‌شود
                    "RETURN_PURCHASE" -> creditAmount // طلب ما زیاد می‌شود
                    else -> creditAmount
                }
                partyDao.updatePartyBalance(invoice.partyId, balanceDelta)
            }
        }

        // 3. خزانه‌داری (صندوق و پوز)
        val treasuryDirection = when (invoice.invoiceType) {
            "SALE", "POS_SALE", "RETURN_PURCHASE" -> 1 // ورود وجه
            "PURCHASE", "RETURN_SALE" -> -1 // خروج وجه
            else -> 1
        }
        if (invoice.cashAmount > 0) {
            treasuryDao.updateCashBalance(invoice.cashDeskId, treasuryDirection * invoice.cashAmount)
        }
        if (invoice.posAmount > 0) {
            treasuryDao.updateBankBalance(invoice.bankAccountId, treasuryDirection * invoice.posAmount)
        }

        // 4. صدور خودکار سند دوبل حسابداری
        val journalItems = mutableListOf<JournalItemEntity>()
        when (invoice.invoiceType) {
            "SALE", "POS_SALE" -> {
                if (invoice.cashAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 101, 10101, null, "تسویه نقدی فاکتور فروش ${invoice.invoiceNumber}", invoice.cashAmount, 0L))
                }
                if (invoice.posAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 101, 10102, null, "تسویه پوز فاکتور فروش ${invoice.invoiceNumber}", invoice.posAmount, 0L))
                }
                val cr = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
                if (cr > 0) {
                    journalItems.add(JournalItemEntity(0L, 102, 10201, invoice.partyId, "فروش نسیه به ${invoice.partyName} فاکتور ${invoice.invoiceNumber}", cr, 0L))
                }
                if (invoice.chequeAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 102, 10202, invoice.partyId, "دریافت چک بابت فاکتور ${invoice.invoiceNumber}", invoice.chequeAmount, 0L))
                }
                journalItems.add(JournalItemEntity(0L, 601, 60101, invoice.partyId, "درآمد فروش کالا - فاکتور ${invoice.invoiceNumber}", 0L, invoice.totalAmount))
            }
            "PURCHASE" -> {
                journalItems.add(JournalItemEntity(0L, 103, 10301, invoice.partyId, "خرید کالا و افزایش انبار - فاکتور خرید ${invoice.invoiceNumber}", invoice.totalAmount, 0L))
                if (invoice.cashAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 101, 10101, null, "پرداخت نقدی خرید ${invoice.invoiceNumber}", 0L, invoice.cashAmount))
                }
                if (invoice.posAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 101, 10102, null, "پرداخت پوز/بانک خرید ${invoice.invoiceNumber}", 0L, invoice.posAmount))
                }
                val cr = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
                if (cr > 0) {
                    journalItems.add(JournalItemEntity(0L, 301, 30101, invoice.partyId, "خرید نسیه از ${invoice.partyName} فاکتور ${invoice.invoiceNumber}", 0L, cr))
                }
                if (invoice.chequeAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 301, 30102, invoice.partyId, "پرداخت چک بابت خرید ${invoice.invoiceNumber}", 0L, invoice.chequeAmount))
                }
            }
            "RETURN_SALE" -> {
                journalItems.add(JournalItemEntity(0L, 601, 60101, invoice.partyId, "برگشت از فروش و مرجوعی کالا - فاکتور ${invoice.invoiceNumber}", invoice.totalAmount, 0L))
                val cr = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
                if (cr > 0) {
                    journalItems.add(JournalItemEntity(0L, 102, 10201, invoice.partyId, "کاهش بدهی مشتری بابت مرجوعی ${invoice.invoiceNumber}", 0L, cr))
                }
                if (invoice.cashAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 101, 10101, null, "استرداد وجه نقد مرجوعی ${invoice.invoiceNumber}", 0L, invoice.cashAmount))
                }
                if (invoice.posAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 101, 10102, null, "استرداد پوز/بانک مرجوعی ${invoice.invoiceNumber}", 0L, invoice.posAmount))
                }
            }
            "RETURN_PURCHASE" -> {
                val cr = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
                if (cr > 0) {
                    journalItems.add(JournalItemEntity(0L, 301, 30101, invoice.partyId, "کاهش طلب تامین‌کننده بابت مرجوعی کالا ${invoice.invoiceNumber}", cr, 0L))
                }
                if (invoice.cashAmount > 0) {
                    journalItems.add(JournalItemEntity(0L, 101, 10101, null, "دریافت وجه نقد بابت برگشت از خرید ${invoice.invoiceNumber}", invoice.cashAmount, 0L))
                }
                journalItems.add(JournalItemEntity(0L, 103, 10301, invoice.partyId, "خروج کالای مرجوعی به تامین‌کننده ${invoice.invoiceNumber}", 0L, invoice.totalAmount))
            }
        }

        val totalDebit = journalItems.sumOf { it.debit }
        val totalCredit = journalItems.sumOf { it.credit }
        if (totalDebit == totalCredit && journalItems.isNotEmpty()) {
            val nextVoucherNumber = accountingDao.getMaxVoucherNumber() + 1
            accountingDao.createVoucherWithItems(
                JournalEntryEntity(
                    voucherNumber = nextVoucherNumber,
                    jalaliDate = invoice.jalaliDate,
                    description = "سند خودکار ${invoice.invoiceType} شماره ${invoice.invoiceNumber}",
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

        if (invoice.status == "FINAL") {
            val isDeductingFromStock = invoice.invoiceType in listOf("SALE", "POS_SALE", "RETURN_PURCHASE")
            for (item in invoiceWithItems.items) {
                val reverseStock = if (isDeductingFromStock) item.quantityInSecondary else -item.quantityInSecondary
                inventoryDao.insertMovement(
                    InventoryMovementEntity(
                        productId = item.productId,
                        warehouseId = invoice.warehouseId,
                        jalaliDate = JalaliDate.now().toStandardString(),
                        documentType = "VOID_" + invoice.invoiceType,
                        referenceNumber = "VOID-${invoice.invoiceNumber}",
                        quantitySecondary = reverseStock,
                        unitPrice = item.unitPrice,
                        description = "ابطال فاکتور ${invoice.invoiceNumber} - اصلاح موجودی ${item.productName}"
                    )
                )
                inventoryDao.adjustStock(item.productId, reverseStock)
            }

            if (invoice.partyId != null) {
                val creditAmount = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
                if (creditAmount > 0) {
                    val balanceDelta = when (invoice.invoiceType) {
                        "SALE", "POS_SALE" -> -creditAmount
                        "PURCHASE" -> creditAmount
                        "RETURN_SALE" -> creditAmount
                        "RETURN_PURCHASE" -> -creditAmount
                        else -> -creditAmount
                    }
                    partyDao.updatePartyBalance(invoice.partyId, balanceDelta)
                }
            }

            val treasuryReverseDir = when (invoice.invoiceType) {
                "SALE", "POS_SALE", "RETURN_PURCHASE" -> -1
                "PURCHASE", "RETURN_SALE" -> 1
                else -> -1
            }
            if (invoice.cashAmount > 0) {
                treasuryDao.updateCashBalance(invoice.cashDeskId, treasuryReverseDir * invoice.cashAmount)
            }
            if (invoice.posAmount > 0) {
                treasuryDao.updateBankBalance(invoice.bankAccountId, treasuryReverseDir * invoice.posAmount)
            }
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
