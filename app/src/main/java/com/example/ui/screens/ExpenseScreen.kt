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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
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
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ExpenseCategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.ExpenseAndIncomeViewModel
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: ExpenseAndIncomeViewModel
) {
    val context = LocalContext.current

    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val cashDesks by viewModel.cashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val editingExpense by viewModel.editingExpense.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "لیست هزینه‌ها",
        if (editingExpense != null) "ویرایش هزینه" else "ثبت هزینه جدید",
        "سرفصل‌ها",
        "گزارش و نمودار"
    )

    LaunchedEffect(alertMessage) {
        alertMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearAlertMessage()
        }
    }

    Scaffold(
        topBar = {
            ErpTopAppBar(
                title = "مدیریت هزینه‌ها و مخارج",
                subtitle = "سرفصل‌ها، ثبت اسناد، تفکیک صندوق/بانک و تحلیل هزینه‌ها"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> ExpenseListTab(
                    expenses = expenses,
                    categories = categories,
                    onEditExpense = {
                        viewModel.setEditingExpense(it)
                        selectedTab = 1
                    },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onNavigateToNew = {
                        viewModel.setEditingExpense(null)
                        selectedTab = 1
                    }
                )
                1 -> ExpenseFormTab(
                    editingExpense = editingExpense,
                    categories = categories,
                    cashDesks = cashDesks,
                    bankAccounts = bankAccounts,
                    onCancelEditing = {
                        viewModel.setEditingExpense(null)
                        selectedTab = 0
                    },
                    onSave = { title, amount, cat, method, cash, bank, date, notes ->
                        viewModel.saveExpense(title, amount, cat, method, cash, bank, date, notes) {
                            selectedTab = 0
                        }
                    }
                )
                2 -> ExpenseCategoriesTab(
                    categories = categories,
                    onAddCategory = { title, code, desc -> viewModel.addExpenseCategory(title, code, desc) },
                    onUpdateCategory = { viewModel.updateExpenseCategory(it) },
                    onDeleteCategory = { viewModel.deleteExpenseCategory(it) }
                )
                3 -> ExpenseAnalyticsTab(
                    expenses = expenses,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun ExpenseListTab(
    expenses: List<ExpenseEntity>,
    categories: List<ExpenseCategoryEntity>,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onNavigateToNew: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var deletingItem by remember { mutableStateOf<ExpenseEntity?>(null) }

    val filtered = expenses.filter {
        (searchQuery.isBlank() || it.title.contains(searchQuery) || it.notes.contains(searchQuery) || it.categoryTitle.contains(searchQuery)) &&
        (selectedCategoryId == null || it.categoryId == selectedCategoryId)
    }

    val totalSum = filtered.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Summary & Action Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مجموع هزینه‌های منتخب:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = NumberFormatUtils.formatPrice(totalSum),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = AccountingCreditRed)
                    )
                    Text(
                        text = "تعداد اسناد: ${NumberFormatUtils.toPersianDigits(filtered.size.toString())} سند",
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onNavigateToNew,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("add_expense_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ثبت هزینه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو در عنوان، شرح یا سرفصل هزینه...", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (filtered.isEmpty()) {
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
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("هیچ سند هزینه‌ای یافت نشد.", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(AccountingCreditRed.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.categoryTitle,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccountingCreditRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = NumberFormatUtils.formatPrice(item.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AccountingCreditRed
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "پرداخت: ${if (item.paymentMethod == "CASH") "صندوق" else "بانک"} (${item.accountName}) | تاریخ: ${item.jalaliDate}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row {
                                    IconButton(
                                        onClick = { onEditExpense(item) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { deletingItem = item },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (item.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "توضیحات: ${item.notes}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("حذف سند هزینه", fontWeight = FontWeight.Bold) },
            text = {
                Text("آیا از حذف سند هزینه «${item.title}» به مبلغ ${NumberFormatUtils.formatPrice(item.amount)} و برگشت وجه به ${item.accountName} اطمینان دارید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(item)
                        deletingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("حذف و برگشت وجه")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseFormTab(
    editingExpense: ExpenseEntity?,
    categories: List<ExpenseCategoryEntity>,
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    onCancelEditing: () -> Unit,
    onSave: (
        title: String,
        amount: Long,
        category: ExpenseCategoryEntity,
        paymentMethod: String,
        cashDesk: CashDeskEntity?,
        bankAccount: BankAccountEntity?,
        jalaliDate: String,
        notes: String
    ) -> Unit
) {
    var title by remember(editingExpense) { mutableStateOf(editingExpense?.title ?: "") }
    var amountStr by remember(editingExpense) { mutableStateOf(editingExpense?.amount?.toString() ?: "") }
    var selectedCategory by remember(editingExpense, categories) {
        mutableStateOf(
            categories.find { it.id == editingExpense?.categoryId } ?: categories.firstOrNull()
        )
    }
    var paymentMethod by remember(editingExpense) { mutableStateOf(editingExpense?.paymentMethod ?: "CASH") }
    var selectedCashDesk by remember(editingExpense, cashDesks) {
        mutableStateOf(
            cashDesks.find { it.id == editingExpense?.cashDeskId } ?: cashDesks.firstOrNull()
        )
    }
    var selectedBankAccount by remember(editingExpense, bankAccounts) {
        mutableStateOf(
            bankAccounts.find { it.id == editingExpense?.bankAccountId } ?: bankAccounts.firstOrNull()
        )
    }
    var jalaliDate by remember(editingExpense) {
        mutableStateOf(editingExpense?.jalaliDate ?: JalaliDate.now().toStandardString())
    }
    var notes by remember(editingExpense) { mutableStateOf(editingExpense?.notes ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    val amountLong = amountStr.toLongOrNull() ?: 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (editingExpense != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PersianGold.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "در حال ویرایش سند هزینه شماره #${editingExpense.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    OutlinedButton(onClick = onCancelEditing) {
                        Text("انصراف و حالت جدید", fontSize = 11.sp)
                    }
                }
            }
        }

        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("بابت / عنوان هزینه (الزامی)") },
            placeholder = { Text("مثال: پرداخت اجاره فروردین دفتر، خرید کارتن و...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("expense_title_input"),
            shape = RoundedCornerShape(10.dp)
        )

        // Amount Field with Live Words
        OutlinedTextField(
            value = amountStr,
            onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
            label = { Text("مبلغ هزینه به تومان (الزامی)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
            shape = RoundedCornerShape(10.dp)
        )

        if (amountLong > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "معادل: ${NumberFormatUtils.numberToPersianWords(amountLong)} تومان",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Category Selector
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory?.title ?: "انتخاب سرفصل هزینه...",
                onValueChange = {},
                readOnly = true,
                label = { Text("سرفصل هزینه") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
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

        // Payment Method Toggle (Cash vs Bank)
        Text("نحوه پرداخت وجه:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = paymentMethod == "CASH",
                onClick = { paymentMethod = "CASH" },
                label = { Text("پرداخت از صندوق (دخل)", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = paymentMethod == "BANK",
                onClick = { paymentMethod = "BANK" },
                label = { Text("پرداخت از حساب بانکی", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        // Source Account Selector (Cash Desk or Bank Account)
        if (paymentMethod == "CASH") {
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCashDesk?.name ?: "انتخاب صندوق...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("صندوق مبدا پرداخت") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    cashDesks.forEach { desk ->
                        DropdownMenuItem(
                            text = { Text("${desk.name} (مانده: ${NumberFormatUtils.formatPrice(desk.currentBalance)})") },
                            onClick = {
                                selectedCashDesk = desk
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
                    value = selectedBankAccount?.let { "${it.bankName} - ${it.accountNumber}" } ?: "انتخاب حساب بانکی...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("حساب بانکی مبدا پرداخت") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    bankAccounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text("${acc.bankName} - ${acc.accountNumber} (مانده: ${NumberFormatUtils.formatPrice(acc.currentBalance)})") },
                            onClick = {
                                selectedBankAccount = acc
                                accountExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Date Field
        OutlinedTextField(
            value = jalaliDate,
            onValueChange = { jalaliDate = it },
            label = { Text("تاریخ سند (هجری شمسی)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        // Notes Field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("توضیحات تکمیلی و مشخصات فاکتور هزینه (اختیاری)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Save Button
        Button(
            onClick = {
                selectedCategory?.let { cat ->
                    onSave(
                        title,
                        amountLong,
                        cat,
                        paymentMethod,
                        selectedCashDesk,
                        selectedBankAccount,
                        jalaliDate,
                        notes
                    )
                }
            },
            enabled = title.isNotBlank() && amountLong > 0 && selectedCategory != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_expense_submit_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (editingExpense != null) "ذخیره تغییرات سند هزینه" else "ثبت نهایی و صدور سند هزینه",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExpenseCategoriesTab(
    categories: List<ExpenseCategoryEntity>,
    onAddCategory: (title: String, code: String, desc: String) -> Unit,
    onUpdateCategory: (ExpenseCategoryEntity) -> Unit,
    onDeleteCategory: (ExpenseCategoryEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<ExpenseCategoryEntity?>(null) }
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
            Text(
                text = "سرفصل‌های هزینه‌های عمومی (${categories.size} سرفصل)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Button(
                onClick = {
                    editingCategory = null
                    title = ""
                    code = ""
                    desc = ""
                    showDialog = true
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("سرفصل جدید", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(cat.code, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cat.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            if (cat.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(cat.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            title = { Text(if (editingCategory != null) "ویرایش سرفصل هزینه" else "تعریف سرفصل هزینه جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان سرفصل (مانند اجاره، حقوق و...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد سرفصل (اختیاری، مثلا EXP-10)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("توضیحات و مصارف سرفصل") },
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
                    Text("ذخیره سرفصل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun ExpenseAnalyticsTab(
    expenses: List<ExpenseEntity>,
    viewModel: ExpenseAndIncomeViewModel
) {
    val context = LocalContext.current
    val summaries = remember(expenses) { viewModel.getExpenseSummariesByCategory(expenses) }
    val totalExpense = expenses.sumOf { it.amount }

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
            colors = CardDefaults.cardColors(containerColor = AccountingCreditRed.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("مجموع کل هزینه‌های دوره:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${NumberFormatUtils.formatPrice(totalExpense)} تومان",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = AccountingCreditRed)
                )
                Text(
                    text = "معادل: ${NumberFormatUtils.numberToPersianWords(totalExpense)} تومان",
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
                onClick = { viewModel.exportExpensesPdf(context, expenses) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("خروجی PDF", fontSize = 11.sp)
            }

            Button(
                onClick = { viewModel.exportExpensesCsv(context, expenses) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("خروجی اکسل", fontSize = 11.sp)
            }
        }

        Text(
            text = "تحلیل و سهم هر سرفصل از کل هزینه‌ها:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        summaries.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            color = AccountingCreditRed
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = AccountingCreditRed,
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
