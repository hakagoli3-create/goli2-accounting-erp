package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.model.InvoiceEntity
import com.example.data.model.PartyEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.PartyViewModel
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(
    viewModel: PartyViewModel
) {
    val context = LocalContext.current
    val parties by viewModel.filteredParties.collectAsStateWithLifecycle()
    val allParties by viewModel.allParties.collectAsStateWithLifecycle()
    val totalReceivables by viewModel.totalReceivables.collectAsStateWithLifecycle()
    val totalPayables by viewModel.totalPayables.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPartyForStatement by viewModel.selectedParty.collectAsStateWithLifecycle()
    val partyInvoices by viewModel.partyInvoices.collectAsStateWithLifecycle()

    var showPartyDialog by remember { mutableStateOf(false) }
    var editingParty by remember { mutableStateOf<PartyEntity?>(null) }
    var deletingParty by remember { mutableStateOf<PartyEntity?>(null) }

    val tabs = listOf("مشتریان", "تأمین‌کنندگان", "بدهکاران", "تجاوز از اعتبار")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                ErpTopAppBar(
                    title = "مدیریت مشتریان و طرف‌های حساب (CRM)",
                    subtitle = "دفتر اشخاص، سقف اعتبار، مانده حساب، سوابق فاکتور و صورت‌حساب"
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingParty = null
                        showPartyDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_party_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "ثبت مشتری جدید")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Top KPI Summary Header: Customer counts, total receivables and payables
                CrmKpiHeader(
                    allParties = allParties,
                    totalReceivables = totalReceivables,
                    totalPayables = totalPayables
                )

                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { viewModel.setSelectedTab(index) },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("crm_tab_$index")
                        )
                    }
                }

                // Search Box and Fast Filter Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("جستجوی نام مشتری، فروشگاه، موبایل، کد تفصیلی...", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_party_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            editingParty = null
                            showPartyDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("add_party_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("شخص جدید", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Results count and summary line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعداد در لیست: ${NumberFormatUtils.toPersianDigits(parties.size)} نفر",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "فیلتر عبارت: «$searchQuery»",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Party List
                if (parties.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "هیچ طرف حسابی با مشخصات انتخابی یافت نشد.",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (searchQuery.isNotEmpty()) {
                                OutlinedButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Text("پاک‌کردن فیلتر جستجو", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                    ) {
                        items(parties, key = { it.id }) { party ->
                            PartyCardItem(
                                party = party,
                                onOpenStatement = { viewModel.selectPartyForStatement(party) },
                                onEdit = {
                                    editingParty = party
                                    showPartyDialog = true
                                },
                                onDelete = { deletingParty = party },
                                onCall = {
                                    val phoneToCall = party.mobile.ifBlank { party.phone }
                                    if (phoneToCall.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phoneToCall")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "شماره تماسی برای این شخص ثبت نشده است.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Party Statement & Purchase History Modal
    selectedPartyForStatement?.let { party ->
        CustomerStatementDialog(
            party = party,
            invoices = partyInvoices,
            onDismiss = { viewModel.selectPartyForStatement(null) }
        )
    }

    // Add / Edit Party Dialog
    if (showPartyDialog) {
        AddOrEditPartyDialog(
            partyToEdit = editingParty,
            onDismiss = {
                showPartyDialog = false
                editingParty = null
            },
            onSave = { code, name, company, mobile, phone, address, national, economic, type, credit, notes ->
                if (editingParty != null) {
                    val updated = editingParty!!.copy(
                        code = code,
                        name = name.trim(),
                        companyName = company.trim(),
                        mobile = mobile.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        nationalCode = national.trim(),
                        economicCode = economic.trim(),
                        partyType = type,
                        creditLimit = credit,
                        notes = notes.trim()
                    )
                    viewModel.updateParty(updated) {
                        Toast.makeText(context, "اطلاعات «$name» با موفقیت ویرایش گردید.", Toast.LENGTH_SHORT).show()
                        showPartyDialog = false
                        editingParty = null
                    }
                } else {
                    viewModel.addParty(code, name, company, mobile, phone, address, national, type, credit, notes) {
                        Toast.makeText(context, "طرف حساب «$name» با موفقیت افزوده شد.", Toast.LENGTH_SHORT).show()
                        showPartyDialog = false
                    }
                }
            }
        )
    }

    // Delete Party Dialog
    deletingParty?.let { party ->
        AlertDialog(
            onDismissRequest = { deletingParty = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed) },
            title = {
                Text(
                    text = "حذف طرف حساب «${party.name}»",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "آیا از حذف این طرف حساب از سیستم اطمینان دارید؟",
                        fontSize = 12.sp
                    )
                    if (party.currentBalance != 0L) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AccountingCreditRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "هشدار: این شخص دارای مانده حساب فعال (${NumberFormatUtils.formatPrice(party.currentBalance)}) است.",
                                fontSize = 11.sp,
                                color = AccountingCreditRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteParty(party) {
                            Toast.makeText(context, "طرف حساب حذف گردید.", Toast.LENGTH_SHORT).show()
                            deletingParty = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("حذف قطعی", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingParty = null }) {
                    Text("انصراف", fontSize = 11.sp)
                }
            }
        )
    }
}

/**
 * Top KPI Summary Header: Customer counts, total receivables, and payables
 */
@Composable
private fun CrmKpiHeader(
    allParties: List<PartyEntity>,
    totalReceivables: Long,
    totalPayables: Long
) {
    val overLimitCount = allParties.count { it.isCreditLimitExceeded() }
    val debtorsCount = allParties.count { it.currentBalance > 0 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Receivables (مطالبات از مشتریان)
            CrmKpiPill(
                title = "کل مطالبات (طلب ما)",
                subtitle = "${NumberFormatUtils.toPersianDigits(debtorsCount)} شخص بدهکار",
                amount = totalReceivables,
                icon = Icons.Default.TrendingUp,
                accentColor = AccountingDebitGreen,
                bgColor = AccountingDebitGreen.copy(alpha = 0.10f)
            )

            // Card 2: Total Payables (بدهی ما به تأمین‌کنندگان)
            CrmKpiPill(
                title = "بدهی به تأمین‌کنندگان",
                subtitle = "اسناد و فاکتورهای باز خرید",
                amount = totalPayables,
                icon = Icons.Default.TrendingDown,
                accentColor = AccountingCreditRed,
                bgColor = AccountingCreditRed.copy(alpha = 0.10f)
            )

            // Card 3: Over Limit Count or Total Active Parties
            CrmKpiPill(
                title = "کل مخاطبین و اشخاص",
                subtitle = if (overLimitCount > 0) "${NumberFormatUtils.toPersianDigits(overLimitCount)} نفر مازاد سقف اعتبار" else "همه در محدوده مجاز اعتبار",
                amount = allParties.size.toLong(),
                isCurrency = false,
                icon = if (overLimitCount > 0) Icons.Default.Warning else Icons.Default.People,
                accentColor = if (overLimitCount > 0) AlertWarningOrange else PersianGold,
                bgColor = if (overLimitCount > 0) AlertWarningOrange.copy(alpha = 0.12f) else PersianGold.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun CrmKpiPill(
    title: String,
    subtitle: String,
    amount: Long,
    isCurrency: Boolean = true,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isCurrency) NumberFormatUtils.formatPrice(amount) else "${NumberFormatUtils.toPersianDigits(amount)} طرف‌حساب",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
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
 * Modern Card Item for Customer / Party
 */
@Composable
private fun PartyCardItem(
    party: PartyEntity,
    onOpenStatement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    val isOverLimit = party.isCreditLimitExceeded()
    val creditUsagePercent = if (party.creditLimit > 0) {
        (party.currentBalance.toFloat() / party.creditLimit.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val typeLabel = when (party.partyType) {
        "SUPPLIER" -> "تأمین‌کننده"
        "BOTH" -> "مشتری و تأمین‌کننده"
        else -> "مشتری"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenStatement() }
            .testTag("party_item_${party.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverLimit) AccountingCreditRed.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isOverLimit) AccountingCreditRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar + Name + Type Badge + Balance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = party.name.take(1),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = party.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = typeLabel,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (party.companyName.isNotBlank()) {
                            Text(
                                text = party.companyName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Balance Badge
                val (balText, balColor, balBg) = when {
                    party.currentBalance > 0 -> Triple(
                        "بدهکار: ${NumberFormatUtils.formatPrice(party.currentBalance)}",
                        AccountingDebitGreen,
                        AccountingDebitGreen.copy(alpha = 0.12f)
                    )
                    party.currentBalance < 0 -> Triple(
                        "بستانکار: ${NumberFormatUtils.formatPrice(-party.currentBalance)}",
                        AccountingCreditRed,
                        AccountingCreditRed.copy(alpha = 0.12f)
                    )
                    else -> Triple(
                        "بی‌حساب (تسویه)",
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(balBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = balText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = balColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Info & Code Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (party.mobile.isNotBlank()) NumberFormatUtils.toPersianDigits(party.mobile) else (party.phone.ifBlank { "بدون تلفن" }),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (party.phone.isNotBlank() && party.mobile.isNotBlank()) {
                        Text(
                            text = " | ${NumberFormatUtils.toPersianDigits(party.phone)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "کد تفصیلی: ${NumberFormatUtils.toPersianDigits(party.code)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Credit Limit Progress Bar
            if (party.creditLimit > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مصرف اعتبار: ${NumberFormatUtils.toPersianDigits(String.format("%.0f", creditUsagePercent * 100))}%",
                        fontSize = 10.sp,
                        color = if (isOverLimit) AccountingCreditRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "سقف: ${NumberFormatUtils.formatPrice(party.creditLimit)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { creditUsagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (isOverLimit) AccountingCreditRed else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (isOverLimit) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "هشدار: بدهی این مشتری از سقف اعتبار مجاز عبور کرده است!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccountingCreditRed
                    )
                }
            }

            // Address if present
            if (party.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = party.address,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Statement & Purchase History Button
                Button(
                    onClick = onOpenStatement,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "سوابق فاکتور و خرید",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (party.mobile.isNotBlank() || party.phone.isNotBlank()) {
                        IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "تماس", tint = AccountingDebitGreen, modifier = Modifier.size(17.dp))
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

/**
 * Statement & Purchase Records Dialog (سوابق خرید، پرداخت و فاکتورها)
 */
@Composable
private fun CustomerStatementDialog(
    party: PartyEntity,
    invoices: List<InvoiceEntity>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "صورت‌حساب و سوابق: ${party.name}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (party.companyName.isNotBlank()) party.companyName else "کد تفصیلی: ${NumberFormatUtils.toPersianDigits(party.code)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary Financial Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "وضعیت مانده حساب:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val balText = when {
                                party.currentBalance > 0 -> "بدهکار به ما: ${NumberFormatUtils.formatPrice(party.currentBalance)}"
                                party.currentBalance < 0 -> "بستانکار از ما: ${NumberFormatUtils.formatPrice(-party.currentBalance)}"
                                else -> "تسویه و بی‌حساب"
                            }
                            Text(
                                text = balText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (party.currentBalance > 0) AccountingDebitGreen else if (party.currentBalance < 0) AccountingCreditRed else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "تعداد کل فاکتورها:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${NumberFormatUtils.toPersianDigits(invoices.size)} فاکتور",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ریز فاکتورهای خرید و صادره:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "هیچ فاکتوری برای این طرف حساب ثبت نشده است.",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(invoices, key = { it.id }) { inv ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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
                                            text = NumberFormatUtils.formatPrice(inv.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "نوع: ${NumberFormatUtils.formatInvoiceType(inv.invoiceType)} | روش تسویه: ${NumberFormatUtils.formatPaymentType(inv.paymentType)}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (inv.creditAmount > 0) {
                                            Text(
                                                text = "نسیه: ${NumberFormatUtils.formatPrice(inv.creditAmount)}",
                                                fontSize = 10.sp,
                                                color = AccountingDebitGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Text(
                                                text = "تسویه نقدی/کارتخوان",
                                                fontSize = 10.sp,
                                                color = AccountingDebitGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بستن صورت‌حساب", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Add or Edit Party Modal Dialog (ثبت و ویرایش اطلاعات مشتری)
 */
@Composable
private fun AddOrEditPartyDialog(
    partyToEdit: PartyEntity?,
    onDismiss: () -> Unit,
    onSave: (
        code: String,
        name: String,
        company: String,
        mobile: String,
        phone: String,
        address: String,
        national: String,
        economic: String,
        partyType: String,
        creditLimit: Long,
        notes: String
    ) -> Unit
) {
    var name by remember(partyToEdit) { mutableStateOf(partyToEdit?.name ?: "") }
    var company by remember(partyToEdit) { mutableStateOf(partyToEdit?.companyName ?: "") }
    var code by remember(partyToEdit) { mutableStateOf(partyToEdit?.code ?: "PRT-${System.currentTimeMillis() % 100000}") }
    var mobile by remember(partyToEdit) { mutableStateOf(partyToEdit?.mobile ?: "") }
    var phone by remember(partyToEdit) { mutableStateOf(partyToEdit?.phone ?: "") }
    var address by remember(partyToEdit) { mutableStateOf(partyToEdit?.address ?: "") }
    var national by remember(partyToEdit) { mutableStateOf(partyToEdit?.nationalCode ?: "") }
    var economic by remember(partyToEdit) { mutableStateOf(partyToEdit?.economicCode ?: "") }
    var creditText by remember(partyToEdit) { mutableStateOf(if (partyToEdit != null) partyToEdit.creditLimit.toString() else "50000000") }
    var partyType by remember(partyToEdit) { mutableStateOf(partyToEdit?.partyType ?: "CUSTOMER") }
    var notes by remember(partyToEdit) { mutableStateOf(partyToEdit?.notes ?: "") }

    val creditLong = creditText.toLongOrNull() ?: 0L

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
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
                        text = if (partyToEdit != null) "ویرایش طرف حساب: ${partyToEdit.name}" else "ثبت مشتری / طرف‌حساب جدید",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                // Party Type Selector
                Text("نوع طرف حساب:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "CUSTOMER" to "مشتری",
                        "SUPPLIER" to "تأمین‌کننده",
                        "BOTH" to "هر دو (مشتری و فروشنده)"
                    ).forEach { (typeVal, typeText) ->
                        FilterChip(
                            selected = partyType == typeVal,
                            onClick = { partyType = typeVal },
                            label = { Text(typeText, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Name and Company
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی (الزامی)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("نام شرکت / فروشگاه") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد تفصیلی") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Contact: Mobile & Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = NumberFormatUtils.toEnglishDigits(it) },
                        label = { Text("شماره موبایل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = NumberFormatUtils.toEnglishDigits(it) },
                        label = { Text("تلفن ثابت") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Credit Limit
                OutlinedTextField(
                    value = creditText,
                    onValueChange = { creditText = NumberFormatUtils.toEnglishDigits(it).filter { ch -> ch.isDigit() } },
                    label = { Text("سقف اعتبار ریالی به تومان (۰ یعنی بدون سقف یا نقدی)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_credit_limit_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick credit buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        10_000_000L to "۱۰ میلیون",
                        20_000_000L to "۲۰ میلیون",
                        50_000_000L to "۵۰ میلیون",
                        100_000_000L to "۱۰۰ میلیون",
                        0L to "بدون سقف"
                    ).forEach { (amt, label) ->
                        AssistChip(
                            onClick = { creditText = amt.toString() },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                if (creditLong > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "سقف اعتبار: ${NumberFormatUtils.numberToPersianWords(creditLong)} تومان",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // National & Economic Code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = national,
                        onValueChange = { national = NumberFormatUtils.toEnglishDigits(it) },
                        label = { Text("کد ملی / شناسه ملی") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = economic,
                        onValueChange = { economic = NumberFormatUtils.toEnglishDigits(it) },
                        label = { Text("کد اقتصادی") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("آدرس کامل پستی") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و یادداشت‌ها") },
                    minLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        onSave(
                            code,
                            name,
                            company,
                            mobile,
                            phone,
                            address,
                            national,
                            economic,
                            partyType,
                            creditLong,
                            notes
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_party_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (partyToEdit != null) "ذخیره تغییرات طرف‌حساب" else "ثبت نهایی در دفتر اشخاص و CRM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
