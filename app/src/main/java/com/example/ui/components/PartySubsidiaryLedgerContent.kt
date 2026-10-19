package com.example.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalItemEntity
import com.example.data.model.PartyEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PersianGold
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartySubsidiaryLedgerContent(
    parties: List<PartyEntity>,
    selectedParty: PartyEntity?,
    ledgerEntries: List<JournalItemEntity>,
    onSelectParty: (PartyEntity) -> Unit,
    onAddTransaction: (partyId: Long, desc: String, debit: Long, credit: Long, date: String) -> Unit,
    onUpdateTransaction: (item: JournalItemEntity, desc: String, debit: Long, credit: Long) -> Unit,
    onDeleteTransaction: (item: JournalItemEntity) -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    val context = LocalContext.current
    var partyMenuExpanded by remember { mutableStateOf(false) }
    var partySearchQuery by remember { mutableStateOf("") }
    var ledgerSearchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<JournalItemEntity?>(null) }
    var deletingItem by remember { mutableStateOf<JournalItemEntity?>(null) }

    val filteredParties = remember(parties, partySearchQuery) {
        if (partySearchQuery.isBlank()) parties
        else parties.filter {
            it.name.contains(partySearchQuery, ignoreCase = true) ||
            it.code.contains(partySearchQuery, ignoreCase = true) ||
            it.mobile.contains(partySearchQuery)
        }
    }

    val filteredEntries = remember(ledgerEntries, ledgerSearchQuery) {
        if (ledgerSearchQuery.isBlank()) ledgerEntries
        else ledgerEntries.filter {
            it.description.contains(ledgerSearchQuery, ignoreCase = true) ||
            it.voucherId.toString().contains(ledgerSearchQuery)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // ۱. کارت انتخاب طرف‌حساب با استایل لوکس
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanSurface),
                    border = BorderStroke(1.dp, CleanBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NavyPrimary.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "دفتر معین تفصیلی اشخاص و طرف‌حساب‌ها",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                                Text(
                                    text = "مشاهده ریز گردش بدهکار، بستانکار و مانده حساب مشتریان و تأمین‌کنندگان",
                                    fontSize = 11.sp,
                                    color = TextDarkSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ExposedDropdownMenuBox(
                            expanded = partyMenuExpanded,
                            onExpandedChange = { partyMenuExpanded = !partyMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedParty?.let {
                                    "${it.name} (${if (it.partyType == "SUPPLIER") "تأمین‌کننده" else "مشتری"}) - کد: ${NumberFormatUtils.toPersianDigits(it.code)}"
                                } ?: "انتخاب طرف‌حساب برای مشاهده معین...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("طرف‌حساب انتخابی") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyMenuExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MatteGold,
                                    unfocusedBorderColor = CleanBorder,
                                    focusedContainerColor = CleanSurface,
                                    unfocusedContainerColor = CleanSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("select_party_ledger_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = partyMenuExpanded,
                                onDismissRequest = {
                                    partyMenuExpanded = false
                                    partySearchQuery = ""
                                },
                                modifier = Modifier
                                    .background(CleanSurface)
                                    .padding(vertical = 4.dp)
                            ) {
                                // فیلد جستجو در داخل منو
                                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    OutlinedTextField(
                                        value = partySearchQuery,
                                        onValueChange = { partySearchQuery = it },
                                        placeholder = { Text("جستجوی نام یا کد...", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                filteredParties.forEach { party ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = party.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = TextDarkPrimary
                                                    )
                                                    Text(
                                                        text = "${if (party.partyType == "SUPPLIER") "تأمین‌کننده" else "مشتری"} • کد: ${NumberFormatUtils.toPersianDigits(party.code)}",
                                                        fontSize = 11.sp,
                                                        color = TextDarkSecondary
                                                    )
                                                }
                                                val (balText, balColor) = when {
                                                    party.currentBalance > 0 -> "بدهکار: ${NumberFormatUtils.formatPrice(party.currentBalance)}" to AccountingDebitGreen
                                                    party.currentBalance < 0 -> "بستانکار: ${NumberFormatUtils.formatPrice(-party.currentBalance)}" to AccountingCreditRed
                                                    else -> "تسویه" to TextDarkMuted
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = balColor.copy(alpha = 0.1f)
                                                ) {
                                                    Text(
                                                        text = balText,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = balColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onSelectParty(party)
                                            partyMenuExpanded = false
                                            partySearchQuery = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ۲. کارت مشخصات طرف حساب انتخاب‌شده و دکمه‌های عملیاتی
            if (selectedParty != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanSurface),
                        border = BorderStroke(1.dp, CleanBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(NavyPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = selectedParty.name.take(1),
                                            color = MatteGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = selectedParty.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextDarkPrimary
                                        )
                                        Text(
                                            text = "نوع: ${if (selectedParty.partyType == "SUPPLIER") "تأمین‌کننده / فروشنده" else "مشتری / خریدار"} • کد حساب: ${NumberFormatUtils.toPersianDigits(selectedParty.code)}",
                                            fontSize = 11.sp,
                                            color = TextDarkSecondary
                                        )
                                    }
                                }

                                val (statusText, statusColor, statusBg) = when {
                                    selectedParty.currentBalance > 0 -> Triple(
                                        "بدهکار: ${NumberFormatUtils.formatPrice(selectedParty.currentBalance)} تومان",
                                        AccountingDebitGreen,
                                        AccountingDebitGreen.copy(alpha = 0.1f)
                                    )
                                    selectedParty.currentBalance < 0 -> Triple(
                                        "بستانکار: ${NumberFormatUtils.formatPrice(-selectedParty.currentBalance)} تومان",
                                        AccountingCreditRed,
                                        AccountingCreditRed.copy(alpha = 0.1f)
                                    )
                                    else -> Triple("بی‌حساب و تسویه", TextDarkMuted, CleanBackground)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusBg,
                                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = statusText,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CleanBackground, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "تلفن: ${if (selectedParty.mobile.isNotBlank()) NumberFormatUtils.toPersianDigits(selectedParty.mobile) else "ثبت نشده"}",
                                        fontSize = 11.sp,
                                        color = TextDarkSecondary
                                    )
                                }
                                Text(
                                    text = "سقف اعتبار: ${NumberFormatUtils.formatPrice(selectedParty.creditLimit)} تومان",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDarkPrimary
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = CleanBorder
                            )

                            // دکمه‌های عملیاتی: ثبت گردش جدید، پرینت PDF و اکسل
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showAddDialog = true },
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(44.dp)
                                        .testTag("add_party_transaction_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NavyPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MatteGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ثبت گردش جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onExportPdf,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                                    border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF معین", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = onExportCsv,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PersianGold),
                                    border = BorderStroke(1.dp, PersianGold)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, tint = PersianGold, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اکسل", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ۳. جستجو و سربرگ فهرست ریز گردش معین
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "ریز گردش حساب معین (${NumberFormatUtils.toPersianDigits(ledgerEntries.size)} سطر)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextDarkPrimary
                            )
                        }
                        Text(
                            text = "امکان ویرایش و حذف",
                            fontSize = 11.sp,
                            color = MatteGold,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (ledgerEntries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = ledgerSearchQuery,
                            onValueChange = { ledgerSearchQuery = it },
                            placeholder = { Text("جستجو در شرح سند یا شماره سند...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (ledgerSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { ledgerSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CleanSurface,
                                unfocusedContainerColor = CleanSurface,
                                focusedBorderColor = MatteGold,
                                unfocusedBorderColor = CleanBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )
                    }
                }

                if (filteredEntries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CleanSurface),
                            border = BorderStroke(1.dp, CleanBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = TextDarkMuted,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = if (ledgerEntries.isEmpty()) "هیچ تراکنش یا سندی برای این طرف‌حساب ثبت نشده است." else "تراکنشی منطبق با عبارت جستجو یافت نشد.",
                                        fontSize = 12.sp,
                                        color = TextDarkSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (ledgerEntries.isEmpty()) {
                                        Text(
                                            text = "جهت ثبت سند دستی، از دکمه «ثبت گردش جدید» استفاده فرمایید.",
                                            fontSize = 11.sp,
                                            color = TextDarkMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // محاسبه مانده تجمعی به شکل اصولی
                    var cumulative = 0L
                    val entriesWithBalance = filteredEntries.map { entry ->
                        cumulative += (entry.debit - entry.credit)
                        entry to cumulative
                    }

                    items(entriesWithBalance) { (entry, runningBalance) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CleanSurface),
                            border = BorderStroke(1.dp, CleanBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NavyPrimary.copy(alpha = 0.08f)
                                        ) {
                                            Text(
                                                text = "سند شماره ${NumberFormatUtils.toPersianDigits(entry.voucherId)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = NavyPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { editingItem = entry },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "ویرایش",
                                                tint = NavyPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { deletingItem = entry },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف",
                                                tint = AccountingCreditRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = entry.description,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDarkPrimary
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CleanBackground, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("بدهکار (افزایش طلب):", fontSize = 10.sp, color = TextDarkSecondary)
                                        Text(
                                            text = if (entry.debit > 0) "${NumberFormatUtils.formatPrice(entry.debit)} ت" else "—",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (entry.debit > 0) AccountingDebitGreen else TextDarkMuted
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("بستانکار (پرداخت):", fontSize = 10.sp, color = TextDarkSecondary)
                                        Text(
                                            text = if (entry.credit > 0) "${NumberFormatUtils.formatPrice(entry.credit)} ت" else "—",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (entry.credit > 0) AccountingCreditRed else TextDarkMuted
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("مانده پس از سند:", fontSize = 10.sp, color = TextDarkSecondary)
                                        val runningColor = when {
                                            runningBalance > 0 -> AccountingDebitGreen
                                            runningBalance < 0 -> AccountingCreditRed
                                            else -> TextDarkMuted
                                        }
                                        Text(
                                            text = "${NumberFormatUtils.formatPrice(Math.abs(runningBalance))} ت ${if (runningBalance > 0) "(بد)" else if (runningBalance < 0) "(بس)" else "(تسویه)"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = runningColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modal: ثبت گردش معین جدید
        if (showAddDialog && selectedParty != null) {
            var desc by remember { mutableStateOf("") }
            var debitText by remember { mutableStateOf("") }
            var creditText by remember { mutableStateOf("") }
            var dateText by remember { mutableStateOf(JalaliDate.now().toStandardString()) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MatteGold)
                        Text(
                            text = "ثبت گردش حساب برای ${selectedParty.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDarkPrimary
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("شرح سند یا تراکنش") },
                            placeholder = { Text("مثال: واریز نقدی، تسویه فاکتور دستی، هزینه حمل") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = debitText,
                                onValueChange = {
                                    debitText = it
                                    if (it.isNotBlank()) creditText = ""
                                },
                                label = { Text("بدهکار (تومان)") },
                                placeholder = { Text("افزایش طلب") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = creditText,
                                onValueChange = {
                                    creditText = it
                                    if (it.isNotBlank()) debitText = ""
                                },
                                label = { Text("بستانکار (تومان)") },
                                placeholder = { Text("پرداخت/کاهش") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = dateText,
                            onValueChange = { dateText = it },
                            label = { Text("تاریخ سند (هجری شمسی)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val d = debitText.toLongOrNull() ?: 0L
                            val c = creditText.toLongOrNull() ?: 0L
                            if (desc.isBlank() || (d == 0L && c == 0L)) {
                                Toast.makeText(context, "لطفاً شرح و حداقل یکی از مبالغ بدهکار یا بستانکار را وارد نمایید.", Toast.LENGTH_SHORT).show()
                            } else {
                                onAddTransaction(selectedParty.id, desc, d, c, dateText)
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ثبت در دفتر معین", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("انصراف", color = TextDarkSecondary)
                    }
                }
            )
        }

        // Modal: ویرایش ردیف معین
        editingItem?.let { item ->
            var editDesc by remember { mutableStateOf(item.description) }
            var editDebit by remember { mutableStateOf(if (item.debit > 0) item.debit.toString() else "") }
            var editCredit by remember { mutableStateOf(if (item.credit > 0) item.credit.toString() else "") }

            AlertDialog(
                onDismissRequest = { editingItem = null },
                title = {
                    Text(
                        text = "ویرایش گردش سند شماره ${NumberFormatUtils.toPersianDigits(item.voucherId)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDarkPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editDesc,
                            onValueChange = { editDesc = it },
                            label = { Text("شرح گردش") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editDebit,
                                onValueChange = {
                                    editDebit = it
                                    if (it.isNotBlank()) editCredit = ""
                                },
                                label = { Text("بدهکار") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editCredit,
                                onValueChange = {
                                    editCredit = it
                                    if (it.isNotBlank()) editDebit = ""
                                },
                                label = { Text("بستانکار") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val d = editDebit.toLongOrNull() ?: 0L
                            val c = editCredit.toLongOrNull() ?: 0L
                            onUpdateTransaction(item, editDesc, d, c)
                            editingItem = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingItem = null }) {
                        Text("انصراف", color = TextDarkSecondary)
                    }
                }
            )
        }

        // Modal: تایید حذف ردیف معین
        deletingItem?.let { item ->
            AlertDialog(
                onDismissRequest = { deletingItem = null },
                title = {
                    Text(
                        text = "حذف گردش حساب",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AccountingCreditRed
                    )
                },
                text = {
                    Text(
                        text = "آیا از حذف این ردیف گردش حساب از دفتر معین اطمینان دارید؟ اثر مالی آن بر مانده حساب طرف‌حساب نیز معکوس خواهد شد.",
                        fontSize = 13.sp,
                        color = TextDarkPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteTransaction(item)
                            deletingItem = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حذف قطعی", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingItem = null }) {
                        Text("انصراف", color = TextDarkSecondary)
                    }
                }
            )
        }
    }
}
