package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * سرفصل‌ها و دسته‌بندی‌های هزینه (مانند اجاره، حقوق، حمل و نقل، ملزومات، بازاریابی و...)
 */
@Entity(tableName = "expense_categories")
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String, // e.g. "EXP-01"
    val title: String, // e.g. "اجاره محل"
    val description: String = "",
    val iconName: String = "Receipt"
)

/**
 * ریز اسناد هزینه ثبت‌شده در سیستم
 */
@Entity(
    tableName = "expenses",
    indices = [Index("categoryId"), Index("jalaliDate"), Index("timestamp")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Long,
    val categoryTitle: String,
    val amount: Long, // مبلغ به تومان
    val jalaliDate: String, // ۱۴۰۴/۰۱/۱۵
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "CASH", // "CASH" (صندوق) or "BANK" (بانک)
    val cashDeskId: Long? = null,
    val bankAccountId: Long? = null,
    val accountName: String = "", // e.g. "صندوق اصلی" یا "بانک ملت"
    val title: String, // بابت / عنوان هزینه
    val notes: String = "", // توضیحات تکمیلی
    val voucherId: Long? = null // شناسه سند حسابداری متصل
)

/**
 * دسته‌بندی‌های درآمد متفرقه و غیرعملیاتی
 */
@Entity(tableName = "income_categories")
data class IncomeCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String, // e.g. "INC-01"
    val title: String, // e.g. "درآمدهای متفرقه"
    val description: String = "",
    val iconName: String = "TrendingUp"
)

/**
 * ریز اسناد درآمد ثبت‌شده در سیستم
 */
@Entity(
    tableName = "incomes",
    indices = [Index("categoryId"), Index("jalaliDate"), Index("timestamp")]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Long,
    val categoryTitle: String,
    val amount: Long, // مبلغ به تومان
    val jalaliDate: String, // ۱۴۰۴/۰۱/۱۵
    val timestamp: Long = System.currentTimeMillis(),
    val depositMethod: String = "BANK", // "CASH" or "BANK"
    val cashDeskId: Long? = null,
    val bankAccountId: Long? = null,
    val accountName: String = "", // حساب واریزی
    val title: String, // بابت / عنوان درآمد
    val notes: String = "", // توضیحات تکمیلی
    val voucherId: Long? = null // شناسه سند حسابداری متصل
)
