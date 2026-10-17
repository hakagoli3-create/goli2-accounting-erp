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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InvoiceEntity
import com.example.data.model.PartyEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.PartyViewModel
import com.example.util.ExportHelper
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(
    viewModel: PartyViewModel
) {
    val context = LocalContext.current
    val parties by viewModel.filteredParties.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPartyForStatement by viewModel.selectedParty.collectAsStateWithLifecycle()
    val partyInvoices by viewModel.partyInvoices.collectAsStateWithLifecycle()
    val reportStats by viewModel.categoryReport.collectAsStateWithLifecycle()

    var showAddPartyDialog by remember { mutableStateOf(false) }
    var editingParty by remember { mutableStateOf<PartyEntity?>(null) }
    var partyToDelete by remember { mutableStateOf<PartyEntity?>(null) }

    val tabs = listOf(
        "مشتریان عمده",
        "مشتریان تکی / خرده",
        "تامین‌کنندگان",
        "گزارش تحلیلی و اعتبار"
    )

    Scaffold(
        topBar = {
            Column {
                ErpTopAppBar(
                    title = "طرف‌حساب‌ها و مدیریت مشتریان (CRM)",
                    subtitle = "تفکیک مشتریان عمده و تکی، سقف اعتبار و گزارش حجم فروش"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { viewModel.setSelectedTab(index) },
                            text = { Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("crm_tab_$index")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != 3) {
                FloatingActionButton(
                    onClick = { showAddPartyDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_party_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "ثبت طرف‌حساب جدید", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == 3) {
                // Customer Volume & Turnover Report Tab
                CustomerCategoryReportView(
                    stats = reportStats,
                    onExportPdf = {
                        val headers = listOf("شاخص گزارش", "مشتریان عمده", "مشتریان تکی/خرده", "تامین‌کنندگان")
                        val rows = listOf(
                            listOf("تعداد طرف‌حساب‌ها", "${reportStats.wholesaleCount} نفر", "${reportStats.retailCount} نفر", "${reportStats.supplierCount} نفر"),
                            listOf("حجم فروش قطعی", NumberFormatUtils.formatPrice(reportStats.wholesaleSalesVolume), NumberFormatUtils.formatPrice(reportStats.retailSalesVolume), "-"),
                            listOf("مانده بدهکاری (طلب ما)", NumberFormatUtils.formatPrice(reportStats.wholesaleTotalBalance), NumberFormatUtils.formatPrice(reportStats.retailTotalBalance), "-"),
                            listOf("تجاوز از سقف اعتبار", "${reportStats.overCreditCount} مورد", "-", "-")
                        )
                        val f = ExportHelper.exportReportPdf(context, "گزارش تفکیکی مشتریان عمده و تکی", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "application/pdf", "گزارش مشتریان PDF")
                    },
                    onExportCsv = {
                        val headers = listOf("بخش", "تعداد", "حجم فروش (تومان)", "مانده حساب (تومان)")
                        val rows = listOf(
                            listOf("مشتریان عمده", reportStats.wholesaleCount.toString(), reportStats.wholesaleSalesVolume.toString(), reportStats.wholesaleTotalBalance.toString()),
                            listOf("مشتریان تکی", reportStats.retailCount.toString(), reportStats.retailSalesVolume.toString(), reportStats.retailTotalBalance.toString()),
                            listOf("تامین‌کنندگان", reportStats.supplierCount.toString(), "0", "0")
                        )
                        val f = ExportHelper.exportReportCsv(context, "گزارش_مشتریان_عمده_تکی", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "text/csv", "خروجی اکسل مشتریان")
                    }
                )
            } else {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("جستجوی نام مشتری، فروشگاه، موبایل یا کد...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_party_input")
                )

                if (parties.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when (selectedTab) {
                                0 -> "هیچ مشتری عمده‌ای ثبت نشده است."
                                1 -> "هیچ مشتری تکی یا خرده‌فروشی یافت نشد."
                                2 -> "تامین‌کننده‌ای یافت نشد."
                                else -> "موردی یافت نشد."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(parties) { party ->
                            val isOverLimit = party.isCreditLimitExceeded()
                            val creditUsagePercent = if (party.creditLimit > 0) {
                                (party.currentBalance.toFloat() / party.creditLimit.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectPartyForStatement(party) }
                                    .testTag("party_item_${party.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOverLimit) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        if (party.isWholesaleCustomer) PersianGold.copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.primaryContainer,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (party.partyType == "SUPPLIER") Icons.Default.Business
                                                    else if (party.isWholesaleCustomer) Icons.Default.Store
                                                    else Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (party.isWholesaleCustomer) PersianGold else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = party.name,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                if (party.companyName.isNotBlank()) {
                                                    Text(
                                                        text = party.companyName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Badge
                                            val badgeText = when {
                                                party.partyType == "SUPPLIER" -> "تامین‌کننده"
                                                party.isWholesaleCustomer -> "مشتری عمده"
                                                else -> "مشتری تکی"
                                            }
                                            val badgeBg = when {
                                                party.partyType == "SUPPLIER" -> Color(0xFFE8F5E9)
                                                party.isWholesaleCustomer -> Color(0xFFFFF8E1)
                                                else -> Color(0xFFE3F2FD)
                                            }
                                            val badgeColor = when {
                                                party.partyType == "SUPPLIER" -> Color(0xFF2E7D32)
                                                party.isWholesaleCustomer -> Color(0xFFB78103)
                                                else -> Color(0xFF1565C0)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeBg, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(text = badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (isOverLimit) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(AccountingCreditRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Warning, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text("سقف اعتبار", color = AccountingCreditRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${party.mobile.ifBlank { "بدون همراه" }} | کد: ${party.code}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = if (party.partyType == "SUPPLIER") "بستانکاری: " + NumberFormatUtils.formatPrice(Math.abs(party.currentBalance))
                                            else "مانده بدهکاری: " + NumberFormatUtils.formatPrice(party.currentBalance),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (party.currentBalance > 0) AccountingDebitGreen else if (party.currentBalance < 0) AccountingCreditRed else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }

                                    if (party.creditLimit > 0) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "مصرف اعتبار: ${(creditUsagePercent * 100).toInt()}%",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "سقف: ${NumberFormatUtils.formatPrice(party.creditLimit)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { creditUsagePercent },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp),
                                            color = if (isOverLimit) AccountingCreditRed else PersianGold,
                                            trackColor = Color.LightGray.copy(alpha = 0.4f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { partyToDelete = party },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        OutlinedButton(
                                            onClick = { editingParty = party },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ویرایش", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = { viewModel.selectPartyForStatement(party) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("دفتر معین و فاکتورها", fontSize = 11.sp)
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

    // Party Statement / Customer Ledger Modal
    selectedPartyForStatement?.let { party ->
        CustomerLedgerDialog(
            party = party,
            invoices = partyInvoices,
            onDismiss = { viewModel.selectPartyForStatement(null) }
        )
    }

    // Edit Party Dialog
    editingParty?.let { p ->
        EditPartyDialog(
            party = p,
            onDismiss = { editingParty = null },
            onConfirm = { updated ->
                viewModel.updateParty(updated) {
                    Toast.makeText(context, "مشخصات ${updated.name} با موفقیت ویرایش شد.", Toast.LENGTH_SHORT).show()
                    editingParty = null
                }
            }
        )
    }

    // Add Party Dialog
    if (showAddPartyDialog) {
        val defaultCategory = if (selectedTab == 0) "WHOLESALE" else "RETAIL"
        val defaultType = if (selectedTab == 2) "SUPPLIER" else "CUSTOMER"

        AddPartyDialog(
            initialCategory = defaultCategory,
            initialType = defaultType,
            onDismiss = { showAddPartyDialog = false },
            onConfirm = { code, name, company, mobile, phone, address, national, type, category, credit, notes ->
                viewModel.addParty(code, name, company, mobile, phone, address, national, type, category, credit, notes) {
                    Toast.makeText(context, "طرف‌حساب جدید با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                    showAddPartyDialog = false
                }
            }
        )
    }

    // Delete confirmation
    partyToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { partyToDelete = null },
            title = { Text("حذف طرف‌حساب") },
            text = { Text("آیا از حذف طرف‌حساب «${p.name}» اطمینان دارید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteParty(p) {
                            Toast.makeText(context, "طرف‌حساب حذف شد", Toast.LENGTH_SHORT).show()
                            partyToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { partyToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun CustomerCategoryReportView(
    stats: com.example.ui.viewmodel.CustomerCategoryReport,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("گزارش تحلیلی تفکیک مشتریان عمده و تکی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("مقایسه حجم فروش، بدهکاری و سقف اعتبار", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row {
                Button(
                    onClick = onExportPdf,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onExportCsv,
                    colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اکسل", fontSize = 11.sp)
                }
            }
        }

        // Wholesale Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = PersianGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشتریان عمده‌فروشی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("${stats.wholesaleCount} طرف‌حساب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PersianGold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("حجم فروش عمده:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(NumberFormatUtils.formatPrice(stats.wholesaleSalesVolume), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مانده طلب (بدهکاری عمده):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(NumberFormatUtils.formatPrice(stats.wholesaleTotalBalance), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccountingDebitGreen)
                    }
                }
            }
        }

        // Retail Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE).copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشتریان تکی و خرده‌فروشی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("${stats.retailCount} طرف‌حساب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("حجم فروش تکی / POS:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(NumberFormatUtils.formatPrice(stats.retailSalesVolume), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مانده طلب خرده‌فروشی:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(NumberFormatUtils.formatPrice(stats.retailTotalBalance), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccountingDebitGreen)
                    }
                }
            }
        }

        // Credit Limits & Over-Limit Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertWarningOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("وضعیت سقف اعتبار و ریسک اعتباری", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تعداد مشتریانی که مانده بدهی آن‌ها از سقف مجاز فراتر رفته است: ${stats.overCreditCount} نفر",
                    fontSize = 12.sp,
                    color = if (stats.overCreditCount > 0) AccountingCreditRed else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CustomerLedgerDialog(
    party: PartyEntity,
    invoices: List<InvoiceEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
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
                            text = "دفتر معین و صورت‌حساب: ${party.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "گروه: ${if (party.isWholesaleCustomer) "مشتری عمده" else if (party.partyType == "SUPPLIER") "تامین‌کننده" else "مشتری تکی"} | مانده: ${NumberFormatUtils.formatPrice(party.currentBalance)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val headers = listOf("شماره فاکتور", "تاریخ", "نحوه تسویه", "وضعیت", "مبلغ (تومان)")
                            val rows = invoices.map {
                                listOf(it.invoiceNumber, it.jalaliDate, it.paymentType, it.status, NumberFormatUtils.formatPrice(it.totalAmount))
                            }
                            val f = ExportHelper.exportReportPdf(context, "صورت‌حساب معین: ${party.name}", headers, rows)
                            if (f != null) ExportHelper.shareFile(context, f, "application/pdf", "معین ${party.name} PDF")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF معین", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val headers = listOf("شماره فاکتور", "تاریخ", "تسویه", "مبلغ کل")
                            val rows = invoices.map {
                                listOf(it.invoiceNumber, it.jalaliDate, it.paymentType, it.totalAmount.toString())
                            }
                            val f = ExportHelper.exportReportCsv(context, "معین_${party.name}", headers, rows)
                            if (f != null) ExportHelper.shareFile(context, f, "text/csv", "اکسل معین")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اکسل معین", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("فاکتوری برای این طرف‌حساب ثبت نشده است.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(invoices) { inv ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "فاکتور ${inv.invoiceNumber} (${inv.jalaliDate})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "نوع: ${inv.invoiceType} | تسویه: ${inv.paymentType}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = NumberFormatUtils.formatPrice(inv.totalAmount),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
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

@Composable
fun AddPartyDialog(
    initialCategory: String = "RETAIL",
    initialType: String = "CUSTOMER",
    onDismiss: () -> Unit,
    onConfirm: (
        code: String,
        name: String,
        company: String,
        mobile: String,
        phone: String,
        address: String,
        national: String,
        type: String,
        category: String,
        creditLimit: Long,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var national by remember { mutableStateOf("") }
    var creditText by remember { mutableStateOf("30000000") }
    var partyType by remember { mutableStateOf(initialType) }
    var customerCategory by remember { mutableStateOf(initialCategory) }
    var notes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ثبت طرف‌حساب جدید",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Party Type: Customer vs Supplier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { partyType = "CUSTOMER" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (partyType == "CUSTOMER") MaterialTheme.colorScheme.primary else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("مشتری", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { partyType = "SUPPLIER" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (partyType == "SUPPLIER") MaterialTheme.colorScheme.primary else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تامین‌کننده", fontSize = 12.sp)
                    }
                }

                if (partyType == "CUSTOMER") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("سطح مشتری:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { customerCategory = "WHOLESALE" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customerCategory == "WHOLESALE") PersianGold else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مشتری عمده", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { customerCategory = "RETAIL" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customerCategory == "RETAIL") MaterialTheme.colorScheme.secondary else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مشتری تکی / خرده", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی طرف‌حساب") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("نام فروشگاه / بنکداری / شرکت") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("شماره همراه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("تلفن ثابت") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = creditText,
                    onValueChange = { creditText = it },
                    label = { Text("سقف اعتبار ریالی مجاز (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_credit_limit_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("آدرس پستی و موقعیت") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("یادداشت و شرایط همکاری") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val credit = creditText.toLongOrNull() ?: 0L
                        onConfirm("", name, company, mobile, phone, address, national, partyType, customerCategory, credit, notes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_party_button")
                ) {
                    Text("ثبت و ذخیره در سیستم", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditPartyDialog(
    party: PartyEntity,
    onDismiss: () -> Unit,
    onConfirm: (PartyEntity) -> Unit
) {
    var name by remember { mutableStateOf(party.name) }
    var company by remember { mutableStateOf(party.companyName) }
    var mobile by remember { mutableStateOf(party.mobile) }
    var phone by remember { mutableStateOf(party.phone) }
    var address by remember { mutableStateOf(party.address) }
    var national by remember { mutableStateOf(party.nationalCode) }
    var creditText by remember { mutableStateOf(party.creditLimit.toString()) }
    var balanceText by remember { mutableStateOf(party.currentBalance.toString()) }
    var customerCategory by remember { mutableStateOf(party.customerCategory) }
    var notes by remember { mutableStateOf(party.notes) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ویرایش مشخصات طرف‌حساب",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer group toggle
                if (party.partyType in listOf("CUSTOMER", "BOTH")) {
                    Text("گروه مشتری:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { customerCategory = "WHOLESALE" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customerCategory == "WHOLESALE") PersianGold else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مشتری عمده", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { customerCategory = "RETAIL" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customerCategory == "RETAIL") MaterialTheme.colorScheme.secondary else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مشتری تکی / خرده", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("نام فروشگاه / شرکت") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("شماره همراه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("تلفن ثابت") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = creditText,
                        onValueChange = { creditText = it },
                        label = { Text("سقف اعتبار (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("مانده حساب فعلی (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("آدرس") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و یادداشت") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val credit = creditText.toLongOrNull() ?: 0L
                        val balance = balanceText.toLongOrNull() ?: party.currentBalance
                        onConfirm(
                            party.copy(
                                name = name,
                                companyName = company,
                                mobile = mobile,
                                phone = phone,
                                address = address,
                                nationalCode = national,
                                customerCategory = customerCategory,
                                creditLimit = credit,
                                currentBalance = balance,
                                notes = notes
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ذخیره تغییرات طرف‌حساب", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
