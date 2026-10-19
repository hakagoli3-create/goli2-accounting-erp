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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanCard
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.MatteGoldOnContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.TransactionType
import com.example.ui.viewmodel.TreasuryDateFilter
import com.example.ui.viewmodel.TreasuryTransaction
import com.example.ui.viewmodel.TreasuryViewModel
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils

/**
 * صفحه مدیریت خزانه‌داری، بانک و نقدینگی (TreasuryScreen)
 * طراحی بازطراحی‌شده: حرفه‌ای، کاملاً فارسی و راست‌چین
 * پالت هویت بصری: سرمه‌ای (#171C2A)، طلایی مات (#C6A15B) و پس‌زمینه پاک (#F5F6F8)
 * شامل موجودی تفکیکی صندوق‌ها و بانک‌ها، انتقال وجه، ثبت دریافت/پرداخت، گردش حساب و فهرست تراکنش‌ها با فیلتر تاریخ شمسی
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryScreen(
    viewModel: TreasuryViewModel
) {
    val context = LocalContext.current
    val securityManager = remember { AppSecurityAndSettingsManager.getInstance(context) }
    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("نقدینگی و حساب‌ها", "گردش حساب و تراکنش‌ها", "چک‌های صیادی", "عملیات و گزارش‌ها")

    val cheques by viewModel.allCheques.collectAsStateWithLifecycle()
    val cashDesks by viewModel.allCashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.allBankAccounts.collectAsStateWithLifecycle()
    val totalCash by viewModel.totalCashBalance.collectAsStateWithLifecycle()
    val totalBank by viewModel.totalBankBalance.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // تراکنش‌ها و فیلترهای تاریخ شمسی
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val transactionSearchQuery by viewModel.transactionSearchQuery.collectAsStateWithLifecycle()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()

    var showAddChequeDialog by remember { mutableStateOf(false) }
    var showAddCashDeskDialog by remember { mutableStateOf(false) }
    var showAddBankDialog by remember { mutableStateOf(false) }
    var editingCashDesk by remember { mutableStateOf<CashDeskEntity?>(null) }
    var editingBankAccount by remember { mutableStateOf<BankAccountEntity?>(null) }

    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showCapitalDialog by remember { mutableStateOf(false) }

    val safeTotalCash = totalCash ?: 0L
    val safeTotalBank = totalBank ?: 0L
    val totalLiquidity = safeTotalCash + safeTotalBank

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = CleanBackground,
            topBar = {
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
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = MatteGoldOnContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "خزانه‌داری، بانک و نقدینگی",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "سامانه نقدینگی، حساب‌های بانکی، پوز، چک صیادی و انتقال وجه",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    )
                                }
                            }

                            // دکمه سریع انتقال وجه در هدر
                            Button(
                                onClick = { showTransferDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MatteGold,
                                    contentColor = NavyDark
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("header_transfer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = NavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "انتقال وجه",
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
                    0 -> FloatingActionButton(
                        onClick = { showAddBankDialog = true },
                        containerColor = NavyPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_bank_fab")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن حساب", tint = MatteGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حساب جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    1 -> FloatingActionButton(
                        onClick = { showIncomeDialog = true },
                        containerColor = AccountingDebitGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_income_fab")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "دریافت وجه", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("دریافت وجه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    2 -> FloatingActionButton(
                        onClick = { showAddChequeDialog = true },
                        containerColor = NavyPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_cheque_fab")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "ثبت چک صیادی", tint = MatteGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ثبت چک", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                // تب‌های ناوبری مدرن خزانه‌داری
                Surface(
                    color = CleanSurface,
                    shadowElevation = 2.dp
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CleanSurface,
                        contentColor = NavyPrimary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isSelected) NavyPrimary else TextDarkSecondary
                                        )
                                    )
                                },
                                modifier = Modifier.testTag("treasury_tab_$index")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanBackground)
                ) {
                    when (selectedTab) {
                        0 -> LiquidityAndAccountsContent(
                            cashDesks = cashDesks,
                            bankAccounts = bankAccounts,
                            totalCash = safeTotalCash,
                            totalBank = safeTotalBank,
                            totalLiquidity = totalLiquidity,
                            canManage = currentRole.canAccessTreasuryBalances,
                            onOpenIncome = { showIncomeDialog = true },
                            onOpenExpense = { showExpenseDialog = true },
                            onOpenTransfer = { showTransferDialog = true },
                            onOpenCapital = { showCapitalDialog = true },
                            onAddCashDesk = { showAddCashDeskDialog = true },
                            onAddBank = { showAddBankDialog = true },
                            onEditCashDesk = { editingCashDesk = it },
                            onDeleteCashDesk = { viewModel.deleteCashDesk(it) },
                            onEditBank = { editingBankAccount = it },
                            onDeleteBank = { viewModel.deleteBankAccount(it) },
                            onGoToTransactions = { selectedTab = 1 }
                        )
                        1 -> TransactionsAndTurnoverContent(
                            transactions = filteredTransactions,
                            searchQuery = transactionSearchQuery,
                            dateFilter = selectedDateFilter,
                            typeFilter = selectedTypeFilter,
                            onSearchChange = { viewModel.setTransactionSearchQuery(it) },
                            onDateFilterSelect = { viewModel.setDateFilter(it) },
                            onTypeFilterSelect = { viewModel.setTypeFilter(it) },
                            onOpenIncome = { showIncomeDialog = true },
                            onOpenExpense = { showExpenseDialog = true },
                            onOpenTransfer = { showTransferDialog = true }
                        )
                        2 -> ChequesContent(
                            cheques = cheques,
                            onUpdateStatus = { cheque, status -> viewModel.updateChequeStatus(cheque, status) },
                            onDeleteCheque = { cheque -> viewModel.deleteCheque(cheque) },
                            onAddCheque = { showAddChequeDialog = true }
                        )
                        3 -> TreasuryOperationsContent(
                            totalCash = safeTotalCash,
                            totalBank = safeTotalBank,
                            onOpenIncome = { showIncomeDialog = true },
                            onOpenExpense = { showExpenseDialog = true },
                            onOpenTransfer = { showTransferDialog = true },
                            onOpenCapital = { showCapitalDialog = true },
                            onExportPdf = { viewModel.exportTreasuryPdf(context) },
                            onExportCsv = { viewModel.exportTreasuryCsv(context) }
                        )
                    }
                }
            }
        }

        // دیالوگ‌های عملیاتی خزانه‌داری
        if (showAddChequeDialog) {
            AddChequeDialog(
                onDismiss = { showAddChequeDialog = false },
                onConfirm = { number, sayad, type, party, bank, branch, amount, issue, due, notes ->
                    viewModel.addCheque(number, sayad, type, party, bank, branch, amount, issue, due, notes) {
                        showAddChequeDialog = false
                    }
                }
            )
        }

        if (showAddCashDeskDialog) {
            AddOrEditCashDeskDialog(
                existing = null,
                onDismiss = { showAddCashDeskDialog = false },
                onConfirm = { name, code, keeper, balance ->
                    viewModel.addCashDesk(name, code, keeper, balance) {
                        showAddCashDeskDialog = false
                    }
                }
            )
        }

        editingCashDesk?.let { desk ->
            AddOrEditCashDeskDialog(
                existing = desk,
                onDismiss = { editingCashDesk = null },
                onConfirm = { name, code, keeper, balance ->
                    viewModel.updateCashDesk(desk.copy(name = name, code = code, keeperName = keeper, currentBalance = balance)) {
                        editingCashDesk = null
                    }
                }
            )
        }

        if (showAddBankDialog) {
            AddOrEditBankDialog(
                existing = null,
                onDismiss = { showAddBankDialog = false },
                onConfirm = { bankName, accNum, cardNum, sheba, branch, balance, hasPos, posId ->
                    viewModel.addBankAccount(bankName, accNum, cardNum, sheba, branch, balance, hasPos, posId) {
                        showAddBankDialog = false
                    }
                }
            )
        }

        editingBankAccount?.let { bank ->
            AddOrEditBankDialog(
                existing = bank,
                onDismiss = { editingBankAccount = null },
                onConfirm = { bankName, accNum, cardNum, sheba, branch, balance, hasPos, posId ->
                    viewModel.updateBankAccount(
                        bank.copy(
                            bankName = bankName,
                            accountNumber = accNum,
                            cardNumber = cardNum,
                            shebaNumber = sheba,
                            branch = branch,
                            currentBalance = balance,
                            hasPos = hasPos,
                            posTerminalId = posId
                        )
                    ) {
                        editingBankAccount = null
                    }
                }
            )
        }

        if (showIncomeDialog) {
            TreasuryActionDialog(
                title = "ثبت دریافت وجه / درآمد جدید",
                actionLabel = "ثبت دریافت و واریز وجه",
                cashDesks = cashDesks,
                bankAccounts = bankAccounts,
                categories = listOf("فروش خدمات و متفرقه", "سود سپرده بانکی", "تسویه دستی مشتری", "سایر درآمدهای عملیاتی"),
                onDismiss = { showIncomeDialog = false },
                onConfirm = { title, amount, destType, targetId, category, notes ->
                    viewModel.registerIncome(title, amount, destType, targetId, category, notes) {
                        showIncomeDialog = false
                    }
                }
            )
        }

        if (showExpenseDialog) {
            TreasuryActionDialog(
                title = "ثبت پرداخت هزینه جدید",
                actionLabel = "پرداخت هزینه و خروج وجه",
                cashDesks = cashDesks,
                bankAccounts = bankAccounts,
                categories = listOf("حقوق و دستمزد پرسنل", "کرایه و حمل کالا", "ملزومات و قبوض اداری", "بسته‌بندی و تبلیغات", "تعمیرات و نگهداری"),
                onDismiss = { showExpenseDialog = false },
                onConfirm = { title, amount, srcType, sourceId, category, notes ->
                    viewModel.registerExpense(title, amount, srcType, sourceId, category, notes) {
                        showExpenseDialog = false
                    }
                }
            )
        }

        if (showTransferDialog) {
            TransferFundsDialog(
                cashDesks = cashDesks,
                bankAccounts = bankAccounts,
                onDismiss = { showTransferDialog = false },
                onConfirm = { fromType, fromId, toType, toId, amount, notes ->
                    viewModel.transferFunds(fromType, fromId, toType, toId, amount, notes) {
                        showTransferDialog = false
                    }
                }
            )
        }

        if (showCapitalDialog) {
            CapitalInvestmentDialog(
                cashDesks = cashDesks,
                bankAccounts = bankAccounts,
                onDismiss = { showCapitalDialog = false },
                onConfirm = { investor, amount, destType, targetId ->
                    viewModel.registerCapitalInvestment(investor, amount, destType, targetId) {
                        showCapitalDialog = false
                    }
                }
            )
        }
    }
}

/**
 * تب ۱: نقدینگی، حساب‌های بانکی و صندوق‌ها
 */
@Composable
private fun LiquidityAndAccountsContent(
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    totalCash: Long,
    totalBank: Long,
    totalLiquidity: Long,
    canManage: Boolean,
    onOpenIncome: () -> Unit,
    onOpenExpense: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenCapital: () -> Unit,
    onAddCashDesk: () -> Unit,
    onAddBank: () -> Unit,
    onEditCashDesk: (CashDeskEntity) -> Unit,
    onDeleteCashDesk: (CashDeskEntity) -> Unit,
    onEditBank: (BankAccountEntity) -> Unit,
    onDeleteBank: (BankAccountEntity) -> Unit,
    onGoToTransactions: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        // ۱. کارت جامع نقدینگی و موجودی کل (سرمه‌ای و طلایی)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NavyPrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "کل نقدینگی در دسترس (صندوق و بانک):",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(totalLiquidity)),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MatteGold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تومان",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MatteGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MatteGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    // تفکیک بانک و صندوق
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "موجودی حساب‌های بانکی:",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(totalBank))} تومان",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "موجودی صندوق‌های نقدی:",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(totalCash))} تومان",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccountingDebitGreen
                            )
                        }
                    }
                }
            }
        }

        // ۲. دکمه‌های عملیاتی بسیار واضح و کاربردی (دریافت، پرداخت، انتقال، گردش)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CleanCard,
                border = BorderStroke(1.dp, CleanBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "عملیات سریع خزانه‌داری",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenIncome,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_income_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("دریافت وجه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOpenExpense,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_expense_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پرداخت هزینه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenTransfer,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MatteGold),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_transfer_btn")
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("انتقال بین حساب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }

                        OutlinedButton(
                            onClick = onGoToTransactions,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CleanBorder),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_turnover_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("گردش حساب‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                        }
                    }
                }
            }
        }

        // ۳. سربرگ حساب‌های بانکی + دکمه افزودن
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حساب‌های بانکی و پوز (${NumberFormatUtils.toPersianDigits(bankAccounts.size)})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }

                TextButton(onClick = onAddBank) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MatteGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حساب بانکی جدید", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // کارت‌های حساب بانکی
        if (bankAccounts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("هیچ حساب بانکی ثبت نشده است.", color = TextDarkSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(bankAccounts, key = { it.id }) { acc ->
                BankAccountCardItem(
                    account = acc,
                    canManage = canManage,
                    onEdit = { onEditBank(acc) },
                    onDelete = { onDeleteBank(acc) }
                )
            }
        }

        // ۴. سربرگ صندوق‌های نقدی + دکمه افزودن
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "صندوق‌های نقدی و دخل (${NumberFormatUtils.toPersianDigits(cashDesks.size)})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }

                TextButton(onClick = onAddCashDesk) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MatteGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صندوق جدید", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // کارت‌های صندوق
        if (cashDesks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("هیچ صندوق نقدی ثبت نشده است.", color = TextDarkSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(cashDesks, key = { it.id }) { desk ->
                CashDeskCardItem(
                    desk = desk,
                    canManage = canManage,
                    onEdit = { onEditCashDesk(desk) },
                    onDelete = { onDeleteCashDesk(desk) }
                )
            }
        }
    }
}

/**
 * تب ۲: گردش حساب و فهرست تراکنش‌ها با فیلتر تاریخ شمسی
 */
@Composable
private fun TransactionsAndTurnoverContent(
    transactions: List<TreasuryTransaction>,
    searchQuery: String,
    dateFilter: TreasuryDateFilter,
    typeFilter: TransactionType?,
    onSearchChange: (String) -> Unit,
    onDateFilterSelect: (TreasuryDateFilter) -> Unit,
    onTypeFilterSelect: (TransactionType?) -> Unit,
    onOpenIncome: () -> Unit,
    onOpenExpense: () -> Unit,
    onOpenTransfer: () -> Unit
) {
    val totalInflow = remember(transactions) {
        transactions.filter { it.type == TransactionType.INFLOW }.sumOf { it.amount }
    }
    val totalOutflow = remember(transactions) {
        transactions.filter { it.type == TransactionType.OUTFLOW }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        // ۱. خلاصه‌ وضعیت بازه انتخابی (جمع واریزها و برداشت‌ها)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مجموع دریافتی/واریز", fontSize = 11.sp, color = TextDarkSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(totalInflow))} تومان",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccountingDebitGreen
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مجموع پرداختی/هزینه", fontSize = 11.sp, color = TextDarkSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(totalOutflow))} تومان",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccountingCreditRed
                        )
                    }
                }
            }
        }

        // ۲. باکس جستجو
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("جستجوی تراکنش، عنوان، حساب یا شماره سند...", fontSize = 13.sp, color = TextDarkMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MatteGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
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
                    .testTag("search_transactions_input")
            )
        }

        // ۳. فیلتر تاریخ شمسی
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("بازه زمانی شمسی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TreasuryDateFilter.values()) { filter ->
                    val isSelected = dateFilter == filter
                    Surface(
                        onClick = { onDateFilterSelect(filter) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) NavyPrimary else CleanCard,
                        border = BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanBorder)
                    ) {
                        Text(
                            text = filter.title,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color = if (isSelected) Color.White else TextDarkPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // ۴. فیلتر نوع تراکنش
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { onTypeFilterSelect(null) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (typeFilter == null) MatteGoldContainer else CleanCard,
                    border = BorderStroke(1.dp, if (typeFilter == null) MatteGold else CleanBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "همه (${NumberFormatUtils.toPersianDigits(transactions.size)})",
                            fontSize = 11.sp,
                            fontWeight = if (typeFilter == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (typeFilter == null) MatteGoldOnContainer else TextDarkPrimary
                        )
                    }
                }

                Surface(
                    onClick = { onTypeFilterSelect(TransactionType.INFLOW) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (typeFilter == TransactionType.INFLOW) AccountingDebitGreen.copy(alpha = 0.12f) else CleanCard,
                    border = BorderStroke(1.dp, if (typeFilter == TransactionType.INFLOW) AccountingDebitGreen else CleanBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "دریافتی/واریز",
                            fontSize = 11.sp,
                            fontWeight = if (typeFilter == TransactionType.INFLOW) FontWeight.Bold else FontWeight.Normal,
                            color = if (typeFilter == TransactionType.INFLOW) AccountingDebitGreen else TextDarkPrimary
                        )
                    }
                }

                Surface(
                    onClick = { onTypeFilterSelect(TransactionType.OUTFLOW) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (typeFilter == TransactionType.OUTFLOW) AccountingCreditRed.copy(alpha = 0.12f) else CleanCard,
                    border = BorderStroke(1.dp, if (typeFilter == TransactionType.OUTFLOW) AccountingCreditRed else CleanBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "پرداختی/هزینه",
                            fontSize = 11.sp,
                            fontWeight = if (typeFilter == TransactionType.OUTFLOW) FontWeight.Bold else FontWeight.Normal,
                            color = if (typeFilter == TransactionType.OUTFLOW) AccountingCreditRed else TextDarkPrimary
                        )
                    }
                }

                Surface(
                    onClick = { onTypeFilterSelect(TransactionType.TRANSFER) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (typeFilter == TransactionType.TRANSFER) NavyPrimary.copy(alpha = 0.12f) else CleanCard,
                    border = BorderStroke(1.dp, if (typeFilter == TransactionType.TRANSFER) NavyPrimary else CleanBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "انتقال وجه",
                            fontSize = 11.sp,
                            fontWeight = if (typeFilter == TransactionType.TRANSFER) FontWeight.Bold else FontWeight.Normal,
                            color = if (typeFilter == TransactionType.TRANSFER) NavyPrimary else TextDarkPrimary
                        )
                    }
                }
            }
        }

        // ۵. فهرست تراکنش‌ها
        if (transactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextDarkMuted, modifier = Modifier.size(38.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "تراکنشی با مشخصات «$searchQuery» یافت نشد." else "تراکنشی در این بازه زمانی ثبت نشده است.",
                                color = TextDarkSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionCardItem(transaction = tx)
            }
        }
    }
}

/**
 * آیتم تراکنش در گردش حساب
 */
@Composable
private fun TransactionCardItem(transaction: TreasuryTransaction) {
    val isIncome = transaction.type == TransactionType.INFLOW
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val accentColor = when (transaction.type) {
        TransactionType.INFLOW -> AccountingDebitGreen
        TransactionType.OUTFLOW -> AccountingCreditRed
        TransactionType.TRANSFER -> NavyPrimary
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CleanCard,
        border = BorderStroke(1.dp, CleanBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // آیکون نوع تراکنش
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (transaction.type) {
                                TransactionType.INFLOW -> Icons.Default.ArrowDownward
                                TransactionType.OUTFLOW -> Icons.Default.ArrowUpward
                                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                            },
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.accountName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${transaction.category}",
                            fontSize = 10.sp,
                            color = TextDarkSecondary
                        )
                        if (transaction.referenceNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${NumberFormatUtils.toPersianDigits(transaction.referenceNumber)}",
                                fontSize = 10.sp,
                                color = TextDarkMuted
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (transaction.amount > 0) {
                    Text(
                        text = (if (isIncome) "+" else "-") + NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(transaction.amount)) + " تومان",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            fontSize = 13.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = NumberFormatUtils.toPersianDigits(transaction.jalaliDate),
                    fontSize = 10.sp,
                    color = TextDarkSecondary
                )
            }
        }
    }
}

/**
 * کارت مدرن حساب بانکی
 */
@Composable
private fun BankAccountCardItem(
    account: BankAccountEntity,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CleanCard,
        border = BorderStroke(1.dp, CleanBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("bank_card_${account.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MatteGoldContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MatteGoldOnContainer, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = account.bankName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary
                            )
                        )
                        if (account.branch.isNotBlank()) {
                            Text(
                                text = "شعبه: ${account.branch}",
                                fontSize = 11.sp,
                                color = TextDarkSecondary
                            )
                        }
                    }
                }

                if (account.hasPos) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccountingDebitGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AccountingDebitGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "دارای پوز",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccountingDebitGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // مشخصات حساب و کارت
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (account.cardNumber.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = TextDarkMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "شماره کارت: ${NumberFormatUtils.toPersianDigits(account.cardNumber)}",
                            fontSize = 11.sp,
                            color = TextDarkPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (account.shebaNumber.isNotBlank()) {
                    Text(
                        text = "شبا: ${NumberFormatUtils.toPersianDigits(account.shebaNumber)}",
                        fontSize = 10.sp,
                        color = TextDarkSecondary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CleanBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "موجودی حساب:", fontSize = 10.sp, color = TextDarkSecondary)
                    Text(
                        text = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(account.currentBalance))} تومان",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyPrimary,
                            fontSize = 15.sp
                        )
                    )
                }

                if (canManage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * کارت مدرن صندوق نقدی
 */
@Composable
private fun CashDeskCardItem(
    desk: CashDeskEntity,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CleanCard,
        border = BorderStroke(1.dp, CleanBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("cashdesk_card_${desk.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccountingDebitGreen.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = desk.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary
                            )
                        )
                        Text(
                            text = "کد: ${NumberFormatUtils.toPersianDigits(desk.code)} | مسئول: ${desk.keeperName.ifBlank { "ثبت نشده" }}",
                            fontSize = 11.sp,
                            color = TextDarkSecondary
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CleanBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "موجودی صندوق نقدی:", fontSize = 10.sp, color = TextDarkSecondary)
                    Text(
                        text = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(desk.currentBalance))} تومان",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = AccountingDebitGreen,
                            fontSize = 15.sp
                        )
                    )
                }

                if (canManage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * تب ۳: چک‌های صیادی
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChequesContent(
    cheques: List<ChequeEntity>,
    onUpdateStatus: (ChequeEntity, String) -> Unit,
    onDeleteCheque: (ChequeEntity) -> Unit = {},
    onAddCheque: () -> Unit
) {
    var filterType by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val displayedCheques = cheques.filter { ch ->
        val q = searchQuery.trim()
        val matchesType = when (filterType) {
            "RECEIVABLE" -> ch.type == "RECEIVABLE"
            "PAYABLE" -> ch.type == "PAYABLE"
            "DUE_ALERTS" -> ch.status == "NORMAL" && (ch.isOverdue() || ch.isDueToday() || ch.daysUntilDue() in 0..7)
            else -> true
        }
        val matchesSearch = q.isBlank() ||
                ch.chequeNumber.contains(q, ignoreCase = true) ||
                ch.sayadNumber.contains(q, ignoreCase = true) ||
                ch.partyName.contains(q, ignoreCase = true) ||
                ch.bankName.contains(q, ignoreCase = true) ||
                ch.branch.contains(q, ignoreCase = true) ||
                ch.notes.contains(q, ignoreCase = true) ||
                NumberFormatUtils.toPersianDigits(ch.chequeNumber).contains(q) ||
                NumberFormatUtils.toPersianDigits(ch.sayadNumber).contains(q)

        matchesType && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی شماره چک، شناسه صیادی، طرف‌حساب، نام بانک...", fontSize = 13.sp, color = TextDarkMuted) },
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
                    .testTag("search_cheques_input")
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("همه (${NumberFormatUtils.toPersianDigits(cheques.size)})", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = filterType == "RECEIVABLE",
                    onClick = { filterType = "RECEIVABLE" },
                    label = { Text("دریافتی", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = filterType == "PAYABLE",
                    onClick = { filterType = "PAYABLE" },
                    label = { Text("پرداختی", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = filterType == "DUE_ALERTS",
                    onClick = { filterType = "DUE_ALERTS" },
                    label = { Text("هشدار سررسید", fontSize = 11.sp) },
                    modifier = Modifier.weight(1.2f)
                )
            }
        }

        if (displayedCheques.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CleanCard,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "هیچ چکی با مشخصات «$searchQuery» یافت نشد." else "هیچ چکی با این فیلتر ثبت نشده است.",
                            color = TextDarkSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(displayedCheques) { cheque ->
                var menuExpanded by remember { mutableStateOf(false) }

                val isOverdue = cheque.isOverdue()
                val isDueToday = cheque.isDueToday()
                val daysUntil = cheque.daysUntilDue()

                val cardBg = when {
                    cheque.status == "PASSED" -> Color(0xFFF1F8E9)
                    cheque.status == "BOUNCED" -> Color(0xFFFFEBEE)
                    isOverdue -> Color(0xFFFFEBEE)
                    isDueToday || daysUntil in 0..3 -> Color(0xFFFFF3E0)
                    else -> CleanCard
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(
                        1.dp,
                        if (isOverdue || cheque.status == "BOUNCED") AccountingCreditRed.copy(alpha = 0.4f) else CleanBorder
                    ),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val tagBg = if (cheque.type == "RECEIVABLE") AccountingDebitGreen else MatteGold
                                Text(
                                    text = if (cheque.type == "RECEIVABLE") "دریافتی" else "پرداختی",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(tagBg, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${cheque.bankName} (${NumberFormatUtils.toPersianDigits(cheque.chequeNumber)})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                                )
                            }

                            Box {
                                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "عملیات")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("وصول شد (پاس شد)") },
                                        onClick = {
                                            onUpdateStatus(cheque, "PASSED")
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("برگشت خورد") },
                                        onClick = {
                                            onUpdateStatus(cheque, "BOUNCED")
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("در جریان وصول (عادی)") },
                                        onClick = {
                                            onUpdateStatus(cheque, "NORMAL")
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("حذف چک", color = AccountingCreditRed) },
                                        onClick = {
                                            onDeleteCheque(cheque)
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "طرف حساب: ${cheque.partyName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkPrimary
                        )

                        if (cheque.sayadNumber.isNotBlank()) {
                            Text(
                                text = "شناسه صیادی: ${NumberFormatUtils.toPersianDigits(cheque.sayadNumber)}",
                                fontSize = 11.sp,
                                color = TextDarkSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "مبلغ چک:", fontSize = 10.sp, color = TextDarkSecondary)
                                Text(
                                    text = "${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(cheque.amount))} تومان",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NavyPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "سررسید: ${NumberFormatUtils.toPersianDigits(cheque.dueDateJalali)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverdue) AccountingCreditRed else TextDarkPrimary
                                )
                                Text(
                                    text = when {
                                        cheque.status == "PASSED" -> "وصول شده"
                                        cheque.status == "BOUNCED" -> "برگشت خورده"
                                        isOverdue -> "سررسید گذشته!"
                                        isDueToday -> "امروز سررسید است"
                                        daysUntil in 1..7 -> "$daysUntil روز مانده"
                                        else -> "عادی"
                                    },
                                    fontSize = 10.sp,
                                    color = if (isOverdue || cheque.status == "BOUNCED") AccountingCreditRed else TextDarkSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * تب ۴: عملیات تکمیلی، سرمایه و خروجی‌ها
 */
@Composable
private fun TreasuryOperationsContent(
    totalCash: Long,
    totalBank: Long,
    onOpenIncome: () -> Unit,
    onOpenExpense: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenCapital: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CleanCard,
            border = BorderStroke(1.dp, CleanBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "عملیات نقدی و دریافت/پرداخت خزانه",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenIncome,
                        modifier = Modifier.weight(1f).testTag("treasury_income_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت درآمد", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenExpense,
                        modifier = Modifier.weight(1f).testTag("treasury_expense_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت هزینه", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenTransfer,
                        modifier = Modifier.weight(1f).testTag("treasury_transfer_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = MatteGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("انتقال وجه", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenCapital,
                        modifier = Modifier.weight(1f).testTag("treasury_capital_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MatteGold, contentColor = NavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = NavyDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("سرمایه شرکا", fontWeight = FontWeight.Bold, color = NavyDark)
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CleanCard,
            border = BorderStroke(1.dp, CleanBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "خروجی چندگانه گزارش خزانه و نقدینگی",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تولید فایل رسمی PDF و اکسل (CSV) با سربرگ استاندارد و امکان اشتراک‌گذاری",
                    fontSize = 11.sp,
                    color = TextDarkSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportPdf,
                        modifier = Modifier.weight(1f).testTag("export_pdf_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CleanBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = AccountingCreditRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خروجی PDF", color = TextDarkPrimary, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onExportCsv,
                        modifier = Modifier.weight(1f).testTag("export_csv_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CleanBorder)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = AccountingDebitGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خروجی اکسل", color = TextDarkPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------- دیالوگ‌های کاربردی خزانه‌داری -----------------

@Composable
private fun AddChequeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        number: String,
        sayad: String,
        type: String,
        party: String,
        bank: String,
        branch: String,
        amount: Long,
        issue: String,
        due: String,
        notes: String
    ) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var sayad by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("RECEIVABLE") }
    var party by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf(JalaliDate.now().toStandardString()) }
    var dueDate by remember { mutableStateOf(JalaliDate.now().addDays(30).toStandardString()) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ثبت چک صیادی جدید", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "RECEIVABLE",
                        onClick = { type = "RECEIVABLE" },
                        label = { Text("چک دریافتی (از مشتری)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "PAYABLE",
                        onClick = { type = "PAYABLE" },
                        label = { Text("چک پرداختی (به تامین‌کننده)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = party,
                    onValueChange = { party = it },
                    label = { Text("طرف حساب / صادرکننده *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("شماره چک *") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sayad,
                        onValueChange = { sayad = it },
                        label = { Text("شناسه صیادی (۱۶ رقم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bank,
                        onValueChange = { bank = it },
                        label = { Text("نام بانک *") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("شعبه") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ به تومان *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = issueDate,
                        onValueChange = { issueDate = it },
                        label = { Text("تاریخ صدور") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("تاریخ سررسید *") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و بابت") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 0L
                        if (amount <= 0 || party.isBlank() || bank.isBlank() || number.isBlank()) return@Button
                        onConfirm(number, sayad, type, party, bank, branch, amount, issueDate, dueDate, notes)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("ثبت قطعی چک صیادی", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddOrEditCashDeskDialog(
    existing: CashDeskEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, keeper: String, balance: Long) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var code by remember { mutableStateOf(existing?.code ?: "") }
    var keeper by remember { mutableStateOf(existing?.keeperName ?: "") }
    var balanceText by remember { mutableStateOf(existing?.currentBalance?.toString() ?: "0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CleanCard,
            border = BorderStroke(1.dp, CleanBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (existing == null) "تعریف صندوق نقدی جدید" else "ویرایش صندوق نقدی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام صندوق (مثال: دخل فروشگاه، تنخواه دفتر)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد صندوق") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = keeper,
                        onValueChange = { keeper = it },
                        label = { Text("مسئول صندوق") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("موجودی نقدی (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val bal = balanceText.toLongOrNull() ?: 0L
                        if (name.isBlank()) return@Button
                        onConfirm(name, code, keeper, bal)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("ذخیره صندوق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddOrEditBankDialog(
    existing: BankAccountEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        bankName: String,
        accNum: String,
        cardNum: String,
        sheba: String,
        branch: String,
        balance: Long,
        hasPos: Boolean,
        posId: String
    ) -> Unit
) {
    var bankName by remember { mutableStateOf(existing?.bankName ?: "") }
    var accNum by remember { mutableStateOf(existing?.accountNumber ?: "") }
    var cardNum by remember { mutableStateOf(existing?.cardNumber ?: "") }
    var sheba by remember { mutableStateOf(existing?.shebaNumber ?: "") }
    var branch by remember { mutableStateOf(existing?.branch ?: "") }
    var balanceText by remember { mutableStateOf(existing?.currentBalance?.toString() ?: "0") }
    var hasPos by remember { mutableStateOf(existing?.hasPos ?: true) }
    var posId by remember { mutableStateOf(existing?.posTerminalId ?: "") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = CleanCard,
            border = BorderStroke(1.dp, CleanBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (existing == null) "افزودن حساب بانکی و کارتخوان جدید" else "ویرایش حساب بانکی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("نام بانک * (مثال: ملت، ملی، سامان)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = accNum,
                        onValueChange = { accNum = it },
                        label = { Text("شماره حساب") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("شعبه") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = cardNum,
                    onValueChange = { cardNum = it },
                    label = { Text("شماره کارت ۱۶ رقمی") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sheba,
                    onValueChange = { sheba = it },
                    label = { Text("شماره شبا (IR...)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("موجودی لحظه‌ای (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("دستگاه کارتخوان (POS) به این حساب متصل است:", fontSize = 12.sp)
                    Switch(checked = hasPos, onCheckedChange = { hasPos = it })
                }

                if (hasPos) {
                    OutlinedTextField(
                        value = posId,
                        onValueChange = { posId = it },
                        label = { Text("شماره ترمینال یا سریال پوز") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val bal = balanceText.toLongOrNull() ?: 0L
                        if (bankName.isBlank()) return@Button
                        onConfirm(bankName, accNum, cardNum, sheba, branch, bal, hasPos, posId)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("ذخیره حساب بانکی", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TreasuryActionDialog(
    title: String,
    actionLabel: String,
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Long, type: String, targetId: Long, category: String, notes: String) -> Unit
) {
    var entryTitle by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("CASH") } // "CASH" or "BANK"
    var selectedCashId by remember { mutableStateOf(cashDesks.firstOrNull()?.id ?: 0L) }
    var selectedBankId by remember { mutableStateOf(bankAccounts.firstOrNull()?.id ?: 0L) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = CleanCard,
            border = BorderStroke(1.dp, CleanBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary))

                OutlinedTextField(
                    value = entryTitle,
                    onValueChange = { entryTitle = it },
                    label = { Text("شرح عملیات / عنوان سند *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ به تومان *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == "CASH",
                        onClick = { selectedType = "CASH" },
                        label = { Text("صندوق نقدی") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == "BANK",
                        onClick = { selectedType = "BANK" },
                        label = { Text("حساب بانکی") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedType == "CASH") {
                    Text("انتخاب صندوق:", fontSize = 11.sp, color = TextDarkSecondary)
                    cashDesks.forEach { c ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedCashId = c.id }
                        ) {
                            Text(
                                text = "${c.name} (مانده: ${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(c.currentBalance))} ت)",
                                fontWeight = if (selectedCashId == c.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCashId == c.id) NavyPrimary else TextDarkPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Text("انتخاب حساب بانکی:", fontSize = 11.sp, color = TextDarkSecondary)
                    bankAccounts.forEach { b ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedBankId = b.id }
                        ) {
                            Text(
                                text = "${b.bankName} (مانده: ${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(b.currentBalance))} ت)",
                                fontWeight = if (selectedBankId == b.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedBankId == b.id) NavyPrimary else TextDarkPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و بابت") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 0L
                        if (amount <= 0 || entryTitle.isBlank()) return@Button
                        val targetId = if (selectedType == "CASH") selectedCashId else selectedBankId
                        onConfirm(entryTitle, amount, selectedType, targetId, selectedCategory, notes)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TransferFundsDialog(
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (fromType: String, fromId: Long, toType: String, toId: Long, amount: Long, notes: String) -> Unit
) {
    var fromType by remember { mutableStateOf("BANK") }
    var toType by remember { mutableStateOf("CASH") }
    var fromId by remember { mutableStateOf(bankAccounts.firstOrNull()?.id ?: 0L) }
    var toId by remember { mutableStateOf(cashDesks.firstOrNull()?.id ?: 0L) }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = CleanCard,
            border = BorderStroke(1.dp, CleanBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("انتقال وجه بین حساب‌ها و صندوق‌ها", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary))

                Text("مبدا انتقال (خروج وجه):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = fromType == "BANK", onClick = { fromType = "BANK" }, label = { Text("از بانک") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = fromType == "CASH", onClick = { fromType = "CASH" }, label = { Text("از صندوق") }, modifier = Modifier.weight(1f))
                }

                Text("مقصد انتقال (ورود وجه):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = toType == "CASH", onClick = { toType = "CASH" }, label = { Text("به صندوق") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = toType == "BANK", onClick = { toType = "BANK" }, label = { Text("به بانک") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ انتقال (تومان) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و بابت انتقال") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 0L
                        if (amount <= 0) return@Button
                        val fId = if (fromType == "BANK") (bankAccounts.firstOrNull()?.id ?: 1L) else (cashDesks.firstOrNull()?.id ?: 1L)
                        val tId = if (toType == "CASH") (cashDesks.firstOrNull()?.id ?: 1L) else (bankAccounts.firstOrNull()?.id ?: 1L)
                        onConfirm(fromType, fId, toType, tId, amount, notes)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("ثبت و تایید انتقال وجه", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CapitalInvestmentDialog(
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (investor: String, amount: Long, destType: String, targetId: Long) -> Unit
) {
    var investor by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var destType by remember { mutableStateOf("BANK") }
    var targetId by remember { mutableStateOf(bankAccounts.firstOrNull()?.id ?: 1L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CleanCard,
            border = BorderStroke(1.dp, CleanBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ثبت آورده نقدی شرکا و افزایش سرمایه", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary))

                OutlinedTextField(
                    value = investor,
                    onValueChange = { investor = it },
                    label = { Text("نام شریک / سهامدار *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ آورده (تومان) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = destType == "BANK", onClick = { destType = "BANK" }, label = { Text("واریز به بانک") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = destType == "CASH", onClick = { destType = "CASH" }, label = { Text("واریز به صندوق") }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 0L
                        if (amount <= 0 || investor.isBlank()) return@Button
                        val tId = if (destType == "BANK") (bankAccounts.firstOrNull()?.id ?: 1L) else (cashDesks.firstOrNull()?.id ?: 1L)
                        onConfirm(investor, amount, destType, tId)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("ثبت آورده سرمایه", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
