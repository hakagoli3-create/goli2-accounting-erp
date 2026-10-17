package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.ProductEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.util.NumberFormatUtils

/**
 * کارت مدرن و باکیفیت نمایش مشخصات کالا با طراحی شیک، گوشه‌های گرد،
 * ترکیب موجودی بسته/عدد و دکمه‌های عملیاتی (ویرایش، حذف، انتقال، کاردکس)
 */
@Composable
fun ProductCardItem(
    product: ProductEntity,
    onViewCardex: () -> Unit,
    onEdit: () -> Unit,
    onTransfer: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowStock = product.currentStockInSecondary <= product.minStockThreshold

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewCardex() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // سربرگ: نام کالا، دسته‌بندی و نشان کسری موجودی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.category.ifBlank { "عمومی" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "کد: ${product.code}" + if (product.barcode.isNotBlank()) " | بارکد: ${product.barcode}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLowStock) {
                    Box(
                        modifier = Modifier
                            .background(AccountingCreditRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AccountingCreditRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "هشدار کسری",
                                color = AccountingCreditRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // باکس برجسته ترکیب موجودی انبار
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isLowStock) Color(0xFFFFF3F0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "موجودی کل انبار:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.formatStockDisplay(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLowStock) AccountingCreditRed else MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (product.conversionRate > 1.0) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ضریب: هر ${product.primaryUnit} = ${NumberFormatUtils.formatQuantity(product.conversionRate)} ${product.secondaryUnit}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // قیمت‌های چندسطحی با فرمت ۳رقمی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // قیمت خرده
                Column {
                    Text(
                        text = "قیمت مصرف‌کننده (خرده):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${NumberFormatUtils.formatPrice(product.retailPrice)} تومان",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // قیمت عمده
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "قیمت عمده‌فروشی:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${NumberFormatUtils.formatPrice(product.wholesalePrice)} تومان",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }

                // قیمت خرید
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "قیمت خرید:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${NumberFormatUtils.formatPrice(product.buyPrice)} تومان",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // دکمه‌های عملیاتی: کاردکس، انتقال، ویرایش، حذف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onViewCardex,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("کاردکس", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onTransfer,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("انتقال انبار", fontSize = 11.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش کالا",
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
                            contentDescription = "حذف کالا",
                            tint = AccountingCreditRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
