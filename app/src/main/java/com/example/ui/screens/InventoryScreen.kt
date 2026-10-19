package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import com.example.ui.components.ProductCardItem
import com.example.ui.components.ProductFormBottomSheet
import com.example.ui.components.StockTransferBottomSheet
import com.example.ui.components.WarehouseCardItem
import com.example.ui.components.WarehouseFormBottomSheet
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanCard
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.MatteGoldLight
import com.example.ui.theme.MatteGoldOnContainer
import com.example.ui.theme.NavyBrand
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.NumberFormatUtils

/**
 * صفحه مدیریت انبارداری و کالاها (InventoryScreen) در سامانه حسابداری گلی
 * طراحی بازطراحی‌شده: حرفه‌ای، کاملاً فارسی و راست‌چین
 * منطبق با پالت هویت برند: سرمه‌ای (#171C2A)، طلایی مات (#C6A15B) و پس‌زمینه پاک (#F5F6F8)
 * نمایش کالاها با عکس محصول، کد، بارکد، موجودی لحظه‌ای ترکیبی، قیمت خرید و فروش، فیلتر دسته‌بندی و هشدار کمبود موجودی
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    val warehouses by viewModel.allWarehouses.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val selectedCardexProduct by viewModel.selectedProductCardex.collectAsStateWithLifecycle()
    val cardexMovements by viewModel.cardexMovements.collectAsStateWithLifecycle()
    val transferMovements by viewModel.transferMovements.collectAsStateWithLifecycle()
    val recentMovements by viewModel.recentMovements.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("کالاها و موجودی", "مدیریت انبارها", "حواله انتقال", "کاردکس و گردش")

    // فیلتر فقط کمبود موجودی در نمای کالاها
    var filterOnlyLowStock by remember { mutableStateOf(false) }

    // Modal BottomSheet States
    val productSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showProductSheet by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }

    val warehouseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showWarehouseSheet by remember { mutableStateOf(false) }
    var warehouseToEdit by remember { mutableStateOf<WarehouseEntity?>(null) }

    val transferSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTransferSheet by remember { mutableStateOf(false) }
    var productForTransfer by remember { mutableStateOf<ProductEntity?>(null) }

    // Dialog States for Deletion & Confirmation
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var warehouseToDelete by remember { mutableStateOf<WarehouseEntity?>(null) }

    val displayProducts = remember(filteredProducts, filterOnlyLowStock) {
        if (filterOnlyLowStock) {
            filteredProducts.filter { it.currentStockInSecondary <= it.minStockThreshold }
        } else {
            filteredProducts
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = CleanBackground,
            topBar = {
                // هدر اختصاصی و مجلل سرمه‌ای گلی
                Surface(
                    color = NavyPrimary,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MatteGoldContainer,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = MatteGoldOnContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "مدیریت انبارها و کالاها",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "موجودی لحظه‌ای، قیمت‌گذاری چندسطحی و کنترل سفارش",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    )
                                }
                            }

                            // دکمه سریع افزودن کالا در هدر
                            Button(
                                onClick = {
                                    productToEdit = null
                                    showProductSheet = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MatteGold,
                                    contentColor = NavyDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("header_add_product_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = NavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "کالای جدید",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                when (selectedTab) {
                    0 -> {
                        FloatingActionButton(
                            onClick = {
                                productToEdit = null
                                showProductSheet = true
                            },
                            containerColor = NavyPrimary,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("add_product_fab")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "تعریف کالا",
                                    tint = MatteGold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "افزودن کالا",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    1 -> {
                        FloatingActionButton(
                            onClick = {
                                warehouseToEdit = null
                                showWarehouseSheet = true
                            },
                            containerColor = NavyPrimary,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("add_warehouse_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "تعریف انبار",
                                tint = MatteGold
                            )
                        }
                    }
                    2 -> {
                        FloatingActionButton(
                            onClick = {
                                productForTransfer = null
                                showTransferSheet = true
                            },
                            containerColor = NavyPrimary,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("add_transfer_fab")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = "حواله جدید",
                                tint = MatteGold
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CleanBackground)
            ) {
                // تب‌های ناوبری مدرن با استایل سرمه‌ای و طلایی
                Surface(
                    color = CleanSurface,
                    shadowElevation = 2.dp
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CleanSurface,
                        contentColor = NavyPrimary
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) NavyPrimary else TextDarkSecondary
                                            )
                                        )

                                        // نشان تعداد هشدار کسری در تب اول
                                        if (index == 0 && lowStockProducts.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = AccountingCreditRed,
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = NumberFormatUtils.toPersianDigits(lowStockProducts.size),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // تب ۱: فهرست کالاها و موجودی
                        ProductsTabContent(
                            products = displayProducts,
                            allProducts = allProducts,
                            allProductsCount = allProducts.size,
                            lowStockCount = lowStockProducts.size,
                            warehousesCount = warehouses.size,
                            categories = categories,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            filterOnlyLowStock = filterOnlyLowStock,
                            onToggleFilterLowStock = { filterOnlyLowStock = !filterOnlyLowStock },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onViewCardex = { viewModel.selectProductForCardex(it) },
                            onEditProduct = {
                                productToEdit = it
                                showProductSheet = true
                            },
                            onTransferProduct = {
                                productForTransfer = it
                                showTransferSheet = true
                            },
                            onDeleteProduct = { productToDelete = it },
                            onAddNewProduct = {
                                productToEdit = null
                                showProductSheet = true
                            }
                        )
                    }
                    1 -> {
                        // تب ۲: مدیریت انبارها
                        WarehousesTabContent(
                            warehouses = warehouses,
                            onAddNewWarehouse = {
                                warehouseToEdit = null
                                showWarehouseSheet = true
                            },
                            onEditWarehouse = {
                                warehouseToEdit = it
                                showWarehouseSheet = true
                            },
                            onDeleteWarehouse = { warehouseToDelete = it },
                            onSetDefaultWarehouse = {
                                viewModel.setWarehouseAsDefault(it.id) {
                                    Toast.makeText(context, "انبار «${it.name}» به عنوان انبار پیش‌فرض تنظیم شد.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    2 -> {
                        // تب ۳: حواله‌های انتقال بین انبارها
                        TransfersTabContent(
                            transfers = transferMovements,
                            products = allProducts,
                            warehouses = warehouses,
                            onNewTransfer = {
                                productForTransfer = null
                                showTransferSheet = true
                            }
                        )
                    }
                    3 -> {
                        // تب ۴: کاردکس و گردش کل انبار
                        CardexTabContent(
                            recentMovements = recentMovements,
                            products = allProducts,
                            selectedProduct = selectedCardexProduct,
                            productMovements = cardexMovements,
                            onSelectProduct = { viewModel.selectProductForCardex(it) },
                            onDeleteMovement = { movId ->
                                viewModel.deleteSingleMovement(movId) { success, msg ->
                                    Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                                }
                            },
                            onClearAllMovements = { prodId ->
                                viewModel.clearAllMovementsForProduct(prodId) { success, msg ->
                                    Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // ModalBottomSheet: افزودن / ویرایش کالا
        if (showProductSheet) {
            ProductFormBottomSheet(
                sheetState = productSheetState,
                productToEdit = productToEdit,
                warehouses = warehouses,
                onDismiss = { showProductSheet = false },
                onSubmit = { code, barcode, name, cat, pUnit, sUnit, rate, stock, minStock, buy, retail, whole, partner, desc, imgUri, targetWhId ->
                    if (productToEdit != null) {
                        val updated = productToEdit!!.copy(
                            code = code.ifBlank { productToEdit!!.code },
                            barcode = barcode,
                            name = name,
                            category = cat.ifBlank { "عمومی" },
                            primaryUnit = pUnit.ifBlank { "کارتن" },
                            secondaryUnit = sUnit.ifBlank { "عدد" },
                            conversionRate = if (rate <= 0) 1.0 else rate,
                            currentStockInSecondary = stock,
                            minStockThreshold = minStock,
                            buyPrice = buy,
                            retailPrice = retail,
                            wholesalePrice = whole,
                            partnerPrice = partner,
                            description = desc,
                            imageUri = imgUri
                        )
                        viewModel.updateProduct(updated) {
                            Toast.makeText(context, "مشخصات کالای «$name» با موفقیت به‌روزرسانی شد.", Toast.LENGTH_SHORT).show()
                            showProductSheet = false
                        }
                    } else {
                        viewModel.addProduct(
                            code, barcode, name, cat, pUnit, sUnit, rate, stock, minStock, buy, retail, whole, partner, desc, imgUri, targetWhId
                        ) {
                            Toast.makeText(context, "کالای «$name» با موفقیت در انبار ثبت گردید.", Toast.LENGTH_SHORT).show()
                            showProductSheet = false
                        }
                    }
                }
            )
        }

        // ModalBottomSheet: افزودن / ویرایش انبار
        if (showWarehouseSheet) {
            WarehouseFormBottomSheet(
                sheetState = warehouseSheetState,
                warehouseToEdit = warehouseToEdit,
                onDismiss = { showWarehouseSheet = false },
                onSubmit = { code, name, manager, phone, address, isDefault ->
                    if (warehouseToEdit != null) {
                        val updated = warehouseToEdit!!.copy(
                            code = code.ifBlank { warehouseToEdit!!.code },
                            name = name,
                            managerName = manager,
                            phone = phone,
                            address = address,
                            isDefault = isDefault
                        )
                        viewModel.updateWarehouse(updated) {
                            Toast.makeText(context, "مشخصات انبار «$name» با موفقیت ویرایش شد.", Toast.LENGTH_SHORT).show()
                            showWarehouseSheet = false
                        }
                    } else {
                        viewModel.addWarehouse(code, name, manager, phone, address, isDefault) {
                            Toast.makeText(context, "انبار «$name» با موفقیت تعریف شد.", Toast.LENGTH_SHORT).show()
                            showWarehouseSheet = false
                        }
                    }
                }
            )
        }

        // ModalBottomSheet: ثبت حواله انتقال
        if (showTransferSheet) {
            StockTransferBottomSheet(
                sheetState = transferSheetState,
                initialProduct = productForTransfer,
                products = allProducts,
                warehouses = warehouses,
                onDismiss = { showTransferSheet = false },
                onSubmit = { productId, srcWhId, dstWhId, qtySecondary, desc ->
                    viewModel.transferStock(productId, srcWhId, dstWhId, qtySecondary, desc) { success, message ->
                        Toast.makeText(context, message, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                        if (success) {
                            showTransferSheet = false
                        }
                    }
                }
            )
        }

        // Stock Cardex Dialog (اگر محصولی در نمای دیالوگ اختصاصی انتخاب شد)
        selectedCardexProduct?.let { product ->
            StockCardexDialog(
                product = product,
                movements = cardexMovements,
                onDismiss = { viewModel.selectProductForCardex(null) },
                onDeleteMovement = { movId ->
                    viewModel.deleteSingleMovement(movId) { success, msg ->
                        Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                    }
                },
                onClearAllMovements = { prodId ->
                    viewModel.clearAllMovementsForProduct(prodId) { success, msg ->
                        Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // دیالوگ تایید حذف کالا
        productToDelete?.let { prod ->
            var depCounts by remember(prod.id) { mutableStateOf<Pair<Int, Int>?>(null) }
            var isForceDeleting by remember { mutableStateOf(false) }

            androidx.compose.runtime.LaunchedEffect(prod.id) {
                viewModel.getProductDependencies(prod.id) { inv, mov ->
                    depCounts = Pair(inv, mov)
                }
            }

            val invoiceDeps = depCounts?.first ?: 0
            val movementDeps = depCounts?.second ?: 0
            val hasDeps = invoiceDeps > 0 || movementDeps > 0

            AlertDialog(
                onDismissRequest = { productToDelete = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = AccountingCreditRed) },
                title = { Text("حذف کالا از انبار", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("آیا از حذف کالای «${prod.name}» (کد: ${NumberFormatUtils.toPersianDigits(prod.code)}) اطمینان دارید؟")
                        Text(
                            text = "موجودی لحظه‌ای: ${NumberFormatUtils.toPersianDigits(prod.formatStockDisplay())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        if (hasDeps) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccountingCreditRed.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "⚠️ وابستگی سند: این کالا دارای $movementDeps گردش کاردکس و $invoiceDeps فاکتور مرتبط است.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccountingCreditRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "برای حذف کامل و رفع بن‌بست، روی «پاکسازی و حذف گردش» بزنید تا تمامی رکوردهای باقیمانده پاک شده و کالا حذف شود.",
                                        fontSize = 11.sp,
                                        color = TextDarkSecondary
                                    )
                                }
                            }
                        } else if (depCounts != null) {
                            Text(
                                text = "این کالا هیچ‌گونه گردش یا فاکتوری ندارد و به‌صورت تمیز حذف خواهد شد.",
                                fontSize = 12.sp,
                                color = TextDarkSecondary
                            )
                        }
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (hasDeps) {
                            Button(
                                onClick = {
                                    isForceDeleting = true
                                    viewModel.forceDeleteProduct(prod) { success, msg ->
                                        isForceDeleting = false
                                        Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                                        if (success) {
                                            productToDelete = null
                                        }
                                    }
                                },
                                enabled = !isForceDeleting,
                                colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isForceDeleting) "در حال پاکسازی..." else "پاکسازی گردش‌ها و حذف قطعی کالا")
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.deleteProduct(prod) { success, msg ->
                                        Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                                        if (success) {
                                            productToDelete = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("حذف کالا")
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("انصراف", color = TextDarkSecondary)
                    }
                }
            )
        }

        // دیالوگ تایید حذف انبار
        warehouseToDelete?.let { wh ->
            AlertDialog(
                onDismissRequest = { warehouseToDelete = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = AccountingCreditRed) },
                title = { Text("حذف انبار", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                text = {
                    Text("آیا از حذف انبار «${wh.name}» (کد: ${wh.code}) اطمینان دارید؟ در صورت وجود موجودی کالا در این انبار، برای جلوگیری از مفقودی کالا، حذف غیرمجاز خواهد بود.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteWarehouse(wh) { success, msg ->
                                Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                                warehouseToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                    ) {
                        Text("حذف انبار")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { warehouseToDelete = null }) {
                        Text("انصراف", color = TextDarkSecondary)
                    }
                }
            )
        }
    }
}

/**
 * تب ۱: کالاها و موجودی
 */
@Composable
private fun ProductsTabContent(
    products: List<ProductEntity>,
    allProducts: List<ProductEntity>,
    allProductsCount: Int,
    lowStockCount: Int,
    warehousesCount: Int,
    categories: List<String>,
    selectedCategory: String?,
    searchQuery: String,
    filterOnlyLowStock: Boolean,
    onToggleFilterLowStock: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onViewCardex: (ProductEntity) -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onTransferProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAddNewProduct: () -> Unit
) {
    // محاسبه ارزش ریالی موجودی بر اساس قیمت خرید
    val totalInventoryValue = remember(allProducts) {
        allProducts.sumOf { (it.currentStockInSecondary * it.buyPrice).toLong() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        // ۱. کارت‌های آماری بالای صفحه (۴ کارت شکیل و تفکیک‌شده)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPillCard(
                        title = "تنوع کل کالاها",
                        value = "${NumberFormatUtils.toPersianDigits(allProductsCount)} قلم",
                        accentColor = NavyPrimary,
                        backgroundColor = CleanCard,
                        modifier = Modifier.weight(1f)
                    )
                    StatPillCard(
                        title = "هشدار کسری موجودی",
                        value = "${NumberFormatUtils.toPersianDigits(lowStockCount)} کالا",
                        accentColor = if (lowStockCount > 0) AccountingCreditRed else AccountingDebitGreen,
                        backgroundColor = if (lowStockCount > 0) Color(0xFFFFF4F2) else CleanCard,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPillCard(
                        title = "ارزش سرمایه انبار (خرید)",
                        value = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(totalInventoryValue))} تومان",
                        accentColor = MatteGold,
                        backgroundColor = CleanCard,
                        modifier = Modifier.weight(1.3f)
                    )
                    StatPillCard(
                        title = "تعداد انبار فعال",
                        value = "${NumberFormatUtils.toPersianDigits(warehousesCount)} انبار",
                        accentColor = NavySecondary,
                        backgroundColor = CleanCard,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        }

        // ۲. فیلد جستجوی سریع و دکمه افزودن کالا
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CleanCard,
                border = BorderStroke(1.dp, CleanBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = {
                                Text(
                                    text = "جستجوی نام کالا، کد، یا بارکد...",
                                    fontSize = 13.sp,
                                    color = TextDarkMuted
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MatteGold
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "پاک کردن جستجو",
                                            tint = TextDarkSecondary
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MatteGold,
                                unfocusedBorderColor = CleanBorder,
                                focusedContainerColor = CleanBackground,
                                unfocusedContainerColor = CleanBackground
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_product_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // دکمه واضح افزودن کالا در کنار جستجو
                        Button(
                            onClick = onAddNewProduct,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("quick_add_product_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MatteGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "کالای جدید",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // کلیدهای فیلتر وضعیت سریع (همه اقلام / فقط کسری موجودی)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPillToggle(
                            title = "همه کالاها (${NumberFormatUtils.toPersianDigits(allProductsCount)})",
                            isSelected = !filterOnlyLowStock,
                            onClick = { if (filterOnlyLowStock) onToggleFilterLowStock() },
                            modifier = Modifier.weight(1f)
                        )

                        FilterPillToggle(
                            title = "⚠️ فقط کسری موجودی (${NumberFormatUtils.toPersianDigits(lowStockCount)})",
                            isSelected = filterOnlyLowStock,
                            activeColor = AccountingCreditRed,
                            onClick = { if (!filterOnlyLowStock) onToggleFilterLowStock() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ۳. فیلتر دسته‌بندی با چیپ‌های اسکرول‌شونده
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = selectedCategory == null
                    Surface(
                        onClick = { onSelectCategory(null) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) NavyPrimary else CleanCard,
                        border = BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanBorder)
                    ) {
                        Text(
                            text = "همه دسته‌ها (${NumberFormatUtils.toPersianDigits(allProductsCount)})",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            color = if (isSelected) Color.White else TextDarkPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val countInCat = allProducts.count { it.category == cat }
                    Surface(
                        onClick = { onSelectCategory(cat) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) NavyPrimary else CleanCard,
                        border = BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanBorder)
                    ) {
                        Text(
                            text = "$cat (${NumberFormatUtils.toPersianDigits(countInCat)})",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            color = if (isSelected) Color.White else TextDarkPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ۴. فهرست کالاها
        if (products.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = MatteGoldContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MatteGoldOnContainer,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "کالایی با عبارت «$searchQuery» یافت نشد."
                                else if (filterOnlyLowStock) "خوشبختانه هیچ کالایی با کسری موجودی وجود ندارد."
                                else "هنوز کالایی در سیستم ثبت نشده است.",
                                color = TextDarkPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "برای شروع ثبت و مدیریت انبارها، دکمه زیر را فشار دهید.",
                                color = TextDarkSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddNewProduct,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MatteGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تعریف و ثبت اولین کالا", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(products, key = { it.id }) { product ->
                ProductCardItem(
                    product = product,
                    onViewCardex = { onViewCardex(product) },
                    onEdit = { onEditProduct(product) },
                    onTransfer = { onTransferProduct(product) },
                    onDelete = { onDeleteProduct(product) }
                )
            }
        }
    }
}

@Composable
private fun FilterPillToggle(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = NavyPrimary
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.12f) else CleanBackground,
        border = BorderStroke(
            1.dp,
            if (isSelected) activeColor else CleanBorder
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) activeColor else TextDarkSecondary
            )
        }
    }
}

/**
 * تب ۲: مدیریت انبارها
 */
@Composable
private fun WarehousesTabContent(
    warehouses: List<WarehouseEntity>,
    onAddNewWarehouse: () -> Unit,
    onEditWarehouse: (WarehouseEntity) -> Unit,
    onDeleteWarehouse: (WarehouseEntity) -> Unit,
    onSetDefaultWarehouse: (WarehouseEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CleanCard,
                border = BorderStroke(1.dp, CleanBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تعریف و مدیریت انبارهای سازمان",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "می‌توانید انبار مرکزی، فروشگاه، ضایعات و شعب را به تفکیک مدیریت فرمایید.",
                            fontSize = 11.sp,
                            color = TextDarkSecondary
                        )
                    }
                    Button(
                        onClick = onAddNewWarehouse,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MatteGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("انبار جدید", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        if (warehouses.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ انباری تعریف نشده است.",
                            color = TextDarkSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(warehouses, key = { it.id }) { wh ->
                WarehouseCardItem(
                    warehouse = wh,
                    onEdit = { onEditWarehouse(wh) },
                    onDelete = { onDeleteWarehouse(wh) },
                    onSetDefault = { onSetDefaultWarehouse(wh) }
                )
            }
        }
    }
}

/**
 * تب ۳: حواله‌های انتقال بین انبارها
 */
@Composable
private fun TransfersTabContent(
    transfers: List<InventoryMovementEntity>,
    products: List<ProductEntity>,
    warehouses: List<WarehouseEntity>,
    onNewTransfer: () -> Unit
) {
    val productsMap = remember(products) { products.associateBy { it.id } }
    val warehousesMap = remember(warehouses) { warehouses.associateBy { it.id } }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTransfers = remember(transfers, searchQuery, productsMap, warehousesMap) {
        val q = searchQuery.trim()
        if (q.isBlank()) transfers
        else {
            transfers.filter { tr ->
                val prod = productsMap[tr.productId]
                val wh = warehousesMap[tr.warehouseId]
                tr.referenceNumber.contains(q, ignoreCase = true) ||
                        (prod?.name?.contains(q, ignoreCase = true) == true) ||
                        (wh?.name?.contains(q, ignoreCase = true) == true) ||
                        tr.jalaliDate.contains(q) ||
                        tr.description.contains(q, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CleanCard,
                border = BorderStroke(1.dp, CleanBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حواله‌های جابجایی و انتقال کالا",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "جابجایی کالا بین شعب و انبارها با ثبت همزمان خروج و ورود",
                            fontSize = 11.sp,
                            color = TextDarkSecondary
                        )
                    }
                    Button(
                        onClick = onNewTransfer,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = MatteGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حواله جدید", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی شماره حواله، نام کالا، انبار...", fontSize = 13.sp, color = TextDarkMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MatteGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "پاک کردن")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatteGold,
                    unfocusedBorderColor = CleanBorder,
                    focusedContainerColor = CleanCard,
                    unfocusedContainerColor = CleanCard
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transfer_input")
            )
        }

        if (filteredTransfers.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (transfers.isEmpty()) "هنوز هیچ حواله انتقالی ثبت نشده است." else "هیچ حواله‌ای مطابق با «$searchQuery» یافت نشد.",
                            color = TextDarkSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredTransfers) { tr ->
                val prod = productsMap[tr.productId]
                val wh = warehousesMap[tr.warehouseId]
                val isEntry = tr.quantitySecondary > 0

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEntry) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isEntry) AccountingDebitGreen.copy(alpha = 0.3f) else AccountingCreditRed.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${prod?.name ?: "کد ${tr.productId}"} (${if (isEntry) "ورود به" else "خروج از"} ${wh?.name ?: "انبار"})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDarkPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "شماره حواله: ${NumberFormatUtils.toPersianDigits(tr.referenceNumber)} | تاریخ: ${NumberFormatUtils.toPersianDigits(tr.jalaliDate)}",
                                fontSize = 11.sp,
                                color = TextDarkSecondary
                            )
                            if (tr.description.isNotBlank()) {
                                Text(
                                    text = tr.description,
                                    fontSize = 11.sp,
                                    color = TextDarkMuted
                                )
                            }
                        }

                        Text(
                            text = (if (isEntry) "+" else "") + NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatQuantity(tr.quantitySecondary)) + " ${prod?.secondaryUnit ?: "عدد"}",
                            fontWeight = FontWeight.Bold,
                            color = if (isEntry) AccountingDebitGreen else AccountingCreditRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * تب ۴: کاردکس و گردش کالا
 */
@Composable
private fun CardexTabContent(
    recentMovements: List<InventoryMovementEntity>,
    products: List<ProductEntity>,
    selectedProduct: ProductEntity?,
    productMovements: List<InventoryMovementEntity>,
    onSelectProduct: (ProductEntity?) -> Unit,
    onDeleteMovement: (Long) -> Unit,
    onClearAllMovements: (Long) -> Unit
) {
    val productsMap = remember(products) { products.associateBy { it.id } }
    var cardexSearchQuery by remember { mutableStateOf("") }
    var movementToDelete by remember { mutableStateOf<InventoryMovementEntity?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val rawMovements = if (selectedProduct != null) productMovements else recentMovements
    val movementsToShow = remember(rawMovements, cardexSearchQuery, productsMap) {
        val q = cardexSearchQuery.trim()
        if (q.isBlank()) rawMovements
        else {
            rawMovements.filter { mov ->
                val prod = productsMap[mov.productId]
                mov.referenceNumber.contains(q, ignoreCase = true) ||
                        (prod?.name?.contains(q, ignoreCase = true) == true) ||
                        mov.description.contains(q, ignoreCase = true) ||
                        mov.jalaliDate.contains(q)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CleanCard,
                border = BorderStroke(1.dp, CleanBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedProduct != null) "کاردکس کالای: ${selectedProduct.name}" else "گردش‌های اخیر کل انبار (تمام تراکنش‌ها)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NavyPrimary
                            )
                            if (selectedProduct != null) {
                                Text(
                                    text = "موجودی لحظه‌ای: ${NumberFormatUtils.toPersianDigits(selectedProduct.formatStockDisplay())} | تعداد گردش: ${NumberFormatUtils.toPersianDigits(productMovements.size)}",
                                    fontSize = 11.sp,
                                    color = AccountingDebitGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedProduct != null) {
                                if (productMovements.isNotEmpty()) {
                                    Button(
                                        onClick = { showClearAllConfirm = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed.copy(alpha = 0.85f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("پاکسازی کل گردش‌ها", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                TextButton(onClick = { onSelectProduct(null) }) {
                                    Text("نمایش همه", color = NavyPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = cardexSearchQuery,
                onValueChange = { cardexSearchQuery = it },
                placeholder = { Text("جستجوی شماره سند، شرح یا کالای گردش انبار...", fontSize = 13.sp, color = TextDarkMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MatteGold) },
                trailingIcon = {
                    if (cardexSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { cardexSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "پاک کردن")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatteGold,
                    unfocusedBorderColor = CleanBorder,
                    focusedContainerColor = CleanCard,
                    unfocusedContainerColor = CleanCard
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_cardex_input")
            )
        }

        if (movementsToShow.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (rawMovements.isEmpty()) "هیچ گردشی برای نمایش وجود ندارد." else "هیچ سندی مطابق با «$cardexSearchQuery» یافت نشد.",
                            color = TextDarkSecondary
                        )
                    }
                }
            }
        } else {
            items(movementsToShow) { mov ->
                val prod = productsMap[mov.productId]
                val isEntry = mov.quantitySecondary > 0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEntry) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isEntry) AccountingDebitGreen.copy(alpha = 0.3f) else AccountingCreditRed.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${prod?.name ?: "کالا ${mov.productId}"} - ${formatDocumentTypePersian(mov.documentType)} (${NumberFormatUtils.toPersianDigits(mov.referenceNumber)})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            )
                            Text(
                                text = "تاریخ: ${NumberFormatUtils.toPersianDigits(mov.jalaliDate)} | ${mov.description}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextDarkSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = (if (isEntry) "+" else "") + NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatQuantity(mov.quantitySecondary)) + " ${prod?.secondaryUnit ?: "عدد"}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEntry) AccountingDebitGreen else AccountingCreditRed
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { movementToDelete = mov },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف مستقیم گردش",
                                    tint = AccountingCreditRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    movementToDelete?.let { mov ->
        AlertDialog(
            onDismissRequest = { movementToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed) },
            title = { Text("حذف مستقیم گردش از کاردکس", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = {
                Text("آیا از حذف مستقیم این گردش («${mov.referenceNumber}» - ${formatDocumentTypePersian(mov.documentType)}) اطمینان دارید؟\nبا حذف این رکورد، اثر آن از موجودی انبار برداشته شده و کالا به امکان حذف بدون مانع نزدیک می‌شود.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMovement(mov.id)
                        movementToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف این گردش")
                }
            },
            dismissButton = {
                TextButton(onClick = { movementToDelete = null }) {
                    Text("انصراف", color = TextDarkSecondary)
                }
            }
        )
    }

    if (showClearAllConfirm && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AccountingCreditRed) },
            title = { Text("پاکسازی کامل کاردکس کالا", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = {
                Text("آیا از پاکسازی تمامی گردش‌های کالای «${selectedProduct.name}» اطمینان دارید؟ با این کار تمام رکوردهای کاردکس پاک شده، موجودی صفر گردیده و کالا آماده حذف کامل خواهد بود.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllMovements(selectedProduct.id)
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("پاکسازی کامل گردش‌ها")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("انصراف", color = TextDarkSecondary)
                }
            }
        )
    }
}

@Composable
private fun StatPillCard(
    title: String,
    value: String,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, CleanBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextDarkSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}

/**
 * دیالوگ کاردکس کالا با امکان مشاهده کامل تمام گردش‌ها و دکمه حذف مستقیم
 */
@Composable
fun StockCardexDialog(
    product: ProductEntity,
    movements: List<InventoryMovementEntity>,
    onDismiss: () -> Unit,
    onDeleteMovement: (Long) -> Unit = {},
    onClearAllMovements: (Long) -> Unit = {}
) {
    var movementToDelete by remember { mutableStateOf<InventoryMovementEntity?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                color = CleanCard,
                border = BorderStroke(1.dp, CleanBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "کاردکس کالا: ${product.name}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                            Text(
                                text = "موجودی لحظه‌ای: ${NumberFormatUtils.toPersianDigits(product.formatStockDisplay())} (${NumberFormatUtils.toPersianDigits(movements.size)} گردش ثبت شده)",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccountingDebitGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextDarkSecondary)
                        }
                    }

                    if (movements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تمامی گردش‌ها (فاکتورها، ابطالی‌ها، حواله‌ها و تعدیل)",
                                fontSize = 11.sp,
                                color = TextDarkSecondary
                            )
                            Button(
                                onClick = { showClearAllConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed.copy(alpha = 0.85f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("پاکسازی کل گردش‌ها", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (movements.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CleanBackground,
                            border = BorderStroke(1.dp, CleanBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "گردشی برای این کالا در سیستم ثبت نشده است (گردش صفر).",
                                    color = TextDarkSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .height(350.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(movements) { mov ->
                                val isEntry = mov.quantitySecondary > 0
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isEntry) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isEntry) AccountingDebitGreen.copy(alpha = 0.3f) else AccountingCreditRed.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${formatDocumentTypePersian(mov.documentType)} - ${NumberFormatUtils.toPersianDigits(mov.referenceNumber)}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                                            )
                                            Text(
                                                text = "تاریخ: ${NumberFormatUtils.toPersianDigits(mov.jalaliDate)} | ${mov.description}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextDarkSecondary
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = (if (isEntry) "+" else "") + NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatQuantity(mov.quantitySecondary)) + " ${product.secondaryUnit}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isEntry) AccountingDebitGreen else AccountingCreditRed
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { movementToDelete = mov },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف مستقیم این گردش",
                                                    tint = AccountingCreditRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        movementToDelete?.let { mov ->
            AlertDialog(
                onDismissRequest = { movementToDelete = null },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed) },
                title = { Text("حذف مستقیم گردش از کاردکس", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                text = {
                    Text("آیا از حذف مستقیم این گردش («${mov.referenceNumber}» - ${formatDocumentTypePersian(mov.documentType)}) اطمینان دارید؟\nبا حذف مستقیم این گردش، گردش کالا صفر شده و کالا قابل حذف خواهد بود.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteMovement(mov.id)
                            movementToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حذف این گردش")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { movementToDelete = null }) {
                        Text("انصراف", color = TextDarkSecondary)
                    }
                }
            )
        }

        if (showClearAllConfirm) {
            AlertDialog(
                onDismissRequest = { showClearAllConfirm = false },
                icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AccountingCreditRed) },
                title = { Text("پاکسازی کامل کاردکس کالا", fontWeight = FontWeight.Bold, color = NavyPrimary) },
                text = {
                    Text("آیا از پاکسازی تمام گردش‌های کاردکس کالای «${product.name}» اطمینان دارید؟ تمام رکوردهای گردش حذف شده و موجودی کالا به صفر تنظیم خواهد شد.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAllMovements(product.id)
                            showClearAllConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("پاکسازی کامل گردش‌ها")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllConfirm = false }) {
                        Text("انصراف", color = TextDarkSecondary)
                    }
                }
            )
        }
    }
}

/**
 * تبدیل کدهای سیستمی گردش کالا به عناوین رسمی حسابداری و انبارداری به زبان فارسی
 */
fun formatDocumentTypePersian(documentType: String): String {
    return when (documentType) {
        "PURCHASE" -> "فاکتور خرید"
        "SALE" -> "فاکتور فروش"
        "POS_SALE" -> "فروش صندوق"
        "RETURN_SALE" -> "برگشت از فروش"
        "RETURN_PURCHASE" -> "برگشت از خرید"
        "ROLLBACK_SALE" -> "ابطال/برگشت فاکتور فروش"
        "ROLLBACK_POS_SALE" -> "ابطال/برگشت فروش صندوق"
        "ROLLBACK_PURCHASE" -> "ابطال/برگشت فاکتور خرید"
        "ROLLBACK_RETURN_SALE" -> "ابطال برگشت از فروش"
        "ROLLBACK_RETURN_PURCHASE" -> "ابطال برگشت از خرید"
        "TRANSFER" -> "حواله انتقال"
        "TRANSFER_IN" -> "ورود حواله انتقال"
        "TRANSFER_OUT" -> "خروج حواله انتقال"
        "ADJUSTMENT" -> "تعدیل انبارگردانی"
        "INITIAL_BALANCE" -> "موجودی اول دوره"
        else -> documentType.replace("ROLLBACK_", "ابطال ").replace("_", " ")
    }
}
