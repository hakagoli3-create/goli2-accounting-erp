package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountGroupEntity
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.TrialBalanceRow
import com.example.ui.components.ErpTopAppBar
import com.example.ui.components.PartySubsidiaryLedgerContent
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.MatteGoldLight
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PersianGold
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.ProfitLossStatement
import com.example.ui.viewmodel.VoucherItemDraft
import com.example.util.ExportHelper
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel
) {
    val context = LocalContext.current
    val securityManager = remember { AppSecurityAndSettingsManager.getInstance(context) }
    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        "سرفصل‌های حسابداری",
        "اسناد مالی و روزنامه",
        "دفتر معین طرف‌حساب‌ها",
        "ثبت سند دوبل",
        "تراز آزمایشی",
        "صورت سود و زیان"
    )

    val groups by viewModel.allGroups.collectAsStateWithLifecycle()
    val generals by viewModel.allGenerals.collectAsStateWithLifecycle()
    val subsidiaries by viewModel.allSubsidiaries.collectAsStateWithLifecycle()
    val journalEntries by viewModel.allJournalEntries.collectAsStateWithLifecycle()
    val parties by viewModel.allParties.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val selectedParty by viewModel.selectedPartyForLedger.collectAsStateWithLifecycle()
    val partyLedgerEntries by viewModel.partyLedgerEntries.collectAsStateWithLifecycle()
    val trialBalance by viewModel.trialBalance.collectAsStateWithLifecycle()
    val profitLoss by viewModel.profitLoss.collectAsStateWithLifecycle()

    val draftDesc by viewModel.draftVoucherDescription.collectAsStateWithLifecycle()
    val draftDate by viewModel.draftVoucherDate.collectAsStateWithLifecycle()
    val draftItems by viewModel.draftItems.collectAsStateWithLifecycle()
    val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()

    LaunchedEffect(operationMessage) {
        operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(NavyPrimary)) {
                    ErpTopAppBar(
                        title = "حسابداری مالی و اسناد",
                        subtitle = "کدینگ حساب‌ها، اسناد دوبل، دفتر معین، تراز آزمایشی و صورت‌های مالی"
                    )

                    // تب‌های اسکرول‌پذیر با دیزاین سرمه‌ای و طلایی
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = NavyPrimary,
                        contentColor = MatteGold,
                        edgePadding = 12.dp,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MatteGold,
                                    height = 3.dp
                                )
                            }
                        },
                        divider = {}
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MatteGold else Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("accounting_tab_$index")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CleanBackground)
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> AccountTreeContent(groups, generals, subsidiaries)
                    1 -> JournalEntriesContent(
                        entries = journalEntries,
                        onExportPdf = {
                            val headers = listOf("شماره سند", "تاریخ سند", "شرح سند", "وضعیت")
                            val rows = journalEntries.map {
                                listOf(it.voucherNumber.toString(), it.jalaliDate, it.description, if (it.isBalanced) "تراز" else "ناهمخوان")
                            }
                            val f = ExportHelper.exportReportPdf(context, "دفتر روزنامه و اسناد حسابداری", headers, rows)
                            if (f != null) ExportHelper.shareFile(context, f, "application/pdf", "اسناد حسابداری PDF")
                        },
                        onExportCsv = {
                            val headers = listOf("شماره سند", "تاریخ", "شرح", "وضعیت")
                            val rows = journalEntries.map {
                                listOf(it.voucherNumber.toString(), it.jalaliDate, it.description, it.status)
                            }
                            val f = ExportHelper.exportReportCsv(context, "فهرست اسناد روزنامه", headers, rows)
                            if (f != null) ExportHelper.shareFile(context, f, "text/csv", "خروجی اکسل اسناد")
                        },
                        onVoidVoucher = { entry ->
                            viewModel.voidVoucher(entry) {}
                        },
                        onUpdateVoucher = { entry, desc, date ->
                            viewModel.updateVoucher(entry, desc, date) {}
                        },
                        onDeleteVoucher = { voucherId ->
                            viewModel.deleteVoucher(voucherId) {}
                        }
                    )
                    2 -> PartySubsidiaryLedgerContent(
                        parties = parties,
                        selectedParty = selectedParty,
                        ledgerEntries = partyLedgerEntries,
                        onSelectParty = { viewModel.selectPartyForLedger(it.id) },
                        onAddTransaction = { pId, desc, d, c, dt ->
                            viewModel.addPartyTransaction(pId, desc, d, c, dt) {}
                        },
                        onUpdateTransaction = { itm, desc, d, c ->
                            viewModel.updatePartyTransaction(itm, desc, d, c) {}
                        },
                        onDeleteTransaction = { itm ->
                            viewModel.deletePartyTransaction(itm) {}
                        },
                        onExportPdf = {
                            if (selectedParty != null) {
                                viewModel.exportPartyLedgerPdf(context, selectedParty!!, partyLedgerEntries)
                            }
                        },
                        onExportCsv = {
                            if (selectedParty != null) {
                                viewModel.exportPartyLedgerCsv(context, selectedParty!!, partyLedgerEntries)
                            }
                        }
                    )
                    3 -> CreateJournalVoucherContent(
                        subsidiaries = subsidiaries,
                        generals = generals,
                        draftDesc = draftDesc,
                        draftDate = draftDate,
                        draftItems = draftItems,
                        onDescChange = { viewModel.updateDraftDescription(it) },
                        onDateChange = { viewModel.updateDraftDate(it) },
                        onAddItem = { viewModel.addDraftItem(it) },
                        onRemoveItem = { viewModel.removeDraftItem(it) },
                        onSubmit = {
                            viewModel.submitJournalVoucher {
                                selectedTab = 1
                            }
                        }
                    )
                    4 -> TrialBalanceContent(
                        trialBalance = trialBalance,
                        onExportPdf = { viewModel.exportTrialBalancePdf(context) },
                        onExportCsv = { viewModel.exportTrialBalanceCsv(context) }
                    )
                    5 -> ProfitLossContent(
                        statement = profitLoss,
                        canViewProfit = currentRole.canViewProfit,
                        onExportPdf = { viewModel.exportProfitLossPdf(context) },
                        onExportCsv = { viewModel.exportProfitLossCsv(context) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// تب ۰: سرفصل‌های حسابداری (درخت حساب‌ها / کدینگ استاندارد)
// ============================================================================
@Composable
private fun AccountTreeContent(
    groups: List<AccountGroupEntity>,
    generals: List<GeneralAccountEntity>,
    subsidiaries: List<SubsidiaryAccountEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandAll by remember { mutableStateOf(true) }

    val filteredGroups = remember(groups, generals, subsidiaries, searchQuery) {
        if (searchQuery.isBlank()) groups
        else {
            val q = searchQuery.trim()
            groups.filter { g ->
                g.title.contains(q, ignoreCase = true) ||
                g.code.toString().contains(q) ||
                generals.any { gen -> gen.groupCode == g.code && (gen.title.contains(q, ignoreCase = true) || gen.code.toString().contains(q)) } ||
                subsidiaries.any { sub -> sub.code.toString().contains(q) || sub.title.contains(q, ignoreCase = true) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // نوار جستجو و خلاصه سرفصل‌ها
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(NavyPrimary.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("ساختار کدینگ و سرفصل‌های حسابداری", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkPrimary)
                                Text("گروه‌ها، حساب‌های کل و حساب‌های معین استاندارد", fontSize = 11.sp, color = TextDarkSecondary)
                            }
                        }

                        TextButton(
                            onClick = { expandAll = !expandAll }
                        ) {
                            Text(
                                text = if (expandAll) "بستن همه" else "باز کردن همه",
                                fontSize = 11.sp,
                                color = MatteGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجو در عنوان یا کد حساب (گروه، کل، معین)...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // ردیف آمار سرفصل‌ها
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CleanBackground, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "گروه‌ها: ${NumberFormatUtils.toPersianDigits(groups.size)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkPrimary
                        )
                        Text(
                            text = "حساب‌های کل: ${NumberFormatUtils.toPersianDigits(generals.size)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkPrimary
                        )
                        Text(
                            text = "حساب‌های معین: ${NumberFormatUtils.toPersianDigits(subsidiaries.size)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MatteGold
                        )
                    }
                }
            }
        }

        // کارت‌های گروه‌های حسابداری
        items(filteredGroups) { group ->
            var expanded by remember(expandAll) { mutableStateOf(expandAll) }
            val groupGenerals = generals.filter { it.groupCode == group.code }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // سربرگ گروه حساب
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NavyPrimary.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "گروه ${NumberFormatUtils.toPersianDigits(group.code)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isDebit = group.nature == "DEBIT"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDebit) AccountingDebitGreen.copy(alpha = 0.1f) else AccountingCreditRed.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (isDebit) "ماهیت بدهکار" else "ماهیت بستانکار",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDebit) AccountingDebitGreen else AccountingCreditRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextDarkSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (groupGenerals.isEmpty()) {
                                Text(
                                    text = "حساب کلی در این گروه تعریف نشده است.",
                                    fontSize = 11.sp,
                                    color = TextDarkMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            groupGenerals.forEach { gen ->
                                val genSubs = subsidiaries.filter { it.generalCode == gen.code }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = CleanBackground),
                                    border = BorderStroke(1.dp, CleanBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        // حساب کل
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MatteGold.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "کل: ${NumberFormatUtils.toPersianDigits(gen.code)}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MatteGold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = gen.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDarkPrimary
                                                )
                                            }
                                            Text(
                                                text = "${NumberFormatUtils.toPersianDigits(genSubs.size)} معین",
                                                fontSize = 10.sp,
                                                color = TextDarkSecondary
                                            )
                                        }

                                        // حساب‌های معین زیرمجموعه
                                        if (genSubs.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                genSubs.forEach { sub ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(CleanSurface, RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            Text(
                                                                text = "↳ ${NumberFormatUtils.toPersianDigits(sub.code)}",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = NavyPrimary
                                                            )
                                                            Text(
                                                                text = sub.title,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = TextDarkPrimary
                                                            )
                                                        }
                                                        Text(
                                                            text = if (sub.nature == "DEBIT") "بدهکار" else "بستانکار",
                                                            fontSize = 9.sp,
                                                            color = if (sub.nature == "DEBIT") AccountingDebitGreen else AccountingCreditRed,
                                                            fontWeight = FontWeight.SemiBold
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
            }
        }
    }
}

// ============================================================================
// تب ۱: اسناد حسابداری (دفتر روزنامه و اسناد مالی)
// ============================================================================
@Composable
private fun JournalEntriesContent(
    entries: List<JournalEntryEntity>,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onVoidVoucher: (JournalEntryEntity) -> Unit,
    onUpdateVoucher: (JournalEntryEntity, String, String) -> Unit,
    onDeleteVoucher: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, BALANCED, UNBALANCED, VOID

    var editingVoucher by remember { mutableStateOf<JournalEntryEntity?>(null) }
    var voidingVoucher by remember { mutableStateOf<JournalEntryEntity?>(null) }
    var deletingVoucherId by remember { mutableStateOf<Long?>(null) }

    val filteredEntries = remember(entries, searchQuery, selectedFilter) {
        entries.filter { entry ->
            val matchesSearch = searchQuery.isBlank() ||
                entry.voucherNumber.toString().contains(searchQuery) ||
                entry.description.contains(searchQuery, ignoreCase = true) ||
                entry.jalaliDate.contains(searchQuery)

            val matchesFilter = when (selectedFilter) {
                "BALANCED" -> entry.isBalanced && entry.status != "VOID"
                "UNBALANCED" -> !entry.isBalanced && entry.status != "VOID"
                "VOID" -> entry.status == "VOID"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // نوار جستجو، فیلترها و دکمه‌های پرینت
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(NavyPrimary.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("دفتر روزنامه و اسناد حسابداری", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkPrimary)
                                Text("مجموع ${NumberFormatUtils.toPersianDigits(entries.size)} سند ثبت شده", fontSize = 11.sp, color = TextDarkSecondary)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = onExportPdf,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onExportCsv,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, PersianGold)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = PersianGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اکسل", fontSize = 11.sp, color = PersianGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجو در شماره سند، شرح یا تاریخ...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
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

                    // چیپ‌های فیلتر وضعیت اسناد
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == "ALL",
                                onClick = { selectedFilter = "ALL" },
                                label = { Text("همه اسناد (${NumberFormatUtils.toPersianDigits(entries.size)})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val balancedCount = entries.count { it.isBalanced && it.status != "VOID" }
                            FilterChip(
                                selected = selectedFilter == "BALANCED",
                                onClick = { selectedFilter = "BALANCED" },
                                label = { Text("اسناد تراز (${NumberFormatUtils.toPersianDigits(balancedCount)})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccountingDebitGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val unbalancedCount = entries.count { !it.isBalanced && it.status != "VOID" }
                            FilterChip(
                                selected = selectedFilter == "UNBALANCED",
                                onClick = { selectedFilter = "UNBALANCED" },
                                label = { Text("ناهمخوان (${NumberFormatUtils.toPersianDigits(unbalancedCount)})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccountingCreditRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val voidCount = entries.count { it.status == "VOID" }
                            FilterChip(
                                selected = selectedFilter == "VOID",
                                onClick = { selectedFilter = "VOID" },
                                label = { Text("باطل شده (${NumberFormatUtils.toPersianDigits(voidCount)})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TextDarkMuted,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
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
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = TextDarkMuted, modifier = Modifier.size(40.dp))
                            Text(
                                text = if (entries.isEmpty()) "هیچ سند حسابداری هنوز ثبت نشده است." else "سندی با مشخصات جستجو شده یافت نشد.",
                                fontSize = 13.sp,
                                color = TextDarkSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredEntries) { entry ->
                val isVoid = entry.status == "VOID"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isVoid) CleanSurface.copy(alpha = 0.7f) else CleanSurface
                    ),
                    border = BorderStroke(1.dp, if (isVoid) CleanBorder else if (!entry.isBalanced) AccountingCreditRed.copy(alpha = 0.4f) else CleanBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isVoid) TextDarkMuted.copy(alpha = 0.15f) else NavyPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "سند شماره ${NumberFormatUtils.toPersianDigits(entry.voucherNumber)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isVoid) TextDarkMuted else NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CleanBackground
                                ) {
                                    Text(
                                        text = NumberFormatUtils.toPersianDigits(entry.jalaliDate),
                                        fontSize = 11.sp,
                                        color = TextDarkSecondary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = entry.description,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isVoid) TextDarkMuted else TextDarkPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // وضعیت تراز و وضعیت قطعی/باطل
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CleanBackground, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = if (entry.isBalanced) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (entry.isBalanced) AccountingDebitGreen else AccountingCreditRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (entry.isBalanced) "سند تراز دوبل دارد" else "عدم انطباق تراز!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (entry.isBalanced) AccountingDebitGreen else AccountingCreditRed
                                )
                            }

                            val (statusLabel, statusColor) = when {
                                isVoid -> "باطل شده" to AccountingCreditRed
                                entry.status == "FINAL" -> "ثبت قطعی" to NavyPrimary
                                else -> "پیش‌نویس" to MatteGold
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = CleanBorder
                        )

                        // دکمه‌های عملیاتی: ویرایش، ابطال و حذف
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isVoid) {
                                OutlinedButton(
                                    onClick = { editingVoucher = entry },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.4f)),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ویرایش سند", fontSize = 11.sp, color = NavyPrimary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { voidingVoucher = entry },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, AccountingCreditRed.copy(alpha = 0.5f)),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("ابطال سند", fontSize = 11.sp, color = AccountingCreditRed)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            OutlinedButton(
                                onClick = { deletingVoucherId = entry.id },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, CleanBorder),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف", fontSize = 11.sp, color = AccountingCreditRed)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: ویرایش شرح و تاریخ سند
    editingVoucher?.let { v ->
        var desc by remember { mutableStateOf(v.description) }
        var date by remember { mutableStateOf(v.jalaliDate) }

        AlertDialog(
            onDismissRequest = { editingVoucher = null },
            title = {
                Text(
                    text = "ویرایش سند شماره ${NumberFormatUtils.toPersianDigits(v.voucherNumber)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextDarkPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("شرح سند مالی") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("تاریخ سند (هجری شمسی)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateVoucher(v, desc, date)
                        editingVoucher = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingVoucher = null }) {
                    Text("انصراف", color = TextDarkSecondary)
                }
            }
        )
    }

    // Modal: تایید ابطال سند
    voidingVoucher?.let { v ->
        AlertDialog(
            onDismissRequest = { voidingVoucher = null },
            title = {
                Text(
                    text = "ابطال سند شماره ${NumberFormatUtils.toPersianDigits(v.voucherNumber)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AccountingCreditRed
                )
            },
            text = {
                Text(
                    text = "آیا از ابطال این سند حسابداری اطمینان دارید؟ وضعیت سند به VOID تغییر خواهد یافت و اثر مالی آن متوقف می‌شود.",
                    fontSize = 13.sp,
                    color = TextDarkPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onVoidVoucher(v)
                        voidingVoucher = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ابطال قطعی سند", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { voidingVoucher = null }) {
                    Text("انصراف", color = TextDarkSecondary)
                }
            }
        )
    }

    // Modal: تایید حذف سند
    deletingVoucherId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingVoucherId = null },
            title = {
                Text(
                    text = "حذف کامل سند مالی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AccountingCreditRed
                )
            },
            text = {
                Text(
                    text = "آیا از حذف این سند و آرتیکل‌های آن اطمینان دارید؟ این عملیات غیرقابل بازگشت است.",
                    fontSize = 13.sp,
                    color = TextDarkPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVoucher(id)
                        deletingVoucherId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف سند", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingVoucherId = null }) {
                    Text("انصراف", color = TextDarkSecondary)
                }
            }
        )
    }
}

// ============================================================================
// تب ۳: ثبت سند دوبل (بدهکار و بستانکار)
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateJournalVoucherContent(
    subsidiaries: List<SubsidiaryAccountEntity>,
    generals: List<GeneralAccountEntity>,
    draftDesc: String,
    draftDate: String,
    draftItems: List<VoucherItemDraft>,
    onDescChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onAddItem: (VoucherItemDraft) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var subSearchQuery by remember { mutableStateOf("") }
    var selectedSub by remember { mutableStateOf<SubsidiaryAccountEntity?>(null) }
    var itemDesc by remember { mutableStateOf("") }
    var itemDebitText by remember { mutableStateOf("") }
    var itemCreditText by remember { mutableStateOf("") }

    val totalDebit = draftItems.sumOf { it.debit }
    val totalCredit = draftItems.sumOf { it.credit }
    val isBalanced = totalDebit == totalCredit && draftItems.isNotEmpty()

    val filteredSubs = remember(subsidiaries, subSearchQuery) {
        if (subSearchQuery.isBlank()) subsidiaries
        else subsidiaries.filter {
            it.title.contains(subSearchQuery, ignoreCase = true) ||
            it.code.toString().contains(subSearchQuery)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // ۱. مشخصات عمومی سند
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(NavyPrimary.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("اطلاعات عمومی سند حسابداری", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkPrimary)
                            Text("شرح کلی و تاریخ صدور سند دوبل مالی", fontSize = 11.sp, color = TextDarkSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = draftDesc,
                        onValueChange = onDescChange,
                        label = { Text("شرح کلی سند حسابداری") },
                        placeholder = { Text("مثلاً: ثبت هزینه کرایه حمل، خرید ملزومات اداری یا تسویه صندوق") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatteGold,
                            unfocusedBorderColor = CleanBorder,
                            focusedContainerColor = CleanSurface,
                            unfocusedContainerColor = CleanSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_description_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draftDate,
                        onValueChange = onDateChange,
                        label = { Text("تاریخ سند (هجری شمسی)") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatteGold,
                            unfocusedBorderColor = CleanBorder,
                            focusedContainerColor = CleanSurface,
                            unfocusedContainerColor = CleanSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ۲. فرم افزودن سطر آرتیکل جدید
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(MatteGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MatteGold, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("افزودن سطر آرتیکل جدید", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkPrimary)
                            Text("انتخاب حساب معین و ثبت مبالغ بدهکار یا بستانکار", fontSize = 11.sp, color = TextDarkSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSub?.let { "${NumberFormatUtils.toPersianDigits(it.code)} - ${it.title}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("انتخاب حساب معین") },
                            placeholder = { Text("برای انتخاب کلیک فرمایید...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MatteGold,
                                unfocusedBorderColor = CleanBorder,
                                focusedContainerColor = CleanSurface,
                                unfocusedContainerColor = CleanSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                                subSearchQuery = ""
                            },
                            modifier = Modifier.background(CleanSurface)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                OutlinedTextField(
                                    value = subSearchQuery,
                                    onValueChange = { subSearchQuery = it },
                                    placeholder = { Text("جستجوی نام یا کد معین...", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            filteredSubs.forEach { sub ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${NumberFormatUtils.toPersianDigits(sub.code)} - ${sub.title}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = if (sub.nature == "DEBIT") "بدهکار" else "بستانکار",
                                                fontSize = 10.sp,
                                                color = if (sub.nature == "DEBIT") AccountingDebitGreen else AccountingCreditRed
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedSub = sub
                                        expanded = false
                                        subSearchQuery = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = itemDesc,
                        onValueChange = { itemDesc = it },
                        label = { Text("شرح سطر آرتیکل (اختیاری)") },
                        placeholder = { Text("در صورت خالی بودن، شرح کلی سند درج می‌شود") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatteGold,
                            unfocusedBorderColor = CleanBorder,
                            focusedContainerColor = CleanSurface,
                            unfocusedContainerColor = CleanSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemDebitText,
                            onValueChange = {
                                itemDebitText = it
                                if (it.isNotBlank()) itemCreditText = ""
                            },
                            label = { Text("بدهکار (تومان)") },
                            placeholder = { Text("۰") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccountingDebitGreen,
                                unfocusedBorderColor = CleanBorder,
                                focusedContainerColor = CleanSurface,
                                unfocusedContainerColor = CleanSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("debit_input")
                        )

                        OutlinedTextField(
                            value = itemCreditText,
                            onValueChange = {
                                itemCreditText = it
                                if (it.isNotBlank()) itemDebitText = ""
                            },
                            label = { Text("بستانکار (تومان)") },
                            placeholder = { Text("۰") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccountingCreditRed,
                                unfocusedBorderColor = CleanBorder,
                                focusedContainerColor = CleanSurface,
                                unfocusedContainerColor = CleanSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("credit_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val sub = selectedSub ?: return@Button
                            val debit = itemDebitText.toLongOrNull() ?: 0L
                            val credit = itemCreditText.toLongOrNull() ?: 0L
                            if (debit == 0L && credit == 0L) return@Button

                            onAddItem(
                                VoucherItemDraft(
                                    subsidiaryCode = sub.code,
                                    subsidiaryTitle = sub.title,
                                    generalCode = sub.generalCode,
                                    description = itemDesc.ifBlank { draftDesc.ifBlank { "آرتیکل سند مالی" } },
                                    debit = debit,
                                    credit = credit
                                )
                            )
                            itemDesc = ""
                            itemDebitText = ""
                            itemCreditText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("add_item_to_voucher_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MatteGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("افزودن سطر به سند", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ۳. فهرست سطرهای اضافه شده به پیش‌نویس
        if (draftItems.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سطرهای آرتیکل سند (${NumberFormatUtils.toPersianDigits(draftItems.size)} ردیف)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextDarkPrimary
                    )
                }
            }

            items(draftItems.size) { index ->
                val item = draftItems[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanSurface),
                    border = BorderStroke(1.dp, CleanBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NavyPrimary.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "${NumberFormatUtils.toPersianDigits(index + 1)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${NumberFormatUtils.toPersianDigits(item.subsidiaryCode)} - ${item.subsidiaryTitle}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextDarkPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = TextDarkSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (item.debit > 0) {
                                    Text(
                                        text = "بدهکار: ${NumberFormatUtils.formatPrice(item.debit)} تومان",
                                        color = AccountingDebitGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                if (item.credit > 0) {
                                    Text(
                                        text = "بستانکار: ${NumberFormatUtils.formatPrice(item.credit)} تومان",
                                        color = AccountingCreditRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onRemoveItem(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف سطر",
                                tint = AccountingCreditRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ۴. ویجت مانیتور تراز لحظه‌ای دوبل
        item {
            val balanceDiff = Math.abs(totalDebit - totalCredit)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBalanced) AccountingDebitGreen.copy(alpha = 0.08f) else AccountingCreditRed.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, if (isBalanced) AccountingDebitGreen.copy(alpha = 0.3f) else AccountingCreditRed.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "وضعیت تراز دوبل سند:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextDarkPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isBalanced) AccountingDebitGreen else AccountingCreditRed
                        ) {
                            Text(
                                text = if (isBalanced) "سند تراز است ✓" else "اختلاف تراز دارد ✕",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = (if (isBalanced) AccountingDebitGreen else AccountingCreditRed).copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مجموع بدهکار:", fontSize = 12.sp, color = TextDarkSecondary)
                        Text(
                            text = "${NumberFormatUtils.formatPrice(totalDebit)} تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccountingDebitGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مجموع بستانکار:", fontSize = 12.sp, color = TextDarkSecondary)
                        Text(
                            text = "${NumberFormatUtils.formatPrice(totalCredit)} تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccountingCreditRed
                        )
                    }

                    if (!isBalanced && draftItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("اختلاف بدهکار و بستانکار:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccountingCreditRed)
                            Text(
                                text = "${NumberFormatUtils.formatPrice(balanceDiff)} تومان",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AccountingCreditRed
                            )
                        }
                    }
                }
            }
        }

        // ۵. دکمه ثبت نهایی سند حسابداری
        item {
            Button(
                onClick = onSubmit,
                enabled = isBalanced && draftItems.size >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_voucher_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = CleanBorder,
                    disabledContentColor = TextDarkMuted
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (isBalanced && draftItems.size >= 2) MatteGold else TextDarkMuted)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ثبت نهایی و صدور سند حسابداری",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// تب ۴: تراز آزمایشی (چهارستونی معین)
// ============================================================================
@Composable
private fun TrialBalanceContent(
    trialBalance: List<TrialBalanceRow>,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var onlyActive by remember { mutableStateOf(false) }

    val filteredRows = remember(trialBalance, searchQuery, onlyActive) {
        trialBalance.filter { row ->
            val matchesSearch = searchQuery.isBlank() ||
                row.subsidiaryCode.toString().contains(searchQuery) ||
                row.title.contains(searchQuery, ignoreCase = true) ||
                row.generalTitle.contains(searchQuery, ignoreCase = true)

            val matchesActive = !onlyActive || (row.totalDebit > 0 || row.totalCredit > 0 || row.balanceDebit > 0 || row.balanceCredit > 0)
            matchesSearch && matchesActive
        }
    }

    val sumTurnoverDebit = remember(trialBalance) { trialBalance.sumOf { it.totalDebit } }
    val sumTurnoverCredit = remember(trialBalance) { trialBalance.sumOf { it.totalCredit } }
    val sumBalanceDebit = remember(trialBalance) { trialBalance.sumOf { it.balanceDebit } }
    val sumBalanceCredit = remember(trialBalance) { trialBalance.sumOf { it.balanceCredit } }
    val isTrialBalanced = sumBalanceDebit == sumBalanceCredit

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // ۱. کارت خلاصه و شاخص‌های آماری تراز آزمایشی
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NavyPrimary.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("تراز آزمایشی چهارستونی معین", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkPrimary)
                                Text("انطباق گردش و مانده حساب‌های سیستم", fontSize = 11.sp, color = TextDarkSecondary)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = onExportPdf,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onExportCsv,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, PersianGold)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = PersianGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اکسل", fontSize = 11.sp, color = PersianGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // خلاصه وضعیت انطباق مانده‌ها
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isTrialBalanced) AccountingDebitGreen.copy(alpha = 0.08f) else AccountingCreditRed.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isTrialBalanced) AccountingDebitGreen.copy(alpha = 0.3f) else AccountingCreditRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTrialBalanced) "تراز سیستم تأیید شده است (مجموع مانده بدهکار = بستانکار)" else "عدم انطباق در تراز آزمایشی!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTrialBalanced) AccountingDebitGreen else AccountingCreditRed
                            )
                            Icon(
                                imageVector = if (isTrialBalanced) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isTrialBalanced) AccountingDebitGreen else AccountingCreditRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // جدول ۴ ستونه مجموع کل
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CleanBackground, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("گردش بدهکار کل:", fontSize = 10.sp, color = TextDarkSecondary)
                            Text(NumberFormatUtils.formatPrice(sumTurnoverDebit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                        }
                        Column {
                            Text("گردش بستانکار کل:", fontSize = 10.sp, color = TextDarkSecondary)
                            Text(NumberFormatUtils.formatPrice(sumTurnoverCredit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                        }
                        Column {
                            Text("مانده بدهکار:", fontSize = 10.sp, color = TextDarkSecondary)
                            Text(NumberFormatUtils.formatPrice(sumBalanceDebit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccountingDebitGreen)
                        }
                        Column {
                            Text("مانده بستانکار:", fontSize = 10.sp, color = TextDarkSecondary)
                            Text(NumberFormatUtils.formatPrice(sumBalanceCredit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccountingCreditRed)
                        }
                    }

                    // فیلتر و جستجو
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجو بر اساس کد یا نام حساب معین...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilterChip(
                            selected = onlyActive,
                            onClick = { onlyActive = !onlyActive },
                            label = { Text("فقط حساب‌های دارای گردش یا مانده", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // ۲. ردیف‌های تراز آزمایشی
        if (filteredRows.isEmpty()) {
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
                        Text("موردی برای نمایش در تراز آزمایشی یافت نشد.", fontSize = 12.sp, color = TextDarkSecondary)
                    }
                }
            }
        } else {
            items(filteredRows) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanSurface),
                    border = BorderStroke(1.dp, CleanBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NavyPrimary.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "${NumberFormatUtils.toPersianDigits(row.subsidiaryCode)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = row.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextDarkPrimary
                                )
                            }
                            Text(
                                text = row.groupTitle,
                                fontSize = 10.sp,
                                color = TextDarkSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "حساب کل: ${row.generalTitle}",
                            fontSize = 11.sp,
                            color = TextDarkMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ۴ ستون مالی
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CleanBackground, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("گردش بدهکار:", fontSize = 9.sp, color = TextDarkSecondary)
                                Text(
                                    text = "${NumberFormatUtils.formatPrice(row.totalDebit)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDarkPrimary
                                )
                            }

                            Column {
                                Text("گردش بستانکار:", fontSize = 9.sp, color = TextDarkSecondary)
                                Text(
                                    text = "${NumberFormatUtils.formatPrice(row.totalCredit)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDarkPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("مانده نهایی:", fontSize = 9.sp, color = TextDarkSecondary)
                                when {
                                    row.balanceDebit > 0 -> {
                                        Text(
                                            text = "${NumberFormatUtils.formatPrice(row.balanceDebit)} (بد)",
                                            color = AccountingDebitGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    row.balanceCredit > 0 -> {
                                        Text(
                                            text = "${NumberFormatUtils.formatPrice(row.balanceCredit)} (بس)",
                                            color = AccountingCreditRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "تسویه",
                                            color = TextDarkMuted,
                                            fontSize = 11.sp
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

// ============================================================================
// تب ۵: صورت سود و زیان (گزارش جامع مالی دوره)
// ============================================================================
@Composable
private fun ProfitLossContent(
    statement: ProfitLossStatement,
    canViewProfit: Boolean,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    if (!canViewProfit) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(48.dp))
                    Text("محدودیت دسترسی نقش کاربر", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDarkPrimary)
                    Text(
                        text = "مشاهده صورت سود و زیان مالی تنها برای نقش «مدیر کل» مجاز است. لطفاً از بخش تنظیمات نقش خود را تغییر دهید.",
                        fontSize = 12.sp,
                        color = TextDarkSecondary
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NavyPrimary.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("صورت سود و زیان جامع دوره مالی", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkPrimary)
                        Text("محاسبه عملکرد مالی بر اساس استاندارد حسابداری", fontSize = 11.sp, color = TextDarkSecondary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, PersianGold)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = PersianGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اکسل", fontSize = 11.sp, color = PersianGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // کارت صورت مالی به تفکیک اقلام استاندارد
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // ۱. درآمد عملیاتی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(18.dp))
                            Text("درآمدهای عملیاتی و حاصل از فروش:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDarkPrimary)
                        }
                        Text(
                            text = "${NumberFormatUtils.formatPrice(statement.totalRevenue)} تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AccountingDebitGreen
                        )
                    }

                    // ۲. بهای تمام شده
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(18.dp))
                            Text("بهای تمام شده کالای فروش رفته (COGS):", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDarkPrimary)
                        }
                        Text(
                            text = "(${NumberFormatUtils.formatPrice(statement.costOfGoodsSold)}) تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AccountingCreditRed
                        )
                    }

                    HorizontalDivider(color = CleanBorder)

                    // ۳. سود ناخالص
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("سود (زیان) ناخالص:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkPrimary)
                        Text(
                            text = "${NumberFormatUtils.formatPrice(statement.grossProfit)} تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (statement.grossProfit >= 0) AccountingDebitGreen else AccountingCreditRed
                        )
                    }

                    // ۴. هزینه‌های عمومی و اداری
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(18.dp))
                            Text("هزینه‌های عمومی، اداری و توزیع:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDarkPrimary)
                        }
                        Text(
                            text = "(${NumberFormatUtils.formatPrice(statement.operatingExpenses)}) تومان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AccountingCreditRed
                        )
                    }

                    HorizontalDivider(thickness = 2.dp, color = MatteGold)

                    // ۵. سود یا زیان خالص نهایی دوره
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (statement.netProfit >= 0) AccountingDebitGreen.copy(alpha = 0.08f) else AccountingCreditRed.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (statement.netProfit >= 0) AccountingDebitGreen.copy(alpha = 0.3f) else AccountingCreditRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "سود (زیان) خالص نهایی دوره:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDarkPrimary
                                )
                                Text(
                                    text = if (statement.netProfit >= 0) "سوددهی دوره مالی مثبت است ✓" else "دوره با زیان مالی همراه بوده است ⚠",
                                    fontSize = 11.sp,
                                    color = if (statement.netProfit >= 0) AccountingDebitGreen else AccountingCreditRed
                                )
                            }
                            Text(
                                text = "${NumberFormatUtils.formatPrice(statement.netProfit)} تومان",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = if (statement.netProfit >= 0) AccountingDebitGreen else AccountingCreditRed
                            )
                        }
                    }
                }
            }
        }
    }
}
