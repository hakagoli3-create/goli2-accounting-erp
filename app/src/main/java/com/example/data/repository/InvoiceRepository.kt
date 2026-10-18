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
            val stockDelta = when (invoice.invoiceType) {
                "SALE", "POS_SALE" -> -item.quantityInSecondary // خروج از انبار
                "PURCHASE" -> item.quantityInSecondary // ورود به انبار
                "RETURN_SALE" -> item.quantityInSecondary // برگشت از فروش: بازگشت به انبار
                "RETURN_PURCHASE" -> -item.quantityInSecondary // برگشت از خرید: خروج از انبار
                else -> -item.quantityInSecondary
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
                    description = "فاکتور ${invoice.invoiceNumber} (${invoice.invoiceType}) - ${item.productName}"
                )
            )
            inventoryDao.adjustStock(item.productId, stockDelta)
        }

        // 2. به روزرسانی مانده حساب طرف حساب (مشتری یا تأمین‌کننده)
        val creditAmount = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
        if (invoice.partyId != null && creditAmount > 0) {
            val factor = when (invoice.invoiceType) {
                "SALE", "POS_SALE", "PURCHASE" -> 1L
                "RETURN_SALE", "RETURN_PURCHASE" -> -1L
                else -> 1L
            }
            partyDao.updatePartyBalance(invoice.partyId, factor * creditAmount)
        }

        // 3. به روزرسانی خزانه‌داری (صندوق و پوز)
        val treasuryFactor = when (invoice.invoiceType) {
            "SALE", "POS_SALE", "RETURN_PURCHASE" -> 1L
            "PURCHASE", "RETURN_SALE" -> -1L
            else -> 1L
        }
        if (invoice.cashAmount > 0) {
            treasuryDao.updateCashBalance(invoice.cashDeskId, treasuryFactor * invoice.cashAmount)
        }
        if (invoice.posAmount > 0) {
            treasuryDao.updateBankBalance(invoice.bankAccountId, treasuryFactor * invoice.posAmount)
        }

        // 4. صدور خودکار سند دوبل حسابداری متناسب با نوع فاکتور
        val journalItems = mutableListOf<JournalItemEntity>()

        when (invoice.invoiceType) {
            "SALE", "POS_SALE" -> {
                // بدهکاران
                if (invoice.cashAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10101, partyId = null,
                            description = "تسویه نقدی فاکتور فروش ${invoice.invoiceNumber}", debit = invoice.cashAmount, credit = 0L
                        )
                    )
                }
                if (invoice.posAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10102, partyId = null,
                            description = "تسویه کارتخوان فاکتور فروش ${invoice.invoiceNumber}", debit = invoice.posAmount, credit = 0L
                        )
                    )
                }
                if (creditAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 102, subsidiaryCode = 10201, partyId = invoice.partyId,
                            description = "فروش نسیه به ${invoice.partyName} - فاکتور ${invoice.invoiceNumber}", debit = creditAmount, credit = 0L
                        )
                    )
                }
                if (invoice.chequeAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 102, subsidiaryCode = 10202, partyId = invoice.partyId,
                            description = "دریافت چک بابت فاکتور فروش ${invoice.invoiceNumber}", debit = invoice.chequeAmount, credit = 0L
                        )
                    )
                }
                // بستانکار: فروش کالا و خدمات
                journalItems.add(
                    JournalItemEntity(
                        voucherId = 0L, generalCode = 601, subsidiaryCode = 60101, partyId = invoice.partyId,
                        description = "درآمد حاصل از فروش کالا - فاکتور ${invoice.invoiceNumber}", debit = 0L, credit = invoice.totalAmount
                    )
                )
            }
            "PURCHASE" -> {
                // بدهکار: خرید کالا و انبار
                journalItems.add(
                    JournalItemEntity(
                        voucherId = 0L, generalCode = 103, subsidiaryCode = 10301, partyId = invoice.partyId,
                        description = "خرید کالا از ${invoice.partyName} - فاکتور خرید ${invoice.invoiceNumber}", debit = invoice.totalAmount, credit = 0L
                    )
                )
                // بستانکاران
                if (invoice.cashAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10101, partyId = null,
                            description = "پرداخت نقدی فاکتور خرید ${invoice.invoiceNumber}", debit = 0L, credit = invoice.cashAmount
                        )
                    )
                }
                if (invoice.posAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10102, partyId = null,
                            description = "پرداخت بانکی/کارت فاکتور خرید ${invoice.invoiceNumber}", debit = 0L, credit = invoice.posAmount
                        )
                    )
                }
                if (creditAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 301, subsidiaryCode = 30101, partyId = invoice.partyId,
                            description = "خرید نسیه از ${invoice.partyName} - فاکتور ${invoice.invoiceNumber}", debit = 0L, credit = creditAmount
                        )
                    )
                }
                if (invoice.chequeAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 301, subsidiaryCode = 30102, partyId = invoice.partyId,
                            description = "صدور چک بابت خرید فاکتور ${invoice.invoiceNumber}", debit = 0L, credit = invoice.chequeAmount
                        )
                    )
                }
            }
            "RETURN_SALE" -> {
                // برگشت از فروش: بدهکار شدن حساب برگشت از فروش
                journalItems.add(
                    JournalItemEntity(
                        voucherId = 0L, generalCode = 601, subsidiaryCode = 60102, partyId = invoice.partyId,
                        description = "برگشت کالا از فروش مشتری ${invoice.partyName} - فاکتور ${invoice.invoiceNumber}", debit = invoice.totalAmount, credit = 0L
                    )
                )
                // بستانکاران: بازپرداخت یا کاهش مطالبات
                if (invoice.cashAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10101, partyId = null,
                            description = "استرداد وجه نقد بابت مرجوعی فروش ${invoice.invoiceNumber}", debit = 0L, credit = invoice.cashAmount
                        )
                    )
                }
                if (invoice.posAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10102, partyId = null,
                            description = "انتقال بانکی بابت مرجوعی فروش ${invoice.invoiceNumber}", debit = 0L, credit = invoice.posAmount
                        )
                    )
                }
                if (creditAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 102, subsidiaryCode = 10201, partyId = invoice.partyId,
                            description = "کاهش بدهی مشتری بابت مرجوعی فروش ${invoice.invoiceNumber}", debit = 0L, credit = creditAmount
                        )
                    )
                }
                if (invoice.chequeAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 102, subsidiaryCode = 10202, partyId = invoice.partyId,
                            description = "ابطال/عودت چک بابت مرجوعی فروش ${invoice.invoiceNumber}", debit = 0L, credit = invoice.chequeAmount
                        )
                    )
                }
            }
            "RETURN_PURCHASE" -> {
                // برگشت از خرید: دریافت وجه یا کاهش بدهی به تأمین‌کننده (بدهکار)
                if (invoice.cashAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10101, partyId = null,
                            description = "دریافت نقد بابت مرجوعی خرید ${invoice.invoiceNumber}", debit = invoice.cashAmount, credit = 0L
                        )
                    )
                }
                if (invoice.posAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 101, subsidiaryCode = 10102, partyId = null,
                            description = "واریز بانکی بابت مرجوعی خرید ${invoice.invoiceNumber}", debit = invoice.posAmount, credit = 0L
                        )
                    )
                }
                if (creditAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 301, subsidiaryCode = 30101, partyId = invoice.partyId,
                            description = "کاهش بدهی به تأمین‌کننده بابت مرجوعی خرید ${invoice.invoiceNumber}", debit = creditAmount, credit = 0L
                        )
                    )
                }
                if (invoice.chequeAmount > 0) {
                    journalItems.add(
                        JournalItemEntity(
                            voucherId = 0L, generalCode = 301, subsidiaryCode = 30102, partyId = invoice.partyId,
                            description = "استرداد چک بابت مرجوعی خرید ${invoice.invoiceNumber}", debit = invoice.chequeAmount, credit = 0L
                        )
                    )
                }
                // بستانکار: برگشت از خرید
                journalItems.add(
                    JournalItemEntity(
                        voucherId = 0L, generalCode = 103, subsidiaryCode = 10302, partyId = invoice.partyId,
                        description = "برگشت از خرید به تأمین‌کننده ${invoice.partyName} - فاکتور ${invoice.invoiceNumber}", debit = 0L, credit = invoice.totalAmount
                    )
                )
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
                    description = "سند خودکار ${invoice.invoiceNumber} (${invoice.invoiceType})",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
        }
    }

    /**
     * معکوس کردن کلیه اثرات فاکتور (انبار، طرف‌حساب، صندوق/بانک و صدور سند معکوس)
     */
    private suspend fun rollbackInvoiceEffects(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        // 1. معکوس کردن کاردکس انبار با اعتبارسنجی انبار و کالا جهت جلوگیری از خطای Foreign Key
        val targetWarehouseId = try {
            val wh = inventoryDao.getWarehouseById(invoice.warehouseId)
                ?: inventoryDao.getDefaultWarehouse()
            wh?.id ?: 1L
        } catch (e: Exception) {
            1L
        }

        for (item in items) {
            val reverseStock = when (invoice.invoiceType) {
                "SALE", "POS_SALE" -> item.quantityInSecondary // برگشت به انبار
                "PURCHASE" -> -item.quantityInSecondary // کسر از انبار
                "RETURN_SALE" -> -item.quantityInSecondary // کسر از انبار
                "RETURN_PURCHASE" -> item.quantityInSecondary // برگشت به انبار
                else -> item.quantityInSecondary
            }
            try {
                val productExists = inventoryDao.getProductById(item.productId) != null
                if (productExists) {
                    inventoryDao.insertMovement(
                        InventoryMovementEntity(
                            productId = item.productId,
                            warehouseId = targetWarehouseId,
                            jalaliDate = JalaliDate.now().toStandardString(),
                            documentType = "ROLLBACK_${invoice.invoiceType}",
                            referenceNumber = "REV-${invoice.invoiceNumber}",
                            quantitySecondary = reverseStock,
                            unitPrice = item.unitPrice,
                            description = "برگشت اثر فاکتور ${invoice.invoiceNumber} - ${item.productName}"
                        )
                    )
                    inventoryDao.adjustStock(item.productId, reverseStock)
                }
            } catch (e: Exception) {
                android.util.Log.e("InvoiceRepository", "Error reversing stock for product ${item.productId}", e)
            }
        }

        // 2. معکوس کردن مانده حساب طرف‌حساب
        try {
            val creditAmount = if (invoice.paymentType == "CREDIT") invoice.totalAmount else invoice.creditAmount
            if (invoice.partyId != null && creditAmount > 0) {
                val factor = when (invoice.invoiceType) {
                    "SALE", "POS_SALE", "PURCHASE" -> -1L
                    "RETURN_SALE", "RETURN_PURCHASE" -> 1L
                    else -> -1L
                }
                partyDao.updatePartyBalance(invoice.partyId, factor * creditAmount)
            }
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Error reversing party balance", e)
        }

        // 3. معکوس کردن خزانه‌داری
        try {
            val treasuryFactor = when (invoice.invoiceType) {
                "SALE", "POS_SALE", "RETURN_PURCHASE" -> -1L
                "PURCHASE", "RETURN_SALE" -> 1L
                else -> -1L
            }
            if (invoice.cashAmount > 0) {
                treasuryDao.updateCashBalance(invoice.cashDeskId, treasuryFactor * invoice.cashAmount)
            }
            if (invoice.posAmount > 0) {
                treasuryDao.updateBankBalance(invoice.bankAccountId, treasuryFactor * invoice.posAmount)
            }
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Error reversing treasury balance", e)
        }

        // 4. ثبت سند معکوس در حسابداری
        try {
            val nextVoucher = accountingDao.getMaxVoucherNumber() + 1
            accountingDao.createVoucherWithItems(
                JournalEntryEntity(
                    voucherNumber = nextVoucher,
                    jalaliDate = JalaliDate.now().toStandardString(),
                    description = "سند ابطال/برگشت فاکتور شماره ${invoice.invoiceNumber}",
                    isBalanced = true,
                    status = "FINAL"
                ),
                listOf(
                    JournalItemEntity(
                        voucherId = 0L, generalCode = 601, subsidiaryCode = 60101, partyId = invoice.partyId,
                        description = "ابطال اثر مالی فاکتور ${invoice.invoiceNumber}", debit = invoice.totalAmount, credit = 0L
                    ),
                    JournalItemEntity(
                        voucherId = 0L, generalCode = 101, subsidiaryCode = if (invoice.posAmount > 0) 10102 else 10101, partyId = null,
                        description = "برگشت تسویه فاکتور باطل شده ${invoice.invoiceNumber}", debit = 0L, credit = invoice.totalAmount
                    )
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Error creating rollback accounting voucher", e)
        }
    }

    /**
     * ویرایش کامل فاکتور موجود: برگشت اثرات قبلی و اعمال تغییرات جدید
     */
    suspend fun updateExistingInvoice(
        invoiceId: Long,
        updatedInvoice: InvoiceEntity,
        newItems: List<InvoiceItemEntity>
    ): Boolean {
        return try {
            val oldInvoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return false
            val oldInvoice = oldInvoiceWithItems.invoice

            // اگر فاکتور قبلاً قطعی بود، اثرات گذشته را برگشت می‌زنیم
            if (oldInvoice.status == "FINAL") {
                rollbackInvoiceEffects(oldInvoice, oldInvoiceWithItems.items)
            }

            // ثبت اطلاعات جدید فاکتور
            val readyInvoice = updatedInvoice.copy(id = invoiceId)
            invoiceDao.updateInvoice(readyInvoice)
            invoiceDao.deleteInvoiceItemsForInvoice(invoiceId)
            val attachedItems = newItems.map { it.copy(invoiceId = invoiceId) }
            invoiceDao.insertInvoiceItems(attachedItems)

            // اعمال مجدد اثرات فاکتور در صورتی که قطعی باشد
            if (readyInvoice.status == "FINAL") {
                applyInvoiceEffects(readyInvoice, attachedItems)
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Error updating existing invoice", e)
            false
        }
    }

    /**
     * تبدیل سریع پیش‌فاکتور به فاکتور فروش قطعی
     */
    suspend fun convertProformaToFinal(invoiceId: Long): Boolean {
        return try {
            val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return false
            val invoice = invoiceWithItems.invoice
            if (invoice.status != "PROFORMA") return false

            val updatedInvoice = invoice.copy(status = "FINAL")
            invoiceDao.updateInvoice(updatedInvoice)
            applyInvoiceEffects(updatedInvoice, invoiceWithItems.items)
            true
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Error converting proforma to final", e)
            false
        }
    }

    /**
     * ابطال تمامی اثرات فاکتور و علامت‌گذاری به عنوان باطل شده (VOID)
     */
    suspend fun voidInvoice(invoiceId: Long): Boolean {
        return try {
            val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return false
            val invoice = invoiceWithItems.invoice
            if (invoice.status == "VOID") return false

            if (invoice.status == "FINAL") {
                rollbackInvoiceEffects(invoice, invoiceWithItems.items)
            }

            invoiceDao.setInvoiceStatus(invoiceId, "VOID")
            true
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Error voiding invoice", e)
            false
        }
    }

    suspend fun deleteInvoice(invoiceId: Long): Boolean {
        return try {
            val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return false
            val invoice = invoiceWithItems.invoice

            // ۱. اگر فاکتور قطعی است، ابتدا اثرات انبار و مالی آن معکوس شوند
            if (invoice.status == "FINAL") {
                try {
                    rollbackInvoiceEffects(invoice, invoiceWithItems.items)
                } catch (e: Exception) {
                    android.util.Log.e("InvoiceRepository", "Warning: rollback before delete failed", e)
                }
            }

            // ۲. پاکسازی قطعی تمام گردش‌های انبار (Kardex) و اسناد معکوس (REV-INV) مربوط به این فاکتور جهت رفع رکوردهای یتیم (Orphan Removal)
            try {
                inventoryDao.deleteMovementsByReference(invoice.invoiceNumber)
                inventoryDao.deleteMovementsByReference("REV-${invoice.invoiceNumber}")
            } catch (e: Exception) {
                android.util.Log.e("InvoiceRepository", "Error cleaning kardex movements for deleted invoice ${invoice.invoiceNumber}", e)
            }

            // ۳. حذف ردیف‌های اقلام فاکتور ابتدا تا نقض کلید خارجی در دیتابیس رخ ندهد
            try {
                invoiceDao.deleteInvoiceItemsForInvoice(invoiceId)
            } catch (e: Exception) {
                android.util.Log.e("InvoiceRepository", "Error deleting invoice items", e)
            }

            // ۴. حذف خود سند فاکتور از دیتابیس
            val rowsDeleted = try {
                invoiceDao.deleteInvoiceById(invoiceId)
            } catch (e: Exception) {
                invoiceDao.deleteInvoice(invoice)
                1
            }
            rowsDeleted > 0
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Fatal error in deleteInvoice", e)
            false
        }
    }

    suspend fun generateNextInvoiceNumber(): String {
        val maxId = invoiceDao.getMaxInvoiceId()
        return "INV-${1000 + maxId + 1}"
    }
}
