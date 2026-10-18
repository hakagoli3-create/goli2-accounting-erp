package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import com.example.ui.components.QuickAddPartyDialog
import com.example.ui.components.QuickAddOrEditProductDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.InvoiceWithItems
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.StoreProfile
import com.example.ui.components.ErpTopAppBar
import com.example.ui.components.InvoiceSettlementDialog
import com.example.ui.components.OfficialInvoiceDetailsDialog
import com.example.ui.components.OfficialInvoicePrintPreviewDialog
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.NavyBrand
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PersianGold
import com.example.ui.theme.StatusInfoBlue
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ExportHelper
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.clip
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: InvoiceViewModel
) {
    val context = LocalContext.current
    val securityManager = remember { AppSecurityAndSettingsManager.getInstance(context) }
    val storeProfile by securityManager.storeProfile.collectAsStateWithLifecycle()
    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedParty by viewModel.selectedParty.collectAsStateWithLifecycle()
    val discount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val applyTax by viewModel.applyTax.collectAsStateWithLifecycle()
    val pricingMode by viewModel.pricingMode.collectAsStateWithLifecycle()
    val currentInvoiceType by viewModel.currentInvoiceType.collectAsStateWithLifecycle()
    val editingInvoiceId by viewModel.editingInvoiceId.collectAsStateWithLifecycle()
    val editingInvoiceNumber by viewModel.editingInvoiceNumber.collectAsStateWithLifecycle()
    val directExpenseAmount by viewModel.directExpenseAmount.collectAsStateWithLifecycle()
    val directExpenseType by viewModel.directExpenseType.collectAsStateWithLifecycle()
    val cashDesks by viewModel.allCashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.allBankAccounts.collectAsStateWithLifecycle()

    val previewReceipt by viewModel.previewReceiptText.collectAsStateWithLifecycle()
    val selectedInvoiceForDetails by viewModel.selectedInvoiceForDetails.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    val tabs = listOf(
        "فاکتورهای ثبت شده",
        if (editingInvoiceId != null) "ویرایش فاکتور ($editingInvoiceNumber)" else "صدور فاکتور جدید"
    )

    var showSettlementDialog by remember { mutableStateOf(false) }
    var showOfficialPrintPreview by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var showPrintChoiceInvoice by remember { mutableStateOf<InvoiceWithItems?>(null) }

    LaunchedEffect(alertMessage) {
        alertMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAlertMessage()
        }
    }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val taxable = if (subtotal > discount) subtotal - discount else 0L
    val tax = if (applyTax) (taxable * 0.10).toLong() else 0L
    val grandTotal = taxable + tax + directExpenseAmount

    Scaffold(
        modifier = Modifier.testTag("invoice_screen"),
        containerColor = CleanBackground,
        topBar = {
            Column {
                ErpTopAppBar(
                    title = "فاکتورهای خرید، فروش و مرجوعی",
                    subtitle = "تسویه ترکیبی (نقدی/پوز/چک/نسیه)، هزینه‌های مستقیم و فیلتر پیشرفته"
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CleanSurface,
                    contentColor = NavyPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MatteGold,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) NavyPrimary else TextDarkSecondary
                                )
                            },
                            modifier = Modifier.testTag("invoice_tab_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> InvoiceListContent(
                    invoices = invoices,
                    onOpenDetails = { showOfficialPrintPreview = it },
                    onPrintInvoice = { showPrintChoiceInvoice = it },
                    onEditInvoice = { inv ->
                        viewModel.loadInvoiceForEditing(inv)
                        selectedTab = 1
                    },
                    onVoidInvoice = { inv ->
                        try {
                            viewModel.voidInvoice(inv.invoice.id) {}
                        } catch (e: Exception) {
                            Toast.makeText(context, "امکان ابطال این سند به دلیل داشتن تراکنش‌های وابسته وجود ندارد. ابتدا تراکنش‌های مرتبط را مدیریت کنید.", Toast.LENGTH_LONG).show()
                        }
                    },
                    onDeleteInvoice = { inv ->
                        try {
                            viewModel.deleteInvoice(inv.invoice.id) {}
                        } catch (e: Exception) {
                            Toast.makeText(context, "امکان حذف این سند به دلیل داشتن تراکنش‌های وابسته وجود ندارد. ابتدا تراکنش‌های مرتبط را مدیریت کنید.", Toast.LENGTH_LONG).show()
                        }
                    },
                    onExportPdf = { viewModel.exportPdf(context, it, storeProfile) },
                    onExportCsv = { viewModel.exportCsv(context, it, storeProfile) },
                    onExportAllPdf = {
                        val headers = listOf("شماره فاکتور", "نوع", "تاریخ", "طرف حساب", "تسویه", "مبلغ کل")
                        val rows = invoices.map {
                            listOf(it.invoice.invoiceNumber, it.invoice.invoiceType, it.invoice.jalaliDate, it.invoice.partyName, it.invoice.paymentType, NumberFormatUtils.formatPrice(it.invoice.totalAmount))
                        }
                        val f = ExportHelper.exportReportPdf(context, "لیست کلیه فاکتورها", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "application/pdf", "گزارش فاکتورها PDF")
                    },
                    onExportAllCsv = {
                        val headers = listOf("شماره فاکتور", "نوع", "تاریخ", "طرف حساب", "تلفن", "تسویه", "مبلغ کل")
                        val rows = invoices.map {
                            listOf(it.invoice.invoiceNumber, it.invoice.invoiceType, it.invoice.jalaliDate, it.invoice.partyName, it.invoice.partyPhone, it.invoice.paymentType, it.invoice.totalAmount.toString())
                        }
                        val f = ExportHelper.exportReportCsv(context, "فهرست کلیه فاکتورها", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "text/csv", "خروجی اکسل فاکتورها")
                    }
                )
                1 -> InvoiceEditorContent(
                    products = products,
                    customers = customers,
                    suppliers = suppliers,
                    cartItems = cartItems,
                    selectedParty = selectedParty,
                    discount = discount,
                    applyTax = applyTax,
                    pricingMode = pricingMode,
                    currentInvoiceType = currentInvoiceType,
                    directExpenseAmount = directExpenseAmount,
                    directExpenseType = directExpenseType,
                    editingInvoiceNumber = editingInvoiceNumber,
                    grandTotal = grandTotal,
                    onInvoiceTypeChange = { viewModel.setCurrentInvoiceType(it) },
                    onPricingModeChange = { viewModel.setPricingMode(it) },
                    onSelectParty = { viewModel.selectParty(it) },
                    onAddToCart = { p, isPrimary -> viewModel.addToCart(p, isPrimary = isPrimary) },
                    onToggleUnit = { idx -> viewModel.toggleCartItemUnit(idx) },
                    onUpdateQuantity = { idx, q -> viewModel.updateCartItemQuantity(idx, q) },
                    onDiscountChange = { viewModel.setDiscount(it) },
                    onToggleTax = { viewModel.toggleTax(it) },
                    onDirectExpenseChange = { amt, type -> viewModel.setDirectExpense(amt, type) },
                    onCancelEditing = {
                        viewModel.cancelEditingInvoice()
                        selectedTab = 0
                    },
                    onOpenSettlement = {
                        if (cartItems.isEmpty()) {
                            Toast.makeText(context, "سبد فاکتور خالی است.", Toast.LENGTH_SHORT).show()
                        } else {
                            showSettlementDialog = true
                        }
                    },
                    onQuickAddParty = { newParty ->
                        viewModel.quickAddParty(newParty) { created ->
                            Toast.makeText(context, "طرف‌حساب «${created.name}» با موفقیت افزوده و انتخاب شد.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onQuickAddOrUpdateProduct = { prod ->
                        viewModel.quickAddOrUpdateProduct(prod) { saved ->
                            val isNew = prod.id == 0L
                            val msg = if (isNew) "کالای «${saved.name}» با موفقیت ثبت و انتخاب شد." else "کالای «${saved.name}» بروزرسانی شد."
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    if (showSettlementDialog) {
        InvoiceSettlementDialog(
            totalAmount = grandTotal,
            cashDesks = cashDesks,
            bankAccounts = bankAccounts,
            selectedParty = selectedParty,
            onDismiss = { showSettlementDialog = false },
            onConfirmSettlement = { paymentType, cash, pos, cheque, credit, deskId, bankId, chNum, sayad, due, bankName, notes ->
                viewModel.checkoutAdvanced(
                    paymentType = paymentType,
                    cashAmount = cash,
                    posAmount = pos,
                    chequeAmount = cheque,
                    creditAmount = credit,
                    cashDeskId = deskId,
                    bankAccountId = bankId,
                    chequeNumber = chNum,
                    sayadNumber = sayad,
                    chequeDueDate = due,
                    chequeBankName = bankName,
                    notes = notes
                ) {
                    showSettlementDialog = false
                    val msg = if (editingInvoiceId != null) "فاکتور با موفقیت ویرایش و ذخیره شد." else "فاکتور با موفقیت ثبت قطعی گردید."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    selectedTab = 0
                }
            }
        )
    }

    selectedInvoiceForDetails?.let { invoiceWithItems ->
        OfficialInvoiceDetailsDialog(
            invoiceWithItems = invoiceWithItems,
            storeProfile = storeProfile,
            onDismiss = { viewModel.closeInvoiceDetails() },
            onExportPdf = { viewModel.exportPdf(context, invoiceWithItems, storeProfile) },
            onExportCsv = { viewModel.exportCsv(context, invoiceWithItems, storeProfile) },
            onShareText = { viewModel.shareText(context, invoiceWithItems, storeProfile) },
            onPrintReceipt = { viewModel.showReceiptForInvoice(invoiceWithItems) },
            onOpenOfficialPrintPreview = { showOfficialPrintPreview = invoiceWithItems }
        )
    }

    // Modal: پیش‌نمایش و چاپ فاکتور رسمی A4 / A5
    showOfficialPrintPreview?.let { invWithItems ->
        OfficialInvoicePrintPreviewDialog(
            invoiceWithItems = invWithItems,
            storeProfile = storeProfile,
            onDismiss = { showOfficialPrintPreview = null },
            onSwitchToThermalReceipt = {
                showOfficialPrintPreview = null
                viewModel.showReceiptForInvoice(invWithItems)
            }
        )
    }

    // Modal: انتخاب قالب چاپ بین رسمی (A4/A5) و حرارتی (ESC/POS)
    showPrintChoiceInvoice?.let { invWithItems ->
        AlertDialog(
            onDismissRequest = { showPrintChoiceInvoice = null },
            title = {
                Text(
                    text = "انتخاب قالب چاپ فاکتور شماره ${invWithItems.invoice.invoiceNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "مایل به چاپ یا پیش‌نمایش فاکتور در کدام قالب هستید؟",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val inv = invWithItems
                            showPrintChoiceInvoice = null
                            showOfficialPrintPreview = inv
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("قالب رسمی A4 / A5 (سربرگ، جدول، به حروف و امضا)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val inv = invWithItems
                            showPrintChoiceInvoice = null
                            viewModel.showReceiptForInvoice(inv)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("قالب فیش حرارتی (فیش‌پرینتر POS)", fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPrintChoiceInvoice = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    previewReceipt?.let { text ->
        ReceiptPreviewDialog(
            receiptText = text,
            onDismiss = { viewModel.closeReceiptPreview() }
        )
    }
}

@Composable
private fun InvoiceListContent(
    invoices: List<InvoiceWithItems>,
    onOpenDetails: (InvoiceWithItems) -> Unit,
    onPrintInvoice: (InvoiceWithItems) -> Unit,
    onEditInvoice: (InvoiceWithItems) -> Unit,
    onVoidInvoice: (InvoiceWithItems) -> Unit,
    onDeleteInvoice: (InvoiceWithItems) -> Unit,
    onExportPdf: (InvoiceWithItems) -> Unit,
    onExportCsv: (InvoiceWithItems) -> Unit,
    onExportAllPdf: () -> Unit,
    onExportAllCsv: () -> Unit
) {
    val context = LocalContext.current
    var filterType by remember { mutableStateOf("ALL") }
    var filterDate by remember { mutableStateOf("ALL") }
    var filterPayment by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var voidingInvoice by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var deletingInvoice by remember { mutableStateOf<InvoiceWithItems?>(null) }

    val today = remember { JalaliDate.now() }
    val todayStr = remember { today.toStandardString() }
    val thisMonthPrefix = remember { "${today.year}/${today.month.toString().padStart(2, '0')}" }
    val sevenDaysAgoEpoch = remember { today.toEpochMillis() - (7 * 86400000L) }

    val filteredInvoices = remember(invoices, filterType, filterDate, filterPayment, searchQuery) {
        val q = searchQuery.trim().lowercase()

        invoices.filter { invWithItems ->
            val inv = invWithItems.invoice

            // ۱. فیلتر نوع فاکتور
            val matchesType = when (filterType) {
                "SALE" -> inv.invoiceType in listOf("SALE", "POS_SALE")
                "PURCHASE" -> inv.invoiceType == "PURCHASE"
                "RETURN_SALE" -> inv.invoiceType == "RETURN_SALE"
                "RETURN_PURCHASE" -> inv.invoiceType == "RETURN_PURCHASE"
                else -> true
            }

            // ۲. فیلتر تاریخ شمسی
            val matchesDate = when (filterDate) {
                "TODAY" -> inv.jalaliDate.contains(todayStr) || inv.jalaliDate.replace('/', '-') == todayStr.replace('/', '-')
                "WEEK" -> try {
                    JalaliDate.parse(inv.jalaliDate).toEpochMillis() >= sevenDaysAgoEpoch
                } catch (e: Exception) { true }
                "MONTH" -> inv.jalaliDate.startsWith(thisMonthPrefix) || inv.jalaliDate.contains("/${today.month.toString().padStart(2, '0')}/")
                else -> true
            }

            // ۳. فیلتر وضعیت پرداخت / تسویه
            val matchesPayment = when (filterPayment) {
                "CASH" -> inv.paymentType.equals("CASH", ignoreCase = true)
                "POS" -> inv.paymentType.equals("POS", ignoreCase = true)
                "CREDIT" -> inv.paymentType.equals("CREDIT", ignoreCase = true)
                "CHEQUE" -> inv.paymentType.equals("CHEQUE", ignoreCase = true)
                "MIXED" -> inv.paymentType.equals("MIXED", ignoreCase = true)
                else -> true
            }

            // ۴. جست‌وجوی متنی
            val matchesSearch = if (q.isBlank()) true else {
                inv.invoiceNumber.lowercase().contains(q) ||
                        NumberFormatUtils.formatInvoiceNumber(inv.invoiceNumber).lowercase().contains(q) ||
                        inv.partyName.lowercase().contains(q) ||
                        inv.partyPhone.lowercase().contains(q) ||
                        NumberFormatUtils.toPersianDigits(inv.partyPhone).contains(q) ||
                        inv.jalaliDate.contains(q) ||
                        NumberFormatUtils.toPersianDigits(inv.jalaliDate).contains(q) ||
                        NumberFormatUtils.formatPaymentType(inv.paymentType).lowercase().contains(q) ||
                        invWithItems.items.any { it.productName.lowercase().contains(q) }
            }

            matchesType && matchesDate && matchesPayment && matchesSearch
        }
    }

    val totalFilteredSum = remember(filteredInvoices) {
        filteredInvoices.filter { it.invoice.status != "VOID" }.sumOf { it.invoice.totalAmount }
    }
    val todayInvoicesCount = remember(invoices, todayStr) {
        invoices.count { it.invoice.jalaliDate.contains(todayStr) || it.invoice.jalaliDate.replace('/', '-') == todayStr.replace('/', '-') }
    }

    if (invoices.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = TextDarkMuted,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "هیچ فاکتوری تا کنون ثبت نشده است.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextDarkSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "از تب «صدور فاکتور جدید» اولین فاکتور فروش یا خرید را ثبت کنید.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDarkMuted)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // ۱. کارت شاخص‌های آماری و گزارشات خروجی (KPI Summary Card)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, MatteGold.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "مجموع مبلغ فاکتورهای نمایش داده شده",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = NumberFormatUtils.formatPrice(totalFilteredSum),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = MatteGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp
                                )
                            )
                        }

                        // دکمه‌های گزارشات جمعی PDF و اکسل
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MatteGold,
                                modifier = Modifier.clickable { onExportAllPdf() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "PDF کلی",
                                        tint = NavyDark,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "PDF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NavyDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { onExportAllCsv() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = "اکسل کلی",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "اکسل",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فاکتورهای فیلترشده: ${NumberFormatUtils.toPersianDigits(filteredInvoices.size.toString())} از ${NumberFormatUtils.toPersianDigits(invoices.size.toString())} سند",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "امروز: ${NumberFormatUtils.toPersianDigits(todayInvoicesCount.toString())} فاکتور",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MatteGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // ۲. باکس جست‌وجوی هوشمند فاکتورها
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "جست‌وجوی شماره فاکتور، طرف‌حساب، کالا، تلفن یا تاریخ...",
                            fontSize = 12.sp,
                            color = TextDarkMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "پاک کردن",
                                    tint = TextDarkSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatteGold,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = CleanSurface,
                        unfocusedContainerColor = CleanSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_search_input")
                )
            }
        }

        // ۳. فیلتر تاریخ شمسی (Shamsi Date Filter)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MatteGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "فیلتر تاریخ شمسی:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dateOptions = listOf(
                        "ALL" to "همه زمان‌ها",
                        "TODAY" to "امروز",
                        "WEEK" to "۷ روز اخیر",
                        "MONTH" to "این ماه"
                    )
                    items(dateOptions) { (key, label) ->
                        val isSelected = filterDate == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NavyPrimary else CleanSurface,
                            border = BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { filterDate = key }
                                .testTag("invoice_date_filter_$key")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) Color.White else TextDarkPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // ۴. فیلتر نوع فاکتور (Type Filter)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "نوع فاکتور:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val typeOptions = listOf(
                        "ALL" to "همه انواع",
                        "SALE" to "فروش",
                        "PURCHASE" to "خرید",
                        "RETURN_SALE" to "مرجوعی فروش",
                        "RETURN_PURCHASE" to "مرجوعی خرید"
                    )
                    items(typeOptions) { (key, label) ->
                        val isSelected = filterType == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MatteGoldContainer else CleanSurface,
                            border = BorderStroke(1.dp, if (isSelected) MatteGold else CleanBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { filterType = key }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) NavyPrimary else TextDarkPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // ۵. فیلتر روش تسویه و وضعیت پرداخت (Payment Status Filter)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = StatusInfoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "وضعیت تسویه و پرداخت:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val paymentOptions = listOf(
                        "ALL" to "همه روش‌ها",
                        "CASH" to "نقدی",
                        "POS" to "کارتخوان POS",
                        "CREDIT" to "نسیه",
                        "CHEQUE" to "چک صیادی",
                        "MIXED" to "ترکیبی"
                    )
                    items(paymentOptions) { (key, label) ->
                        val isSelected = filterPayment == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFE8EDF5) else CleanSurface,
                            border = BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { filterPayment = key }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) NavyPrimary else TextDarkPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // پیام در صورت خالی بودن نتایج فیلتر
        if (filteredInvoices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanSurface),
                    border = BorderStroke(1.dp, CleanBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextDarkMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "هیچ فاکتوری با شرایط انتخابی یافت نشد.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextDarkSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "فیلترها را تغییر دهید یا عبارت جست‌وجو را پاک کنید.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkMuted)
                        )
                    }
                }
            }
        }

        // ۶. فهرست کارت‌های شکیل فاکتورها (Invoice Cards)
        items(filteredInvoices) { inv ->
            val invoice = inv.invoice
            val isVoid = invoice.status == "VOID"

            val typeTitle = when (invoice.invoiceType) {
                "PURCHASE" -> "فاکتور خرید"
                "RETURN_SALE" -> "برگشت از فروش"
                "RETURN_PURCHASE" -> "برگشت از خرید"
                else -> "فاکتور فروش"
            }
            val typeColor = when (invoice.invoiceType) {
                "PURCHASE" -> MatteGold
                "RETURN_SALE" -> AccountingCreditRed
                "RETURN_PURCHASE" -> Color(0xFF00897B)
                else -> NavyPrimary
            }
            val typeContainerColor = when (invoice.invoiceType) {
                "PURCHASE" -> MatteGoldContainer
                "RETURN_SALE" -> Color(0xFFFFECEC)
                "RETURN_PURCHASE" -> Color(0xFFE0F2F1)
                else -> Color(0xFFE9EDF5)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenDetails(inv) }
                    .testTag("invoice_card_${invoice.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVoid) Color(0xFFF7F7F8) else CleanSurface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isVoid) CleanBorder else CleanBorder
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // ردیف اول: شماره فاکتور، نشانگر نوع، حالت عمده/تکی و وضعیت قطعی/ابطال
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = NumberFormatUtils.formatInvoiceNumber(invoice.invoiceNumber),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isVoid) TextDarkMuted else TextDarkPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            )

                            // بج نوع فاکتور
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = typeContainerColor
                            ) {
                                Text(
                                    text = typeTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = typeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // بج نرخ‌گذاری عمده یا تکی
                            val modeBadge = if (invoice.pricingMode == "WHOLESALE") "عمده" else "تکی"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFECEFF4)
                            ) {
                                Text(
                                    text = modeBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextDarkSecondary,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // وضعیت فاکتور (قطعی / پیش‌فاکتور / باطل شده)
                        if (isVoid) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccountingCreditRed.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "باطل شده",
                                    color = AccountingCreditRed,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (invoice.status == "PROFORMA") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusInfoBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "پیش‌فاکتور",
                                    color = StatusInfoBlue,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccountingDebitGreen.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "قطعی",
                                    color = AccountingDebitGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ردیف دوم: اطلاعات طرف‌حساب، تاریخ و روش تسویه
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "طرف حساب: ${invoice.partyName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextDarkPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = TextDarkMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = NumberFormatUtils.toPersianDigits(invoice.jalaliDate),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextDarkSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ردیف سوم: روش تسویه و تعداد اقلام
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CleanBackground,
                            border = BorderStroke(1.dp, CleanBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = MatteGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "تسویه: ${NumberFormatUtils.formatPaymentType(invoice.paymentType)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextDarkSecondary,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = "${NumberFormatUtils.toPersianDigits(inv.items.size.toString())} قلم کالا",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextDarkMuted,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // هزینه مستقیم (کرایه حمل)
                    if (invoice.directExpenseAmount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val expLabel = if (invoice.directExpenseType.isNotBlank()) invoice.directExpenseType else "کرایه حمل"
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MatteGoldContainer
                        ) {
                            Text(
                                text = "$expLabel: ${NumberFormatUtils.formatPrice(invoice.directExpenseAmount)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CleanBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    // ردیف مبالغ و دکمه‌های عملیاتی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "مبلغ کل فاکتور:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextDarkMuted,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = NumberFormatUtils.formatPrice(invoice.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isVoid) TextDarkMuted else NavyPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        // ابزارهای فاکتور: جزئیات، ویرایش، ابطال، چاپ، PDF و اکسل
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // دکمه مشاهده جزئیات
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NavyPrimary,
                                modifier = Modifier.clickable { onOpenDetails(inv) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "جزئیات فاکتور",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "جزئیات",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }

                            // دکمه ویرایش (در صورت عدم ابطال)
                            if (!isVoid) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CleanBackground,
                                    border = BorderStroke(1.dp, CleanBorder),
                                    modifier = Modifier.clickable { onEditInvoice(inv) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "ویرایش",
                                        tint = NavyPrimary,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(16.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CleanBackground,
                                    border = BorderStroke(1.dp, CleanBorder),
                                    modifier = Modifier.clickable { voidingInvoice = inv }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "ابطال فاکتور",
                                        tint = AlertWarningOrange,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(16.dp)
                                    )
                                }
                            }

                            // دکمه حذف فاکتور
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CleanBackground,
                                border = BorderStroke(1.dp, CleanBorder),
                                modifier = Modifier.clickable { deletingInvoice = inv }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف فاکتور",
                                    tint = AccountingCreditRed,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(16.dp)
                                )
                            }

                            // دکمه چاپ و خروجی‌ها
                            IconButton(
                                onClick = { onPrintInvoice(inv) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "چاپ فاکتور",
                                    tint = MatteGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onExportPdf(inv) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "خروجی PDF",
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onExportCsv(inv) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = "خروجی اکسل",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // مودال تأیید ابطال فاکتور
    voidingInvoice?.let { inv ->
        AlertDialog(
            onDismissRequest = { voidingInvoice = null },
            title = {
                Text(
                    text = "ابطال فاکتور شماره ${NumberFormatUtils.formatInvoiceNumber(inv.invoice.invoiceNumber)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "آیا از ابطال این فاکتور اطمینان دارید؟ با ابطال فاکتور، کلیه اثرات انبارداری، سند دوبل حسابداری و مانده خزانه‌داری به طور کامل معکوس می‌گردد.",
                    fontSize = 12.sp,
                    color = TextDarkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onVoidInvoice(inv)
                        voidingInvoice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("ابطال قطعی فاکتور", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { voidingInvoice = null }) {
                    Text("انصراف", fontSize = 12.sp)
                }
            }
        )
    }

    // مودال تأیید حذف کامل فاکتور
    deletingInvoice?.let { inv ->
        AlertDialog(
            onDismissRequest = { deletingInvoice = null },
            title = {
                Text(
                    text = "حذف کامل فاکتور ${NumberFormatUtils.formatInvoiceNumber(inv.invoice.invoiceNumber)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "آیا از حذف کامل این فاکتور اطمینان دارید؟ در صورت وجود تراکنش‌های وابسته، ابتدا آنها را تسویه کنید.",
                    fontSize = 12.sp,
                    color = TextDarkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            onDeleteInvoice(inv)
                        } catch (e: Exception) {
                            Toast.makeText(context, "امکان حذف این سند به دلیل داشتن تراکنش‌های وابسته وجود ندارد.", Toast.LENGTH_LONG).show()
                        }
                        deletingInvoice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("حذف فاکتور", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingInvoice = null }) {
                    Text("انصراف", fontSize = 12.sp)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceEditorContent(
    products: List<ProductEntity>,
    customers: List<PartyEntity>,
    suppliers: List<PartyEntity>,
    cartItems: List<CartItem>,
    selectedParty: PartyEntity?,
    discount: Long,
    applyTax: Boolean,
    pricingMode: String,
    currentInvoiceType: String,
    directExpenseAmount: Long,
    directExpenseType: String,
    editingInvoiceNumber: String?,
    grandTotal: Long,
    onInvoiceTypeChange: (String) -> Unit,
    onPricingModeChange: (String) -> Unit,
    onSelectParty: (PartyEntity?) -> Unit,
    onAddToCart: (ProductEntity, Boolean) -> Unit,
    onToggleUnit: (Int) -> Unit,
    onUpdateQuantity: (Int, Double) -> Unit,
    onDiscountChange: (Long) -> Unit,
    onToggleTax: (Boolean) -> Unit,
    onDirectExpenseChange: (Long, String) -> Unit,
    onCancelEditing: () -> Unit,
    onOpenSettlement: () -> Unit,
    onQuickAddParty: (PartyEntity) -> Unit,
    onQuickAddOrUpdateProduct: (ProductEntity) -> Unit
) {
    var partyMenuExpanded by remember { mutableStateOf(false) }
    var productMenuExpanded by remember { mutableStateOf(false) }
    var selectedProductToAdd by remember { mutableStateOf<ProductEntity?>(null) }
    var showQuickAddPartyDialog by remember { mutableStateOf(false) }
    var showQuickAddProductDialog by remember { mutableStateOf(false) }
    var usePrimaryUnit by remember { mutableStateOf(true) } // کارتن vs عدد
    var discountText by remember { mutableStateOf(if (discount > 0) discount.toString() else "") }
    var directExpenseAmtText by remember { mutableStateOf(if (directExpenseAmount > 0) directExpenseAmount.toString() else "") }
    var directExpenseDescText by remember { mutableStateOf(directExpenseType) }

    val isPurchase = currentInvoiceType in listOf("PURCHASE", "RETURN_PURCHASE")
    val partyList = if (isPurchase) suppliers else customers
    val partyLabel = if (isPurchase) "تأمین‌کننده / فروشنده" else "مشتری / خریدار"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // بنر وضعیت ویرایش فاکتور موجود
        if (editingInvoiceNumber != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("در حال ویرایش فاکتور: $editingInvoiceNumber", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text("اقلام یا مبالغ را تغییر داده و در پایان ذخیره نمایید.", fontSize = 11.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = onCancelEditing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("لغو ویرایش", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // نوع فاکتور (فروش، خرید، مرجوعی فروش، مرجوعی خرید)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "نوع فاکتور و سند تجاری:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = currentInvoiceType == "SALE",
                            onClick = { onInvoiceTypeChange("SALE") },
                            label = { Text("فاکتور فروش", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = currentInvoiceType == "PURCHASE",
                            onClick = { onInvoiceTypeChange("PURCHASE") },
                            label = { Text("فاکتور خرید", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        FilterChip(
                            selected = currentInvoiceType == "RETURN_SALE",
                            onClick = { onInvoiceTypeChange("RETURN_SALE") },
                            label = { Text("مرجوعی فروش", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = currentInvoiceType == "RETURN_PURCHASE",
                            onClick = { onInvoiceTypeChange("RETURN_PURCHASE") },
                            label = { Text("مرجوعی خرید", fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        // نظام قیمت‌گذاری (عمده vs تکی)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "نظام قیمت‌گذاری فاکتور:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPurchase) "نرخ خرید پایه کالا از تأمین‌کننده" else if (pricingMode == "WHOLESALE") "نرخ همکار و عمده‌فروشی" else "نرخ تکی و مصرف‌کننده",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = pricingMode == "RETAIL",
                            onClick = { onPricingModeChange("RETAIL") },
                            label = { Text("فروش تکی") },
                            modifier = Modifier.testTag("pricing_mode_retail")
                        )
                        FilterChip(
                            selected = pricingMode == "WHOLESALE",
                            onClick = { onPricingModeChange("WHOLESALE") },
                            label = { Text("فروش عمده", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("pricing_mode_wholesale")
                        )
                    }
                }
            }
        }

        // انتخاب طرف حساب (مشتری یا تأمین‌کننده)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "طرف حساب: $partyLabel",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(
                            onClick = { showQuickAddPartyDialog = true },
                            modifier = Modifier.testTag("quick_add_party_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ افزودن طرف حساب جدید", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    val availableParties = remember(partyList, selectedParty) {
                        if (selectedParty != null && partyList.none { it.id == selectedParty.id }) {
                            listOf(selectedParty) + partyList
                        } else {
                            partyList
                        }
                    }

                    val partyDisplayText = selectedParty?.let {
                        if (it.code.isNotBlank()) "${it.name} (${it.code})" else it.name
                    } ?: "انتخاب $partyLabel..."

                    ExposedDropdownMenuBox(
                        expanded = partyMenuExpanded,
                        onExpandedChange = { partyMenuExpanded = !partyMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = partyDisplayText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(partyLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_party_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = partyMenuExpanded,
                            onDismissRequest = { partyMenuExpanded = false }
                        ) {
                            availableParties.forEach { party ->
                                DropdownMenuItem(
                                    text = { Text("${party.name} - اعتبار: ${NumberFormatUtils.formatPrice(party.creditLimit)} (مانده: ${NumberFormatUtils.formatPrice(party.currentBalance)})") },
                                    onClick = {
                                        onSelectParty(party)
                                        partyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    selectedParty?.let { party ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val isOverLimit = !isPurchase && (party.isCreditLimitExceeded() || (party.creditLimit > 0 && (party.currentBalance + grandTotal) > party.creditLimit))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOverLimit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "سقف اعتبار ریالی: ${NumberFormatUtils.formatPrice(party.creditLimit)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "مانده حساب: ${NumberFormatUtils.formatPrice(party.currentBalance)}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                if (isOverLimit) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "هشدار: این سفارش از سقف مجاز اعتبار مشتری فراتر می‌رود!",
                                            color = AccountingCreditRed,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ۳. تعریف هزینه مستقیم حین صدور فاکتور (کرایه حمل، باربری، بسته‌بندی و...)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "هزینه مستقیم فاکتور (کرایه حمل، باربری، بسته‌بندی، نصب، بیمه):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = directExpenseAmtText,
                            onValueChange = {
                                directExpenseAmtText = it
                                onDirectExpenseChange(it.toLongOrNull() ?: 0L, directExpenseDescText)
                            },
                            label = { Text("مبلغ هزینه (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = directExpenseDescText,
                            onValueChange = {
                                directExpenseDescText = it
                                onDirectExpenseChange(directExpenseAmtText.toLongOrNull() ?: 0L, it)
                            },
                            label = { Text("شرح هزینه مستقیم") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }
        }

        // Add Product to Cart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "افزودن قلم به فاکتور",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(
                            onClick = { showQuickAddProductDialog = true },
                            modifier = Modifier.testTag("quick_add_product_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedProductToAdd != null) "ویرایش سریع کالا" else "+ افزودن سریع کالا",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = productMenuExpanded,
                        onExpandedChange = { productMenuExpanded = !productMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProductToAdd?.name ?: "انتخاب کالا از انبار...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("کالا") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_product_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = productMenuExpanded,
                            onDismissRequest = { productMenuExpanded = false }
                        ) {
                            products.forEach { prod ->
                                val priceToShow = if (pricingMode == "WHOLESALE") prod.wholesalePrice else prod.retailPrice
                                DropdownMenuItem(
                                    text = { Text("${prod.name} | موجودی: ${prod.formatStockDisplay()} | فی: ${NumberFormatUtils.formatPrice(priceToShow)}") },
                                    onClick = {
                                        selectedProductToAdd = prod
                                        productMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    selectedProductToAdd?.let { prod ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "واحد سنجش: ${if (usePrimaryUnit) prod.primaryUnit else prod.secondaryUnit}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(prod.secondaryUnit, fontSize = 12.sp)
                                Switch(
                                    checked = usePrimaryUnit,
                                    onCheckedChange = { usePrimaryUnit = it },
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                Text(prod.primaryUnit, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val unitPrice = if (pricingMode == "WHOLESALE") {
                            if (usePrimaryUnit) (prod.wholesalePrice * prod.conversionRate).toLong() else prod.wholesalePrice
                        } else {
                            if (usePrimaryUnit) (prod.retailPrice * prod.conversionRate).toLong() else prod.retailPrice
                        }

                        Text(
                            text = "قیمت اعمال شده هر ${if (usePrimaryUnit) prod.primaryUnit else prod.secondaryUnit}: ${NumberFormatUtils.formatPrice(unitPrice)}",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onAddToCart(prod, usePrimaryUnit)
                                selectedProductToAdd = null
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_item_to_cart_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("افزودن این کالا به پیش‌فاکتور")
                        }
                    }
                }
            }
        }

        // Cart items list
        if (cartItems.isNotEmpty()) {
            item {
                Text(
                    text = "اقلام پیش‌فاکتور (${cartItems.size} ردیف):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(cartItems.indices.toList()) { index ->
                val item = cartItems[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "واحد: ${item.unitName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(تغییر به ${if (item.isPrimaryUnit) item.product.secondaryUnit else item.product.primaryUnit})",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onToggleUnit(index) }
                                )
                            }
                            Text(
                                text = "فی: ${NumberFormatUtils.formatPrice(item.unitPrice)} | کل: ${NumberFormatUtils.formatPrice(item.totalPrice)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onUpdateQuantity(index, item.quantity - 1.0) }) {
                                Icon(Icons.Default.Delete, contentDescription = "کاهش", tint = Color.Gray)
                            }
                            Text(
                                text = NumberFormatUtils.toPersianDigits(item.quantity.toString()),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { onUpdateQuantity(index, item.quantity + 1.0) }) {
                                Icon(Icons.Default.Add, contentDescription = "افزایش", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Summary & Checkout Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = {
                                discountText = it
                                onDiscountChange(it.toLongOrNull() ?: 0L)
                            },
                            label = { Text("تخفیف کل (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("محاسبه مالیات بر ارزش افزوده (۱۰٪):")
                            Switch(checked = applyTax, onCheckedChange = onToggleTax)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مبلغ کل قابل پرداخت:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = NumberFormatUtils.formatPrice(grandTotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenSettlement,
                            modifier = Modifier.fillMaxWidth().testTag("open_settlement_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("انتخاب نوع تسویه و ثبت نهایی فاکتور")
                        }
                    }
                }
            }
        }
    }

    if (showQuickAddPartyDialog) {
        QuickAddPartyDialog(
            initialType = if (isPurchase) "SUPPLIER" else "CUSTOMER",
            onDismiss = { showQuickAddPartyDialog = false },
            onConfirm = { newParty ->
                showQuickAddPartyDialog = false
                onQuickAddParty(newParty)
            }
        )
    }

    if (showQuickAddProductDialog) {
        QuickAddOrEditProductDialog(
            existingProduct = selectedProductToAdd,
            onDismiss = { showQuickAddProductDialog = false },
            onConfirm = { product ->
                showQuickAddProductDialog = false
                onQuickAddOrUpdateProduct(product)
                selectedProductToAdd = product
            }
        )
    }
}
