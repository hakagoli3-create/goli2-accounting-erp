package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AppDatabase
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.CurrencyItem
import com.example.data.model.ExchangeRateEntity
import com.example.data.model.InvoiceWithItems
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyManagementSection(
    securityManager: AppSecurityAndSettingsManager,
    db: AppDatabase? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = db ?: remember { AppDatabase.getInstance(context) }
    val currencies by securityManager.currencies.collectAsStateWithLifecycle()

    val exchangeRates by database.exchangeRateDao().getAllRates().collectAsStateWithLifecycle(initialValue = emptyList())
    val invoices by database.invoiceDao().getAllInvoicesWithItems().collectAsStateWithLifecycle(initialValue = emptyList())

    // Sub-tab: 0 = Currency List, 1 = Exchange Rates Panel
    var selectedSubTab by remember { mutableIntStateOf(1) }

    var showAddCurrencyDialog by remember { mutableStateOf(false) }
    var editingCurrency by remember { mutableStateOf<CurrencyItem?>(null) }
    var deletingCurrency by remember { mutableStateOf<CurrencyItem?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Manual Rate Registration Dialog
    var showAddRateDialog by remember { mutableStateOf(false) }
    var editingRate by remember { mutableStateOf<ExchangeRateEntity?>(null) }
    var deletingRate by remember { mutableStateOf<ExchangeRateEntity?>(null) }

    val baseCurrency = currencies.find { it.isBaseCurrency } ?: currencies.firstOrNull()
    val defaultInvoiceCurrency = currencies.find { it.isDefaultInvoiceCurrency } ?: baseCurrency

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Top Sub-Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedSubTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("currency_sub_tab_row"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("پنل نرخ تبدیل و تسعیر", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_exchange_rates")
                )
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تعریف و فهرست ارزها", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_currency_list")
                )
            }

            if (selectedSubTab == 1) {
                // ==========================================
                // پنل نرخ‌های تبدیل (EXCHANGE RATES PANEL)
                // ==========================================
                ExchangeRatesPanelContent(
                    currencies = currencies,
                    exchangeRates = exchangeRates,
                    invoices = invoices,
                    baseCurrency = baseCurrency,
                    onOpenAddRate = {
                        editingRate = null
                        showAddRateDialog = true
                    },
                    onEditRate = { rate ->
                        editingRate = rate
                        showAddRateDialog = true
                    },
                    onDeleteRate = { rate ->
                        deletingRate = rate
                    }
                )
            } else {
                // ==========================================
                // پنل تعریف و فهرست ارزها (CURRENCIES LIST)
                // ==========================================
                CurrenciesListContent(
                    currencies = currencies,
                    baseCurrency = baseCurrency,
                    defaultInvoiceCurrency = defaultInvoiceCurrency,
                    securityManager = securityManager,
                    onOpenAddCurrency = {
                        editingCurrency = null
                        showAddCurrencyDialog = true
                    },
                    onEditCurrency = { item ->
                        editingCurrency = item
                        showAddCurrencyDialog = true
                    },
                    onDeleteCurrency = { item ->
                        deletingCurrency = item
                    },
                    onOpenResetDialog = {
                        showResetConfirmDialog = true
                    }
                )
            }
        }
    }

    // Add / Edit Manual Exchange Rate Dialog
    if (showAddRateDialog) {
        AddOrEditExchangeRateDialog(
            rateToEdit = editingRate,
            currencies = currencies.filter { it.isActive },
            baseCurrency = baseCurrency,
            onDismiss = {
                showAddRateDialog = false
                editingRate = null
            },
            onSave = { entity ->
                scope.launch {
                    if (editingRate != null) {
                        database.exchangeRateDao().updateRate(entity)
                        Toast.makeText(context, "نرخ تبدیل ویرایش گردید.", Toast.LENGTH_SHORT).show()
                    } else {
                        database.exchangeRateDao().insertRate(entity)
                        Toast.makeText(context, "نرخ تبدیل جدید ثبت گردید.", Toast.LENGTH_SHORT).show()
                    }
                    // Also sync to currency if target is base currency
                    if (entity.targetCurrency.equals(baseCurrency?.code, ignoreCase = true)) {
                        val curr = currencies.find { it.code.equals(entity.sourceCurrency, ignoreCase = true) }
                        if (curr != null) {
                            securityManager.updateCurrency(curr.copy(exchangeRateToBase = entity.rate))
                        }
                    }
                    showAddRateDialog = false
                    editingRate = null
                }
            }
        )
    }

    // Delete Exchange Rate Confirmation
    deletingRate?.let { rate ->
        AlertDialog(
            onDismissRequest = { deletingRate = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed) },
            title = { Text("حذف نرخ تبدیل", fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف رکورد نرخ تبدیل ${rate.sourceCurrency} به ${rate.targetCurrency} مورخ ${rate.effectiveDateJalali} اطمینان دارید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            database.exchangeRateDao().deleteRate(rate)
                            Toast.makeText(context, "نرخ حذف شد.", Toast.LENGTH_SHORT).show()
                            deletingRate = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { deletingRate = null }) { Text("انصراف") }
            }
        )
    }

    // Add / Edit Currency Dialog
    if (showAddCurrencyDialog) {
        AddOrEditCurrencyDialog(
            currencyToEdit = editingCurrency,
            baseCurrency = baseCurrency,
            onDismiss = {
                showAddCurrencyDialog = false
                editingCurrency = null
            },
            onSave = { updatedItem ->
                if (editingCurrency != null) {
                    securityManager.updateCurrency(updatedItem)
                    Toast.makeText(context, "ارز «${updatedItem.nameFa}» با موفقیت ویرایش گردید.", Toast.LENGTH_SHORT).show()
                } else {
                    securityManager.addCurrency(updatedItem)
                    Toast.makeText(context, "ارز «${updatedItem.nameFa}» با موفقیت اضافه شد.", Toast.LENGTH_SHORT).show()
                }
                showAddCurrencyDialog = false
                editingCurrency = null
            }
        )
    }

    // Reset Currencies Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("بازنشانی ارزهای استاندارد", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Text(
                    text = "آیا می‌خواهید فهرست ارزها به حالت استاندارد (ریال، تومان، دلار، یورو، لیر و دینار) بازنشانی شود؟",
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        securityManager.resetToDefaultCurrencies()
                        Toast.makeText(context, "ارزهای استاندارد با موفقیت بازنشانی شدند.", Toast.LENGTH_SHORT).show()
                        showResetConfirmDialog = false
                    }
                ) { Text("بله، بازنشانی کن", fontSize = 12.sp) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("انصراف", fontSize = 12.sp) }
            }
        )
    }

    // Delete Currency Confirmation Dialog
    deletingCurrency?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingCurrency = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed) },
            title = { Text("حذف ارز ${item.nameFa} (${item.code})", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = { Text("آیا از حذف ارز «${item.nameFa}» از سیستم اطمینان دارید؟", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        securityManager.deleteCurrency(item.code)
                        Toast.makeText(context, "ارز حذف شد.", Toast.LENGTH_SHORT).show()
                        deletingCurrency = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) { Text("حذف ارز", fontSize = 11.sp) }
            },
            dismissButton = {
                TextButton(onClick = { deletingCurrency = null }) { Text("انصراف", fontSize = 11.sp) }
            }
        )
    }
}

/**
 * محتوای تب نرخ‌های تبدیل (Exchange Rates Panel)
 */
@Composable
private fun ExchangeRatesPanelContent(
    currencies: List<CurrencyItem>,
    exchangeRates: List<ExchangeRateEntity>,
    invoices: List<InvoiceWithItems>,
    baseCurrency: CurrencyItem?,
    onOpenAddRate: () -> Unit,
    onEditRate: (ExchangeRateEntity) -> Unit,
    onDeleteRate: (ExchangeRateEntity) -> Unit
) {
    val activeCurrencies = currencies.filter { it.isActive }
    val baseCode = baseCurrency?.code ?: "TOMAN"

    // 1. Header Card with Action Button
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PersianGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = PersianGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "سامانه نرخ‌های تبدیل و تسعیر ارز",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ثبت دستی، نرخ معکوس، تبدیل متقاطع، تاریخ اعتبار و سوابق معاملات",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onOpenAddRate,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PersianGold, contentColor = Color.Black),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_exchange_rate_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ثبت نرخ جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Info note about transaction rate preservation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccountingDebitGreen.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "نرخ تبدیل زمان ثبت هر معامله و فاکتور منجمد و ذخیره می‌گردد تا تغییرات بعدی نرخ ارز، سوابق و تراز دفاتر مالی را تغییر ندهد.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }

    // 2. Interactive Cross-Rate & Inverse Calculator Widget
    CrossRateCalculatorCard(
        currencies = activeCurrencies,
        baseCurrency = baseCurrency,
        exchangeRates = exchangeRates
    )

    // 3. Matrix Table of Cross Rates
    CrossRatesMatrixCard(
        currencies = activeCurrencies,
        baseCurrency = baseCurrency,
        exchangeRates = exchangeRates
    )

    // 4. Rate History List
    ExchangeRateHistorySection(
        rates = exchangeRates,
        currencies = currencies,
        onEdit = onEditRate,
        onDelete = onDeleteRate
    )

    // 5. Preserved Rates in Transactions Log
    PreservedTransactionRatesSection(
        invoices = invoices,
        currencies = currencies,
        baseCurrency = baseCurrency
    )
}

/**
 * ویجت تعاملی محاسبه نرخ معکوس و تبدیل متقاطع (Interactive Cross-Rate Calculator)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrossRateCalculatorCard(
    currencies: List<CurrencyItem>,
    baseCurrency: CurrencyItem?,
    exchangeRates: List<ExchangeRateEntity>
) {
    var sourceCode by remember { mutableStateOf(if (currencies.any { it.code == "USD" }) "USD" else (currencies.firstOrNull()?.code ?: "TOMAN")) }
    var targetCode by remember { mutableStateOf(if (currencies.any { it.code == "EUR" }) "EUR" else (baseCurrency?.code ?: "TOMAN")) }
    var amountInput by remember { mutableStateOf("100") }

    val baseCode = baseCurrency?.code ?: "TOMAN"
    val amountDouble = amountInput.toDoubleOrNull() ?: 1.0

    // Resolve rates
    val sourceItem = currencies.find { it.code == sourceCode }
    val targetItem = currencies.find { it.code == targetCode }

    val rateSourceToBase = if (sourceCode == baseCode) 1.0 else {
        exchangeRates.firstOrNull { it.sourceCurrency == sourceCode && it.targetCurrency == baseCode && it.isActive }?.rate
            ?: sourceItem?.exchangeRateToBase ?: 1.0
    }

    val rateTargetToBase = if (targetCode == baseCode) 1.0 else {
        exchangeRates.firstOrNull { it.sourceCurrency == targetCode && it.targetCurrency == baseCode && it.isActive }?.rate
            ?: targetItem?.exchangeRateToBase ?: 1.0
    }

    // Direct pair if exists
    val directRateEntity = exchangeRates.firstOrNull {
        it.sourceCurrency == sourceCode && it.targetCurrency == targetCode && it.isActive
    }

    val crossRate = if (sourceCode == targetCode) 1.0 else if (directRateEntity != null && directRateEntity.rate > 0) {
        directRateEntity.rate
    } else if (rateTargetToBase > 0) {
        rateSourceToBase / rateTargetToBase
    } else 1.0

    val inverseRate = if (crossRate > 0) 1.0 / crossRate else 0.0
    val convertedAmount = amountDouble * crossRate

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, PersianGold.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = PersianGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ماشین‌حساب تبدیل متقاطع (Cross-Rate) و نرخ معکوس",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PersianGold)
                )
            }

            Text(
                text = "محاسبه نرخ برابری هر دو ارز بر مبنای نرخ‌های ثبت‌شده و ارز پایه سامانه:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Selectors row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source Currency
                Column(modifier = Modifier.weight(1f)) {
                    Text("ارز مبدأ:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        currencies.forEach { c ->
                            FilterChip(
                                selected = sourceCode == c.code,
                                onClick = { sourceCode = c.code },
                                label = { Text(c.code, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // Swap icon
                IconButton(
                    onClick = {
                        val tmp = sourceCode
                        sourceCode = targetCode
                        targetCode = tmp
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "جابجایی", tint = PersianGold)
                }

                // Target Currency
                Column(modifier = Modifier.weight(1f)) {
                    Text("ارز مقصد:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        currencies.forEach { c ->
                            FilterChip(
                                selected = targetCode == c.code,
                                onClick = { targetCode = c.code },
                                label = { Text(c.code, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }

            // Amount input
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    val clean = NumberFormatUtils.toEnglishDigits(input).filter { it.isDigit() || it == '.' }
                    if (clean.count { it == '.' } <= 1) amountInput = clean
                },
                label = { Text("مبلغ ارز مبدأ (${sourceItem?.nameFa ?: sourceCode})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("cross_rate_amount_input")
            )

            // Conversion result box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مبلغ معادل در مقصد:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${formatDouble(convertedAmount, 2)} ${targetItem?.symbol ?: targetCode}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccountingDebitGreen
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Direct & Inverse Rates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نرخ برابری (Direct): ۱ $sourceCode = ${formatDouble(crossRate, 4)} $targetCode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "نرخ معکوس (Inverse): ۱ $targetCode = ${formatDouble(inverseRate, 4)} $sourceCode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PersianGold
                        )
                    }

                    // Calculation Path
                    if (sourceCode != baseCode && targetCode != baseCode && sourceCode != targetCode) {
                        Text(
                            text = "فرمول تبدیل متقاطع: (نرخ $sourceCode به $baseCode: ${NumberFormatUtils.formatPriceOnlyNumber(rateSourceToBase.toLong())}) ÷ (نرخ $targetCode به $baseCode: ${NumberFormatUtils.formatPriceOnlyNumber(rateTargetToBase.toLong())}) = ${formatDouble(crossRate, 4)}",
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
 * جدول ماتریس تبدیل متقاطع بین ارزهای فعال سیستم
 */
@Composable
private fun CrossRatesMatrixCard(
    currencies: List<CurrencyItem>,
    baseCurrency: CurrencyItem?,
    exchangeRates: List<ExchangeRateEntity>
) {
    val baseCode = baseCurrency?.code ?: "TOMAN"
    val displayCurrencies = currencies.take(6)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "ماتریس نرخ‌های تبدیل متقاطع ارزهای فعال:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "برابری هر ارز (ستون افقی) به ازای ۱ واحد از ارز متناظر (ردیف عمودی):",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Matrix Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(vertical = 6.dp)
                    ) {
                        Box(modifier = Modifier.width(70.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                            Text("ارز", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        displayCurrencies.forEach { c ->
                            Box(modifier = Modifier.width(85.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                Text(c.code, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Matrix Data Rows
                    displayCurrencies.forEach { rowCurr ->
                        val rowRateToBase = if (rowCurr.code == baseCode) 1.0 else {
                            exchangeRates.firstOrNull { it.sourceCurrency == rowCurr.code && it.targetCurrency == baseCode && it.isActive }?.rate
                                ?: rowCurr.exchangeRateToBase
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(70.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.CenterStart) {
                                Text(rowCurr.code, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            displayCurrencies.forEach { colCurr ->
                                val colRateToBase = if (colCurr.code == baseCode) 1.0 else {
                                    exchangeRates.firstOrNull { it.sourceCurrency == colCurr.code && it.targetCurrency == baseCode && it.isActive }?.rate
                                        ?: colCurr.exchangeRateToBase
                                }

                                val matrixRate = if (rowCurr.code == colCurr.code) {
                                    1.0
                                } else if (colRateToBase > 0) {
                                    rowRateToBase / colRateToBase
                                } else 0.0

                                Box(
                                    modifier = Modifier
                                        .width(85.dp)
                                        .background(
                                            if (rowCurr.code == colCurr.code) PersianGold.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (matrixRate >= 100) NumberFormatUtils.formatPriceOnlyNumber(matrixRate.toLong()) else formatDouble(matrixRate, 2),
                                        fontSize = 10.sp,
                                        fontWeight = if (rowCurr.code == colCurr.code) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                }
            }
        }
    }
}

/**
 * بخش تاریخچه و سوابق ثبت دستی نرخ‌ها
 */
@Composable
private fun ExchangeRateHistorySection(
    rates: List<ExchangeRateEntity>,
    currencies: List<CurrencyItem>,
    onEdit: (ExchangeRateEntity) -> Unit,
    onDelete: (ExchangeRateEntity) -> Unit
) {
    var selectedFilterPair by remember { mutableStateOf("ALL") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تاریخچه نرخ‌های تبدیل ثبت‌شده (${NumberFormatUtils.toPersianDigits(rates.size)} رکورد):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Quick Pair Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilterPair == "ALL",
                onClick = { selectedFilterPair = "ALL" },
                label = { Text("همه رکوردها", fontSize = 10.sp) }
            )
            listOf("USD", "EUR", "TRY", "IQD", "AED", "IRR").forEach { curr ->
                FilterChip(
                    selected = selectedFilterPair == curr,
                    onClick = { selectedFilterPair = curr },
                    label = { Text("$curr / تومان", fontSize = 10.sp) }
                )
            }
        }

        val filteredRates = if (selectedFilterPair == "ALL") rates else rates.filter {
            it.sourceCurrency.equals(selectedFilterPair, ignoreCase = true) || it.targetCurrency.equals(selectedFilterPair, ignoreCase = true)
        }

        if (filteredRates.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Text(
                    text = "هنوز نرخ تبدیلی برای این ارز در تاریخچه ثبت نشده است.",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            filteredRates.forEach { rate ->
                RateHistoryCard(
                    rate = rate,
                    onEdit = { onEdit(rate) },
                    onDelete = { onDelete(rate) }
                )
            }
        }
    }
}

/**
 * کارت نمایش سابقه یک نرخ در تاریخچه
 */
@Composable
private fun RateHistoryCard(
    rate: ExchangeRateEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(PersianGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${rate.sourceCurrency} ◄► ${rate.targetCurrency}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PersianGold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تاریخ اعتبار: ${NumberFormatUtils.toPersianDigits(rate.effectiveDateJalali)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Rates display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "نرخ مستقیم: ۱ ${rate.sourceCurrency} = ${if (rate.rate >= 100) NumberFormatUtils.formatPriceOnlyNumber(rate.rate.toLong()) else formatDouble(rate.rate, 4)} ${rate.targetCurrency}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = AccountingDebitGreen
                    )
                    Text(
                        text = "نرخ معکوس: ۱ ${rate.targetCurrency} = ${formatDouble(rate.inverseRate, 6)} ${rate.sourceCurrency}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "ثبت‌کننده: ${rate.recordedBy}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (rate.notes.isNotBlank()) {
                Text(
                    text = "توضیحات: ${rate.notes}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * سوابق نرخ‌های استفاده شده در معاملات و فاکتورها (Preserved Transaction Rates)
 */
@Composable
private fun PreservedTransactionRatesSection(
    invoices: List<InvoiceWithItems>,
    currencies: List<CurrencyItem>,
    baseCurrency: CurrencyItem?
) {
    val nonBaseInvoices = invoices.filter { it.invoice.currencyCode != "TOMAN" && it.invoice.currencyRateToBase > 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "سوابق نرخ‌های قفل‌شده در فاکتورها و معاملات (${NumberFormatUtils.toPersianDigits(nonBaseInvoices.size)} فاکتور ارزی):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "این سوابق نشان‌دهنده نرخ تبدیل قطعی در لحظه صدور هر فاکتور است که همواره بدون تغییر باقی می‌ماند:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (nonBaseInvoices.isEmpty()) {
                Text(
                    text = "هنوز فاکتوری با ارزهای خارجی (دلار، یورو، لیر و...) ثبت نشده است. در صدور فاکتور جدید، ارز دلخواه را انتخاب نمایید.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            } else {
                nonBaseInvoices.take(8).forEach { item ->
                    val inv = item.invoice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = NumberFormatUtils.formatInvoiceNumber(inv.invoiceNumber),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${NumberFormatUtils.toPersianDigits(inv.jalaliDate)})",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "طرف حساب: ${inv.partyName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${NumberFormatUtils.formatPriceOnlyNumber(inv.totalAmount)} ${inv.currencyCode}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = AccountingDebitGreen
                            )
                            Text(
                                text = "نرخ تسعیر: ${NumberFormatUtils.formatPriceOnlyNumber(inv.currencyRateToBase.toLong())} تومان",
                                fontSize = 10.sp,
                                color = PersianGold,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "معادل: ${NumberFormatUtils.formatPrice(inv.totalAmountInBase)}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ ثبت یا ویرایش نرخ تبدیل دستی
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditExchangeRateDialog(
    rateToEdit: ExchangeRateEntity?,
    currencies: List<CurrencyItem>,
    baseCurrency: CurrencyItem?,
    onDismiss: () -> Unit,
    onSave: (ExchangeRateEntity) -> Unit
) {
    val baseCode = baseCurrency?.code ?: "TOMAN"
    var sourceCurrency by remember(rateToEdit) { mutableStateOf(rateToEdit?.sourceCurrency ?: (if (currencies.any { it.code == "USD" }) "USD" else (currencies.firstOrNull()?.code ?: "USD"))) }
    var targetCurrency by remember(rateToEdit) { mutableStateOf(rateToEdit?.targetCurrency ?: baseCode) }
    var rateStr by remember(rateToEdit) {
        mutableStateOf(
            rateToEdit?.let {
                if (it.rate % 1.0 == 0.0) it.rate.toLong().toString() else it.rate.toString()
            } ?: "92000"
        )
    }
    var effectiveDate by remember(rateToEdit) { mutableStateOf(rateToEdit?.effectiveDateJalali ?: JalaliDate.now().toStandardString()) }
    var notes by remember(rateToEdit) { mutableStateOf(rateToEdit?.notes ?: "نرخ روز صرافی و بازار آزاد") }
    var recordedBy by remember(rateToEdit) { mutableStateOf(rateToEdit?.recordedBy ?: "مدیر سیستم") }

    val rateDouble = rateStr.toDoubleOrNull() ?: 1.0
    val inverse = if (rateDouble > 0) 1.0 / rateDouble else 0.0

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 18.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (rateToEdit != null) "ویرایش نرخ تبدیل دستی" else "ثبت دستی نرخ تبدیل ارز",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                        }
                    }

                    // Source and Target Pickers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ارز مبدأ:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                currencies.forEach { c ->
                                    FilterChip(
                                        selected = sourceCurrency == c.code,
                                        onClick = { sourceCurrency = c.code },
                                        label = { Text(c.code, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("ارز مقصد:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                currencies.forEach { c ->
                                    FilterChip(
                                        selected = targetCurrency == c.code,
                                        onClick = { targetCurrency = c.code },
                                        label = { Text(c.code, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Manual Rate Input
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { input ->
                            val clean = NumberFormatUtils.toEnglishDigits(input).filter { it.isDigit() || it == '.' }
                            if (clean.count { it == '.' } <= 1) rateStr = clean
                        },
                        label = { Text("نرخ تبدیل مستقیم (۱ $sourceCurrency = چند $targetCurrency)") },
                        placeholder = { Text("مثلاً: 92000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("manual_exchange_rate_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Auto-calculated Inverse rate preview
                    if (rateDouble > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PersianGold.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "محاسبه خودکار نرخ معکوس (Inverse Rate):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PersianGold
                                )
                                Text(
                                    text = "۱ $targetCurrency = ${formatDouble(inverse, 8)} $sourceCurrency",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "هر ۱ واحد از $targetCurrency معادل ${formatDouble(inverse, 6)} واحد از $sourceCurrency است.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Effective Date
                    OutlinedTextField(
                        value = effectiveDate,
                        onValueChange = { effectiveDate = it },
                        label = { Text("تاریخ اعتبار (شمسی)") },
                        placeholder = { Text("۱۴۰۵/۰۱/۳۰") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Notes / Source
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("منبع / توضیحات نرخ (مثلا: صرافی ملی، بازار آزاد)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Recorded By
                    OutlinedTextField(
                        value = recordedBy,
                        onValueChange = { recordedBy = it },
                        label = { Text("ثبت‌کننده نرخ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (sourceCurrency.equals(targetCurrency, ignoreCase = true)) {
                                return@Button
                            }
                            if (rateDouble <= 0) return@Button
                            onSave(
                                ExchangeRateEntity(
                                    id = rateToEdit?.id ?: 0L,
                                    sourceCurrency = sourceCurrency.uppercase(),
                                    targetCurrency = targetCurrency.uppercase(),
                                    rate = rateDouble,
                                    inverseRate = inverse,
                                    effectiveDateJalali = effectiveDate.trim(),
                                    timestamp = System.currentTimeMillis(),
                                    notes = notes.trim(),
                                    recordedBy = recordedBy.trim(),
                                    isActive = true
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_exchange_rate_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold, contentColor = Color.Black)
                    ) {
                        Text("ذخیره نرخ در تاریخچه و سیستم", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * فهرست ارزها و تنظیمات پایه (Currencies List Content)
 */
@Composable
private fun CurrenciesListContent(
    currencies: List<CurrencyItem>,
    baseCurrency: CurrencyItem?,
    defaultInvoiceCurrency: CurrencyItem?,
    securityManager: AppSecurityAndSettingsManager,
    onOpenAddCurrency: () -> Unit,
    onEditCurrency: (CurrencyItem) -> Unit,
    onDeleteCurrency: (CurrencyItem) -> Unit,
    onOpenResetDialog: () -> Unit
) {
    val context = LocalContext.current

    // Overview KPIs
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PersianGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = PersianGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مدیریت جامع ارزهای سامانه",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ریال، تومان، دلار، یورو، لیر ترکیه، دینار و درهم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onOpenResetDialog,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بازنشانی", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onOpenAddCurrency,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ارز جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Overview KPIs: Base Currency & Invoice Default Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Base Currency Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(PersianGold.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = PersianGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ارز پایه حسابداری:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PersianGold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = baseCurrency?.let { "${it.nameFa} (${it.code})" } ?: "تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "مبنای دفاتر کل و ترازنامه مالی",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Default Invoice Currency Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(AccountingDebitGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ارز پیش‌فرض فاکتورها:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AccountingDebitGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = defaultInvoiceCurrency?.let { "${it.nameFa} (${it.code})" } ?: "تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "انتخاب خودکار در صدور فاکتور جدید",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Quick Selector Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "تنظیمات سریع ارزهای سیستم:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Base Currency selector chips
            Text(text = "ارز پایه دفاتر کل مالی:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currencies.filter { it.isActive }.forEach { curr ->
                    FilterChip(
                        selected = curr.isBaseCurrency,
                        onClick = {
                            securityManager.setBaseCurrency(curr.code)
                            Toast.makeText(context, "ارز پایه به «${curr.nameFa}» تغییر یافت.", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text(curr.nameFa, fontSize = 11.sp, fontWeight = if (curr.isBaseCurrency) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (curr.isBaseCurrency) {
                            { Icon(Icons.Default.Star, contentDescription = null, tint = PersianGold, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Default Invoice Currency selector chips
            Text(text = "ارز پیش‌فرض برای صدور فاکتورهای فروش و POS:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currencies.filter { it.isActive }.forEach { curr ->
                    FilterChip(
                        selected = curr.isDefaultInvoiceCurrency,
                        onClick = {
                            securityManager.setDefaultInvoiceCurrency(curr.code)
                            Toast.makeText(context, "ارز پیش‌فرض فاکتورها به «${curr.nameFa}» تغییر یافت.", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text(curr.nameFa, fontSize = 11.sp, fontWeight = if (curr.isDefaultInvoiceCurrency) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (curr.isDefaultInvoiceCurrency) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }
    }

    // List of Currencies
    Text(
        text = "فهرست ارزهای تعریف شده در سیستم (${NumberFormatUtils.toPersianDigits(currencies.size)} ارز):",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary
    )

    currencies.forEach { item ->
        CurrencyCardItem(
            item = item,
            baseCurrency = baseCurrency,
            onToggleActive = { active -> securityManager.toggleCurrencyActive(item.code, active) },
            onSetBase = {
                securityManager.setBaseCurrency(item.code)
                Toast.makeText(context, "ارز پایه به «${item.nameFa}» تغییر یافت.", Toast.LENGTH_SHORT).show()
            },
            onSetDefaultInvoice = {
                securityManager.setDefaultInvoiceCurrency(item.code)
                Toast.makeText(context, "ارز پیش‌فرض فاکتور به «${item.nameFa}» تغییر یافت.", Toast.LENGTH_SHORT).show()
            },
            onEdit = { onEditCurrency(item) },
            onDelete = { onDeleteCurrency(item) }
        )
    }
}

/**
 * کارت مشخصات ارز
 */
@Composable
private fun CurrencyCardItem(
    item: CurrencyItem,
    baseCurrency: CurrencyItem?,
    onToggleActive: (Boolean) -> Unit,
    onSetBase: () -> Unit,
    onSetDefaultInvoice: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (item.isBaseCurrency) PersianGold else if (item.isDefaultInvoiceCurrency) AccountingDebitGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (item.isBaseCurrency) PersianGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.symbol.take(4),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (item.isBaseCurrency) PersianGold else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.nameFa, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "(${item.code})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (item.isBaseCurrency) {
                                Box(
                                    modifier = Modifier
                                        .background(PersianGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ارز پایه سیستم", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PersianGold)
                                }
                            }
                            if (item.isDefaultInvoiceCurrency) {
                                Box(
                                    modifier = Modifier
                                        .background(AccountingDebitGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("پیش‌فرض فاکتورها", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccountingDebitGreen)
                                }
                            }
                            if (!item.isActive) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("غیرفعال", fontSize = 9.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.isActive) "فعال" else "غیرفعال",
                        fontSize = 11.sp,
                        color = if (item.isActive) AccountingDebitGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = item.isActive,
                        onCheckedChange = { onToggleActive(it) },
                        enabled = !item.isBaseCurrency
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rate description box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val rateText = if (item.isBaseCurrency) {
                            "نرخ برابری: ۱.۰ (واحد مبنای دفاتر مالی)"
                        } else if (item.exchangeRateToBase < 1.0 && item.exchangeRateToBase > 0) {
                            val inverse = (1.0 / item.exchangeRateToBase).toLong()
                            "۱ ${item.symbol} = ${item.exchangeRateToBase} ${baseCurrency?.nameFa ?: "تومان"} (هر ${NumberFormatUtils.formatPriceOnlyNumber(inverse)} ${item.nameFa} = ۱ ${baseCurrency?.nameFa ?: "تومان"})"
                        } else {
                            val rLong = item.exchangeRateToBase.toLong()
                            "۱ ${item.symbol} = ${NumberFormatUtils.formatPriceOnlyNumber(rLong)} ${baseCurrency?.nameFa ?: "تومان"}"
                        }

                        Text(text = rateText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Text(
                        text = "اعشار: ${NumberFormatUtils.toPersianDigits(item.decimals.toString())}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!item.isBaseCurrency && item.isActive) {
                        OutlinedButton(
                            onClick = onSetBase,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PersianGold)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = PersianGold, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("انتخاب ارز پایه", fontSize = 10.sp, color = PersianGold)
                        }
                    }

                    if (!item.isDefaultInvoiceCurrency && item.isActive) {
                        OutlinedButton(
                            onClick = onSetDefaultInvoice,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AccountingDebitGreen)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پیش‌فرض فاکتور", fontSize = 10.sp, color = AccountingDebitGreen)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                    }
                    if (!item.isBaseCurrency && item.code !in listOf("TOMAN", "IRR")) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ تعریف یا ویرایش ارز
 */
@Composable
private fun AddOrEditCurrencyDialog(
    currencyToEdit: CurrencyItem?,
    baseCurrency: CurrencyItem?,
    onDismiss: () -> Unit,
    onSave: (CurrencyItem) -> Unit
) {
    var code by remember(currencyToEdit) { mutableStateOf(currencyToEdit?.code ?: "") }
    var nameFa by remember(currencyToEdit) { mutableStateOf(currencyToEdit?.nameFa ?: "") }
    var symbol by remember(currencyToEdit) { mutableStateOf(currencyToEdit?.symbol ?: "") }
    var rateStr by remember(currencyToEdit) {
        mutableStateOf(
            currencyToEdit?.let {
                if (it.exchangeRateToBase % 1.0 == 0.0) it.exchangeRateToBase.toLong().toString() else it.exchangeRateToBase.toString()
            } ?: "1"
        )
    }
    var decimals by remember(currencyToEdit) { mutableIntStateOf(currencyToEdit?.decimals ?: 0) }

    val rateDouble = rateStr.toDoubleOrNull() ?: 1.0

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).padding(vertical = 18.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currencyToEdit != null) "ویرایش مشخصات ارز ${currencyToEdit.nameFa}" else "تعریف و ثبت ارز جدید",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onDismiss) { Icon(imageVector = Icons.Default.Close, contentDescription = "بستن") }
                    }

                    if (currencyToEdit == null) {
                        Text("انتخاب سریع ارزهای پرکاربرد:", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                Triple("TOMAN", "تومان", "تومان") to (1.0 to 0),
                                Triple("IRR", "ریال ایران", "ریال") to (0.1 to 0),
                                Triple("USD", "دلار آمریکا", "$") to (92000.0 to 2),
                                Triple("EUR", "یورو اروپا", "€") to (101000.0 to 2),
                                Triple("TRY", "لیر ترکیه", "₺") to (2650.0 to 2),
                                Triple("IQD", "دینار عراق", "د.ع") to (62.0 to 0),
                                Triple("AED", "درهم امارات", "د.إ") to (25100.0 to 2),
                                Triple("GBP", "پوند انگلیس", "£") to (119000.0 to 2)
                            ).forEach { (meta, details) ->
                                val (defRate, defDec) = details
                                AssistChip(
                                    onClick = {
                                        code = meta.first
                                        nameFa = meta.second
                                        symbol = meta.third
                                        rateStr = if (defRate % 1.0 == 0.0) defRate.toLong().toString() else defRate.toString()
                                        decimals = defDec
                                    },
                                    label = { Text(meta.second, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nameFa,
                            onValueChange = { nameFa = it },
                            label = { Text("نام فارسی ارز") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            label = { Text("کد ارز (USD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = { Text("نماد اختصاری نمایش (مثلا $ یا ₺)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { input ->
                            val clean = NumberFormatUtils.toEnglishDigits(input).filter { it.isDigit() || it == '.' }
                            if (clean.count { it == '.' } <= 1) rateStr = clean
                        },
                        label = { Text("نرخ برابری نسبت به ${baseCurrency?.nameFa ?: "تومان"}") },
                        placeholder = { Text("مثلاً برای ۱ دلار: 92000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (code.isBlank() || nameFa.isBlank()) return@Button
                            onSave(
                                CurrencyItem(
                                    code = code.trim().uppercase(),
                                    nameFa = nameFa.trim(),
                                    symbol = symbol.ifBlank { code.trim() },
                                    exchangeRateToBase = rateDouble,
                                    isActive = true,
                                    isBaseCurrency = currencyToEdit?.isBaseCurrency ?: false,
                                    isDefaultInvoiceCurrency = currencyToEdit?.isDefaultInvoiceCurrency ?: false,
                                    decimals = decimals
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره مشخصات ارز", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatDouble(value: Double, decimals: Int): String {
    if (value.isNaN() || value.isInfinite()) return "۰"
    val pattern = "%.${decimals}f"
    val formatted = String.format(Locale.US, pattern, value)
    return NumberFormatUtils.toPersianDigits(formatted)
}
