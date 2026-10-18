package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// ============================================================================
// استانداردهای فرم و هندسه کارت‌ها (Card Shapes: 14dp - 16dp)
// ============================================================================
val CardCornerRadius: Dp = 16.dp
val CardShape = RoundedCornerShape(CardCornerRadius)
val AppCardBorder = BorderStroke(1.dp, CleanBorder)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),  // گوشه استاندارد کارت‌ها (۱۴ تا ۱۶ dp)
    large = RoundedCornerShape(16.dp),   // کارت‌های اصلی و دیالوگ‌ها (۱۴ تا ۱۶ dp)
    extraLarge = RoundedCornerShape(20.dp)
)

// ============================================================================
// سیستم رنگ‌بندی روشن (Light Color Scheme)
// سرمه‌ای: #171C2A | طلایی مات: #C6A15B | پس‌زمینه: #F5F6F8 | کارت: #FFFFFF | حاشیه: #E5E8ED
// ============================================================================
private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,                   // سرمه‌ای اصلی (#171C2A)
    onPrimary = Color.White,
    primaryContainer = MatteGoldContainer,   // کانتینر طلایی ملایم (#F7F2E7)
    onPrimaryContainer = NavyPrimary,
    secondary = MatteGold,                   // طلایی مات (#C6A15B)
    onSecondary = Color.White,
    secondaryContainer = MatteGoldContainer,
    onSecondaryContainer = MatteGoldOnContainer,
    tertiary = SlateBlueAccent,
    onTertiary = Color.White,
    background = CleanBackground,            // پس‌زمینه روشن (#F5F6F8)
    onBackground = TextDarkPrimary,          // متن سرمه‌ای روی پس‌زمینه (#171C2A)
    surface = CleanSurface,                  // کارت و سطوح سفید (#FFFFFF)
    onSurface = TextDarkPrimary,             // متن روی کارت (#171C2A)
    surfaceVariant = CleanSurface,           // کارت‌های پیش‌فرض M3 به رنگ سفید (#FFFFFF)
    onSurfaceVariant = TextDarkSecondary,   // متن ثانویه (#5A6478)
    outline = CleanBorder,                   // حاشیه کارت‌ها (#E5E8ED)
    outlineVariant = CleanBorder             // خطوط جداکننده و مرزبندی (#E5E8ED)
)

// ============================================================================
// سیستم رنگ‌بندی تیره (Dark Color Scheme)
// ============================================================================
private val DarkColorScheme = darkColorScheme(
    primary = MatteGoldLight,
    onPrimary = NavyDark,
    primaryContainer = NavyCard,
    onPrimaryContainer = MatteGoldLight,
    secondary = MatteGold,
    onSecondary = NavyDark,
    secondaryContainer = NavySecondary,
    onSecondaryContainer = MatteGoldContainer,
    tertiary = SlateBlueAccent,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

val LocalThemeIsDark = compositionLocalOf { mutableStateOf(false) }

@Composable
fun GoliAccountingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = AppShapes,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GoliAccountingTheme(darkTheme = darkTheme, content = content)
}
