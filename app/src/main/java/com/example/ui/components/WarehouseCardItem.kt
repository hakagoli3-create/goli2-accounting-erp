package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WarehouseEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen

/**
 * کارت مدرن نمایش انبار با قابلیت ویرایش، حذف و تعیین انبار پیش‌فرض
 */
@Composable
fun WarehouseCardItem(
    warehouse: WarehouseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("warehouse_card_${warehouse.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // سربرگ: نام و کد انبار، بج انبار پیش‌فرض
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = warehouse.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "کد انبار: ${warehouse.code}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (warehouse.isDefault) {
                    Box(
                        modifier = Modifier
                            .background(AccountingDebitGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccountingDebitGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "انبار پیش‌فرض",
                                color = AccountingDebitGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // اطلاعات مسئول، تلفن و آدرس
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                if (warehouse.managerName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مدیر انبار: ${warehouse.managerName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (warehouse.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تلفن: ${warehouse.phone}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (warehouse.address.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "آدرس: ${warehouse.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // عملیات: تعیین پیش‌فرض، ویرایش، حذف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!warehouse.isDefault) {
                    OutlinedButton(
                        onClick = onSetDefault,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("انتخاب به عنوان پیش‌فرض", fontSize = 11.sp)
                    }
                } else {
                    Text(
                        text = "پیش‌فرض ثبت کلیه فاکتورها",
                        fontSize = 11.sp,
                        color = AccountingDebitGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش مشخصات انبار",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف انبار",
                            tint = AccountingCreditRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
