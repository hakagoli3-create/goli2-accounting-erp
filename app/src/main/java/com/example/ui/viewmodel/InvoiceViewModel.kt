package com.example.ui.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceWithItems
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.StoreProfile
import com.example.data.repository.InventoryRepository
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.PartyRepository
import com.example.data.repository.TreasuryRepository
import com.example.util.BluetoothThermalPrinter
import com.example.util.EscPosPrinterHelper
import com.example.util.ExportHelper
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val product: ProductEntity,
    val isPrimaryUnit: Boolean, // آیا واحد اصلی (کارتن) است یا فرعی (عدد)
    val quantity: Double,
    val unitPrice: Long,
    val discount: Long = 0L // تخفیف ردیفی روی این قلم
) {
    val unitName: String get() = if (isPrimaryUnit) product.primaryUnit else product.secondaryUnit
    val quantityInSecondary: Double get() = if (isPrimaryUnit) quantity * product.conversionRate else quantity
    val rawTotal: Long get() = (quantity * unitPrice).toLong()
    val totalPrice: Long get() = if (rawTotal > discount) rawTotal - discount else 0L
}

class InvoiceViewModel(
    private val invoiceRepo: InvoiceRepository,
    private val inventoryRepo: InventoryRepository,
    private val partyRepo: PartyRepository,
    private val treasuryRepo: TreasuryRepository
) : ViewModel() {

    val allInvoices: StateFlow<List<InvoiceWithItems>> = invoiceRepo.allInvoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = inventoryRepo.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<PartyEntity>> = partyRepo.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<PartyEntity>> = partyRepo.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParties: StateFlow<List<PartyEntity>> = partyRepo.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCashDesks: StateFlow<List<CashDeskEntity>> = treasuryRepo.allCashDesks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBankAccounts: StateFlow<List<BankAccountEntity>> = treasuryRepo.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Invoice Type: SALE, PURCHASE, RETURN_SALE, RETURN_PURCHASE ---
    private val _currentInvoiceType = MutableStateFlow("SALE")
    val currentInvoiceType: StateFlow<String> = _currentInvoiceType.asStateFlow()

    // --- Pricing Mode: RETAIL (تکی) vs WHOLESALE (عمده) ---
    private val _pricingMode = MutableStateFlow("RETAIL")
    val pricingMode: StateFlow<String> = _pricingMode.asStateFlow()

    // --- POS & Fast Cart State ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedParty = MutableStateFlow<PartyEntity?>(null)
    val selectedParty: StateFlow<PartyEntity?> = _selectedParty.asStateFlow()

    private val _discountAmount = MutableStateFlow(0L)
    val discountAmount: StateFlow<Long> = _discountAmount.asStateFlow()

    private val _applyTax = MutableStateFlow(false)
    val applyTax: StateFlow<Boolean> = _applyTax.asStateFlow()

    // Selected destination bank & cash desk
    private val _selectedCashDeskId = MutableStateFlow(1L)
    val selectedCashDeskId: StateFlow<Long> = _selectedCashDeskId.asStateFlow()

    private val _selectedBankAccountId = MutableStateFlow(1L)
    val selectedBankAccountId: StateFlow<Long> = _selectedBankAccountId.asStateFlow()

    // Direct Expense on invoice (هزینه مستقیم مانند حمل، بسته‌بندی و...)
    private val _directExpenseAmount = MutableStateFlow(0L)
    val directExpenseAmount: StateFlow<Long> = _directExpenseAmount.asStateFlow()

    private val _directExpenseType = MutableStateFlow("کرایه حمل و باربری")
    val directExpenseType: StateFlow<String> = _directExpenseType.asStateFlow()

    // Issue as Proforma (پیش‌فاکتور)
    private val _issueAsProforma = MutableStateFlow(false)
    val issueAsProforma: StateFlow<Boolean> = _issueAsProforma.asStateFlow()

    // Printable Receipt State
    private val _previewReceiptText = MutableStateFlow<String?>(null)
    val previewReceiptText: StateFlow<String?> = _previewReceiptText.asStateFlow()

    // Selected Invoice for Detailed Official View / Export Dialog
    private val _selectedInvoiceForDetails = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoiceForDetails: StateFlow<InvoiceWithItems?> = _selectedInvoiceForDetails.asStateFlow()

    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    fun setCurrentInvoiceType(type: String) {
        _currentInvoiceType.value = type
        clearCart()
    }

    fun setPricingMode(mode: String) {
        _pricingMode.value = mode
        val isWholesale = mode == "WHOLESALE"
        val isPurchase = _currentInvoiceType.value in listOf("PURCHASE", "RETURN_PURCHASE")
        val updated = _cartItems.value.map { item ->
            val p = item.product
            val price = if (isPurchase) {
                if (item.isPrimaryUnit) (p.buyPrice * p.conversionRate).toLong() else p.buyPrice
            } else if (isWholesale) {
                if (item.isPrimaryUnit) (p.wholesalePrice * p.conversionRate).toLong() else p.wholesalePrice
            } else {
                if (item.isPrimaryUnit) (p.retailPrice * p.conversionRate).toLong() else p.retailPrice
            }
            item.copy(unitPrice = price)
        }
        _cartItems.value = updated
    }

    fun setSelectedCashDesk(id: Long) {
        _selectedCashDeskId.value = id
    }

    fun setSelectedBankAccount(id: Long) {
        _selectedBankAccountId.value = id
    }

    fun addToCart(product: ProductEntity, isPrimary: Boolean = false, forceWholesale: Boolean? = null) {
        val isWholesale = forceWholesale ?: (_pricingMode.value == "WHOLESALE")
        val isPurchase = _currentInvoiceType.value in listOf("PURCHASE", "RETURN_PURCHASE")
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id && it.isPrimaryUnit == isPrimary }

        val price = if (isPurchase) {
            if (isPrimary) (product.buyPrice * product.conversionRate).toLong() else product.buyPrice
        } else if (isWholesale) {
            if (isPrimary) (product.wholesalePrice * product.conversionRate).toLong() else product.wholesalePrice
        } else {
            if (isPrimary) (product.retailPrice * product.conversionRate).toLong() else product.retailPrice
        }

        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1.0)
        } else {
            current.add(
                CartItem(
                    product = product,
                    isPrimaryUnit = isPrimary,
                    quantity = 1.0,
                    unitPrice = price,
                    discount = 0L
                )
            )
        }
        _cartItems.value = current
    }

    fun toggleCartItemUnit(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            val newIsPrimary = !item.isPrimaryUnit
            val isWholesale = _pricingMode.value == "WHOLESALE"
            val isPurchase = _currentInvoiceType.value in listOf("PURCHASE", "RETURN_PURCHASE")
            val price = if (isPurchase) {
                if (newIsPrimary) (item.product.buyPrice * item.product.conversionRate).toLong() else item.product.buyPrice
            } else if (isWholesale) {
                if (newIsPrimary) (item.product.wholesalePrice * item.product.conversionRate).toLong() else item.product.wholesalePrice
            } else {
                if (newIsPrimary) (item.product.retailPrice * item.product.conversionRate).toLong() else item.product.retailPrice
            }
            current[index] = item.copy(isPrimaryUnit = newIsPrimary, unitPrice = price)
            _cartItems.value = current
        }
    }

    fun updateCartItemQuantity(index: Int, newQuantity: Double) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            if (newQuantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQuantity)
            }
            _cartItems.value = current
        }
    }

    fun updateCartItemPrice(index: Int, newPrice: Long) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(unitPrice = newPrice)
            _cartItems.value = current
        }
    }

    fun updateCartItemDiscount(index: Int, discount: Long) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(discount = discount)
            _cartItems.value = current
        }
    }

    fun setDirectExpense(amount: Long, type: String = "کرایه حمل و باربری") {
        _directExpenseAmount.value = amount
        _directExpenseType.value = type
    }

    fun setIssueAsProforma(isProforma: Boolean) {
        _issueAsProforma.value = isProforma
    }

    fun setCartItemQuantities(index: Int, cartons: Double, units: Double, rowDiscount: Long = 0L) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            val conversion = if (item.product.conversionRate > 0) item.product.conversionRate else 1.0
            val totalInSecondary = (cartons * conversion) + units
            if (totalInSecondary <= 0) {
                current.removeAt(index)
            } else {
                val qty = if (item.isPrimaryUnit) totalInSecondary / conversion else totalInSecondary
                current[index] = item.copy(quantity = qty, discount = rowDiscount)
            }
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountAmount.value = 0L
        _applyTax.value = false
        _directExpenseAmount.value = 0L
        _issueAsProforma.value = false
        _selectedParty.value = null
    }

    fun selectParty(party: PartyEntity?) {
        _selectedParty.value = party
        // If customer has a specific category, automatically align pricing mode
        if (party != null && party.partyType in listOf("CUSTOMER", "BOTH")) {
            if (party.customerCategory == "WHOLESALE") {
                setPricingMode("WHOLESALE")
            } else {
                setPricingMode("RETAIL")
            }
        }
    }

    fun setDiscount(amount: Long) {
        _discountAmount.value = amount
    }

    fun toggleTax(apply: Boolean) {
        _applyTax.value = apply
    }

    fun clearAlertMessage() {
        _alertMessage.value = null
    }

    fun closeReceiptPreview() {
        _previewReceiptText.value = null
    }

    fun openInvoiceDetails(invoice: InvoiceWithItems) {
        _selectedInvoiceForDetails.value = invoice
    }

    fun closeInvoiceDetails() {
        _selectedInvoiceForDetails.value = null
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = inventoryRepo.getProductByBarcode(barcode.trim())
            if (product != null) {
                addToCart(product, isPrimary = false)
            } else {
                _alertMessage.value = "کالایی با بارکد $barcode در انبار یا فروشگاه یافت نشد."
            }
        }
    }

    fun calculateGrossProfit(inv: InvoiceWithItems): Long {
        val totalRevenue = inv.invoice.subtotalAmount - inv.invoice.discountAmount
        val productMap = allProducts.value.associateBy { it.id }
        val cogs = inv.items.sumOf { item ->
            val p = productMap[item.productId]
            val buyPrice = p?.buyPrice ?: (item.unitPrice * 0.75).toLong()
            (item.quantityInSecondary * buyPrice).toLong()
        }
        return totalRevenue - cogs
    }

    /**
     * محاسبه پورسانت ویزیتور یا فروشنده بر اساس درصد تعیین شده
     */
    fun calculateVisitorCommission(inv: InvoiceWithItems, ratePercent: Double = 3.0): Long {
        val netSales = inv.invoice.subtotalAmount - inv.invoice.discountAmount
        return if (netSales > 0) ((netSales * ratePercent) / 100.0).toLong() else 0L
    }

    fun convertProformaToFinal(invoiceWithItems: InvoiceWithItems, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val success = invoiceRepo.convertProformaToFinal(invoiceWithItems.invoice.id)
            if (success) {
                _alertMessage.value = "پیش‌فاکتور شماره ${invoiceWithItems.invoice.invoiceNumber} به فاکتور قطعی تبدیل شد و موجودی انبار کسر گردید."
                onCompleted()
            } else {
                _alertMessage.value = "خطا در تبدیل پیش‌فاکتور."
            }
        }
    }

    fun voidInvoice(invoiceWithItems: InvoiceWithItems, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val success = invoiceRepo.voidInvoice(invoiceWithItems.invoice.id)
            if (success) {
                _alertMessage.value = "فاکتور شماره ${invoiceWithItems.invoice.invoiceNumber} با موفقیت باطل شد و موجودی انبار به حالت قبل بازگشت."
                onCompleted()
            } else {
                _alertMessage.value = "فاکتور قبلاً باطل شده است یا یافت نشد."
            }
        }
    }

    fun deleteInvoice(invoiceId: Long, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val success = invoiceRepo.deleteInvoice(invoiceId)
            if (success) {
                _alertMessage.value = "فاکتور با موفقیت حذف گردید."
                onCompleted()
            }
        }
    }

    fun printThermalReceiptBluetooth(
        device: BluetoothDevice,
        invoiceWithItems: InvoiceWithItems,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val inv = invoiceWithItems.invoice
            val printable = EscPosPrinterHelper.PrintableInvoice(
                invoiceNumber = inv.invoiceNumber,
                invoiceDate = inv.jalaliDate,
                customerName = inv.partyName,
                customerPhone = inv.partyPhone,
                warehouseName = inv.warehouseName,
                items = invoiceWithItems.items.map {
                    EscPosPrinterHelper.PrintableItem(
                        name = it.productName,
                        quantity = it.quantity,
                        unit = it.unitName,
                        unitPrice = it.unitPrice,
                        totalPrice = it.totalPrice
                    )
                },
                subtotalAmount = inv.subtotalAmount,
                discountAmount = inv.discountAmount,
                taxAmount = inv.taxAmount,
                totalAmount = inv.totalAmount,
                paymentMethod = inv.paymentType,
                note = inv.notes
            )
            val bytes = EscPosPrinterHelper.buildEscPosBytes(printable)
            val result = BluetoothThermalPrinter.printBytes(device, bytes)
            if (result.isSuccess) {
                onResult(true, "فاکتور با موفقیت به پرینتر حرارتی ارسال و چاپ شد.")
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطای اتصال به پرینتر بلوتوثی"
                onResult(false, "عدم موفقیت در چاپ بلوتوث: $err")
            }
        }
    }

    fun checkoutAdvanced(
        paymentType: String,
        cashAmount: Long = 0L,
        posAmount: Long = 0L,
        chequeAmount: Long = 0L,
        creditAmount: Long = 0L,
        cashDeskId: Long? = null,
        bankAccountId: Long? = null,
        chequeNumber: String = "",
        sayadNumber: String = "",
        chequeDueDate: String = "",
        chequeBankName: String = "",
        notes: String = "",
        overrideInvoiceType: String? = null,
        onCompleted: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) {
                _alertMessage.value = "سبد اقلام خالی است."
                return@launch
            }

            val subtotal = items.sumOf { it.totalPrice }
            val discount = _discountAmount.value
            val taxable = if (subtotal > discount) subtotal - discount else 0L
            val tax = if (_applyTax.value) (taxable * 0.10).toLong() else 0L
            val directExpense = _directExpenseAmount.value
            val total = taxable + tax + directExpense

            val party = _selectedParty.value
            val invoiceType = overrideInvoiceType ?: _currentInvoiceType.value

            val effectiveCredit = when (paymentType) {
                "CREDIT" -> total
                "MIXED" -> creditAmount
                else -> 0L
            }

            // کنترل سقف اعتبار برای فاکتورهای فروش نسیه
            if (invoiceType in listOf("SALE", "POS_SALE") && effectiveCredit > 0 && !_issueAsProforma.value) {
                if (party == null) {
                    _alertMessage.value = "برای خرید نسیه، مشخص کردن طرف‌حساب الزامی است."
                    return@launch
                }
                if (!party.canAcceptCredit(effectiveCredit)) {
                    val remaining = party.remainingCredit()
                    _alertMessage.value = "هشدار سقف اعتبار! بدهی مشتری از سقف مجاز فراتر می‌رود. مانده اعتبار مجاز: $remaining تومان"
                    return@launch
                }
            }

            val effectiveCash = when (paymentType) {
                "CASH" -> total
                "MIXED" -> cashAmount
                else -> 0L
            }
            val effectivePos = when (paymentType) {
                "POS" -> total
                "MIXED" -> posAmount
                else -> 0L
            }
            val effectiveCheque = when (paymentType) {
                "CHEQUE" -> total
                "MIXED" -> chequeAmount
                else -> 0L
            }

            val effectiveCashDesk = cashDeskId ?: _selectedCashDeskId.value
            val effectiveBank = bankAccountId ?: _selectedBankAccountId.value

            val nextNumber = invoiceRepo.generateNextInvoiceNumber()
            val today = JalaliDate.now()

            val defaultPartyName = when (invoiceType) {
                "PURCHASE" -> "تامین‌کننده ناشناس"
                "RETURN_PURCHASE" -> "تامین‌کننده مرجوعی"
                else -> "مشتری نقدی"
            }

            val invoice = InvoiceEntity(
                invoiceNumber = nextNumber,
                invoiceType = invoiceType,
                jalaliDate = today.toStandardString(),
                timestamp = System.currentTimeMillis(),
                partyId = party?.id,
                partyName = party?.name ?: defaultPartyName,
                partyPhone = party?.mobile ?: "",
                warehouseId = 1L,
                warehouseName = "انبار مرکزی تهران",
                subtotalAmount = subtotal,
                discountAmount = discount,
                taxAmount = tax,
                totalAmount = total,
                paymentType = paymentType,
                cashAmount = effectiveCash,
                posAmount = effectivePos,
                creditAmount = effectiveCredit,
                chequeAmount = effectiveCheque,
                cashDeskId = effectiveCashDesk,
                bankAccountId = effectiveBank,
                pricingMode = _pricingMode.value,
                directExpenseAmount = directExpense,
                directExpenseType = _directExpenseType.value,
                isPaid = effectiveCredit == 0L,
                status = if (_issueAsProforma.value && invoiceType == "SALE") "PROFORMA" else "FINAL",
                notes = notes
            )

            val invoiceItems = items.map { cartItem ->
                InvoiceItemEntity(
                    invoiceId = 0L,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    barcode = cartItem.product.barcode,
                    unitName = cartItem.unitName,
                    isPrimaryUnit = cartItem.isPrimaryUnit,
                    quantity = cartItem.quantity,
                    conversionRate = cartItem.product.conversionRate,
                    quantityInSecondary = cartItem.quantityInSecondary,
                    unitPrice = cartItem.unitPrice,
                    discount = cartItem.discount,
                    totalPrice = cartItem.totalPrice
                )
            }

            val invoiceId = invoiceRepo.createInvoice(invoice, invoiceItems)

            // در صورت ثبت چک در فاکتور
            if (!_issueAsProforma.value && effectiveCheque > 0 && chequeNumber.isNotBlank()) {
                val dueEpoch = try {
                    JalaliDate.parse(chequeDueDate).toEpochMillis()
                } catch (e: Exception) {
                    today.addDays(30).toEpochMillis()
                }
                val chequeType = if (invoiceType in listOf("PURCHASE", "RETURN_SALE")) "PAYABLE" else "RECEIVABLE"
                treasuryRepo.insertCheque(
                    ChequeEntity(
                        chequeNumber = chequeNumber,
                        sayadNumber = sayadNumber,
                        type = chequeType,
                        partyId = party?.id,
                        partyName = party?.name ?: "طرف‌حساب فاکتور $nextNumber",
                        bankName = chequeBankName.ifBlank { "بانک ملی" },
                        branch = "مرکزی",
                        amount = effectiveCheque,
                        issueDateJalali = today.toStandardString(),
                        dueDateJalali = chequeDueDate.ifBlank { today.addDays(30).toStandardString() },
                        dueDateEpochMillis = dueEpoch,
                        status = "NORMAL",
                        notes = "ثبت چک بابت فاکتور $nextNumber ($invoiceType)"
                    )
                )
            }

            val printable = EscPosPrinterHelper.PrintableInvoice(
                invoiceNumber = invoice.invoiceNumber,
                invoiceDate = today.format(includeTime = true),
                customerName = invoice.partyName,
                customerPhone = invoice.partyPhone,
                warehouseName = invoice.warehouseName,
                items = invoiceItems.map {
                    EscPosPrinterHelper.PrintableItem(
                        name = it.productName,
                        quantity = it.quantity,
                        unit = it.unitName,
                        unitPrice = it.unitPrice,
                        totalPrice = it.totalPrice
                    )
                },
                subtotalAmount = invoice.subtotalAmount,
                discountAmount = invoice.discountAmount,
                taxAmount = invoice.taxAmount,
                totalAmount = invoice.totalAmount,
                paymentMethod = invoice.paymentType,
                note = notes
            )

            _previewReceiptText.value = EscPosPrinterHelper.buildPlainTextReceipt(printable)
            clearCart()
            onCompleted(invoiceId)
        }
    }

    fun showReceiptForInvoice(inv: InvoiceWithItems) {
        val invoice = inv.invoice
        val printable = EscPosPrinterHelper.PrintableInvoice(
            invoiceNumber = invoice.invoiceNumber,
            invoiceDate = invoice.jalaliDate,
            customerName = invoice.partyName,
            customerPhone = invoice.partyPhone,
            warehouseName = invoice.warehouseName,
            items = inv.items.map {
                EscPosPrinterHelper.PrintableItem(
                    name = it.productName,
                    quantity = it.quantity,
                    unit = it.unitName,
                    unitPrice = it.unitPrice,
                    totalPrice = it.totalPrice
                )
            },
            subtotalAmount = invoice.subtotalAmount,
            discountAmount = invoice.discountAmount,
            taxAmount = invoice.taxAmount,
            totalAmount = invoice.totalAmount,
            paymentMethod = invoice.paymentType,
            note = invoice.notes
        )
        _previewReceiptText.value = EscPosPrinterHelper.buildPlainTextReceipt(printable)
    }

    fun exportPdf(context: Context, invoiceWithItems: InvoiceWithItems, storeProfile: StoreProfile) {
        val file = ExportHelper.exportInvoicePdf(context, invoiceWithItems, storeProfile)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "فاکتور ${invoiceWithItems.invoice.invoiceNumber} PDF")
        }
    }

    fun exportCsv(context: Context, invoiceWithItems: InvoiceWithItems, storeProfile: StoreProfile) {
        val file = ExportHelper.exportInvoiceCsv(context, invoiceWithItems, storeProfile)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل فاکتور ${invoiceWithItems.invoice.invoiceNumber}")
        }
    }

    fun shareText(context: Context, invoiceWithItems: InvoiceWithItems, storeProfile: StoreProfile) {
        val text = buildInvoiceShareText(invoiceWithItems, storeProfile)
        ExportHelper.shareText(context, text, "ارسال فاکتور ${invoiceWithItems.invoice.invoiceNumber}")
    }

    private fun buildInvoiceShareText(inv: InvoiceWithItems, store: StoreProfile): String {
        val i = inv.invoice
        val sb = StringBuilder()
        sb.appendLine("🧾 ${store.name}")
        sb.appendLine("فاکتور شماره: ${i.invoiceNumber}")
        sb.appendLine("نوع: ${i.invoiceType}")
        sb.appendLine("تاریخ: ${i.jalaliDate}")
        sb.appendLine("طرف‌حساب: ${i.partyName}")
        sb.appendLine("-------------------------")
        inv.items.forEachIndexed { idx, it ->
            sb.appendLine("${idx + 1}. ${it.productName} - ${it.quantity} ${it.unitName} × ${NumberFormatUtils.formatPrice(it.unitPrice)} = ${NumberFormatUtils.formatPrice(it.totalPrice)}")
        }
        sb.appendLine("-------------------------")
        sb.appendLine("جمع اقلام: ${NumberFormatUtils.formatPrice(i.subtotalAmount)}")
        if (i.discountAmount > 0) sb.appendLine("تخفیف: ${NumberFormatUtils.formatPrice(i.discountAmount)}")
        if (i.taxAmount > 0) sb.appendLine("مالیات: ${NumberFormatUtils.formatPrice(i.taxAmount)}")
        if (i.directExpenseAmount > 0) sb.appendLine("${i.directExpenseType}: ${NumberFormatUtils.formatPrice(i.directExpenseAmount)}")
        sb.appendLine("مبلغ نهایی: ${NumberFormatUtils.formatPrice(i.totalAmount)}")
        sb.appendLine("تسویه: ${i.paymentType}")
        if (store.phone.isNotBlank()) sb.appendLine("تلفن: ${store.phone}")
        return sb.toString()
    }
}

