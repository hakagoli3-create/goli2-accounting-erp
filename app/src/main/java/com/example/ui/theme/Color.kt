package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// سامانه جامع حسابداری و مدیریت مالی گلی (GOLI ACCOUNTING)
// سیستم طراحی رابط کاربری (Design System)
// ============================================================================

// --- رنگ‌های اصلی هویت تجاری (Official Brand Palette) ---
val NavyPrimary = Color(0xFF171C2A)        // سرمه‌ای اصلی (#171C2A)
val NavyBrand = NavyPrimary
val NavyDark = Color(0xFF0F121C)
val NavySurface = Color(0xFF1E2435)
val NavyCard = Color(0xFF262D42)
val NavyBorder = Color(0xFF38425E)
val NavySecondary = Color(0xFF2D354D)

// --- طلایی مات اختصاصی گلی (Matte Gold Accent) ---
val MatteGold = Color(0xFFC6A15B)          // طلایی مات (#C6A15B)
val MatteGoldLight = Color(0xFFD8B573)
val MatteGoldContainer = Color(0xFFF7F2E7) // کانتینر طلایی بسیار ملایم
val MatteGoldOnContainer = Color(0xFF4C3B14)

// --- سطوح و پس‌زمینه (Surfaces & Background) ---
val CleanBackground = Color(0xFFF5F6F8)    // پس‌زمینه روشن (#F5F6F8)
val CleanSurface = Color(0xFFFFFFFF)       // کارت و سطوح سفید (#FFFFFF)
val CleanCard = Color(0xFFFFFFFF)          // کارت سفید (#FFFFFF)
val CleanBorder = Color(0xFFE5E8ED)        // حاشیه استاندارد (#E5E8ED)

// --- متون و خوانایی (Typography & Text Hierarchy) ---
val TextDarkPrimary = Color(0xFF171C2A)    // متن اصلی سرمه‌ای (#171C2A)
val TextDarkSecondary = Color(0xFF5A6478)  // متن ثانویه و برچسب‌ها (#5A6478)
val TextDarkMuted = Color(0xFF8A94A6)      // متن کم‌رنگ و راهنما (#8A94A6)

// --- سازگاری با متغیرهای قدیمی پروژه (Backwards-compatibility Aliases) ---
val CharcoalPrimary = NavyPrimary
val CharcoalDark = NavyDark
val CharcoalSurface = NavySurface
val CharcoalCard = NavyCard
val CharcoalBorder = NavyBorder
val CharcoalSecondary = NavySecondary

val WarmCreamBackground = CleanBackground
val WarmBeigeCard = CleanCard
val CreamSurface = CleanSurface
val BeigeBorder = CleanBorder
val WarmTextDark = TextDarkPrimary
val WarmTextMuted = TextDarkSecondary

val PersianGold = MatteGold
val PersianGoldLight = MatteGoldLight
val PersianGoldContainer = MatteGoldContainer
val PersianGoldOnContainer = MatteGoldOnContainer

val SlateBlueAccent = Color(0xFF2E3D4F)
val SlateBlueContainer = Color(0xFFDDE4EC)

// --- شاخص‌های مالی و اعتباری حسابداری (Financial Semantic Status) ---
val AccountingDebitGreen = Color(0xFF1A7A4C) // مثبت / بستانکار / واریز / سود
val AccountingCreditRed = Color(0xFFBA2525)  // منفی / بدهکار / برداشت / ضرر
val AlertWarningOrange = Color(0xFFC97D18)   // هشدار / سررسید / معوقه
val StatusInfoBlue = Color(0xFF1E5BB8)       // اطلاع‌رسانی / عادی

// --- حالت تیره (Dark Theme Variants) ---
val DarkBackground = Color(0xFF10131A)
val DarkSurface = Color(0xFF171C2A)
val DarkCard = Color(0xFF1F2536)
val DarkBorder = Color(0xFF2E374D)
val DarkTextPrimary = Color(0xFFF5F7FA)
val DarkTextSecondary = Color(0xFFA5B0BF)
