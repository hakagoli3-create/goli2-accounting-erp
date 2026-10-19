package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.CurrencyItem
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.util.NumberFormatUtils

@Composable
fun CurrencyManagementSection(
    securityManager: AppSecurityAndSettingsManager
) {
    val context = LocalContext.current
    val currencies by securityManager.currencies.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCurrency by remember { mutableStateOf<CurrencyItem?>(null) }
    var deletingCurrency by remember { mutableStateOf<CurrencyItem?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val baseCurrency = currencies.find { it.isBaseCurrency } ?: currencies.firstOrNull()
    val defaultInvoiceCurrency = currencies.find { it.isDefaultInvoiceCurrency } ?: baseCurrency

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info & Overview Card
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
                                    text = "پشتیبانی از ریال، تومان، دلار، یورو، لیر ترکیه، دینار و...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { showResetConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("reset_currencies_button")
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("بازنشانی", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    editingCurrency = null
                                    showAddDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_currency_button")
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

            // Quick Selector Card: Base Currency and Default Invoice Currency Pickers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تنظیمات سریع ارزهای فعال سیستم:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Base Currency selector chips
                    Text(text = "ارز پایه حسابداری و دفاتر را انتخاب فرمایید:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            // List of Currencies Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فهرست ارزهای تعریف شده در سیستم (${NumberFormatUtils.toPersianDigits(currencies.size)} ارز):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${NumberFormatUtils.toPersianDigits(currencies.count { it.isActive })} فعال",
                    fontSize = 11.sp,
                    color = AccountingDebitGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            currencies.forEach { item ->
                CurrencyCardItem(
                    item = item,
                    baseCurrency = baseCurrency,
                    onToggleActive = { active ->
                        securityManager.toggleCurrencyActive(item.code, active)
                    },
                    onSetBase = {
                        securityManager.setBaseCurrency(item.code)
                        Toast.makeText(context, "ارز پایه به «${item.nameFa}» تغییر یافت.", Toast.LENGTH_SHORT).show()
                    },
                    onSetDefaultInvoice = {
                        securityManager.setDefaultInvoiceCurrency(item.code)
                        Toast.makeText(context, "ارز پیش‌فرض فاکتور به «${item.nameFa}» تغییر یافت.", Toast.LENGTH_SHORT).show()
                    },
                    onEdit = {
                        editingCurrency = item
                        showAddDialog = true
                    },
                    onDelete = { deletingCurrency = item }
                )
            }
        }
    }

    // Add / Edit Currency Dialog
    if (showAddDialog) {
        AddOrEditCurrencyDialog(
            currencyToEdit = editingCurrency,
            baseCurrency = baseCurrency,
            onDismiss = {
                showAddDialog = false
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
                showAddDialog = false
                editingCurrency = null
            }
        )
    }

    // Reset Currencies Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = {
                Text("بازنشانی ارزهای استاندارد", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Text(
                    text = "آیا می‌خواهید فهرست ارزها به حالت استاندارد (ریال ایران، تومان، دلار آمریکا، یورو، لیر ترکیه و دینار عراق) بازنشانی شود؟",
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
                ) {
                    Text("بله، بازنشانی کن", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("انصراف", fontSize = 12.sp)
                }
            }
        )
    }

    // Delete Currency Confirmation Dialog
    deletingCurrency?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingCurrency = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed) },
            title = {
                Text("حذف ارز ${item.nameFa} (${item.code})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Text(
                    text = "آیا از حذف ارز «${item.nameFa}» از لیست ارزهای سامانه اطمینان دارید؟",
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        securityManager.deleteCurrency(item.code)
                        Toast.makeText(context, "ارز حذف شد.", Toast.LENGTH_SHORT).show()
                        deletingCurrency = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("حذف ارز", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCurrency = null }) {
                    Text("انصراف", fontSize = 11.sp)
                }
            }
        )
    }
}

/**
 * Currency Card Item
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
            // Top Row: Symbol Avatar, Name, Code, Badges, and Active Switch
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
                            Text(
                                text = item.nameFa,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${item.code})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Status badges
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

                // Active Switch
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
                        enabled = !item.isBaseCurrency // Cannot deactivate base currency
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Exchange rate description
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
                            "نرخ برابری: ۱.۰ (واحد مبنای ثبت کلیه اسناد)"
                        } else if (item.exchangeRateToBase < 1.0 && item.exchangeRateToBase > 0) {
                            val inverse = (1.0 / item.exchangeRateToBase).toLong()
                            "۱ ${item.symbol} = ${item.exchangeRateToBase} ${baseCurrency?.nameFa ?: "تومان"} (هر ${NumberFormatUtils.formatPriceOnlyNumber(inverse)} ${item.nameFa} = ۱ ${baseCurrency?.nameFa ?: "تومان"})"
                        } else {
                            val rLong = item.exchangeRateToBase.toLong()
                            "۱ ${item.symbol} = ${NumberFormatUtils.formatPriceOnlyNumber(rLong)} ${baseCurrency?.nameFa ?: "تومان"}"
                        }

                        Text(
                            text = rateText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!item.isBaseCurrency && item.exchangeRateToBase >= 1.0) {
                            Text(
                                text = "معادل: ${NumberFormatUtils.numberToPersianWords(item.exchangeRateToBase.toLong())} ${baseCurrency?.nameFa ?: "تومان"}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (!item.isBaseCurrency && item.exchangeRateToBase < 1.0 && item.exchangeRateToBase > 0) {
                            val inverse = (1.0 / item.exchangeRateToBase).toLong()
                            Text(
                                text = "معادل: هر ${NumberFormatUtils.numberToPersianWords(inverse)} ${item.nameFa} معادل یک ${baseCurrency?.nameFa ?: "تومان"}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "ارقام اعشار: ${NumberFormatUtils.toPersianDigits(item.decimals.toString())}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
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
 * Dialog to define or edit a currency
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
                if (it.exchangeRateToBase % 1.0 == 0.0) {
                    it.exchangeRateToBase.toLong().toString()
                } else {
                    it.exchangeRateToBase.toString()
                }
            } ?: "1"
        )
    }
    var decimals by remember(currencyToEdit) { mutableStateOf(currencyToEdit?.decimals ?: 0) }
    var isActive by remember(currencyToEdit) { mutableStateOf(currencyToEdit?.isActive ?: true) }
    var isDefaultInvoice by remember(currencyToEdit) { mutableStateOf(currencyToEdit?.isDefaultInvoiceCurrency ?: false) }

    val rateDouble = rateStr.toDoubleOrNull() ?: 1.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
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
                            text = if (currencyToEdit != null) "ویرایش مشخصات ارز ${currencyToEdit.nameFa}" else "تعریف و ثبت ارز جدید",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                        }
                    }

                    // Quick pre-set currency suggestions (if adding new)
                    if (currencyToEdit == null) {
                        Text("انتخاب سریع از میان ارزهای پرکاربرد:", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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

                    // Code and Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nameFa,
                            onValueChange = { nameFa = it },
                            label = { Text("نام فارسی ارز (مثال: دلار، لیر)") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            label = { Text("کد بین‌المللی (USD, TRY)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Symbol
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = { Text("نماد اختصاری نمایش (مثال: $ ، ₺ ، د.ع یا تومان)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Exchange Rate
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { input ->
                            val clean = NumberFormatUtils.toEnglishDigits(input).filter { ch -> ch.isDigit() || ch == '.' }
                            if (clean.count { it == '.' } <= 1) {
                                rateStr = clean
                            }
                        },
                        label = { Text("نرخ برابری نسبت به ${baseCurrency?.nameFa ?: "تومان"}") },
                        placeholder = { Text("مثلاً برای ۱ دلار: 92000 یا برای ریال: 0.1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (rateDouble > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val descText = if (rateDouble < 1.0) {
                                val inverse = (1.0 / rateDouble).toLong()
                                "معادل: هر ${NumberFormatUtils.formatPriceOnlyNumber(inverse)} $nameFa برابر ۱ ${baseCurrency?.nameFa ?: "تومان"} (${NumberFormatUtils.numberToPersianWords(inverse)} $nameFa)"
                            } else {
                                val rLong = rateDouble.toLong()
                                "معادل: ۱ $symbol = ${NumberFormatUtils.formatPriceOnlyNumber(rLong)} ${baseCurrency?.nameFa ?: "تومان"} (${NumberFormatUtils.numberToPersianWords(rLong)} ${baseCurrency?.nameFa ?: "تومان"})"
                            }
                            Text(
                                text = descText,
                                fontSize = 11.sp,
                                color = AccountingDebitGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Decimals selector
                    Text("تعداد ارقام اعشار در محاسبات و اسناد:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0 to "بدون اعشار (۰)", 2 to "دو رقم اعشار (.00)", 3 to "سه رقم اعشار (.000)").forEach { (dec, lbl) ->
                            FilterChip(
                                selected = decimals == dec,
                                onClick = { decimals = dec },
                                label = { Text(lbl, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("وضعیت ارز (فعال در سامانه):", fontSize = 11.sp)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("انتخاب به عنوان ارز پیش‌فرض فاکتورها:", fontSize = 11.sp)
                        Switch(checked = isDefaultInvoice, onCheckedChange = { isDefaultInvoice = it })
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (nameFa.isBlank() || code.isBlank()) return@Button
                            val item = CurrencyItem(
                                code = code.trim().uppercase(),
                                nameFa = nameFa.trim(),
                                symbol = symbol.trim().ifBlank { code.trim() },
                                exchangeRateToBase = rateStr.toDoubleOrNull() ?: 1.0,
                                isActive = isActive,
                                isBaseCurrency = currencyToEdit?.isBaseCurrency ?: false,
                                isDefaultInvoiceCurrency = isDefaultInvoice,
                                decimals = decimals
                            )
                            onSave(item)
                        },
                        enabled = nameFa.isNotBlank() && code.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_currency_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currencyToEdit != null) "ذخیره تغییرات ارز" else "ثبت نهایی ارز جدید",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
