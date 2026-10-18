package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.NavyBrand
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.StatusInfoBlue
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.NumberFormatUtils

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToRoute: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val totalLiquidity = state.totalCashBalance + state.totalBankBalance

    val quickActions = listOf(
        QuickActionItem("فاکتور جدید", "صدور و چاپ فاکتور", Icons.AutoMirrored.Filled.ReceiptLong, MatteGold, "invoices"),
        QuickActionItem("صندوق POS", "فروش سریع فروشگاهی", Icons.Default.PointOfSale, NavyPrimary, "pos"),
        QuickActionItem("خزانه‌داری", "دریافت، پرداخت و چک", Icons.Default.AccountBalance, Color(0xFF673AB7), "treasury"),
        QuickActionItem("ثبت هزینه", "هزینه‌های جاری شرکت", Icons.Default.TrendingDown, AccountingCreditRed, "expenses"),
        QuickActionItem("ثبت درآمد", "درآمدهای عملیاتی", Icons.Default.TrendingUp, AccountingDebitGreen, "incomes"),
        QuickActionItem("انبار و کالا", "موجودی و کاردکس", Icons.Default.Inventory2, Color(0xFF00796B), "inventory"),
        QuickActionItem("مشتریان CRM", "طرف‌های حساب و سقف", Icons.Default.People, Color(0xFFE65100), "crm"),
        QuickActionItem("حسابداری", "سند دوبل و ترازنامه", Icons.AutoMirrored.Filled.Assignment, Color(0xFF1976D2), "accounting"),
        QuickActionItem("تنظیمات", "پشتیبان‌گیری و امنیتی", Icons.Default.Settings, Color(0xFF455A64), "settings")
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        containerColor = CleanBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // =========================================================================
            // 1. هدر اختصاصی و فاخر با تاریخ شمسی واقعی (Executive Corporate Header)
            // =========================================================================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(NavyDark, NavyPrimary, Color(0xFF22293C))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // ردیف بالایی: عنوان برند و تگ وضعیت
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "مدیریت حسابداری گلی",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MatteGold.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, MatteGold.copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = "ERP ابری",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MatteGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "سامانه یکپارچه مالی، انبارداری و صندوق فروشگاهی",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            // دکمه میانبر سریع به صندوق
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MatteGold.copy(alpha = 0.2f))
                                    .clickable { onNavigateToRoute("pos") }
                                    .testTag("dashboard_header_pos_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = "صندوق فروش",
                                    tint = MatteGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // نوار تاریخ شمسی واقعی با تقویم
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "تاریخ شمسی",
                                        tint = MatteGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = state.currentShamsiFullDate,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MatteGoldContainer
                                ) {
                                    Text(
                                        text = state.currentShamsiDate,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NavyPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. کارت جامع نقدینگی صندوق و بانک (Liquidity Card)
            // =========================================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("liquidity_card")
                        .clickable { onNavigateToRoute("treasury") },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NavySurface
                    ),
                    border = BorderStroke(1.5.dp, MatteGold.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // تیتر و کل نقدینگی
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MatteGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MatteGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "مجموع نقدینگی در دسترس",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 12.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = NumberFormatUtils.formatPrice(totalLiquidity),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = MatteGold,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 21.sp
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "مشاهده خزانه‌داری",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // تفکیک دقیق: صندوق‌ها و بانک‌ها در دو ستون شکیل
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // ستون موجودی صندوق
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(AccountingDebitGreen)
                                        )
                                        Text(
                                            text = "موجودی صندوق‌ها",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = NumberFormatUtils.formatPrice(state.totalCashBalance),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "وجوه نقد صندوق‌دار",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // ستون موجودی بانک‌ها
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(StatusInfoBlue)
                                        )
                                        Text(
                                            text = "موجودی حساب‌های بانک",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = NumberFormatUtils.formatPrice(state.totalBankBalance),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "کارتخوان‌ها و حساب‌ها",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. چهار کارت آماری اصلی (Four Key Metric Cards)
            // فروش امروز، تعداد فاکتورها، مطالبات و هزینه‌ها
            // =========================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "شاخص‌های مالی کلیدی",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextDarkPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )

                    // ردیف اول کارت‌های آماری: فروش امروز و تعداد فاکتورها
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ۱. فروش امروز
                        StatGridCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("metric_sales"),
                            title = "فروش امروز",
                            value = NumberFormatUtils.formatPrice(state.todaySalesAmount),
                            subtitle = "مجموع فروش ثبت‌شده",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            iconTint = AccountingDebitGreen,
                            badgeColor = AccountingDebitGreen.copy(alpha = 0.12f),
                            onClick = { onNavigateToRoute("invoices") }
                        )

                        // ۲. تعداد فاکتورها
                        StatGridCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("metric_invoices"),
                            title = "تعداد فاکتورها",
                            value = "${NumberFormatUtils.toPersianDigits(state.todayInvoicesCount.toString())} فاکتور",
                            subtitle = "اسناد فروش روز جاری",
                            icon = Icons.Default.PointOfSale,
                            iconTint = MatteGold,
                            badgeColor = MatteGold.copy(alpha = 0.15f),
                            onClick = { onNavigateToRoute("invoices") }
                        )
                    }

                    // ردیف دوم کارت‌های آماری: مطالبات و هزینه‌ها
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ۳. مطالبات (طلب از مشتریان)
                        StatGridCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("metric_receivables"),
                            title = "مطالبات از مشتریان",
                            value = NumberFormatUtils.formatPrice(state.totalReceivables),
                            subtitle = "مانده بدهکاری اشخاص",
                            icon = Icons.Default.TrendingUp,
                            iconTint = StatusInfoBlue,
                            badgeColor = StatusInfoBlue.copy(alpha = 0.12f),
                            onClick = { onNavigateToRoute("crm") }
                        )

                        // ۴. هزینه‌ها
                        StatGridCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("metric_expenses"),
                            title = "هزینه‌های ثبت‌شده",
                            value = NumberFormatUtils.formatPrice(state.todayExpenses),
                            subtitle = "کل دوره: ${NumberFormatUtils.formatPrice(state.totalExpenses)}",
                            icon = Icons.Default.TrendingDown,
                            iconTint = AccountingCreditRed,
                            badgeColor = AccountingCreditRed.copy(alpha = 0.12f),
                            onClick = { onNavigateToRoute("expenses") }
                        )
                    }
                }
            }

            // =========================================================================
            // 4. مرکز هوشمند هشدارها و سررسیدها (Alerts Center)
            // =========================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "هشدارها و موارد نیازمند اقدام",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextDarkPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )

                        val totalAlerts = (if (state.dueChequesAlertCount > 0) 1 else 0) +
                                (if (state.lowStockCount > 0) 1 else 0) +
                                (if (state.overCreditPartiesCount > 0) 1 else 0)

                        if (totalAlerts > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AlertWarningOrange.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, AlertWarningOrange.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${NumberFormatUtils.toPersianDigits(totalAlerts.toString())} مورد مهم",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AlertWarningOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // هشدار ۱: سررسید چک‌ها
                    if (state.dueChequesAlertCount > 0) {
                        DashboardAlertCard(
                            title = "سررسید چک‌های خزانه‌داری",
                            description = "${NumberFormatUtils.toPersianDigits(state.dueChequesAlertCount.toString())} فقره چک دریافتی یا پرداختی در ۷ روز آینده یا معوقه",
                            icon = Icons.Default.DateRange,
                            accentColor = AlertWarningOrange,
                            containerColor = Color(0xFFFFF9E6),
                            actionText = "بررسی چک‌ها",
                            onClick = { onNavigateToRoute("treasury") }
                        )
                    }

                    // هشدار ۲: کسری موجودی کالا در انبار
                    if (state.lowStockCount > 0) {
                        DashboardAlertCard(
                            title = "کسری موجودی انبار",
                            description = "${NumberFormatUtils.toPersianDigits(state.lowStockCount.toString())} قلم کالا به حداقل نقطه سفارش رسیده و نیاز به تأمین دارد",
                            icon = Icons.Default.Warning,
                            accentColor = AccountingCreditRed,
                            containerColor = Color(0xFFFFF0F0),
                            actionText = "مدیریت انبار",
                            onClick = { onNavigateToRoute("inventory") }
                        )
                    }

                    // هشدار ۳: سقف اعتبار مشتریان
                    if (state.overCreditPartiesCount > 0) {
                        DashboardAlertCard(
                            title = "تخطی از سقف اعتبار مشتریان",
                            description = "${NumberFormatUtils.toPersianDigits(state.overCreditPartiesCount.toString())} مشتری از سقف مجاز خرید نسیه عبور کرده‌اند",
                            icon = Icons.Default.People,
                            accentColor = Color(0xFFD32F2F),
                            containerColor = Color(0xFFFFECEC),
                            actionText = "مشاهده CRM",
                            onClick = { onNavigateToRoute("crm") }
                        )
                    }

                    // در صورت نبود هیچ هشدار فعالی
                    if (state.dueChequesAlertCount == 0 && state.lowStockCount == 0 && state.overCreditPartiesCount == 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CleanSurface),
                            border = BorderStroke(1.dp, CleanBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AccountingDebitGreen.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccountingDebitGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "وضعیت سیستم پایدار و مطلوب است",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextDarkPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    )
                                    Text(
                                        text = "هیچ چک معوق، کسری بحرانی انبار یا تخطی اعتباری وجود ندارد.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextDarkSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 5. عملیات و دسترسی سریع به ماژول‌های ERP (Quick Actions Grid)
            // =========================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "عملیات و دسترسی سریع",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextDarkPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )

                    // شبکه دسترسی به ماژول‌ها در ردیف‌های منظم
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in quickActions.indices step 3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (j in 0 until 3) {
                                    val index = i + j
                                    if (index < quickActions.size) {
                                        val action = quickActions[index]
                                        QuickActionTile(
                                            modifier = Modifier.weight(1f),
                                            action = action,
                                            onClick = { onNavigateToRoute(action.route) }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * کارت آماری گرید با پشتیبانی کامل از زبان فارسی و راست‌چین
 */
@Composable
private fun StatGridCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanSurface),
        border = BorderStroke(1.dp, CleanBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextDarkSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextDarkPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextDarkMuted,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * کارت هوشمند هشدار با رنگ‌بندی تفکیک‌شده و دکمه ارجاع
 */
@Composable
private fun DashboardAlertCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    actionText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkPrimary,
                            fontSize = 11.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * کاشی عملیات سریع
 */
@Composable
private fun QuickActionTile(
    action: QuickActionItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("quick_action_${action.route}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanSurface),
        border = BorderStroke(1.dp, CleanBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(action.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextDarkPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextDarkMuted,
                    fontSize = 9.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
