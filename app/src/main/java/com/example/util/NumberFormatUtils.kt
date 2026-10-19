package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toPersianDigits(number: Number): String {
        return toPersianDigits(number.toString())
    }

    fun toEnglishDigits(text: String): String {
        return text
            .replace('۰', '0')
            .replace('۱', '1')
            .replace('۲', '2')
            .replace('۳', '3')
            .replace('۴', '4')
            .replace('۵', '5')
            .replace('۶', '6')
            .replace('۷', '7')
            .replace('۸', '8')
            .replace('۹', '9')
            .replace('٠', '0')
            .replace('١', '1')
            .replace('٢', '2')
            .replace('٣', '3')
            .replace('٤', '4')
            .replace('٥', '5')
            .replace('٦', '6')
            .replace('٧', '7')
            .replace('٨', '8')
            .replace('٩', '9')
    }

    fun formatPrice(amount: Long, isToman: Boolean = true): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        val formatted = formatter.format(amount)
        val persian = toPersianDigits(formatted)
        val unit = if (isToman) "تومان" else "ریال"
        return "$persian $unit"
    }

    fun formatPriceOnlyNumber(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        return toPersianDigits(formatter.format(amount))
    }

    fun formatQuantity(quantity: Double): String {
        return if (quantity % 1.0 == 0.0) {
            toPersianDigits(quantity.toInt().toString())
        } else {
            toPersianDigits(String.format(Locale.US, "%.2f", quantity))
        }
    }

    /**
     * تبدیل شماره فاکتورهای انگلیسی مانند INV-1001 یا INV_1001 به قالب شکیل فارسی: «فاکتور-۱۰۰۱»
     */
    fun formatInvoiceNumber(number: String): String {
        if (number.isBlank()) return "فاکتور"
        val cleaned = number
            .replace("INV-", "فاکتور-")
            .replace("INV_", "فاکتور-")
            .replace("inv-", "فاکتور-")
            .replace("inv_", "فاکتور-")
            .replace("PRO-", "پیش‌فاکتور-")
            .replace("RET-", "مرجوعی-")
        return toPersianDigits(cleaned)
    }

    /**
     * تبدیل کدهای روش تسویه انگلیسی به متن فارسی روان
     */
    fun formatPaymentType(paymentType: String): String {
        return when (paymentType.trim().uppercase()) {
            "CASH" -> "نقدی"
            "POS" -> "کارتخوان بانکی (POS)"
            "CREDIT" -> "نسیه (حساب مشتری)"
            "CHEQUE" -> "چک صیادی"
            "MIXED" -> "ترکیبی (چندحالته)"
            else -> paymentType
        }
    }

    /**
     * تبدیل نوع فاکتور به فارسی
     */
    fun formatInvoiceType(invoiceType: String): String {
        return when (invoiceType.trim().uppercase()) {
            "SALE" -> "فاکتور فروش"
            "PURCHASE" -> "فاکتور خرید"
            "RETURN_SALE" -> "برگشت از فروش"
            "RETURN_PURCHASE" -> "برگشت از خرید"
            "POS_SALE" -> "فروش صندوق (POS)"
            else -> invoiceType
        }
    }

    /**
     * تبدیل وضعیت فاکتور به فارسی
     */
    fun formatInvoiceStatus(status: String): String {
        return when (status.trim().uppercase()) {
            "FINAL" -> "ثبت قطعی"
            "PROFORMA" -> "پیش‌فاکتور"
            "VOID" -> "ابطال شده"
            else -> status
        }
    }

    private val ones = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val teens = arrayOf(
        "ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده"
    )
    private val tens = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val hundreds = arrayOf(
        "", "یکصد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد"
    )
    private val thousands = arrayOf("", "هزار", "میلیون", "میلیارد", "تریلیون")

    fun numberToPersianWords(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 0) return "منفی " + numberToPersianWords(-number)

        var num = number
        var chunkIndex = 0
        val parts = mutableListOf<String>()

        while (num > 0) {
            val chunk = (num % 1000).toInt()
            if (chunk != 0) {
                val chunkText = convertThreeDigits(chunk)
                val suffix = thousands[chunkIndex]
                val fullChunk = if (suffix.isNotEmpty()) "$chunkText $suffix" else chunkText
                parts.add(0, fullChunk)
            }
            num /= 1000
            chunkIndex++
        }

        return parts.joinToString(" و ")
    }

    private fun convertThreeDigits(n: Int): String {
        val h = n / 100
        val remainder = n % 100
        val t = remainder / 10
        val o = remainder % 10

        val list = mutableListOf<String>()
        if (h > 0) list.add(hundreds[h])

        if (remainder in 10..19) {
            list.add(teinsText(remainder))
        } else {
            if (t > 0) list.add(tens[t])
            if (o > 0) list.add(ones[o])
        }

        return list.joinToString(" و ")
    }

    private fun teinsText(n: Int): String = teens[n - 10]
}
