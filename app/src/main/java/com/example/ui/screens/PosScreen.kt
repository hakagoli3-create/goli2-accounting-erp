package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.components.InvoiceSettlementDialog
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
import com.example.ui.theme.StatusInfoBlue
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: InvoiceViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val selectedParty by viewModel.selectedParty.collectAsStateWithLifecycle()
    val discount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val applyTax by viewModel.applyTax.collectAsStateWithLifecycle()
    val pricingMode by viewModel.pricingMode.collectAsStateWithLifecycle()
    val cashDesks by viewModel.allCashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.allBankAccounts.collectAsStateWithLifecycle()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()
    val invoiceCurrencyRate by viewModel.invoiceCurrencyRate.collectAsStateWithLifecycle()

    val previewReceipt by viewModel.previewReceiptText.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    // Local UI State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: کاتالوگ محصولات, 1: سبد خرید
    var customerMenuExpanded by remember { mutableStateOf(false) }
    var showSettlementDialog by remember { mutableStateOf(false) }
    var customDiscountText by remember { mutableStateOf("") }

    LaunchedEffect(alertMessage) {
        alertMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAlertMessage()
        }
    }

    LaunchedEffect(discount) {
        if (discount > 0L) {
            customDiscountText = discount.toString()
        } else if (customDiscountText.isNotBlank() && customDiscountText != "0") {
            customDiscountText = ""
        }
    }

    // Calculations
    val subtotal = cartItems.sumOf { it.totalPrice }
    val taxable = if (subtotal > discount) subtotal - discount else 0L
    val tax = if (applyTax) (taxable * 0.10).toLong() else 0L
    val grandTotal = taxable + tax
    val totalCartItemsCount = cartItems.sumOf { it.quantity }.toInt()

    // Unique Categories from products
    val categories = remember(products) {
        val list = products.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        listOf("ALL") + list
    }

    // Filtered Products
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()
        products.filter { p ->
            val matchesCategory = selectedCategory == "ALL" || p.category.trim() == selectedCategory
            val matchesQuery = query.isBlank() ||
                    p.name.lowercase().contains(query) ||
                    p.code.lowercase().contains(query) ||
                    p.barcode.lowercase().contains(query)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_screen"),
        containerColor = CleanBackground,
        topBar = {
            ErpTopAppBar(
                title = "صندوق فروشگاهی لمسی POS",
                subtitle = "فروش سریع، اسکن بارکد، تسویه چندگانه و صدور فاکتور",
                actions = {
                    // سوئیچ قیمت عمده / خرده‌فروشی
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (pricingMode == "RETAIL") MatteGoldContainer else CleanBackground,
                            border = BorderStroke(
                                1.dp,
                                if (pricingMode == "RETAIL") MatteGold else CleanBorder
                            ),
                            modifier = Modifier.clickable { viewModel.setPricingMode("RETAIL") }
                        ) {
                            Text(
                                text = "تکی",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (pricingMode == "RETAIL") NavyPrimary else TextDarkSecondary,
                                    fontWeight = if (pricingMode == "RETAIL") FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (pricingMode == "WHOLESALE") MatteGoldContainer else CleanBackground,
                            border = BorderStroke(
                                1.dp,
                                if (pricingMode == "WHOLESALE") MatteGold else CleanBorder
                            ),
                            modifier = Modifier.clickable { viewModel.setPricingMode("WHOLESALE") }
                        ) {
                            Text(
                                text = "عمده",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (pricingMode == "WHOLESALE") NavyPrimary else TextDarkSecondary,
                                    fontWeight = if (pricingMode == "WHOLESALE") FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrLandscape = maxWidth >= 760.dp

            if (isTabletOrLandscape) {
                // =========================================================================
                // چیدمان افقی و عریض (Tablet / Wide Landscape Screen)
                // سمت راست: کاتالوگ و دسته‌بندی اقلام (۶۰٪) | سمت چپ: سبد خرید و تسویه (۴۰٪)
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ستون سمت راست: اقلام و جست‌وجو
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                    ) {
                        PosSearchAndCategoryHeader(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onBarcodeSubmitted = { code ->
                                viewModel.scanBarcode(code)
                                searchQuery = ""
                                focusManager.clearFocus()
                            },
                            categories = categories,
                            products = products,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        PosProductGrid(
                            products = filteredProducts,
                            cartItems = cartItems,
                            pricingMode = pricingMode,
                            onAddToCart = { product ->
                                viewModel.addToCart(product, isPrimary = false)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ستون سمت چپ: سبد خرید و خلاصه تسویه
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanSurface),
                        border = BorderStroke(1.dp, CleanBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        PosCartCheckoutPanel(
                            cartItems = cartItems,
                            customers = customers,
                            selectedParty = selectedParty,
                            onSelectParty = { viewModel.selectParty(it) },
                            customerMenuExpanded = customerMenuExpanded,
                            onToggleCustomerMenu = { customerMenuExpanded = it },
                            onUpdateQuantity = { idx, qty -> viewModel.updateCartItemQuantity(idx, qty) },
                            onToggleUnit = { idx -> viewModel.toggleCartItemUnit(idx) },
                            onClearCart = { viewModel.clearCart() },
                            discount = discount,
                            customDiscountText = customDiscountText,
                            onCustomDiscountChange = { text ->
                                customDiscountText = text
                                val parsed = NumberFormatUtils.toEnglishDigits(text).filter { it.isDigit() }.toLongOrNull() ?: 0L
                                viewModel.setDiscount(parsed)
                            },
                            onQuickDiscountSelected = { percent ->
                                val calculated = (subtotal * percent / 100)
                                viewModel.setDiscount(calculated)
                            },
                            applyTax = applyTax,
                            onToggleTax = { viewModel.toggleTax(it) },
                            subtotal = subtotal,
                            grandTotal = grandTotal,
                            onFastCash = {
                                if (cartItems.isEmpty()) {
                                    Toast.makeText(context, "سبد خرید خالی است.", Toast.LENGTH_SHORT).show()
                                    return@PosCartCheckoutPanel
                                }
                                viewModel.checkoutAdvanced(
                                    paymentType = "CASH",
                                    cashAmount = grandTotal
                                ) {
                                    Toast.makeText(context, "تسویه نقدی با موفقیت انجام و ثبت شد.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onFastPos = {
                                if (cartItems.isEmpty()) {
                                    Toast.makeText(context, "سبد خرید خالی است.", Toast.LENGTH_SHORT).show()
                                    return@PosCartCheckoutPanel
                                }
                                viewModel.checkoutAdvanced(
                                    paymentType = "POS",
                                    posAmount = grandTotal
                                ) {
                                    Toast.makeText(context, "تسویه کارتخوان با موفقیت انجام و ثبت شد.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onAdvancedSettlement = {
                                if (cartItems.isEmpty()) {
                                    Toast.makeText(context, "سبد خرید خالی است.", Toast.LENGTH_SHORT).show()
                                    return@PosCartCheckoutPanel
                                }
                                showSettlementDialog = true
                            }
                        )
                    }
                }
            } else {
                // =========================================================================
                // چیدمان استاندارد گوشی عمودی (Handheld Phone Adaptive Layout)
                // تب‌های مدرن: کاتالوگ اقلام ↔ سبد خرید + نوار شناور پایین
                // =========================================================================
                Column(modifier = Modifier.fillMaxSize()) {
                    // تب‌های انتخاب نمای صندوق
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
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PointOfSale,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (selectedTab == 0) NavyPrimary else TextDarkSecondary
                                    )
                                    Text(
                                        text = "کاتالوگ اقلام (${NumberFormatUtils.toPersianDigits(filteredProducts.size.toString())})",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 0) NavyPrimary else TextDarkSecondary
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (selectedTab == 1) NavyPrimary else TextDarkSecondary
                                    )
                                    Text(
                                        text = "سبد فروش (${NumberFormatUtils.toPersianDigits(cartItems.size.toString())})",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 1) NavyPrimary else TextDarkSecondary
                                    )
                                    if (cartItems.isNotEmpty()) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MatteGold
                                        ) {
                                            Text(
                                                text = NumberFormatUtils.toPersianDigits(totalCartItemsCount.toString()),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // بدنه بر اساس تب انتخابی
                    Box(modifier = Modifier.weight(1f)) {
                        if (selectedTab == 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                PosSearchAndCategoryHeader(
                                    searchQuery = searchQuery,
                                    onSearchChange = { searchQuery = it },
                                    onBarcodeSubmitted = { code ->
                                        viewModel.scanBarcode(code)
                                        searchQuery = ""
                                        focusManager.clearFocus()
                                    },
                                    categories = categories,
                                    products = products,
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { selectedCategory = it }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                PosProductGrid(
                                    products = filteredProducts,
                                    cartItems = cartItems,
                                    pricingMode = pricingMode,
                                    onAddToCart = { product ->
                                        viewModel.addToCart(product, isPrimary = false)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                                border = BorderStroke(1.dp, CleanBorder)
                            ) {
                                PosCartCheckoutPanel(
                                    cartItems = cartItems,
                                    customers = customers,
                                    selectedParty = selectedParty,
                                    onSelectParty = { viewModel.selectParty(it) },
                                    customerMenuExpanded = customerMenuExpanded,
                                    onToggleCustomerMenu = { customerMenuExpanded = it },
                                    onUpdateQuantity = { idx, qty -> viewModel.updateCartItemQuantity(idx, qty) },
                                    onToggleUnit = { idx -> viewModel.toggleCartItemUnit(idx) },
                                    onClearCart = { viewModel.clearCart() },
                                    discount = discount,
                                    customDiscountText = customDiscountText,
                                    onCustomDiscountChange = { text ->
                                        customDiscountText = text
                                        val parsed = NumberFormatUtils.toEnglishDigits(text).filter { it.isDigit() }.toLongOrNull() ?: 0L
                                        viewModel.setDiscount(parsed)
                                    },
                                    onQuickDiscountSelected = { percent ->
                                        val calculated = (subtotal * percent / 100)
                                        viewModel.setDiscount(calculated)
                                    },
                                    applyTax = applyTax,
                                    onToggleTax = { viewModel.toggleTax(it) },
                                    subtotal = subtotal,
                                    grandTotal = grandTotal,
                                    onFastCash = {
                                        if (cartItems.isEmpty()) {
                                            Toast.makeText(context, "سبد خرید خالی است.", Toast.LENGTH_SHORT).show()
                                            return@PosCartCheckoutPanel
                                        }
                                        viewModel.checkoutAdvanced(
                                            paymentType = "CASH",
                                            cashAmount = grandTotal
                                        ) {
                                            Toast.makeText(context, "تسویه نقدی با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onFastPos = {
                                        if (cartItems.isEmpty()) {
                                            Toast.makeText(context, "سبد خرید خالی است.", Toast.LENGTH_SHORT).show()
                                            return@PosCartCheckoutPanel
                                        }
                                        viewModel.checkoutAdvanced(
                                            paymentType = "POS",
                                            posAmount = grandTotal
                                        ) {
                                            Toast.makeText(context, "تسویه کارتخوان با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onAdvancedSettlement = {
                                        if (cartItems.isEmpty()) {
                                            Toast.makeText(context, "سبد خرید خالی است.", Toast.LENGTH_SHORT).show()
                                            return@PosCartCheckoutPanel
                                        }
                                        showSettlementDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // نوار چسبان پایین فقط در نمای کاتالوگ زمانی که سبد حاوی کالا است
                    if (selectedTab == 0 && cartItems.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pos_bottom_cart_summary_bar"),
                            color = NavyPrimary,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, MatteGold.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MatteGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = NumberFormatUtils.toPersianDigits(cartItems.size.toString()),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = NavyDark,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "مبلغ قابل پرداخت:",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White.copy(alpha = 0.75f),
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = NumberFormatUtils.formatPrice(grandTotal),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = MatteGold,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }

                                Button(
                                    onClick = { selectedTab = 1 },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MatteGold,
                                        contentColor = NavyDark
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مشاهده سبد و تسویه",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // دیالوگ تسویه پیشرفته (نقدی، پوز، چک، نسیه)
    if (showSettlementDialog) {
        InvoiceSettlementDialog(
            totalAmount = grandTotal,
            cashDesks = cashDesks,
            bankAccounts = bankAccounts,
            selectedParty = selectedParty,
            currencyCode = selectedCurrencyCode,
            currencyRateToBase = invoiceCurrencyRate,
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
                    Toast.makeText(context, "فاکتور فروش با موفقیت تسویه و ثبت گردید.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // پیش‌نمایش چاپ فیش حرارتی
    previewReceipt?.let { text ->
        ReceiptPreviewDialog(
            receiptText = text,
            onDismiss = { viewModel.closeReceiptPreview() }
        )
    }
}

/**
 * هدر جست‌وجوی کالا و بارکد و ردیف دسته‌بندی‌ها
 */
@Composable
private fun PosSearchAndCategoryHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBarcodeSubmitted: (String) -> Unit,
    categories: List<String>,
    products: List<ProductEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // نوار جست‌وجوی کالا و بارکد
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CleanSurface),
            border = BorderStroke(1.dp, CleanBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "جست‌وجوی نام کالا، کد یا اسکن بارکد...",
                            fontSize = 12.sp,
                            color = TextDarkMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جست‌وجو",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "پاک کردن",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchQuery.isNotBlank()) {
                            onBarcodeSubmitted(searchQuery)
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatteGold,
                        unfocusedBorderColor = CleanBorder,
                        focusedContainerColor = CleanSurface,
                        unfocusedContainerColor = CleanSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // دکمه اسکن و ثبت بارکد
                Button(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            onBarcodeSubmitted(searchQuery)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("pos_scan_barcode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "ثبت بارکد",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ثبت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // دسته‌بندی محصولات (چیپ‌های افقی)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                val count = if (cat == "ALL") products.size else products.count { it.category.trim() == cat }
                val label = if (cat == "ALL") "همه اقلام" else cat

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NavyPrimary else CleanSurface,
                    border = BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onCategorySelected(cat) }
                        .testTag("category_chip_$cat")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSelected) Color.White else TextDarkPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MatteGold else Color(0xFFE8ECF2)
                        ) {
                            Text(
                                text = NumberFormatUtils.toPersianDigits(count.toString()),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) NavyDark else TextDarkSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * گرید اقلام محصولات با کارت‌های لمسی و نمایش وضعیت موجودی
 */
@Composable
private fun PosProductGrid(
    products: List<ProductEntity>,
    cartItems: List<CartItem>,
    pricingMode: String,
    onAddToCart: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = TextDarkMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "کالایی با این مشخصات یافت نشد.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDarkSecondary)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 145.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(products) { product ->
                val displayPrice = if (pricingMode == "WHOLESALE") product.wholesalePrice else product.retailPrice
                val inCartItem = cartItems.find { it.product.id == product.id }
                val isLowStock = product.currentStockInSecondary <= product.minStockThreshold

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onAddToCart(product) }
                        .testTag("pos_product_item_${product.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanSurface),
                    border = BorderStroke(
                        1.dp,
                        if (inCartItem != null) MatteGold else CleanBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        // نشانگر وضعیت دسته‌بندی و تعداد موجود در سبد
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFECEFF4)
                            ) {
                                Text(
                                    text = product.category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextDarkSecondary,
                                        fontSize = 9.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (inCartItem != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MatteGold
                                ) {
                                    Text(
                                        text = "${NumberFormatUtils.formatQuantity(inCartItem.quantity)} در سبد",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NavyDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextDarkPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // قیمت کالا
                        Text(
                            text = NumberFormatUtils.formatPrice(displayPrice),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = NavyPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // وضعیت موجودی انبار
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isLowStock) AccountingCreditRed else AccountingDebitGreen)
                            )
                            Text(
                                text = "موجودی: ${product.formatStockDisplay()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isLowStock) AccountingCreditRed else TextDarkMuted,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * پنل کامل سبد خرید، انتخاب مشتری، اعمال تخفیف، مالیات و دکمه‌های تسویه
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PosCartCheckoutPanel(
    cartItems: List<CartItem>,
    customers: List<PartyEntity>,
    selectedParty: PartyEntity?,
    onSelectParty: (PartyEntity?) -> Unit,
    customerMenuExpanded: Boolean,
    onToggleCustomerMenu: (Boolean) -> Unit,
    onUpdateQuantity: (Int, Double) -> Unit,
    onToggleUnit: (Int) -> Unit,
    onClearCart: () -> Unit,
    discount: Long,
    customDiscountText: String,
    onCustomDiscountChange: (String) -> Unit,
    onQuickDiscountSelected: (Int) -> Unit,
    applyTax: Boolean,
    onToggleTax: (Boolean) -> Unit,
    subtotal: Long,
    grandTotal: Long,
    onFastCash: () -> Unit,
    onFastPos: () -> Unit,
    onAdvancedSettlement: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // ردیف بالای سبد خرید: تیتر، تعداد اقلام و دکمه خالی کردن سبد
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "سبد فروش (${NumberFormatUtils.toPersianDigits(cartItems.size.toString())} ردیف)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextDarkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            if (cartItems.isNotEmpty()) {
                IconButton(
                    onClick = onClearCart,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "خالی کردن سبد",
                        tint = AccountingCreditRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // انتخاب طرف حساب / مشتری
        ExposedDropdownMenuBox(
            expanded = customerMenuExpanded,
            onExpandedChange = onToggleCustomerMenu,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedParty?.name ?: "مشتری نقدی (عمومی)",
                onValueChange = {},
                readOnly = true,
                label = { Text("طرف حساب / مشتری", fontSize = 11.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatteGold,
                    unfocusedBorderColor = CleanBorder,
                    focusedContainerColor = CleanSurface,
                    unfocusedContainerColor = CleanSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = customerMenuExpanded,
                onDismissRequest = { onToggleCustomerMenu(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("مشتری نقدی (عمومی)") },
                    onClick = {
                        onSelectParty(null)
                        onToggleCustomerMenu(false)
                    }
                )
                customers.forEach { customer ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(customer.name, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "مانده حساب: ${NumberFormatUtils.formatPrice(customer.currentBalance)}",
                                    fontSize = 11.sp,
                                    color = if (customer.currentBalance > 0) AccountingDebitGreen else TextDarkMuted
                                )
                            }
                        },
                        onClick = {
                            onSelectParty(customer)
                            onToggleCustomerMenu(false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = CleanBorder)
        Spacer(modifier = Modifier.height(8.dp))

        // لیست اقلام داخل سبد خرید
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = TextDarkMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "سبد فروش خالی است.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkMuted)
                    )
                    Text(
                        text = "با لمس اقلام، آنها را به سبد اضافه کنید.",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextDarkMuted, fontSize = 10.sp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems.size) { index ->
                    val item = cartItems[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanBackground),
                        border = BorderStroke(1.dp, CleanBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // ردیف نام کالا و کلید تغییر واحد
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextDarkPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (item.product.conversionRate > 1.0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MatteGoldContainer,
                                        border = BorderStroke(1.dp, MatteGold.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { onToggleUnit(index) }
                                    ) {
                                        Text(
                                            text = item.unitName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NavyPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // ردیف تغییر تعداد و مبلغ
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // دکمه‌های کنترل تعداد با تاچ تارگت استاندارد ۴۸dp
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onUpdateQuantity(index, item.quantity - 1.0) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "کاهش تعداد",
                                            tint = AccountingCreditRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CleanSurface,
                                        border = BorderStroke(1.dp, CleanBorder)
                                    ) {
                                        Text(
                                            text = "${NumberFormatUtils.formatQuantity(item.quantity)} ${item.unitName}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextDarkPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onUpdateQuantity(index, item.quantity + 1.0) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "افزایش تعداد",
                                            tint = AccountingDebitGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = NumberFormatUtils.formatPrice(item.totalPrice),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = NavyPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = CleanBorder)
        Spacer(modifier = Modifier.height(8.dp))

        // بخش تخفیف، مالیات و مبالغ
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // فیلد تخفیف و چیپ‌های سریع درصد
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تخفیف نقدی:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextDarkSecondary,
                        fontSize = 11.sp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0 to "۰٪", 5 to "۵٪", 10 to "۱۰٪").forEach { (pct, label) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CleanBackground,
                            border = BorderStroke(1.dp, CleanBorder),
                            modifier = Modifier.clickable { onQuickDiscountSelected(pct) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextDarkPrimary,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customDiscountText,
                        onValueChange = onCustomDiscountChange,
                        placeholder = { Text("مبلغ", fontSize = 10.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(100.dp)
                            .height(44.dp)
                            .testTag("pos_discount_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatteGold,
                            unfocusedBorderColor = CleanBorder
                        )
                    )
                }
            }

            // سوئیچ مالیات ارزش افزوده
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "مالیات بر ارزش افزوده (۱۰٪):",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextDarkSecondary,
                        fontSize = 11.sp
                    )
                )
                Switch(
                    checked = applyTax,
                    onCheckedChange = onToggleTax,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MatteGold,
                        checkedTrackColor = NavyPrimary
                    )
                )
            }

            // کارت مبلغ نهایی شکیل
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = NavySurface,
                border = BorderStroke(1.dp, MatteGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (discount > 0 || applyTax) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "جمع اقلام: ${NumberFormatUtils.formatPrice(subtotal)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            )
                            if (discount > 0) {
                                Text(
                                    text = "تخفیف: ${NumberFormatUtils.formatPrice(discount)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MatteGold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مبلغ کل قابل پرداخت:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = NumberFormatUtils.formatPrice(grandTotal),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MatteGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // دکمه‌های تسویه سریع و چندگانه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ۱. تسویه نقد
                Button(
                    onClick = onFastCash,
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_fast_cash_button"),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Money, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "نقد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // ۲. تسویه پوز
                Button(
                    onClick = onFastPos,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_fast_pos_button"),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "پوز", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // ۳. تسویه ترکیبی / نسیه / چک
                Button(
                    onClick = onAdvancedSettlement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatteGold,
                        contentColor = NavyDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("pos_advanced_settlement_button"),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ترکیبی / نسیه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
