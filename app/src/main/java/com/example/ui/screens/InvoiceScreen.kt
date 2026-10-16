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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CartItemQuantityDialog
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ExportHelper
import com.example.util.NumberFormatUtils

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
    val tabs = listOf("فاکتورهای ثبت شده", "صدور فاکتور رسمی / عمده")

    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedParty by viewModel.selectedParty.collectAsStateWithLifecycle()
    val discount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val applyTax by viewModel.applyTax.collectAsStateWithLifecycle()
    val pricingMode by viewModel.pricingMode.collectAsStateWithLifecycle()
    val cashDesks by viewModel.allCashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.allBankAccounts.collectAsStateWithLifecycle()

    val previewReceipt by viewModel.previewReceiptText.collectAsStateWithLifecycle()
    val selectedInvoiceForDetails by viewModel.selectedInvoiceForDetails.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    var showSettlementDialog by remember { mutableStateOf(false) }

    LaunchedEffect(alertMessage) {
        alertMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAlertMessage()
        }
    }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val taxable = if (subtotal > discount) subtotal - discount else 0L
    val tax = if (applyTax) (taxable * 0.10).toLong() else 0L
    val grandTotal = taxable + tax

    Scaffold(
        topBar = {
            Column {
                ErpTopAppBar(
                    title = "فاکتورهای رسمی، عمده و خرده‌فروشی",
                    subtitle = "تسویه نقدی/پوز/چک/نسیه، دانلود PDF، خروجی اکسل و کنترل سقف اعتبار"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
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
                    onOpenDetails = { viewModel.openInvoiceDetails(it) },
                    onPrintInvoice = { viewModel.showReceiptForInvoice(it) },
                    onExportPdf = { viewModel.exportPdf(context, it, storeProfile) },
                    onExportCsv = { viewModel.exportCsv(context, it, storeProfile) },
                    onVoidInvoice = { inv ->
                        viewModel.voidInvoice(inv) {}
                    },
                    onConvertProforma = { inv ->
                        viewModel.convertProformaToFinal(inv) {}
                    },
                    onExportAllPdf = {
                        val headers = listOf("شماره فاکتور", "تاریخ", "نام طرف حساب", "نوع تسویه", "مبلغ کل (تومان)")
                        val rows = invoices.map {
                            listOf(it.invoice.invoiceNumber, it.invoice.jalaliDate, it.invoice.partyName, it.invoice.paymentType, NumberFormatUtils.formatPrice(it.invoice.totalAmount))
                        }
                        val f = ExportHelper.exportReportPdf(context, "لیست کلیه فاکتورهای صادره", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "application/pdf", "گزارش فاکتورها PDF")
                    },
                    onExportAllCsv = {
                        val headers = listOf("شماره فاکتور", "تاریخ", "طرف حساب", "تلفن", "تسویه", "مبلغ کل")
                        val rows = invoices.map {
                            listOf(it.invoice.invoiceNumber, it.invoice.jalaliDate, it.invoice.partyName, it.invoice.partyPhone, it.invoice.paymentType, it.invoice.totalAmount.toString())
                        }
                        val f = ExportHelper.exportReportCsv(context, "فهرست کلیه فاکتورها", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "text/csv", "خروجی اکسل فاکتورها")
                    }
                )
                1 -> InvoiceEditorContent(
                    products = products,
                    customers = customers,
                    cartItems = cartItems,
                    selectedParty = selectedParty,
                    discount = discount,
                    applyTax = applyTax,
                    pricingMode = pricingMode,
                    grandTotal = grandTotal,
                    directExpense = viewModel.directExpenseAmount.collectAsStateWithLifecycle().value,
                    directExpenseType = viewModel.directExpenseType.collectAsStateWithLifecycle().value,
                    issueAsProforma = viewModel.issueAsProforma.collectAsStateWithLifecycle().value,
                    onDirectExpenseChange = { amt, typ -> viewModel.setDirectExpense(amt, typ) },
                    onToggleProforma = { viewModel.setIssueAsProforma(it) },
                    onPricingModeChange = { viewModel.setPricingMode(it) },
                    onSelectParty = { viewModel.selectParty(it) },
                    onAddToCart = { p, isPrimary -> viewModel.addToCart(p, isPrimary = isPrimary) },
                    onToggleUnit = { idx -> viewModel.toggleCartItemUnit(idx) },
                    onUpdateQuantity = { idx, q -> viewModel.updateCartItemQuantity(idx, q) },
                    onSetCartItemQuantities = { idx, c, u -> viewModel.setCartItemQuantities(idx, c, u) },
                    onDiscountChange = { viewModel.setDiscount(it) },
                    onToggleTax = { viewModel.toggleTax(it) },
                    onScanBarcode = { viewModel.scanBarcode(it) },
                    onOpenSettlement = {
                        if (cartItems.isEmpty()) {
                            Toast.makeText(context, "سبد فاکتور خالی است.", Toast.LENGTH_SHORT).show()
                        } else {
                            showSettlementDialog = true
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
                    Toast.makeText(context, "فاکتور با موفقیت ثبت گردید.", Toast.LENGTH_SHORT).show()
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
            onVoidInvoice = {
                viewModel.voidInvoice(invoiceWithItems) {
                    viewModel.closeInvoiceDetails()
                }
            },
            onConvertProforma = {
                viewModel.convertProformaToFinal(invoiceWithItems) {
                    viewModel.closeInvoiceDetails()
                }
            },
            grossProfit = if (currentRole.canViewProfit) viewModel.calculateGrossProfit(invoiceWithItems) else null
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
    onExportPdf: (InvoiceWithItems) -> Unit,
    onExportCsv: (InvoiceWithItems) -> Unit,
    onVoidInvoice: (InvoiceWithItems) -> Unit,
    onConvertProforma: (InvoiceWithItems) -> Unit,
    onExportAllPdf: () -> Unit,
    onExportAllCsv: () -> Unit
) {
    if (invoices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("هیچ فاکتوری تا کنون ثبت نشده است.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لیست فاکتورها (${invoices.size} فقره)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onExportAllPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF کل", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onExportAllCsv,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اکسل کل", fontSize = 11.sp)
                    }
                }
            }
        }

        items(invoices) { inv ->
            val invoice = inv.invoice
            val isProforma = invoice.status == "PROFORMA"
            val isVoid = invoice.status == "VOID"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetails(inv) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isVoid -> Color(0xFFFFEBEE)
                        isProforma -> Color(0xFFFFFDE7)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "فاکتور ${invoice.invoiceNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${invoice.jalaliDate})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            // Status badge
                            val statusTitle = when {
                                isVoid -> "ابطال شده"
                                isProforma -> "پیش‌فاکتور"
                                else -> if (invoice.pricingMode == "WHOLESALE") "عمده" else "تکی"
                            }
                            val statusBg = when {
                                isVoid -> AccountingCreditRed.copy(alpha = 0.2f)
                                isProforma -> PersianGold.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                            val statusFg = when {
                                isVoid -> AccountingCreditRed
                                isProforma -> Color(0xFF8D6E63)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = statusBg)
                            ) {
                                Text(
                                    text = statusTitle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusFg,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onExportPdf(inv) }) {
                                Icon(Icons.Default.Description, contentDescription = "PDF", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onExportCsv(inv) }) {
                                Icon(Icons.Default.TableChart, contentDescription = "Excel", tint = PersianGold)
                            }
                            IconButton(onClick = { onPrintInvoice(inv) }) {
                                Icon(Icons.Default.Print, contentDescription = "چاپ حرارتی", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "طرف حساب: ${invoice.partyName} | تسویه: ${invoice.paymentType}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اقلام: ${NumberFormatUtils.toPersianDigits(inv.items.size.toString())} ردیف",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = NumberFormatUtils.formatPrice(invoice.totalAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (isProforma || !isVoid) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isProforma) {
                                androidx.compose.material3.Button(
                                    onClick = { onConvertProforma(inv) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen)
                                ) {
                                    Text("تبدیل به فاکتور نهایی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (!isVoid) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = { onVoidInvoice(inv) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = AccountingCreditRed)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ابطال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceEditorContent(
    products: List<ProductEntity>,
    customers: List<PartyEntity>,
    cartItems: List<CartItem>,
    selectedParty: PartyEntity?,
    discount: Long,
    applyTax: Boolean,
    pricingMode: String,
    grandTotal: Long,
    directExpense: Long,
    directExpenseType: String,
    issueAsProforma: Boolean,
    onDirectExpenseChange: (Long, String) -> Unit,
    onToggleProforma: (Boolean) -> Unit,
    onPricingModeChange: (String) -> Unit,
    onSelectParty: (PartyEntity?) -> Unit,
    onAddToCart: (ProductEntity, Boolean) -> Unit,
    onToggleUnit: (Int) -> Unit,
    onUpdateQuantity: (Int, Double) -> Unit,
    onSetCartItemQuantities: (Int, Double, Double) -> Unit,
    onDiscountChange: (Long) -> Unit,
    onToggleTax: (Boolean) -> Unit,
    onScanBarcode: (String) -> Unit,
    onOpenSettlement: () -> Unit
) {
    var customerMenuExpanded by remember { mutableStateOf(false) }
    var productMenuExpanded by remember { mutableStateOf(false) }
    var selectedProductToAdd by remember { mutableStateOf<ProductEntity?>(null) }
    var usePrimaryUnit by remember { mutableStateOf(true) } // کارتن vs عدد
    var discountText by remember { mutableStateOf(if (discount > 0) discount.toString() else "") }
    var directExpenseText by remember { mutableStateOf(if (directExpense > 0) directExpense.toString() else "") }
    var expenseTypeInput by remember { mutableStateOf(directExpenseType.ifBlank { "کرایه حمل و باربری" }) }

    var productSearchQuery by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }
    var editingCartIndex by remember { mutableStateOf<Int?>(null) }

    if (showScanner) {
        BarcodeScannerDialog(
            title = "اسکن بارکد کالا برای فاکتور",
            onDismiss = { showScanner = false },
            onBarcodeScanned = { barcode ->
                onScanBarcode(barcode)
                showScanner = false
            }
        )
    }

    editingCartIndex?.let { idx ->
        if (idx in cartItems.indices) {
            CartItemQuantityDialog(
                cartItem = cartItems[idx],
                onDismiss = { editingCartIndex = null },
                onConfirmQuantity = { cartons, units ->
                    onSetCartItemQuantities(idx, cartons, units)
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Pricing Mode Switcher (عمده vs تکی)
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
                            text = if (pricingMode == "WHOLESALE") "نرخ همکار و عمده‌فروشی اعمال می‌شود" else "نرخ تکی و مصرف‌کننده اعمال می‌شود",
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

        // Customer Selection & Credit Limit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "طرف حساب و شرایط اعتباری",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = customerMenuExpanded,
                        onExpandedChange = { customerMenuExpanded = !customerMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedParty?.let { "${it.name} (${it.code})" } ?: "انتخاب مشتری...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مشتری") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_customer_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = customerMenuExpanded,
                            onDismissRequest = { customerMenuExpanded = false }
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text("${customer.name} - سقف اعتبار: ${NumberFormatUtils.formatPrice(customer.creditLimit)}") },
                                    onClick = {
                                        onSelectParty(customer)
                                        customerMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    selectedParty?.let { party ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val isOverLimit = party.isCreditLimitExceeded() || (party.creditLimit > 0 && (party.currentBalance + grandTotal) > party.creditLimit)

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
                                        text = "مانده بدهی فعلی: ${NumberFormatUtils.formatPrice(party.currentBalance)}",
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
                        Button(
                            onClick = { showScanner = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اسکن بارکد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instant search field with first letter matching
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = {
                            productSearchQuery = it
                            productMenuExpanded = true
                        },
                        placeholder = { Text("جستجوی سریع با تایپ حرف اول، نام یا کد...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invoice_search_product_fast"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredProducts = products.filter {
                        productSearchQuery.isBlank() ||
                                it.name.contains(productSearchQuery, ignoreCase = true) ||
                                it.code.contains(productSearchQuery, ignoreCase = true) ||
                                it.barcode.contains(productSearchQuery, ignoreCase = true)
                    }

                    ExposedDropdownMenuBox(
                        expanded = productMenuExpanded,
                        onExpandedChange = { productMenuExpanded = !productMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProductToAdd?.name ?: "انتخاب کالا از انبار (${filteredProducts.size} کالا)...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("کالای انتخابی") },
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
                            filteredProducts.forEach { prod ->
                                val priceToShow = if (pricingMode == "WHOLESALE") prod.wholesalePrice else prod.retailPrice
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(
                                                text = "موجودی: ${prod.formatStockDisplay()} | فی واحد: ${NumberFormatUtils.formatPrice(priceToShow)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
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
                            Text("افزودن این کالا به فاکتور")
                        }
                    }
                }
            }
        }

        // Cart items list
        if (cartItems.isNotEmpty()) {
            item {
                Text(
                    text = "اقلام فاکتور (${cartItems.size} ردیف) - برای تغییر تعداد کارتن/تکی روی مقدار کلیک کنید:",
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
                                    text = "(تغییر واحد)",
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

                            // Clickable quantity badge opening CartItemQuantityDialog
                            Card(
                                onClick = { editingCartIndex = index },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${NumberFormatUtils.toPersianDigits(item.quantity.toString())} ${item.unitName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            IconButton(onClick = { onUpdateQuantity(index, item.quantity + 1.0) }) {
                                Icon(Icons.Default.Add, contentDescription = "افزایش", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Direct Expenses & Proforma Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "هزینه مستقیم فاکتور (کرایه حمل، بسته‌بندی، انبارداری):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = directExpenseText,
                                onValueChange = {
                                    directExpenseText = it
                                    onDirectExpenseChange(it.toLongOrNull() ?: 0L, expenseTypeInput)
                                },
                                label = { Text("مبلغ هزینه (تومان)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = expenseTypeInput,
                                onValueChange = {
                                    expenseTypeInput = it
                                    onDirectExpenseChange(directExpenseText.toLongOrNull() ?: 0L, it)
                                },
                                label = { Text("شرح هزینه") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("صدور به صورت پیش‌فاکتور (Proforma):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("پیش‌فاکتور از موجودی انبار کسر نمی‌کند و بعداً قابل تأیید است", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = issueAsProforma, onCheckedChange = onToggleProforma)
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
                            Text(if (issueAsProforma) "ثبت پیش‌فاکتور" else "انتخاب نوع تسویه و ثبت نهایی فاکتور")
                        }
                    }
                }
            }
        }
    }
}
