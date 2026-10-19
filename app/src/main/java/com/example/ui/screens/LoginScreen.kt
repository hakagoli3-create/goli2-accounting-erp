package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.database.AppDatabase
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.UserRole
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.NavyBrand
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PersianGold
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    securityManager: AppSecurityAndSettingsManager,
    db: AppDatabase? = null,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val storeProfile by securityManager.storeProfile.collectAsStateWithLifecycle()

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showRolesDialog by remember { mutableStateOf(false) }

    fun validateInputs(): Boolean {
        var isValid = true
        usernameError = null
        passwordError = null
        errorMessage = null

        val trimmedUser = username.trim()
        val trimmedPass = password.trim()

        if (trimmedUser.isBlank()) {
            usernameError = "لطفاً نام کاربری یا شناسه پرسنلی را وارد کنید."
            isValid = false
        }

        if (trimmedPass.isBlank()) {
            passwordError = "لطفاً رمز عبور را وارد کنید."
            isValid = false
        }

        return isValid
    }

    fun doLogin() {
        focusManager.clearFocus()
        if (!validateInputs()) {
            errorMessage = "اطلاعات فرم ورود کامل نیست. لطفاً فیلدهای مشخص‌شده را پر کنید."
            return
        }

        val trimmedUser = username.trim()
        val trimmedPass = password.trim()

        isLoading = true
        errorMessage = null

        scope.launch {
            val (success, errorMsg) = if (db != null) {
                securityManager.loginWithDb(trimmedUser, trimmedPass, db)
            } else {
                val ok = securityManager.login(trimmedUser, trimmedPass)
                Pair(ok, if (!ok) "نام کاربری یا رمز عبور اشتباه است." else null)
            }

            isLoading = false
            if (success) {
                Toast.makeText(context, "ورود موفقیت‌آمیز به سامانه گلی خوش آمدید", Toast.LENGTH_SHORT).show()
                if (!rememberMe) {
                    username = ""
                }
                password = ""
                errorMessage = null
                isPasswordVisible = false
                onLoginSuccess()
            } else {
                errorMessage = errorMsg ?: "نام کاربری یا رمز عبور وارد شده نامعتبر است."
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NavyPrimary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background,
                                MatteGoldContainer.copy(alpha = 0.25f)
                            )
                        )
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // ==========================================
                    // هدر برند و لوگوی نرم‌افزار
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .border(BorderStroke(2.5.dp, PersianGold), CircleShape)
                            .background(Color.White, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "لوگوی حسابداری و بازرگانی گلی",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = storeProfile.name.ifBlank { "مدیریت حسابداری گلی" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = 0.sp
                        ),
                        color = NavyPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "سامانه یکپارچه مالی، انبارداری و صندوق فروشگاهی",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextDarkSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // نشان وضعیت امنیت سیستم
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AccountingDebitGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AccountingDebitGreen.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccountingDebitGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "دیتابیس آفلاین محلی و رمزگذاری امن آماده است",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AccountingDebitGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ==========================================
                    // کارت اصلی فرم ورود
                    // ==========================================
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, CleanBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // عنوان کارت
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(NavyPrimary, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = PersianGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ورود به حساب کاربری",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "مشخصات سازمانی خود را وارد کنید",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = TextDarkSecondary
                                        )
                                    }
                                }

                                // راهنمای نقش‌ها
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MatteGoldContainer,
                                    border = BorderStroke(1.dp, PersianGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { showRolesDialog = !showRolesDialog }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "راهنمای نقش‌ها",
                                            tint = PersianGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "راهنما",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = NavyPrimary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = CleanBorder, thickness = 1.dp)

                            // دکمه‌های ورود سریع برای تست و راهنمایی کاربران
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "حساب‌های پیش‌فرض سامانه (کلیک جهت انتخاب سریع):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = TextDarkSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    QuickRoleChip(
                                        title = "مدیر کل",
                                        username = "admin",
                                        icon = Icons.Default.AdminPanelSettings,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        username = "admin"
                                        password = "1234"
                                        usernameError = null
                                        passwordError = null
                                        errorMessage = null
                                    }

                                    QuickRoleChip(
                                        title = "صندوق‌دار",
                                        username = "seller",
                                        icon = Icons.Default.PointOfSale,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        username = "seller"
                                        password = "1234"
                                        usernameError = null
                                        passwordError = null
                                        errorMessage = null
                                    }

                                    QuickRoleChip(
                                        title = "ویزیتور",
                                        username = "visitor",
                                        icon = Icons.Default.SupportAgent,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        username = "visitor"
                                        password = "1234"
                                        usernameError = null
                                        passwordError = null
                                        errorMessage = null
                                    }
                                }
                            }

                            // فیلد نام کاربری
                            Column {
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        username = it
                                        usernameError = null
                                        errorMessage = null
                                    },
                                    label = { Text("نام کاربری یا شناسه پرسنلی") },
                                    placeholder = { Text("مثلاً admin یا نام کاربری اختصاصی", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (usernameError != null) MaterialTheme.colorScheme.error else NavyPrimary
                                        )
                                    },
                                    trailingIcon = {
                                        if (username.isNotEmpty()) {
                                            IconButton(onClick = { username = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "پاک کردن فیلد",
                                                    tint = TextDarkSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    isError = usernameError != null,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PersianGold,
                                        unfocusedBorderColor = CleanBorder,
                                        focusedLabelColor = NavyPrimary,
                                        cursorColor = PersianGold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_username_field"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                if (usernameError != null) {
                                    Text(
                                        text = usernameError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }

                            // فیلد رمز عبور
                            Column {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordError = null
                                        errorMessage = null
                                    },
                                    label = { Text("رمز عبور") },
                                    placeholder = { Text("رمز عبور سیستم (پیش‌فرض: 1234)", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (passwordError != null) MaterialTheme.colorScheme.error else NavyPrimary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { isPasswordVisible = !isPasswordVisible },
                                            modifier = Modifier.testTag("toggle_password_visibility")
                                        ) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (isPasswordVisible) "مخفی کردن رمز عبور" else "نمایش رمز عبور",
                                                tint = if (isPasswordVisible) PersianGold else TextDarkSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = passwordError != null,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { doLogin() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PersianGold,
                                        unfocusedBorderColor = CleanBorder,
                                        focusedLabelColor = NavyPrimary,
                                        cursorColor = PersianGold
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_field"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                if (passwordError != null) {
                                    Text(
                                        text = passwordError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }

                            // ردیف به‌خاطرسپاری ورود
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = NavyPrimary,
                                            checkmarkColor = PersianGold
                                        )
                                    )
                                    Text(
                                        text = "ذخیره نام کاربری برای ورودهای بعدی",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = TextDarkPrimary
                                    )
                                }
                            }

                            // کارت نمایش خطاهای اعتبارسنجی
                            AnimatedVisibility(
                                visible = errorMessage != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                if (errorMessage != null) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = AccountingCreditRed.copy(alpha = 0.10f),
                                        border = BorderStroke(1.dp, AccountingCreditRed.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = AccountingCreditRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = errorMessage!!,
                                                color = AccountingCreditRed,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { errorMessage = null },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "بستن پیام خطا",
                                                    tint = AccountingCreditRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // دکمه ورود به سیستم
                            Button(
                                onClick = { doLogin() },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("login_submit_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyPrimary,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = PersianGold,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "در حال اعتبارسنجی و بررسی دسترسی...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        tint = PersianGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ورود به سامانه حسابداری",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // راهنمای باز شونده سطوح دسترسی
                    AnimatedVisibility(
                        visible = showRolesDialog,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MatteGoldContainer),
                            border = BorderStroke(1.dp, PersianGold.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "سطوح دسترسی کاربری در نرم‌افزار گلی:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = NavyPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                RoleInfoRow(
                                    roleName = "مدیر کل (ADMIN):",
                                    description = "دسترسی آزاد به ترازنامه، دفاتر حسابداری، خزانه‌داری، چک‌ها، انبار و تنظیمات"
                                )
                                RoleInfoRow(
                                    roleName = "فروشنده / صندوق‌دار (POS):",
                                    description = "دسترسی به صدور فاکتور، پایانه فروشگاهی POS، کاردکس کالا و صندوق"
                                )
                                RoleInfoRow(
                                    roleName = "ویزیتور (VISITOR):",
                                    description = "دسترسی محدود به مشاهده کاتالوگ قیمت و ثبت پیش‌فاکتور مشتریان"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // پاورقی امنیت و نشان نسخه
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TextDarkSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "کنترل دسترسی مبتنی بر نقش (RBAC) • رمزنگاری داده‌ها • نسخه ۱.۰.۰",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickRoleChip(
    title: String,
    username: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = NavyBrand.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, CleanBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = NavyPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = username,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextDarkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RoleInfoRow(
    roleName: String,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = roleName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = TextDarkPrimary
            )
        )
    }
}
