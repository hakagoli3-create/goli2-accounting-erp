package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.util.NumberFormatUtils

/**
 * دیالوگ ایجاد سریع طرف حساب (مشتری / تأمین‌کننده) درون فرم صدور فاکتور
 */
@Composable
fun QuickAddPartyDialog(
    initialType: String = "CUSTOMER", // "CUSTOMER", "SUPPLIER"
    onDismiss: () -> Unit,
    onConfirm: (PartyEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var partyType by remember { mutableStateOf(initialType) }
    var mobile by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var creditLimitText by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "افزودن سریع طرف‌حساب جدید",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ثبت مشخصات و انتخاب خودکار در فاکتور جاری",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Party Type Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = partyType == "CUSTOMER",
                        onClick = { partyType = "CUSTOMER" },
                        label = { Text("مشتری (خریدار)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                    FilterChip(
                        selected = partyType == "SUPPLIER",
                        onClick = { partyType = "SUPPLIER" },
                        label = { Text("تأمین‌کننده (فروشنده)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                    FilterChip(
                        selected = partyType == "BOTH",
                        onClick = { partyType = "BOTH" },
                        label = { Text("هردو", fontSize = 11.sp) },
                        modifier = Modifier.weight(0.7f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) errorMessage = null
                    },
                    label = { Text("نام شخص یا شرکت (الزامی)") },
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_party_name_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("شماره همراه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("تلفن ثابت") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nationalCode,
                        onValueChange = { nationalCode = it },
                        label = { Text("کد / شناسه ملی") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = it },
                        label = { Text("سقف اعتبار (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("نشانی و آدرس") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("انصراف")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "لطفاً نام طرف‌حساب را وارد نمایید."
                                return@Button
                            }
                            val credit = creditLimitText.toLongOrNull() ?: 0L
                            val newParty = PartyEntity(
                                code = "P-${System.currentTimeMillis() % 100000}",
                                name = name.trim(),
                                partyType = partyType,
                                mobile = mobile.trim(),
                                phone = phone.trim(),
                                nationalCode = nationalCode.trim(),
                                address = address.trim(),
                                creditLimit = credit,
                                currentBalance = 0L
                            )
                            onConfirm(newParty)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_quick_party_btn")
                    ) {
                        Text("ثبت و انتخاب در فاکتور", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ افزودن یا ویرایش سریع کالا درون فرم صدور فاکتور
 */
@Composable
fun QuickAddOrEditProductDialog(
    existingProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (ProductEntity) -> Unit
) {
    val isEditMode = existingProduct != null
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var category by remember { mutableStateOf(existingProduct?.category ?: "کالای عمومی") }
    var primaryUnit by remember { mutableStateOf(existingProduct?.primaryUnit ?: "عدد") }
    var retailPriceText by remember { mutableStateOf(existingProduct?.retailPrice?.toString() ?: "") }
    var wholesalePriceText by remember { mutableStateOf(existingProduct?.wholesalePrice?.toString() ?: "") }
    var purchasePriceText by remember { mutableStateOf(existingProduct?.buyPrice?.toString() ?: "") }
    var barcode by remember { mutableStateOf(existingProduct?.barcode ?: "") }
    var stockText by remember { mutableStateOf(existingProduct?.currentStockInSecondary?.toInt()?.toString() ?: "10") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                                .background(if (isEditMode) PersianGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Inventory else Icons.Default.AddBusiness,
                                contentDescription = null,
                                tint = if (isEditMode) PersianGold else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isEditMode) "ویرایش سریع مشخصات کالا" else "افزودن سریع کالای جدید",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isEditMode) "اصلاح فوری نام، قیمت و واحد" else "ثبت فوری کالا در انبار و درج در فاکتور",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) errorMessage = null
                    },
                    label = { Text("نام و شرح کالا (الزامی)") },
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_product_name_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("دسته‌بندی") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = primaryUnit,
                        onValueChange = { primaryUnit = it },
                        label = { Text("واحد شمارش") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = retailPriceText,
                        onValueChange = { retailPriceText = it },
                        label = { Text("قیمت تک‌فروشی (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wholesalePriceText,
                        onValueChange = { wholesalePriceText = it },
                        label = { Text("قیمت عمده (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("قیمت خرید (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("کد بارکد") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isEditMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("موجودی اولیه در انبار") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("انصراف")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "لطفاً نام کالا را وارد نمایید."
                                return@Button
                            }
                            val retPrice = retailPriceText.toLongOrNull() ?: 0L
                            val whoPrice = wholesalePriceText.toLongOrNull() ?: retPrice
                            val purPrice = purchasePriceText.toLongOrNull() ?: 0L
                            val stock = stockText.toDoubleOrNull() ?: 0.0

                            val toSave = if (existingProduct != null) {
                                existingProduct.copy(
                                    name = name.trim(),
                                    category = category.trim(),
                                    primaryUnit = primaryUnit.trim(),
                                    retailPrice = retPrice,
                                    wholesalePrice = whoPrice,
                                    buyPrice = purPrice,
                                    barcode = barcode.trim()
                                )
                            } else {
                                val generatedCode = "PRD-${System.currentTimeMillis() % 100000}"
                                ProductEntity(
                                    code = generatedCode,
                                    name = name.trim(),
                                    category = category.trim(),
                                    primaryUnit = primaryUnit.trim(),
                                    secondaryUnit = primaryUnit.trim(),
                                    conversionRate = 1.0,
                                    retailPrice = retPrice,
                                    wholesalePrice = whoPrice,
                                    buyPrice = purPrice,
                                    barcode = barcode.trim(),
                                    currentStockInSecondary = stock
                                )
                            }
                            onConfirm(toSave)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditMode) PersianGold else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_quick_product_btn")
                    ) {
                        Text(
                            text = if (isEditMode) "ذخیره تغییرات کالا" else "ثبت و انتخاب کالا",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
