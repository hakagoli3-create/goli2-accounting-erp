package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ProductEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanCard
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.MatteGoldOnContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.util.NumberFormatUtils

/**
 * کارت حرفه‌ای و لوکس نمایش مشخصات کالا در سیستم حسابداری و انبارداری گلی
 * طراحی کاملاً فارسی، راست‌چین، هماهنگ با هویت بصری سرمه‌ای (#171C2A) و طلایی (#C6A15B)
 * نمایش تصویر کالا، کد، بارکد، موجودی لحظه‌ای، قیمت خرید و فروش و دکمه‌های واضح ویرایش و عملیات
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onViewCardex() }
                .testTag("product_card_${product.id}"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = CleanCard
            ),
            border = BorderStroke(
                width = if (isLowStock) 1.5.dp else 1.dp,
                color = if (isLowStock) AccountingCreditRed.copy(alpha = 0.45f) else CleanBorder
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // بخش بالایی: تصویر محصول، نام، کد، بارکد و هشدار کسری
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // تصویر محصول (در صورت وجود imageUri) یا آیکون گرافیکی شیک
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLowStock) AccountingCreditRed.copy(alpha = 0.08f) else MatteGoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!product.imageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(product.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = if (isLowStock) AccountingCreditRed else MatteGold,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = product.primaryUnit.take(5),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowStock) AccountingCreditRed else MatteGoldOnContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // اطلاعات متنی کالا
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextDarkPrimary
                                ),
                                maxLines = 2,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (isLowStock) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccountingCreditRed.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, AccountingCreditRed.copy(alpha = 0.35f)),
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "هشدار کسری موجودی",
                                            tint = AccountingCreditRed,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "کسری موجودی",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccountingCreditRed
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // برچسب دسته‌بندی و کد و بارکد
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // دسته‌بندی
                            Box(
                                modifier = Modifier
                                    .background(MatteGoldContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = product.category.ifBlank { "عمومی" },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MatteGoldOnContainer
                                )
                            }

                            // کد کالا
                            Text(
                                text = "کد: ${NumberFormatUtils.toPersianDigits(product.code)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkSecondary,
                                fontSize = 11.sp
                            )

                            if (product.barcode.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = TextDarkMuted,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = NumberFormatUtils.toPersianDigits(product.barcode),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextDarkMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // باکس شیک موجودی و وضعیت کارتن/عدد
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLowStock) Color(0xFFFFF4F2) else Color(0xFFF7F8FA),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isLowStock) AccountingCreditRed.copy(alpha = 0.2f) else CleanBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "موجودی لحظه‌ای:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextDarkSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = NumberFormatUtils.toPersianDigits(product.formatStockDisplay()),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isLowStock) AccountingCreditRed else AccountingDebitGreen,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "حداقل سفارش: ${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatQuantity(product.minStockThreshold))} ${product.secondaryUnit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextDarkMuted,
                                fontSize = 10.sp
                            )
                            if (product.conversionRate > 1.0) {
                                Text(
                                    text = "هر ${product.primaryUnit} = ${NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatQuantity(product.conversionRate))} ${product.secondaryUnit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MatteGold,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // جدول قیمت‌ها: قیمت فروش و قیمت خرید با فرمت رسمی تومان
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // قیمت فروش مصرف‌کننده
                    Column {
                        Text(
                            text = "قیمت فروش (مصرف‌کننده):",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDarkSecondary,
                            fontSize = 10.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(product.retailPrice)),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "تومان",
                                fontSize = 10.sp,
                                color = TextDarkSecondary
                            )
                        }
                    }

                    // قیمت عمده‌فروشی
                    if (product.wholesalePrice > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "قیمت عمده:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextDarkSecondary,
                                fontSize = 10.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(product.wholesalePrice)),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDarkPrimary,
                                        fontSize = 12.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "تومان",
                                    fontSize = 9.sp,
                                    color = TextDarkMuted
                                )
                            }
                        }
                    }

                    // قیمت خرید
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "قیمت خرید:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDarkSecondary,
                            fontSize = 10.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(product.buyPrice)),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = TextDarkMuted,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "تومان",
                                fontSize = 9.sp,
                                color = TextDarkMuted
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = CleanBorder
                )

                // دکمه‌های واضح و کاربردی: ویرایش، کاردکس، انتقال، حذف
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // دکمه برجسته ویرایش کالا (کاربردی و واضح بنا به درخواست کاربر)
                    FilledTonalButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NavyPrimary.copy(alpha = 0.08f),
                            contentColor = NavyPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("edit_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            modifier = Modifier.size(15.dp),
                            tint = NavyPrimary
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ویرایش کالا",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onViewCardex,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            border = BorderStroke(1.dp, CleanBorder),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextDarkSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "کاردکس",
                                fontSize = 11.sp,
                                color = TextDarkPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = onTransfer,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            border = BorderStroke(1.dp, CleanBorder),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MatteGold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "انتقال",
                                fontSize = 11.sp,
                                color = TextDarkPrimary
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
}
