package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import com.example.data.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class UserRole(
    val code: String,
    val title: String,
    val description: String
) {
    ADMIN(
        code = "ADMIN",
        title = "مدیر کل (Admin)",
        description = "دسترسی کاملاً آزاد به تمام بخش‌ها، تنظیمات و CRUD کامل"
    ),
    POS_USER(
        code = "POS_USER",
        title = "فروشنده / صندوق‌دار (POS User)",
        description = "محدود به صدور فاکتور، مشاهده کالا و صندوق"
    ),
    VISITOR(
        code = "VISITOR",
        title = "ویزیتور (Visitor)",
        description = "محدود به کاتالوگ و ثبت پیش‌فاکتور"
    );

    val canViewProfit: Boolean get() = this == ADMIN
    val canDeleteDocuments: Boolean get() = this == ADMIN
    val canEditCostPrices: Boolean get() = this == ADMIN
    val canAccessAccountingLedger: Boolean get() = this == ADMIN
    val canAccessAccounting: Boolean get() = this == ADMIN
    val canAccessTreasuryBalances: Boolean get() = this == ADMIN
    val canAccessTreasury: Boolean get() = this == ADMIN
    val canAccessExpensesAndIncomes: Boolean get() = this == ADMIN
    val canAccessSettings: Boolean get() = this == ADMIN
    val canAccessPos: Boolean get() = this == ADMIN || this == POS_USER
    val canIssueInvoices: Boolean get() = true
    val isVisitorOnly: Boolean get() = this == VISITOR

    companion object {
        val MANAGER = ADMIN
        val SELLER = POS_USER

        fun fromString(value: String?): UserRole {
            return when (value?.uppercase()) {
                "ADMIN", "MANAGER" -> ADMIN
                "POS_USER", "SELLER" -> POS_USER
                "VISITOR" -> VISITOR
                else -> ADMIN
            }
        }
    }
}

data class StoreProfile(
    val name: String = "حسابداری و بازرگانی گلی",
    val managerName: String = "آقای گلی",
    val phone: String = "021-88990011",
    val mobile: String = "09121112233",
    val address: String = "تهران، بازار بزرگ، سرای امید، پلاک ۴۴",
    val nationalCode: String = "10103456789",
    val economicCode: String = "411234567891",
    val defaultBankCard: String = "6037-9975-1234-5678",
    val defaultSheba: String = "IR120170000000123456789012",
    val taxRatePercent: Int = 10,
    val wholesaleDiscountLimitPercent: Int = 25
)

data class CurrencyItem(
    val code: String,
    val nameFa: String,
    val symbol: String,
    val exchangeRateToBase: Double = 1.0,
    val isActive: Boolean = true,
    val isBaseCurrency: Boolean = false,
    val isDefaultInvoiceCurrency: Boolean = false,
    val decimals: Int = 0
)

enum class CloudSyncStatus(val title: String) {
    ONLINE_SYNCED("همگام‌سازی ابری فعال و آنلاین"),
    SYNCING("در حال ارسال و دریافت اطلاعات ابری..."),
    OFFLINE_LOCAL("کارکرد آفلاین محلی (ذخیره در دیتابیس دستگاه)"),
    FAILED("خطا در ارتباط با سرور ابری")
}

class AppSecurityAndSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("goli_erp_settings", Context.MODE_PRIVATE)

    // Currencies State
    private val _currencies = MutableStateFlow<List<CurrencyItem>>(loadInitialCurrencies())
    val currencies: StateFlow<List<CurrencyItem>> = _currencies.asStateFlow()

    // Session status - defaults to false on first launch to show LoginScreen as start destination
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInUsername = MutableStateFlow(prefs.getString("logged_in_username", "admin") ?: "admin")
    val loggedInUsername: StateFlow<String> = _loggedInUsername.asStateFlow()

    private val _loggedInFullName = MutableStateFlow(prefs.getString("logged_in_fullname", "مدیر سیستم") ?: "مدیر سیستم")
    val loggedInFullName: StateFlow<String> = _loggedInFullName.asStateFlow()

    private val _currentRole = MutableStateFlow(
        UserRole.fromString(prefs.getString("user_role", UserRole.ADMIN.name))
    )
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _storeProfile = MutableStateFlow(
        StoreProfile(
            name = prefs.getString("store_name", "حسابداری و بازرگانی گلی") ?: "حسابداری و بازرگانی گلی",
            managerName = prefs.getString("manager_name", "آقای گلی") ?: "آقای گلی",
            phone = prefs.getString("store_phone", "021-88990011") ?: "021-88990011",
            mobile = prefs.getString("store_mobile", "09121112233") ?: "09121112233",
            address = prefs.getString("store_address", "تهران، بازار بزرگ، سرای امید، پلاک ۴۴") ?: "تهران، بازار بزرگ، سرای امید، پلاک ۴۴",
            nationalCode = prefs.getString("store_national", "10103456789") ?: "10103456789",
            economicCode = prefs.getString("store_economic", "411234567891") ?: "411234567891",
            defaultBankCard = prefs.getString("store_card", "6037-9975-1234-5678") ?: "6037-9975-1234-5678",
            defaultSheba = prefs.getString("store_sheba", "IR120170000000123456789012") ?: "IR120170000000123456789012",
            taxRatePercent = prefs.getInt("tax_rate", 10),
            wholesaleDiscountLimitPercent = prefs.getInt("wholesale_discount_limit", 25)
        )
    )
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.OFFLINE_LOCAL)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getString("last_sync_time", "هنوز همگام‌سازی نشده") ?: "هنوز همگام‌سازی نشده")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    suspend fun loginWithDb(username: String, pass: String, db: AppDatabase): Pair<Boolean, String?> {
        val u = username.trim()
        val p = pass.trim()
        if (u.isBlank() || p.isBlank()) {
            return Pair(false, "لطفاً نام کاربری و رمز عبور را وارد نمایید.")
        }

        val user = try {
            db.userDao().getUserByUsername(u)
        } catch (_: Exception) {
            null
        }

        if (user != null) {
            if (!user.isActive) {
                return Pair(false, "حساب کاربری «$u» توسط مدیر سیستم غیرفعال شده است.")
            }
            if (user.passwordHash != p) {
                return Pair(false, "رمز عبور وارد شده برای کاربر «$u» نادرست است.")
            }
            val role = UserRole.fromString(user.role)
            setLoggedInUser(user.username, user.fullName, role)
            return Pair(true, null)
        }

        // Fallback in case DB is being initialized or default admin
        val fallbackSuccess = login(u, p)
        return if (fallbackSuccess) {
            Pair(true, null)
        } else {
            Pair(false, "کاربری با نام «$u» یافت نشد یا رمز عبور اشتباه است.")
        }
    }

    fun login(username: String, pass: String): Boolean {
        val u = username.trim().lowercase()
        val p = pass.trim()
        val (role, fullName) = when {
            u == "admin" && (p == "1234" || p == "admin") -> Pair(UserRole.ADMIN, "مدیر کل سیستم")
            u == "seller" && (p == "1234" || p == "seller") -> Pair(UserRole.POS_USER, "صندوق‌دار / فروشنده")
            u == "visitor" && (p == "1234" || p == "visitor") -> Pair(UserRole.VISITOR, "ویزیتور / بازاریاب")
            u == "manager" && p == "1234" -> Pair(UserRole.ADMIN, "مدیر کل")
            else -> Pair(null, null)
        }

        return if (role != null) {
            setLoggedInUser(u, fullName ?: u, role)
            true
        } else {
            false
        }
    }

    fun setLoggedInUser(username: String, fullName: String, role: UserRole) {
        _isLoggedIn.value = true
        _loggedInUsername.value = username
        _loggedInFullName.value = fullName
        _currentRole.value = role
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("logged_in_username", username)
            .putString("logged_in_fullname", fullName)
            .putString("user_role", role.name)
            .apply()
    }

    fun logout() {
        _isLoggedIn.value = false
        _loggedInUsername.value = ""
        _loggedInFullName.value = ""
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("logged_in_username", "")
            .putString("logged_in_fullname", "")
            .apply()
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        prefs.edit().putString("user_role", role.name).apply()
    }

    fun updateProfile(profile: StoreProfile) {
        _storeProfile.value = profile
        prefs.edit()
            .putString("store_name", profile.name)
            .putString("manager_name", profile.managerName)
            .putString("store_phone", profile.phone)
            .putString("store_mobile", profile.mobile)
            .putString("store_address", profile.address)
            .putString("store_national", profile.nationalCode)
            .putString("store_economic", profile.economicCode)
            .putString("store_card", profile.defaultBankCard)
            .putString("store_sheba", profile.defaultSheba)
            .putInt("tax_rate", profile.taxRatePercent)
            .putInt("wholesale_discount_limit", profile.wholesaleDiscountLimitPercent)
            .apply()
    }

    fun setSyncStatus(status: CloudSyncStatus, time: String? = null) {
        _syncStatus.value = status
        if (time != null) {
            _lastSyncTime.value = time
            prefs.edit().putString("last_sync_time", time).apply()
        }
    }

    // ==========================================
    // CURRENCY MANAGEMENT
    // ==========================================

    private fun loadInitialCurrencies(): List<CurrencyItem> {
        val rawJson = prefs.getString("system_currencies", null)
        if (!rawJson.isNullOrBlank()) {
            try {
                val array = JSONArray(rawJson)
                val list = mutableListOf<CurrencyItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CurrencyItem(
                            code = obj.optString("code", "TOMAN"),
                            nameFa = obj.optString("nameFa", "تومان"),
                            symbol = obj.optString("symbol", "تومان"),
                            exchangeRateToBase = obj.optDouble("exchangeRateToBase", 1.0),
                            isActive = obj.optBoolean("isActive", true),
                            isBaseCurrency = obj.optBoolean("isBaseCurrency", false),
                            isDefaultInvoiceCurrency = obj.optBoolean("isDefaultInvoiceCurrency", false),
                            decimals = obj.optInt("decimals", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }

        // Default initial standard currencies
        return listOf(
            CurrencyItem("TOMAN", "تومان", "تومان", 1.0, isActive = true, isBaseCurrency = true, isDefaultInvoiceCurrency = true, decimals = 0),
            CurrencyItem("IRR", "ریال ایران", "ریال", 0.1, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 0),
            CurrencyItem("USD", "دلار آمریکا", "$", 92000.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 2),
            CurrencyItem("EUR", "یورو", "€", 101000.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 2),
            CurrencyItem("TRY", "لیر ترکیه", "₺", 2650.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 2),
            CurrencyItem("IQD", "دینار عراق", "د.ع", 62.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 0)
        )
    }

    fun getDefaultCurrenciesList(): List<CurrencyItem> = listOf(
        CurrencyItem("TOMAN", "تومان", "تومان", 1.0, isActive = true, isBaseCurrency = true, isDefaultInvoiceCurrency = true, decimals = 0),
        CurrencyItem("IRR", "ریال ایران", "ریال", 0.1, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 0),
        CurrencyItem("USD", "دلار آمریکا", "$", 92000.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 2),
        CurrencyItem("EUR", "یورو", "€", 101000.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 2),
        CurrencyItem("TRY", "لیر ترکیه", "₺", 2650.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 2),
        CurrencyItem("IQD", "دینار عراق", "د.ع", 62.0, isActive = true, isBaseCurrency = false, isDefaultInvoiceCurrency = false, decimals = 0)
    )

    fun resetToDefaultCurrencies() {
        saveCurrencies(getDefaultCurrenciesList())
    }

    fun saveCurrencies(list: List<CurrencyItem>) {
        _currencies.value = list
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject()
                obj.put("code", item.code)
                obj.put("nameFa", item.nameFa)
                obj.put("symbol", item.symbol)
                obj.put("exchangeRateToBase", item.exchangeRateToBase)
                obj.put("isActive", item.isActive)
                obj.put("isBaseCurrency", item.isBaseCurrency)
                obj.put("isDefaultInvoiceCurrency", item.isDefaultInvoiceCurrency)
                obj.put("decimals", item.decimals)
                array.put(obj)
            }
            prefs.edit().putString("system_currencies", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun setBaseCurrency(code: String) {
        val current = _currencies.value
        val updated = current.map { item ->
            val isBase = item.code.equals(code, ignoreCase = true)
            item.copy(
                isBaseCurrency = isBase,
                isActive = if (isBase) true else item.isActive,
                exchangeRateToBase = if (isBase) 1.0 else item.exchangeRateToBase
            )
        }
        saveCurrencies(updated)
    }

    fun setDefaultInvoiceCurrency(code: String) {
        val current = _currencies.value
        val updated = current.map { item ->
            val isDefault = item.code.equals(code, ignoreCase = true)
            item.copy(
                isDefaultInvoiceCurrency = isDefault,
                isActive = if (isDefault) true else item.isActive
            )
        }
        saveCurrencies(updated)
    }

    fun updateCurrency(updatedItem: CurrencyItem) {
        val current = _currencies.value
        val updated = current.map { item ->
            if (item.code.equals(updatedItem.code, ignoreCase = true)) updatedItem else item
        }
        saveCurrencies(updated)
    }

    fun addCurrency(newItem: CurrencyItem) {
        val current = _currencies.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.code.equals(newItem.code, ignoreCase = true) }
        if (existingIndex >= 0) {
            current[existingIndex] = newItem
        } else {
            current.add(newItem)
        }
        saveCurrencies(current)
    }

    fun toggleCurrencyActive(code: String, isActive: Boolean) {
        val current = _currencies.value
        val updated = current.map { item ->
            if (item.code.equals(code, ignoreCase = true)) {
                // Cannot deactivate base currency
                if (item.isBaseCurrency && !isActive) item else item.copy(isActive = isActive)
            } else item
        }
        saveCurrencies(updated)
    }

    fun deleteCurrency(code: String) {
        val current = _currencies.value
        // Protect base currency from deletion
        val updated = current.filterNot { it.code.equals(code, ignoreCase = true) && !it.isBaseCurrency }
        saveCurrencies(updated)
    }

    companion object {
        @Volatile
        private var INSTANCE: AppSecurityAndSettingsManager? = null

        fun getInstance(context: Context): AppSecurityAndSettingsManager {
            return INSTANCE ?: synchronized(this) {
                val inst = AppSecurityAndSettingsManager(context.applicationContext)
                INSTANCE = inst
                inst
            }
        }
    }
}
