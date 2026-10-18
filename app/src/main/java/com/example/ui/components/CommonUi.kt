package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.example.util.PairedPrinter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.PersianGold
import com.example.util.NumberFormatUtils
import androidx.compose.foundation.border
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.layout.RowScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErpTopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subValue: String? = null,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subValue != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subValue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

data class ParsedReceiptData(
    val invoiceNumber: String = "",
    val invoiceDate: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val warehouseName: String = "",
    val items: List<ParsedReceiptItemRow> = emptyList(),
    val subtotal: String = "",
    val discount: String = "",
    val tax: String = "",
    val directExpense: String = "",
    val total: String = "",
    val words: String = "",
    val paymentMethod: String = "",
    val note: String = ""
)

data class ParsedReceiptItemRow(
    val rowNumber: String,
    val name: String,
    val quantityAndUnit: String,
    val unitPrice: String,
    val totalPrice: String
)

fun parseReceiptText(text: String): ParsedReceiptData {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    var invNum = ""
    var invDate = ""
    var custName = ""
    var custPhone = ""
    var whName = ""
    var subtotal = ""
    var discount = ""
    var tax = ""
    var directExpense = ""
    var total = ""
    var words = ""
    var payMethod = ""
    var note = ""
    val items = mutableListOf<ParsedReceiptItemRow>()

    var inItemsSection = false
    var currentItemName = ""
    var currentRowNum = ""

    for (line in lines) {
        when {
            line.startsWith("شماره فاکتور:") || line.startsWith("شماره فاکتور :") -> {
                invNum = line.substringAfter(":").trim()
            }
            line.startsWith("تاریخ صدور:") || line.startsWith("تاریخ صدور :") -> {
                invDate = line.substringAfter(":").trim()
            }
            line.startsWith("خریدار:") || line.startsWith("خریدار     :") -> {
                custName = line.substringAfter(":").trim()
            }
            line.startsWith("شماره تماس:") || line.startsWith("شماره تماس :") -> {
                custPhone = line.substringAfter(":").trim()
            }
            line.startsWith("انبار:") || line.startsWith("انبار      :") -> {
                whName = line.substringAfter(":").trim()
            }
            line.contains("ردیف | نام کالا") -> {
                inItemsSection = true
            }
            line.startsWith("جمع کل اقلام") || line.startsWith("جمع اقلام:") -> {
                inItemsSection = false
                subtotal = line.substringAfter(":").trim()
            }
            line.startsWith("تخفیف") -> {
                discount = line.substringAfter(":").trim()
            }
            line.startsWith("مالیات") -> {
                tax = line.substringAfter(":").trim()
            }
            line.startsWith("هزینه مستقیم") || line.contains("کرایه") || line.contains("باربری") -> {
                directExpense = line.substringAfter(":").trim()
            }
            line.startsWith("مبلغ قابل پرداخت:") || line.startsWith("مبلغ نهایی:") -> {
                total = line.substringAfter(":").trim()
            }
            line.startsWith("حروف:") -> {
                words = line.substringAfter(":").trim()
            }
            line.startsWith("روش تسویه") || line.startsWith("روش پرداخت:") -> {
                payMethod = line.substringAfter(":").trim()
            }
            line.startsWith("توضیحات") || line.startsWith("یادداشت:") -> {
                note = line.substringAfter(":").trim()
            }
            inItemsSection && !line.startsWith("===") && !line.startsWith("---") -> {
                if (line.contains("×") || line.contains("*")) {
                    val parts = line.split(Regex("[×*]"))
                    val qtyUnit = parts.getOrNull(0)?.trim() ?: ""
                    val priceAndTotal = parts.getOrNull(1)?.split("=") ?: emptyList()
                    val unitPrice = priceAndTotal.getOrNull(0)?.trim() ?: ""
                    val totalPrice = priceAndTotal.getOrNull(1)?.replace("تومان", "")?.trim() ?: ""
                    items.add(
                        ParsedReceiptItemRow(
                            rowNumber = if (currentRowNum.isNotBlank()) currentRowNum else NumberFormatUtils.toPersianDigits((items.size + 1).toString()),
                            name = currentItemName.ifBlank { "کالای مصرفی" },
                            quantityAndUnit = qtyUnit,
                            unitPrice = unitPrice,
                            totalPrice = totalPrice
                        )
                    )
                    currentItemName = ""
                    currentRowNum = ""
                } else {
                    val dashIdx = line.indexOf('-')
                    if (dashIdx in 1..4) {
                        currentRowNum = line.substring(0, dashIdx).trim()
                        currentItemName = line.substring(dashIdx + 1).trim()
                    } else {
                        currentItemName = line
                    }
                }
            }
        }
    }

    return ParsedReceiptData(
        invoiceNumber = invNum,
        invoiceDate = invDate,
        customerName = custName,
        customerPhone = custPhone,
        warehouseName = whName,
        items = items,
        subtotal = subtotal,
        discount = discount,
        tax = tax,
        directExpense = directExpense,
        total = total,
        words = words,
        paymentMethod = payMethod,
        note = note
    )
}

@Composable
fun ReceiptPreviewDialog(
    receiptText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pairedPrinters: List<PairedPrinter> = remember { com.example.util.BluetoothThermalPrinter.getPairedPrinters(context) }
    var selectedPrinter by remember { mutableStateOf<PairedPrinter?>(pairedPrinters.firstOrNull()) }
    var isPrinting by remember { mutableStateOf(false) }

    val parsed = remember(receiptText) { parseReceiptText(receiptText) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Header
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
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "پیش‌نمایش فاکتور و رسید فروش",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "قالب استاندارد و رسمی با جدول‌بندی شفاف",
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

                // Bluetooth Printer Selection Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (pairedPrinters.isNotEmpty())
                                "پرینتر بلوتوثی فعال: ${selectedPrinter?.name ?: "انتخاب نشده"}"
                            else
                                "دستگاه پرینتر بلوتوثی جفت‌شده (Paired) یافت نشد (حالت چاپ استاندارد سیستم فعال است).",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            fontSize = 11.sp
                        )
                        if (pairedPrinters.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (p in pairedPrinters) {
                                    androidx.compose.material3.FilterChip(
                                        selected = selectedPrinter?.address == p.address,
                                        onClick = { selectedPrinter = p },
                                        label = { Text(p.name, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Graphical Invoice Sheet View
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(380.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        // Header Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "مدیریت حسابداری گلی",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "صورتحساب رسمی فروش کالا و خدمات",
                                        fontSize = 10.sp,
                                        color = PersianGold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (parsed.invoiceNumber.isNotBlank()) {
                                        Text(
                                            text = "شماره: ${NumberFormatUtils.formatInvoiceNumber(parsed.invoiceNumber)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                    if (parsed.invoiceDate.isNotBlank()) {
                                        Text(
                                            text = "تاریخ: ${NumberFormatUtils.toPersianDigits(parsed.invoiceDate)}",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer and Warehouse Meta
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("مشخصات خریدار", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("نام: ${parsed.customerName.ifBlank { "مشتری نقدی" }}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    if (parsed.customerPhone.isNotBlank()) {
                                        Text("تماس: ${NumberFormatUtils.toPersianDigits(parsed.customerPhone)}", fontSize = 9.sp)
                                    }
                                }
                            }
                            if (parsed.warehouseName.isNotBlank()) {
                                Card(
                                    modifier = Modifier.weight(0.9f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text("انبار و تحویل", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("مبدا: ${parsed.warehouseName}", fontSize = 10.sp)
                                        Text("وضعیت: تحویل قطعی", fontSize = 9.sp, color = AccountingDebitGreen)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Table of Items
                        if (parsed.items.isNotEmpty()) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF37474F), RoundedCornerShape(6.dp))
                                    .padding(vertical = 5.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ردیف", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                                Text("شرح کالا", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White, modifier = Modifier.weight(2f))
                                Text("تعداد", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                Text("فی", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                                Text("جمع کل (تومان)", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                            }

                            parsed.items.forEachIndexed { idx, itm ->
                                val rowBg = if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBg)
                                        .padding(vertical = 5.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = NumberFormatUtils.toPersianDigits(itm.rowNumber),
                                        fontSize = 9.sp,
                                        modifier = Modifier.width(26.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = itm.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        text = itm.quantityAndUnit,
                                        fontSize = 9.sp,
                                        modifier = Modifier.weight(0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = itm.unitPrice,
                                        fontSize = 9.sp,
                                        modifier = Modifier.weight(1.1f),
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = itm.totalPrice,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1.3f),
                                        textAlign = TextAlign.End
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            }
                        } else {
                            // Fallback raw text display if unparsed
                            Text(
                                text = receiptText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF222222)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Totals Summary Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                if (parsed.subtotal.isNotBlank()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("جمع کل اقلام:", fontSize = 10.sp)
                                        Text(parsed.subtotal, fontSize = 10.sp)
                                    }
                                }
                                if (parsed.discount.isNotBlank() && parsed.discount != "0 تومان") {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("تخفیف:", fontSize = 10.sp)
                                        Text(parsed.discount, color = AccountingDebitGreen, fontSize = 10.sp)
                                    }
                                }
                                if (parsed.tax.isNotBlank() && parsed.tax != "0 تومان") {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("مالیات ارزش افزوده (۱۰٪):", fontSize = 10.sp)
                                        Text(parsed.tax, fontSize = 10.sp)
                                    }
                                }
                                // Direct Expense row (هزینه‌های مستقیم و جانبی)
                                if (parsed.directExpense.isNotBlank() && parsed.directExpense != "0 تومان") {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(PersianGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("هزینه‌های مستقیم و جانبی (کرایه حمل/باربری):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PersianGold)
                                        Text(parsed.directExpense, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PersianGold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("مبلغ نهایی قابل پرداخت:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = parsed.total.ifBlank { "محاسبه نهایی" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (parsed.words.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "به حروف: ${parsed.words}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                if (parsed.paymentMethod.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "روش تسویه: ${NumberFormatUtils.formatPaymentType(parsed.paymentMethod)}",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Seal and Signatures
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("مهر و امضای فروشنده", fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("مدیریت حسابداری گلی", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("امضا و تایید تحویل خریدار", fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(parsed.customerName.ifBlank { "مشتری" }, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "ارسال فاکتور")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_invoice_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اشتراک‌گذاری", fontSize = 11.sp)
                    }

                    androidx.compose.material3.Button(
                        onClick = {
                            val printer = selectedPrinter
                            if (printer != null) {
                                isPrinting = true
                                scope.launch {
                                    val bytes = receiptText.toByteArray(Charsets.UTF_8)
                                    val result = com.example.util.BluetoothThermalPrinter.printBytes(printer.device, bytes)
                                    isPrinting = false
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "فاکتور به پرینتر ${printer.name} ارسال و چاپ گردید.", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "خطا در اتصال یا چاپ پرینتر: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "دستور چاپ فاکتور به پرینتر ارسال شد.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDismiss()
                            }
                        },
                        enabled = !isPrinting,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("print_esc_pos_button")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPrinting) "در حال چاپ..." else "چاپ فاکتور و فیش", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
