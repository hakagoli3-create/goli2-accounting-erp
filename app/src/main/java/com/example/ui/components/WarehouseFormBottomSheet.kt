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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WarehouseEntity

/**
 * شیت مدرن برای تعریف و ویرایش انبارها (افزودن، ویرایش، تعیین انبار پیش‌فرض)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseFormBottomSheet(
    sheetState: SheetState,
    warehouseToEdit: WarehouseEntity?,
    onDismiss: () -> Unit,
    onSubmit: (
        code: String,
        name: String,
        manager: String,
        phone: String,
        address: String,
        isDefault: Boolean
    ) -> Unit
) {
    val isEditing = warehouseToEdit != null

    var name by remember { mutableStateOf(warehouseToEdit?.name ?: "") }
    var code by remember { mutableStateOf(warehouseToEdit?.code ?: "") }
    var manager by remember { mutableStateOf(warehouseToEdit?.managerName ?: "") }
    var phone by remember { mutableStateOf(warehouseToEdit?.phone ?: "") }
    var address by remember { mutableStateOf(warehouseToEdit?.address ?: "") }
    var isDefault by remember { mutableStateOf(warehouseToEdit?.isDefault ?: false) }

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
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "ویرایش مشخصات انبار" else "تعریف و ثبت انبار جدید",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("نام انبار (مثال: انبار مرکزی، انبار سردخانه) *") },
                        leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("warehouse_form_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("کد انبار (مثال: W101)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = manager,
                            onValueChange = { manager = it },
                            label = { Text("نام مسئول / مدیر انبار") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("شماره تلفن / داخلی انبار") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس فیزیکی و موقعیت انبار") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // تعیین به عنوان انبار پیش‌فرض
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تعیین به عنوان انبار پیش‌فرض سیستم",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "فاکتورهای خرید، فروش و موجودی اولیه به صورت پیش‌فرض در این انبار ثبت می‌شوند.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        modifier = Modifier.testTag("warehouse_default_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    onSubmit(code, name, manager, phone, address, isDefault)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_warehouse_form_btn")
            ) {
                Text(
                    text = if (isEditing) "ذخیره تغییرات انبار" else "ثبت و تعریف انبار",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
