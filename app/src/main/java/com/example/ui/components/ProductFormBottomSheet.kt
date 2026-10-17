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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.QrCode
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
import com.example.util.NumberFormatUtils

/**
 * شیت مدرن BottomSheet برای افزودن و ویرایش کامل مشخصات کالا (CRUD)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormBottomSheet(
    sheetState: SheetState,
    productToEdit: ProductEntity?,
    warehouses: List<WarehouseEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        code: String,
        barcode: String,
        name: String,
        category: String,
        primaryUnit: String,
        secondaryUnit: String,
        conversionRate: Double,
        initialStockSecondary: Double,
        minStock: Double,
        buyPrice: Long,
        retailPrice: Long,
        wholesalePrice: Long,
        partnerPrice: Long,
        description: String,
        warehouseId: Long?
    ) -> Unit
) {
    val isEditing = productToEdit != null

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var code by remember { mutableStateOf(productToEdit?.code ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "") }

    var primaryUnit by remember { mutableStateOf(productToEdit?.primaryUnit ?: "کارتن") }
    var secondaryUnit by remember { mutableStateOf(productToEdit?.secondaryUnit ?: "عدد") }
    var conversionRateText by remember {
        mutableStateOf(
            if (productToEdit != null) {
                if (productToEdit.conversionRate % 1.0 == 0.0) productToEdit.conversionRate.toInt().toString()
                else productToEdit.conversionRate.toString()
            } else "12"
        )
    }

    var buyPriceText by remember { mutableStateOf(if (productToEdit != null) productToEdit.buyPrice.toString() else "0") }
    var retailPriceText by remember { mutableStateOf(if (productToEdit != null) productToEdit.retailPrice.toString() else "0") }
    var wholesalePriceText by remember { mutableStateOf(if (productToEdit != null) productToEdit.wholesalePrice.toString() else "0") }
    var partnerPriceText by remember { mutableStateOf(if (productToEdit != null) productToEdit.partnerPrice.toString() else "0") }

    var initialStockText by remember {
        mutableStateOf(
            if (productToEdit != null) {
                if (productToEdit.currentStockInSecondary % 1.0 == 0.0) productToEdit.currentStockInSecondary.toInt().toString()
                else productToEdit.currentStockInSecondary.toString()
            } else "0"
        )
    }
    var minStockText by remember {
        mutableStateOf(
            if (productToEdit != null) {
                if (productToEdit.minStockThreshold % 1.0 == 0.0) productToEdit.minStockThreshold.toInt().toString()
                else productToEdit.minStockThreshold.toString()
            } else "10"
        )
    }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }

    var selectedWarehouse by remember {
        mutableStateOf(warehouses.firstOrNull { it.isDefault } ?: warehouses.firstOrNull())
    }
    var warehouseDropdownExpanded by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "ویرایش مشخصات کالا" else "تعریف و افزودن کالای جدید",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ۱. اطلاعات اصلی
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مشخصات هویتی و بارکد کالا",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("نام کالا و محصول *") },
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_form_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("کد کالا (مثال: PRD-101)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("بارکد ملی / تجاری") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("گروه‌بندی / دسته‌بندی کالا (مثال: روغن، لبنیات، خشکبار)") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ۲. واحدهای سنجش و ضریب تبدیل
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "واحدهای سنجش و ضریب بسته‌بندی",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = primaryUnit,
                            onValueChange = { primaryUnit = it },
                            label = { Text("واحد اصلی (کارتن/کیسه)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = secondaryUnit,
                            onValueChange = { secondaryUnit = it },
                            label = { Text("واحد فرعی (عدد/کیلو)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = conversionRateText,
                            onValueChange = { conversionRateText = it },
                            label = { Text("ضریب تبدیل") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "راهنما: هر ۱ $primaryUnit معادل ${conversionRateText.ifBlank { "۱" }} $secondaryUnit است.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ۳. قیمت‌گذاری چندسطحی
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "قیمت‌گذاری چندسطحی (به تومان به ازای هر $secondaryUnit)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = buyPriceText,
                            onValueChange = { buyPriceText = it },
                            label = { Text("قیمت خرید") },
                            leadingIcon = { Icon(Icons.Default.PriceChange, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = retailPriceText,
                            onValueChange = { retailPriceText = it },
                            label = { Text("قیمت خرده‌فروشی *") },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = wholesalePriceText,
                            onValueChange = { wholesalePriceText = it },
                            label = { Text("قیمت عمده‌فروشی *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = partnerPriceText,
                            onValueChange = { partnerPriceText = it },
                            label = { Text("قیمت همکار") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ۴. موجودی اولیه و نقطه سفارش
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مدیریت موجودی انبار و نقطه سفارش",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = initialStockText,
                            onValueChange = { initialStockText = it },
                            label = { Text(if (isEditing) "موجودی فعلی ($secondaryUnit)" else "موجودی اولیه ($secondaryUnit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minStockText,
                            onValueChange = { minStockText = it },
                            label = { Text("نقطه سفارش ($secondaryUnit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!isEditing && warehouses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = warehouseDropdownExpanded,
                            onExpandedChange = { warehouseDropdownExpanded = !warehouseDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedWarehouse?.let { "${it.name} (${it.code})" } ?: "انتخاب انبار...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("انبار استقرار موجودی اولیه") },
                                leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = warehouseDropdownExpanded,
                                onDismissRequest = { warehouseDropdownExpanded = false }
                            ) {
                                warehouses.forEach { wh ->
                                    DropdownMenuItem(
                                        text = { Text("${wh.name} (${wh.code})${if (wh.isDefault) " - پیش‌فرض" else ""}") },
                                        onClick = {
                                            selectedWarehouse = wh
                                            warehouseDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات و مشخصات تکمیلی کالا") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val rate = conversionRateText.toDoubleOrNull() ?: 1.0
                    val stock = initialStockText.toDoubleOrNull() ?: 0.0
                    val minStock = minStockText.toDoubleOrNull() ?: 10.0
                    val buy = buyPriceText.toLongOrNull() ?: 0L
                    val retail = retailPriceText.toLongOrNull() ?: 0L
                    val whole = wholesalePriceText.toLongOrNull() ?: 0L
                    val partner = partnerPriceText.toLongOrNull() ?: 0L

                    onSubmit(
                        code, barcode, name, category, primaryUnit, secondaryUnit,
                        rate, stock, minStock, buy, retail, whole, partner, description,
                        selectedWarehouse?.id
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_product_form_btn")
            ) {
                Text(
                    text = if (isEditing) "ذخیره تغییرات کالا" else "ثبت قطعی کالا در سیستم",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
