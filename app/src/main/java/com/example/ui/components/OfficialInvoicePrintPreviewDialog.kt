package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvoiceWithItems
import com.example.data.model.StoreProfile
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.util.ExportHelper
import com.example.util.NumberFormatUtils

/**
 * قالب پیش‌نمایش و چاپ فاکتور رسمی استاندارد بازرگانی (A4 / A5)
 * همراه با سربرگ شرکتی، جدول‌بندی کامل، جمع کل به حروف و عدد، شرایط تسویه و محل مهر و امضا
 */
@Composable
fun OfficialInvoicePrintPreviewDialog(
    invoiceWithItems: InvoiceWithItems,
    storeProfile: StoreProfile,
    onDismiss: () -> Unit,
    onSwitchToThermalReceipt: () -> Unit = {}
) {
    val context = LocalContext.current
    val inv = invoiceWithItems.invoice
    val items = invoiceWithItems.items

    var selectedFormat by remember { mutableStateOf("A4") } // "A4" or "A5"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Action & Close Bar
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
                                text = "صورتحساب رسمی فروش کالا و خدمات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "قالب استاندارد دارایی و اداری (${selectedFormat})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Format Selector (A4 vs A5) and Switch to Thermal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedFormat == "A4",
                            onClick = { selectedFormat = "A4" },
                            label = { Text("قالب A4 (کامل)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        FilterChip(
                            selected = selectedFormat == "A5",
                            onClick = { selectedFormat = "A5" },
                            label = { Text("قالب A5 (فشرده)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onSwitchToThermalReceipt,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("چاپ فیش حرارتی", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Bar (Print, PDF, Excel, Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val pdf = ExportHelper.exportInvoicePdf(context, invoiceWithItems, storeProfile)
                            if (pdf != null) {
                                ExportHelper.printPdf(context, pdf, "فاکتور_${inv.invoiceNumber}")
                            } else {
                                Toast.makeText(context, "خطا در آماده‌سازی پرینت", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.2f).testTag("print_official_invoice_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("چاپ پرینتر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val pdf = ExportHelper.exportInvoicePdf(context, invoiceWithItems, storeProfile)
                            if (pdf != null) {
                                ExportHelper.shareFile(context, pdf, "application/pdf", "اشتراک فاکتور PDF")
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("export_pdf_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val csv = ExportHelper.exportInvoiceCsv(context, invoiceWithItems, storeProfile)
                            if (csv != null) {
                                ExportHelper.shareFile(context, csv, "text/csv", "خروجی اکسل فاکتور")
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("export_csv_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اکسل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val printable = com.example.util.EscPosPrinterHelper.PrintableInvoice(
                                invoiceNumber = inv.invoiceNumber,
                                invoiceDate = inv.jalaliDate,
                                customerName = inv.partyName,
                                customerPhone = inv.partyPhone,
                                warehouseName = inv.warehouseName,
                                items = items.map {
                                    com.example.util.EscPosPrinterHelper.PrintableItem(
                                        name = it.productName,
                                        quantity = it.quantity,
                                        unit = it.unitName,
                                        unitPrice = it.unitPrice,
                                        totalPrice = it.totalPrice
                                    )
                                },
                                subtotalAmount = inv.subtotalAmount,
                                discountAmount = inv.discountAmount,
                                taxAmount = inv.taxAmount,
                                totalAmount = inv.totalAmount,
                                paymentMethod = inv.paymentType
                            )
                            val text = com.example.util.EscPosPrinterHelper.buildPlainTextReceipt(printable)
                            ExportHelper.shareText(context, text, "ارسال فاکتور متنی")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("متن", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // THE OFFICIAL PAPER PREVIEW SHEET (A4 / A5)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (selectedFormat == "A4") 16.dp else 10.dp)
                    ) {
                        // Official Header Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = storeProfile.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "صورتحساب رسمی فروش کالا و خدمات",
                                            fontSize = 11.sp,
                                            color = PersianGold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "شماره: ${inv.invoiceNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "تاریخ: ${inv.jalaliDate}",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = if (inv.status == "PROFORMA") "«پیش‌فاکتور»" else "«نسخه نهایی»",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.status == "PROFORMA") PersianGold else AccountingDebitGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Seller & Buyer Specification Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Seller Box
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("مشخصات فروشنده", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text("نام: ${storeProfile.name}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    Text("شناسه ملی: ${storeProfile.nationalCode.ifBlank { "۱۰۱۰۲۳۴۵۶۷۸" }}", fontSize = 10.sp)
                                    Text("تلفن: ${storeProfile.phone}", fontSize = 10.sp)
                                    Text("نشانی: ${storeProfile.address}", fontSize = 9.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // Buyer Box
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("مشخصات خریدار / طرف‌حساب", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text("نام: ${inv.partyName}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("کد/شناسه: ${if ((inv.partyId ?: 0L) > 0) "CUS-${inv.partyId}" else "مشتری نقدی"}", fontSize = 10.sp)
                                    Text("شماره تماس: ${inv.partyPhone.ifEmpty { "نقدی" }}", fontSize = 10.sp)
                                    Text("نوع تسویه: ${inv.paymentType}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Items Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF37474F), RoundedCornerShape(6.dp))
                                .padding(vertical = 6.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ردیف", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                            Text("شرح کالا / خدمات", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, modifier = Modifier.weight(2.2f))
                            Text("تعداد", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                            Text("فی (تومان)", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                            Text("مبلغ کل", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                        }

                        // Items Rows
                        items.forEachIndexed { idx, itm ->
                            val rowBg = if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .padding(vertical = 6.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = NumberFormatUtils.toPersianDigits((idx + 1).toString()),
                                    fontSize = 10.sp,
                                    modifier = Modifier.width(30.dp),
                                    textAlign = TextAlign.Center
                                )
                                Column(modifier = Modifier.weight(2.2f)) {
                                    Text(itm.productName, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    if (itm.barcode.isNotBlank()) {
                                        Text("بارکد: ${itm.barcode}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    text = "${NumberFormatUtils.toPersianDigits(itm.quantity.toString())} ${itm.unitName}",
                                    fontSize = 10.sp,
                                    modifier = Modifier.weight(0.9f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = NumberFormatUtils.formatPrice(itm.unitPrice),
                                    fontSize = 10.sp,
                                    modifier = Modifier.weight(1.3f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = NumberFormatUtils.formatPrice(itm.totalPrice),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1.5f),
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Totals & Verbal Breakdown Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("جمع اقلام:", fontSize = 11.sp)
                                    Text(NumberFormatUtils.formatPrice(inv.subtotalAmount), fontSize = 11.sp)
                                }
                                if (inv.discountAmount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("تخفیف:", fontSize = 11.sp)
                                        Text(NumberFormatUtils.formatPrice(inv.discountAmount), color = AccountingDebitGreen, fontSize = 11.sp)
                                    }
                                }
                                if (inv.taxAmount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("مالیات بر ارزش افزوده (۱۰٪):", fontSize = 11.sp)
                                        Text(NumberFormatUtils.formatPrice(inv.taxAmount), fontSize = 11.sp)
                                    }
                                }
                                if (inv.directExpenseAmount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("هزینه جانبی (${inv.directExpenseType}):", fontSize = 11.sp)
                                        Text(NumberFormatUtils.formatPrice(inv.directExpenseAmount), fontSize = 11.sp, color = PersianGold)
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("مبلغ نهایی قابل پرداخت:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = NumberFormatUtils.formatPrice(inv.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Verbal Price (به حروف)
                                val verbalPrice = NumberFormatUtils.numberToPersianWords(inv.totalAmount)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "مبلغ به حروف: $verbalPrice تومان",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Settlement Summary
                                val payDesc = buildString {
                                    append("شیوه تسویه: ${inv.paymentType}")
                                    if (inv.cashAmount > 0) append(" | نقد: ${NumberFormatUtils.formatPrice(inv.cashAmount)}")
                                    if (inv.posAmount > 0) append(" | پوز: ${NumberFormatUtils.formatPrice(inv.posAmount)}")
                                    if (inv.chequeAmount > 0) append(" | چک صیادی: ${NumberFormatUtils.formatPrice(inv.chequeAmount)}")
                                    if (inv.creditAmount > 0) append(" | نسیه: ${NumberFormatUtils.formatPrice(inv.creditAmount)}")
                                }
                                Text(
                                    text = payDesc,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (storeProfile.defaultBankCard.isNotBlank() || storeProfile.defaultSheba.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "شماره شبا: ${storeProfile.defaultSheba.ifBlank { "IR------------------------" }} | شماره کارت: ${storeProfile.defaultBankCard.ifBlank { "---- ---- ---- ----" }}",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Seal and Signature Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("مهر و امضای فروشنده", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(storeProfile.name, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("امضا و تایید تحویل خریدار", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(inv.partyName, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
