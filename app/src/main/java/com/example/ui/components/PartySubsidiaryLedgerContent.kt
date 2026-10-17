package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import com.example.data.model.JournalItemEntity
import com.example.data.model.PartyEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
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
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<JournalItemEntity?>(null) }
    var deletingItem by remember { mutableStateOf<JournalItemEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. انتخاب طرف حساب (مشتری یا تأمین‌کننده)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "دفتر معین اشخاص و طرف‌حساب‌ها (حساب‌های تفصیلی)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = partyMenuExpanded,
                        onExpandedChange = { partyMenuExpanded = !partyMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedParty?.let { "${it.name} (${if (it.partyType == "SUPPLIER") "تأمین‌کننده" else "مشتری"}) - کد: ${it.code}" }
                                ?: "انتخاب طرف‌حساب برای مشاهده معین...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("طرف‌حساب") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_party_ledger_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = partyMenuExpanded,
                            onDismissRequest = { partyMenuExpanded = false }
                        ) {
                            parties.forEach { party ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "${party.name} (${if (party.partyType == "SUPPLIER") "تأمین‌کننده" else "مشتری"})",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "مانده: ${NumberFormatUtils.formatPrice(party.currentBalance)} تومان",
                                                fontSize = 11.sp,
                                                color = if (party.currentBalance > 0) AccountingDebitGreen else Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectParty(party)
                                        partyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. اطلاعات کارت طرف حساب و دکمه‌های عملیات
        if (selectedParty != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(selectedParty.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            val balanceText = if (selectedParty.currentBalance > 0) {
                                "بدهکار: ${NumberFormatUtils.formatPrice(selectedParty.currentBalance)}"
                            } else if (selectedParty.currentBalance < 0) {
                                "بستانکار: ${NumberFormatUtils.formatPrice(-selectedParty.currentBalance)}"
                            } else {
                                "تسویه و بی‌حساب"
                            }
                            Text(
                                text = balanceText,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedParty.currentBalance > 0) AccountingDebitGreen else if (selectedParty.currentBalance < 0) AccountingCreditRed else Color.Gray,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تلفن: ${selectedParty.mobile.ifBlank { "ثبت نشده" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("سقف اعتبار: ${NumberFormatUtils.formatPrice(selectedParty.creditLimit)} تومان", fontSize = 11.sp)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // دکمه‌های عملیاتی: ثبت گردش معین جدید، اکسل و PDF
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.weight(1.5f).testTag("add_party_transaction_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ثبت گردش جدید", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = onExportPdf,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF معین", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = onExportCsv,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = PersianGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اکسل", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 3. فهرست ریز گردش معین
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ریز گردش حساب معین (${ledgerEntries.size} ردیف)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("قابل ویرایش و حذف", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (ledgerEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("هیچ سند یا فاکتوری برای این طرف‌حساب ثبت نشده است.\nجهت ثبت سند دستی، از دکمه «ثبت گردش جدید» استفاده فرمایید.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(ledgerEntries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "شماره سند: ${entry.voucherId}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row {
                                    IconButton(
                                        onClick = { editingItem = entry },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { deletingItem = entry },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("بدهکار (افزایش طلب/بدهی):", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (entry.debit > 0) NumberFormatUtils.formatPrice(entry.debit) else "-",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (entry.debit > 0) AccountingDebitGreen else Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("بستانکار (پرداخت/کاهش بدهی):", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (entry.credit > 0) NumberFormatUtils.formatPrice(entry.credit) else "-",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (entry.credit > 0) AccountingCreditRed else Color.Gray
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
            title = { Text("ثبت گردش حساب معین برای ${selectedParty.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("شرح سند/تراکنش") },
                        placeholder = { Text("مثال: واریز نقدی علی‌الحساب، تسویه دستی") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = debitText,
                            onValueChange = { debitText = it },
                            label = { Text("مبلغ بدهکار (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = creditText,
                            onValueChange = { creditText = it },
                            label = { Text("مبلغ بستانکار (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("تاریخ سند (هجری شمسی)") },
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
                    }
                ) {
                    Text("ثبت در معین")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("انصراف")
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
            title = { Text("ویرایش ردیف معین شماره سند ${item.voucherId}", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("شرح گردش") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = editDebit,
                            onValueChange = { editDebit = it },
                            label = { Text("بدهکار") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editCredit,
                            onValueChange = { editCredit = it },
                            label = { Text("بستانکار") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    }
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Modal: تایید حذف ردیف معین
    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("حذف گردش حساب", fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف این ردیف گردش حساب از دفتر معین اطمینان دارید؟ مانده حساب طرف‌حساب نیز معکوس خواهد شد.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(item)
                        deletingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed)
                ) {
                    Text("حذف قطعی")
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
