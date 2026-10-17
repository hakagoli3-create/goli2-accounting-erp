package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import com.example.ui.theme.AccountingCreditRed

/**
 * شیت مدرن ثبت حواله انتقال کالا بین دو انبار
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferBottomSheet(
    sheetState: SheetState,
    initialProduct: ProductEntity?,
    products: List<ProductEntity>,
    warehouses: List<WarehouseEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        productId: Long,
        fromWarehouseId: Long,
        toWarehouseId: Long,
        quantitySecondary: Double,
        description: String
    ) -> Unit
) {
    var selectedProduct by remember {
        mutableStateOf(initialProduct ?: products.firstOrNull())
    }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    var sourceWarehouse by remember {
        mutableStateOf(warehouses.firstOrNull { it.isDefault } ?: warehouses.firstOrNull())
    }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }

    var targetWarehouse by remember {
        mutableStateOf(warehouses.firstOrNull { it.id != sourceWarehouse?.id } ?: warehouses.getOrNull(1))
    }
    var targetDropdownExpanded by remember { mutableStateOf(false) }

    var usePrimaryUnit by remember { mutableStateOf(false) }
    var quantityText by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("حواله جابجایی کالا بین انبارها") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "صدور حواله انتقال کالا بین انبارها",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ۱. انتخاب کالا
            ExposedDropdownMenuBox(
                expanded = productDropdownExpanded,
                onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.let { "${it.name} (موجودی کل: ${it.formatStockDisplay()})" } ?: "انتخاب کالا...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("کالای مورد انتقال *") },
                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("transfer_product_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = productDropdownExpanded,
                    onDismissRequest = { productDropdownExpanded = false }
                ) {
                    products.forEach { prod ->
                        DropdownMenuItem(
                            text = { Text("${prod.name} (کد: ${prod.code}) | موجودی: ${prod.formatStockDisplay()}") },
                            onClick = {
                                selectedProduct = prod
                                productDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ۲. انتخاب انبار مبدأ و مقصد
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مسیریابی انبارها (مبدأ و مقصد)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // انبار مبدأ
                    ExposedDropdownMenuBox(
                        expanded = sourceDropdownExpanded,
                        onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = sourceWarehouse?.let { "${it.name} (${it.code})" } ?: "انتخاب انبار مبدأ...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("انبار مبدأ (خروج کالا) *") },
                            leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null, tint = AccountingCreditRed) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = sourceDropdownExpanded,
                            onDismissRequest = { sourceDropdownExpanded = false }
                        ) {
                            warehouses.forEach { wh ->
                                DropdownMenuItem(
                                    text = { Text("${wh.name} (${wh.code})${if (wh.isDefault) " [پیش‌فرض]" else ""}") },
                                    onClick = {
                                        sourceWarehouse = wh
                                        sourceDropdownExpanded = false
                                        if (targetWarehouse?.id == wh.id) {
                                            targetWarehouse = warehouses.firstOrNull { it.id != wh.id }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // انبار مقصد
                    ExposedDropdownMenuBox(
                        expanded = targetDropdownExpanded,
                        onExpandedChange = { targetDropdownExpanded = !targetDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = targetWarehouse?.let { "${it.name} (${it.code})" } ?: "انتخاب انبار مقصد...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("انبار مقصد (ورود کالا) *") },
                            leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = targetDropdownExpanded,
                            onDismissRequest = { targetDropdownExpanded = false }
                        ) {
                            warehouses.forEach { wh ->
                                DropdownMenuItem(
                                    text = { Text("${wh.name} (${wh.code})${if (wh.isDefault) " [پیش‌فرض]" else ""}") },
                                    onClick = {
                                        targetWarehouse = wh
                                        targetDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ۳. مقدار و واحد انتقال
            selectedProduct?.let { prod ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "واحد سنجش انتقال:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(prod.secondaryUnit, fontSize = 12.sp)
                                Switch(
                                    checked = usePrimaryUnit,
                                    onCheckedChange = { usePrimaryUnit = it },
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                Text(prod.primaryUnit, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val unitLabel = if (usePrimaryUnit) prod.primaryUnit else prod.secondaryUnit
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = {
                                quantityText = it
                                errorMessage = null
                            },
                            label = { Text("تعداد / مقدار انتقال ($unitLabel) *") },
                            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        val enteredQty = quantityText.toDoubleOrNull() ?: 0.0
                        val secondaryEquivalent = if (usePrimaryUnit) enteredQty * prod.conversionRate else enteredQty

                        if (usePrimaryUnit && prod.conversionRate > 1.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "معادل $secondaryEquivalent ${prod.secondaryUnit} (ضریب هر ${prod.primaryUnit}: ${prod.conversionRate} ${prod.secondaryUnit})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("شرح و علت حواله انتقال") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = err,
                    color = AccountingCreditRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val prod = selectedProduct
                    if (prod == null) {
                        errorMessage = "لطفاً ابتدا کالا را انتخاب کنید."
                        return@Button
                    }
                    val src = sourceWarehouse
                    val dst = targetWarehouse
                    if (src == null || dst == null) {
                        errorMessage = "لطفاً انبار مبدأ و مقصد را انتخاب کنید."
                        return@Button
                    }
                    if (src.id == dst.id) {
                        errorMessage = "انبار مبدأ و مقصد نمی‌توانند یکسان باشند."
                        return@Button
                    }
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    if (qty <= 0.0) {
                        errorMessage = "لطفاً مقدار معتبری برای انتقال وارد کنید."
                        return@Button
                    }

                    val finalQtySecondary = if (usePrimaryUnit) qty * prod.conversionRate else qty
                    onSubmit(prod.id, src.id, dst.id, finalQtySecondary, description)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_transfer_btn")
            ) {
                Text(
                    text = "ثبت نهایی حواله انتقال بین انبارها",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
