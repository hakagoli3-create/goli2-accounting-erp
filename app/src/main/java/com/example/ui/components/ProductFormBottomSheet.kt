package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanCard
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.MatteGoldOnContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary

/**
 * شیت مدرن BottomSheet برای افزودن و ویرایش کامل مشخصات کالا در سیستم حسابداری گلی
 * همراه با انتخاب و نمایش تصویر، بارکد، ضریب کارتن/عدد، قیمت‌های چندسطحی و استقرار در انبار
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
        imageUri: String?,
        warehouseId: Long?
    ) -> Unit
) {
    val isEditing = productToEdit != null

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var code by remember { mutableStateOf(productToEdit?.code ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "") }
    var imageUri by remember { mutableStateOf(productToEdit?.imageUri) }

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

    // انتخاب تصویر با Photo Picker امن اندروید
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = CleanCard,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(CleanBorder, CircleShape)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // هدر فرم
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MatteGoldContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = MatteGoldOnContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEditing) "ویرایش مشخصات کالا" else "تعریف و افزودن کالای جدید",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                            Text(
                                text = "اطلاعات شناسنامه‌ای، قیمت‌گذاری و کنترل انبار",
                                fontSize = 11.sp,
                                color = TextDarkSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextDarkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // بخش تصویر کالا
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanBackground,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MatteGoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "تصویر کالا",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = MatteGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "تصویر محصول",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                                Text(
                                    text = if (!imageUri.isNullOrBlank()) "تصویر بارگذاری شد" else "بدون تصویر (اختیاری)",
                                    fontSize = 11.sp,
                                    color = TextDarkSecondary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MatteGold),
                                modifier = Modifier.testTag("pick_product_photo_btn")
                            ) {
                                Text(
                                    text = if (!imageUri.isNullOrBlank()) "تغییر تصویر" else "انتخاب تصویر",
                                    fontSize = 11.sp,
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!imageUri.isNullOrBlank()) {
                                IconButton(onClick = { imageUri = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف عکس",
                                        tint = AccountingCreditRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ۱. اطلاعات اصلی و هویتی
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanBackground,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "مشخصات شناسنامه‌ای و بارکد",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام رسمی کالا *") },
                            leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null, tint = MatteGold) },
                            shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                label = { Text("بارکد ملی / تجاری") },
                                leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = TextDarkMuted) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("دسته‌بندی / گروه‌بندی کالا (مثال: لبنیات، خشکبار، شوینده)") },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = MatteGold) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ۲. واحدهای سنجش و ضریب تبدیل
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanBackground,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "واحدهای سنجش و ضریب بسته‌بندی",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = primaryUnit,
                                onValueChange = { primaryUnit = it },
                                label = { Text("واحد کلان (کارتن/باکس)") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = secondaryUnit,
                                onValueChange = { secondaryUnit = it },
                                label = { Text("واحد خرد (عدد/بسته)") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = conversionRateText,
                                onValueChange = { conversionRateText = it },
                                label = { Text("تعداد در بسته") },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "فرمول انبارداری: هر ۱ $primaryUnit برابر با ${conversionRateText.ifBlank { "۱" }} $secondaryUnit محاسبه می‌شود.",
                            fontSize = 11.sp,
                            color = TextDarkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ۳. قیمت‌گذاری چندسطحی (خرید و فروش)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanBackground,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "قیمت‌گذاری چندسطحی خرید و فروش (تومان)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = buyPriceText,
                                onValueChange = { buyPriceText = it },
                                label = { Text("قیمت خرید *") },
                                leadingIcon = { Icon(Icons.Default.PriceChange, contentDescription = null, tint = TextDarkSecondary) },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = retailPriceText,
                                onValueChange = { retailPriceText = it },
                                label = { Text("قیمت مصرف‌کننده (خرده) *") },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MatteGold) },
                                shape = RoundedCornerShape(12.dp),
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
                                label = { Text("قیمت عمده‌فروشی") },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = partnerPriceText,
                                onValueChange = { partnerPriceText = it },
                                label = { Text("قیمت همکار") },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ۴. موجودی اولیه و نقطه سفارش و انبار
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanBackground,
                    border = BorderStroke(1.dp, CleanBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "کنترل موجودی و انبارداری",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = NavyPrimary
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
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minStockText,
                                onValueChange = { minStockText = it },
                                label = { Text("نقطه هشدار کسری ($secondaryUnit)") },
                                shape = RoundedCornerShape(12.dp),
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
                                    leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null, tint = MatteGold) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseDropdownExpanded) },
                                    shape = RoundedCornerShape(12.dp),
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
                    label = { Text("توضیحات و مشخصات تکمیلی کالا (اختیاری)") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = TextDarkMuted) },
                    shape = RoundedCornerShape(12.dp),
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
                            imageUri, selectedWarehouse?.id
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_product_form_btn")
                ) {
                    Text(
                        text = if (isEditing) "ذخیره تغییرات کالا" else "ثبت قطعی کالا در سیستم",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
