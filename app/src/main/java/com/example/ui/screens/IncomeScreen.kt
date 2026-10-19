package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.IncomeCategoryEntity
import com.example.data.model.IncomeEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.CategorySummary
import com.example.ui.viewmodel.ExpenseAndIncomeViewModel
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    viewModel: ExpenseAndIncomeViewModel
) {
    val context = LocalContext.current

    val incomes by viewModel.allIncomes.collectAsStateWithLifecycle()
    val categories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val cashDesks by viewModel.cashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val editingIncome by viewModel.editingIncome.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "فهرست درآمدها",
        if (editingIncome != null) "ویرایش سند" else "ثبت درآمد جدید",
        "دسته‌بندی‌ها",
        "گزارش و تحلیل"
    )

    LaunchedEffect(editingIncome) {
        if (editingIncome != null) {
            selectedTab = 1
        }
    }

    LaunchedEffect(alertMessage) {
        alertMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearAlertMessage()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                ErpTopAppBar(
                    title = "مدیریت درآمدهای متفرقه",
                    subtitle = "وصول مطالبات، سود بانکی، کارمزد خدمات، فروش ضایعات و اجاره"
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Top KPI Summary Header: Daily, Monthly, and Total Incomes
                IncomeKpiHeader(incomes = incomes)

                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Tab Contents
                when (selectedTab) {
                    0 -> IncomeListTab(
                        incomes = incomes,
                        categories = categories,
                        onEditIncome = {
                            viewModel.setEditingIncome(it)
                            selectedTab = 1
                        },
                        onDeleteIncome = { viewModel.deleteIncome(it) },
                        onNavigateToNew = {
                            viewModel.setEditingIncome(null)
                            selectedTab = 1
                        }
                    )
                    1 -> IncomeFormTab(
                        editingIncome = editingIncome,
                        categories = categories,
                        cashDesks = cashDesks,
                        bankAccounts = bankAccounts,
                        onCancelEditing = {
                            viewModel.setEditingIncome(null)
                            selectedTab = 0
                        },
                        onQuickAddCategory = { title, code, desc ->
                            viewModel.addIncomeCategory(title, code, desc)
                        },
                        onSave = { title, amount, cat, method, cash, bank, date, notes ->
                            viewModel.saveIncome(title, amount, cat, method, cash, bank, date, notes) {
                                selectedTab = 0
                            }
                        }
                    )
                    2 -> IncomeCategoriesTab(
                        categories = categories,
                        incomes = incomes,
                        onAddCategory = { title, code, desc -> viewModel.addIncomeCategory(title, code, desc) },
                        onUpdateCategory = { viewModel.updateIncomeCategory(it) },
                        onDeleteCategory = { viewModel.deleteIncomeCategory(it) }
                    )
                    3 -> IncomeAnalyticsTab(
                        incomes = incomes,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

/**
 * Header banner showing Daily, Monthly, and Total Incomes in Persian Shamsi calendar
 */
@Composable
private fun IncomeKpiHeader(incomes: List<IncomeEntity>) {
    val today = remember { JalaliDate.now() }
    val todayStandardStr = remember { today.toStandardString() }
    val currentYear = today.year
    val currentMonth = today.month
    val monthName = today.getMonthName()

    // Calculate Daily Income (امروز)
    val todayIncomes = incomes.filter { it.jalaliDate.trim() == todayStandardStr }
    val todayTotal = todayIncomes.sumOf { it.amount }
    val todayCount = todayIncomes.size

    // Calculate Monthly Income (ماه جاری)
    val thisMonthIncomes = incomes.filter { inc ->
        val parsed = try { JalaliDate.parse(inc.jalaliDate) } catch (e: Exception) { null }
        parsed != null && parsed.year == currentYear && parsed.month == currentMonth
    }
    val thisMonthTotal = thisMonthIncomes.sumOf { it.amount }
    val thisMonthCount = thisMonthIncomes.size

    // Total Incomes
    val grandTotal = incomes.sumOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Horizontal row with scrollable KPI metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Today's Income (درآمد امروز)
                KpiPillItem(
                    title = "درآمد امروز",
                    subtitle = "${NumberFormatUtils.toPersianDigits(todayCount)} تراکنش",
                    amount = todayTotal,
                    badgeText = today.format(),
                    icon = Icons.Default.Today,
                    accentColor = AccountingDebitGreen,
                    containerColor = AccountingDebitGreen.copy(alpha = 0.10f)
                )

                // Card 2: Current Month's Income (درآمد ماه جاری)
                KpiPillItem(
                    title = "درآمد ماه جاری",
                    subtitle = "$monthName (${NumberFormatUtils.toPersianDigits(thisMonthCount)} سند)",
                    amount = thisMonthTotal,
                    badgeText = "$monthName $currentYear",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = Color(0xFF00796B),
                    containerColor = Color(0xFF00796B).copy(alpha = 0.10f)
                )

                // Card 3: Total Recorded Incomes (مجموع کل)
                KpiPillItem(
                    title = "کل درآمدهای متفرقه",
                    subtitle = "${NumberFormatUtils.toPersianDigits(incomes.size)} سند کلی",
                    amount = grandTotal,
                    badgeText = "تمام دوره‌ها",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    accentColor = PersianGold,
                    containerColor = PersianGold.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun KpiPillItem(
    title: String,
    subtitle: String,
    amount: Long,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    containerColor: Color
) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.2f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = NumberFormatUtils.toPersianDigits(badgeText),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "+${NumberFormatUtils.formatPrice(amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = accentColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Incomes List with Search, Multi-Filter, and Item Details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomeListTab(
    incomes: List<IncomeEntity>,
    categories: List<IncomeCategoryEntity>,
    onEditIncome: (IncomeEntity) -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
    onNavigateToNew: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, MONTH
    var selectedMethodFilter by remember { mutableStateOf("ALL") } // ALL, BANK, CASH
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var deletingItem by remember { mutableStateOf<IncomeEntity?>(null) }
    var previewItem by remember { mutableStateOf<IncomeEntity?>(null) }

    val today = remember { JalaliDate.now() }
    val todayStr = remember { today.toStandardString() }
    val currentYear = today.year
    val currentMonth = today.month

    // Filtering logic
    val filtered = incomes.filter { item ->
        // Search matching
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true) ||
                item.categoryTitle.contains(searchQuery, ignoreCase = true) ||
                item.accountName.contains(searchQuery, ignoreCase = true)

        // Time filter matching
        val matchesTime = when (selectedTimeFilter) {
            "TODAY" -> item.jalaliDate.trim() == todayStr
            "MONTH" -> {
                val parsed = try { JalaliDate.parse(item.jalaliDate) } catch (e: Exception) { null }
                parsed != null && parsed.year == currentYear && parsed.month == currentMonth
            }
            else -> true
        }

        // Method filter matching
        val matchesMethod = when (selectedMethodFilter) {
            "BANK" -> item.depositMethod == "BANK"
            "CASH" -> item.depositMethod == "CASH"
            else -> true
        }

        // Category matching
        val matchesCategory = selectedCategoryId == null || item.categoryId == selectedCategoryId

        matchesSearch && matchesTime && matchesMethod && matchesCategory
    }

    val filteredSum = filtered.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Search & Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو در عنوان، شرح، سرفصل، حساب...", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("income_search_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = onNavigateToNew,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
                modifier = Modifier.testTag("add_income_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ثبت درآمد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedTimeFilter == "ALL",
                onClick = { selectedTimeFilter = "ALL" },
                label = { Text("همه تاریخ‌ها", fontSize = 11.sp) },
                leadingIcon = if (selectedTimeFilter == "ALL") {
                    { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null
            )
            FilterChip(
                selected = selectedTimeFilter == "TODAY",
                onClick = { selectedTimeFilter = "TODAY" },
                label = { Text("امروز", fontSize = 11.sp) },
                leadingIcon = if (selectedTimeFilter == "TODAY") {
                    { Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null
            )
            FilterChip(
                selected = selectedTimeFilter == "MONTH",
                onClick = { selectedTimeFilter = "MONTH" },
                label = { Text("ماه جاری (${today.getMonthName()})", fontSize = 11.sp) },
                leadingIcon = if (selectedTimeFilter == "MONTH") {
                    { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null
            )

            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))

            // Deposit Method Chips
            FilterChip(
                selected = selectedMethodFilter == "ALL",
                onClick = { selectedMethodFilter = "ALL" },
                label = { Text("همه روش‌ها", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedMethodFilter == "BANK",
                onClick = { selectedMethodFilter = "BANK" },
                label = { Text("واریز بانکی", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
            FilterChip(
                selected = selectedMethodFilter == "CASH",
                onClick = { selectedMethodFilter = "CASH" },
                label = { Text("صندوق (نقد)", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
        }

        // Category Filter Chips (if any)
        if (categories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { selectedCategoryId = null },
                    label = {
                        Text(
                            text = "همه گروه‌ها",
                            fontSize = 10.sp,
                            fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategoryId == null) AccountingDebitGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                categories.forEach { cat ->
                    val isCatSelected = selectedCategoryId == cat.id
                    AssistChip(
                        onClick = {
                            selectedCategoryId = if (isCatSelected) null else cat.id
                        },
                        label = {
                            Text(
                                text = cat.title,
                                fontSize = 10.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCatSelected) AccountingDebitGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Results Summary Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "یافت شده: ${NumberFormatUtils.toPersianDigits(filtered.size)} سند درآمد",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "مجموع: ${NumberFormatUtils.formatPrice(filteredSum)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccountingDebitGreen
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "هیچ سند درآمدی با فیلترهای جاری یافت نشد.",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            searchQuery = ""
                            selectedTimeFilter = "ALL"
                            selectedMethodFilter = "ALL"
                            selectedCategoryId = null
                        }
                    ) {
                        Text("پاک‌کردن فیلترها", fontSize = 11.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    IncomeCardItem(
                        item = item,
                        onEdit = { onEditIncome(item) },
                        onDelete = { deletingItem = item },
                        onPreview = { previewItem = item }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed)
            },
            title = {
                Text(
                    text = "حذف سند درآمد #${NumberFormatUtils.toPersianDigits(item.id.toString())}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "آیا از حذف درآمد «${item.title}» به مبلغ ${NumberFormatUtils.formatPrice(item.amount)} اطمینان دارید؟",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AccountingCreditRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "هشدار: با حذف این سند، مبلغ معادل از موجودی حساب «${item.accountName}» کسر خواهد شد.",
                            fontSize = 11.sp,
                            color = AccountingCreditRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteIncome(item)
                        deletingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("حذف و کسر از موجودی", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("انصراف", fontSize = 11.sp)
                }
            }
        )
    }

    // Detailed Voucher Preview Dialog
    previewItem?.let { item ->
        IncomeDetailVoucherDialog(
            item = item,
            onDismiss = { previewItem = null }
        )
    }
}

/**
 * Individual Income Item Card
 */
@Composable
private fun IncomeCardItem(
    item: IncomeEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Category Tag, Date, and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AccountingDebitGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.categoryTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccountingDebitGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = NumberFormatUtils.toPersianDigits(item.jalaliDate),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "+${NumberFormatUtils.formatPrice(item.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = AccountingDebitGreen
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Title & Persian Amount in Words Preview
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Row 3: Account info & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (item.depositMethod == "CASH") Icons.Default.PointOfSale else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "واریز به: ${item.accountName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onPreview,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "مشاهده سند",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = AccountingCreditRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "شرح: ${item.notes}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Income Form with Persian words, Quick Buttons, Bank/Cash selection, and Date picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomeFormTab(
    editingIncome: IncomeEntity?,
    categories: List<IncomeCategoryEntity>,
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    onCancelEditing: () -> Unit,
    onQuickAddCategory: (title: String, code: String, desc: String) -> Unit,
    onSave: (
        title: String,
        amount: Long,
        category: IncomeCategoryEntity,
        depositMethod: String,
        cashDesk: CashDeskEntity?,
        bankAccount: BankAccountEntity?,
        jalaliDate: String,
        notes: String
    ) -> Unit
) {
    var title by remember(editingIncome) { mutableStateOf(editingIncome?.title ?: "") }
    var amountStr by remember(editingIncome) { mutableStateOf(editingIncome?.amount?.toString() ?: "") }
    var selectedCategory by remember(editingIncome, categories) {
        mutableStateOf(
            categories.find { it.id == editingIncome?.categoryId } ?: categories.firstOrNull()
        )
    }
    var depositMethod by remember(editingIncome) { mutableStateOf(editingIncome?.depositMethod ?: "BANK") }
    var selectedCashDesk by remember(editingIncome, cashDesks) {
        mutableStateOf(
            cashDesks.find { it.id == editingIncome?.cashDeskId } ?: cashDesks.firstOrNull()
        )
    }
    var selectedBankAccount by remember(editingIncome, bankAccounts) {
        mutableStateOf(
            bankAccounts.find { it.id == editingIncome?.bankAccountId } ?: bankAccounts.firstOrNull()
        )
    }
    var jalaliDate by remember(editingIncome) {
        mutableStateOf(editingIncome?.jalaliDate ?: JalaliDate.now().toStandardString())
    }
    var notes by remember(editingIncome) { mutableStateOf(editingIncome?.notes ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var showQuickCategoryDialog by remember { mutableStateOf(false) }

    val amountLong = amountStr.toLongOrNull() ?: 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Editing Mode Banner
        if (editingIncome != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PersianGold.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, PersianGold)
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
                            text = "در حال ویرایش سند درآمد شماره #${NumberFormatUtils.toPersianDigits(editingIncome.id.toString())}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PersianGold
                        )
                        Text(
                            text = "تغییرات به صورت خودکار مانده حساب مقصد را بروز خواهد کرد.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onCancelEditing) {
                        Text("انصراف", fontSize = 11.sp)
                    }
                }
            }
        }

        // Section 1: Title and Category
        Text("۱. مشخصات و سرفصل درآمد:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان / بابت درآمد (الزامی)") },
            placeholder = { Text("مثال: واریز سود سپرده بانک، کارمزد خدمات، فروش ضایعات، اجاره...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("income_title_input"),
            shape = RoundedCornerShape(10.dp)
        )

        // Title Quick Chips for common types
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("سود بانکی", "کارمزد خدمات", "فروش ضایعات", "اجاره ملک", "وصول طلب متفرقه", "پاداش و جایزه").forEach { suggestion ->
                AssistChip(
                    onClick = { title = suggestion },
                    label = { Text(suggestion, fontSize = 10.sp) }
                )
            }
        }

        // Category Selector with quick add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedCategory?.title ?: "انتخاب گروه درآمدی...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("سرفصل / دسته‌بندی درآمد") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.title} (${cat.code})") },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showQuickCategoryDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن گروه", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("گروه جدید", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Section 2: Amount & Date
        Text("۲. مبلغ و تاریخ وصول وجه:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = amountStr,
            onValueChange = { amountStr = NumberFormatUtils.toEnglishDigits(it).filter { ch -> ch.isDigit() } },
            label = { Text("مبلغ واریزی به تومان (الزامی)") },
            placeholder = { Text("مثال: 5000000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("income_amount_input"),
            shape = RoundedCornerShape(10.dp)
        )

        // Quick amount additive buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                100_000L to "+۱۰۰ هزار",
                500_000L to "+۵۰۰ هزار",
                1_000_000L to "+۱ میلیون",
                5_000_000L to "+۵ میلیون",
                10_000_000L to "+۱۰ میلیون"
            ).forEach { (addAmount, label) ->
                AssistChip(
                    onClick = {
                        val current = amountStr.toLongOrNull() ?: 0L
                        amountStr = (current + addAmount).toString()
                    },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }

        // Live verbal conversion in Persian
        if (amountLong > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccountingDebitGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "معادل: ${NumberFormatUtils.numberToPersianWords(amountLong)} تومان",
                    fontSize = 11.sp,
                    color = AccountingDebitGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Jalali Date Field with "Today" quick button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = jalaliDate,
                onValueChange = { jalaliDate = it },
                label = { Text("تاریخ وصول (شمسی: YYYY/MM/DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedButton(
                onClick = { jalaliDate = JalaliDate.now().toStandardString() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("امروز", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Section 3: Destination Account
        Text("۳. حساب مقصد جهت واریز وجه:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { depositMethod = "BANK" },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (depositMethod == "BANK") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (depositMethod == "BANK") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (depositMethod == "BANK") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "واریز به حساب بانک",
                        fontSize = 11.sp,
                        fontWeight = if (depositMethod == "BANK") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { depositMethod = "CASH" },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (depositMethod == "CASH") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (depositMethod == "CASH") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PointOfSale,
                        contentDescription = null,
                        tint = if (depositMethod == "CASH") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "واریز به صندوق نقدی",
                        fontSize = 11.sp,
                        fontWeight = if (depositMethod == "CASH") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Dropdown of specific bank account or cash desk
        if (depositMethod == "BANK") {
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBankAccount?.let { "${it.bankName} - ${it.accountNumber}" } ?: "انتخاب حساب بانکی...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("حساب بانکی مقصد واریز") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    bankAccounts.forEach { acc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("${acc.bankName} - ${acc.accountNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("موجودی فعلی: ${NumberFormatUtils.formatPrice(acc.currentBalance)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = {
                                selectedBankAccount = acc
                                accountExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCashDesk?.name ?: "انتخاب صندوق نقدی...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("صندوق نقدی مقصد واریز") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    cashDesks.forEach { desk ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(desk.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("موجودی فعلی: ${NumberFormatUtils.formatPrice(desk.currentBalance)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = {
                                selectedCashDesk = desk
                                accountExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Section 4: Notes
        Text("۴. شرح و توضیحات تکمیلی:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("شرح سند، شماره پیگیری فیش و توضیحات (اختیاری)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Save Submit Button
        Button(
            onClick = {
                selectedCategory?.let { cat ->
                    onSave(
                        title,
                        amountLong,
                        cat,
                        depositMethod,
                        selectedCashDesk,
                        selectedBankAccount,
                        jalaliDate,
                        notes
                    )
                }
            },
            enabled = title.isNotBlank() && amountLong > 0 && selectedCategory != null &&
                    ((depositMethod == "BANK" && selectedBankAccount != null) || (depositMethod == "CASH" && selectedCashDesk != null)),
            colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_income_submit_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (editingIncome != null) "ذخیره ویرایش سند درآمد" else "ثبت نهایی و واریز درآمد به حساب",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Quick Add Category Dialog
    if (showQuickCategoryDialog) {
        var newCatTitle by remember { mutableStateOf("") }
        var newCatCode by remember { mutableStateOf("") }
        var newCatDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showQuickCategoryDialog = false },
            title = { Text("تعریف سریع گروه درآمدی", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCatTitle,
                        onValueChange = { newCatTitle = it },
                        label = { Text("عنوان گروه (مانند فروش ضایعات، اجاره)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCatCode,
                        onValueChange = { newCatCode = it },
                        label = { Text("کد حسابداری (اختیاری، مثلا INC-09)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCatDesc,
                        onValueChange = { newCatDesc = it },
                        label = { Text("توضیحات گروه") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatTitle.isNotBlank()) {
                            onQuickAddCategory(newCatTitle, newCatCode, newCatDesc)
                            showQuickCategoryDialog = false
                        }
                    },
                    enabled = newCatTitle.isNotBlank()
                ) {
                    Text("ثبت گروه", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickCategoryDialog = false }) {
                    Text("انصراف", fontSize = 11.sp)
                }
            }
        )
    }
}

/**
 * Categories management tab
 */
@Composable
private fun IncomeCategoriesTab(
    categories: List<IncomeCategoryEntity>,
    incomes: List<IncomeEntity>,
    onAddCategory: (title: String, code: String, desc: String) -> Unit,
    onUpdateCategory: (IncomeCategoryEntity) -> Unit,
    onDeleteCategory: (IncomeCategoryEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<IncomeCategoryEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "سرفصل‌های درآمد (${NumberFormatUtils.toPersianDigits(categories.size)} دسته‌بندی)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "طبقه‌بندی درآمدهای متفرقه و غیرعملیاتی شرکت",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    editingCategory = null
                    title = ""
                    code = ""
                    desc = ""
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("گروه جدید", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                val associatedCount = incomes.count { it.categoryId == cat.id }
                val associatedSum = incomes.filter { it.categoryId == cat.id }.sumOf { it.amount }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(AccountingDebitGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(cat.code, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccountingDebitGreen)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cat.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "تعداد اسناد: ${NumberFormatUtils.toPersianDigits(associatedCount)} | مجموع: ${NumberFormatUtils.formatPrice(associatedSum)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (cat.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(cat.description, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    editingCategory = cat
                                    title = cat.title
                                    code = cat.code
                                    desc = cat.description
                                    showDialog = true
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { onDeleteCategory(cat) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingCategory != null) "ویرایش دسته‌بندی درآمد" else "تعریف گروه درآمدی جدید",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان گروه (مانند سود بانکی، خدمات و...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد حسابداری (اختیاری)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("توضیحات تکمیلی") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingCategory != null) {
                            onUpdateCategory(editingCategory!!.copy(title = title, code = code, description = desc))
                        } else {
                            onAddCategory(title, code, desc)
                        }
                        showDialog = false
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("ذخیره گروه", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("انصراف", fontSize = 11.sp)
                }
            }
        )
    }
}

/**
 * Analytics and Reports with Category breakdown & PDF/Excel Export
 */
@Composable
private fun IncomeAnalyticsTab(
    incomes: List<IncomeEntity>,
    viewModel: ExpenseAndIncomeViewModel
) {
    val context = LocalContext.current
    val summaries = remember(incomes) { viewModel.getIncomeSummariesByCategory(incomes) }
    val totalIncome = incomes.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AccountingDebitGreen.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, AccountingDebitGreen.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("مجموع کل درآمدهای متفرقه دوره:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${NumberFormatUtils.formatPrice(totalIncome)}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = AccountingDebitGreen)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "معادل: ${NumberFormatUtils.numberToPersianWords(totalIncome)} تومان",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Export Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.exportIncomesPdf(context, incomes) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("خروجی PDF رسمی", fontSize = 11.sp)
            }

            Button(
                onClick = { viewModel.exportIncomesCsv(context, incomes) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("خروجی اکسل (CSV)", fontSize = 11.sp)
            }
        }

        Text(
            text = "تفکیک و سهم درصدی گروه‌های درآمدی:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        if (summaries.isEmpty()) {
            Text("هیچ داده‌ای برای نمایش تحلیل وجود ندارد.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        } else {
            summaries.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = "${NumberFormatUtils.toPersianDigits(String.format("%.1f", item.percentage))}٪  (${NumberFormatUtils.formatPrice(item.totalAmount)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AccountingDebitGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { item.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AccountingDebitGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تعداد اسناد: ${NumberFormatUtils.toPersianDigits(item.count.toString())} سند",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed Voucher Receipt Dialog for Income
 */
@Composable
private fun IncomeDetailVoucherDialog(
    item: IncomeEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("رسید رسمی دریافت درآمد", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("مدیریت حسابداری گلی", fontSize = 10.sp, color = PersianGold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("شماره: #${NumberFormatUtils.toPersianDigits(item.id.toString())}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                            Text("تاریخ: ${NumberFormatUtils.toPersianDigits(item.jalaliDate)}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                // Details Table
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        VoucherRow("عنوان و بابت درآمد:", item.title)
                        VoucherRow("دسته‌بندی و سرفصل:", item.categoryTitle)
                        VoucherRow("روش واریز وجه:", if (item.depositMethod == "CASH") "دریافت نقدی در صندوق" else "واریز مستقیم به حساب بانکی")
                        VoucherRow("حساب / صندوق مقصد:", item.accountName)
                        if (item.notes.isNotBlank()) {
                            VoucherRow("توضیحات و پیگیری:", item.notes)
                        }
                    }
                }

                // Amount Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccountingDebitGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مبلغ وصول شده:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = "+${NumberFormatUtils.formatPrice(item.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = AccountingDebitGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "به حروف: ${NumberFormatUtils.numberToPersianWords(item.amount)} تومان",
                            fontSize = 10.sp,
                            color = AccountingDebitGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بستن رسید", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun VoucherRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
