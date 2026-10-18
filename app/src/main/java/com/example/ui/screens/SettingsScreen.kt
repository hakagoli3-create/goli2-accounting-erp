package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AppDatabase
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.CloudSyncStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.util.BluetoothThermalPrinter
import com.example.util.DatabaseBackupHelper
import com.example.util.JalaliDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    securityManager: AppSecurityAndSettingsManager,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()
    val storeProfile by securityManager.storeProfile.collectAsStateWithLifecycle()
    val syncStatus by securityManager.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by securityManager.lastSyncTime.collectAsStateWithLifecycle()
    val loggedInUsername by securityManager.loggedInUsername.collectAsStateWithLifecycle()
    val loggedInFullName by securityManager.loggedInFullName.collectAsStateWithLifecycle()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        "مدیریت کاربران و امنیت",
        "مشخصات بازرگانی",
        "پشتیبان‌گیری و داده‌ها",
        "تنظیمات و چاپگر"
    )

    Scaffold(
        topBar = {
            ErpTopAppBar(
                title = "تنظیمات سامانه و مدیریت دسترسی‌ها",
                subtitle = "پنل مدیر کل: مدیریت کاربران (RBAC)، مشخصات فروشگاه، پایگاه داده و امنیت"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.ManageAccounts
                                    1 -> Icons.Default.Business
                                    2 -> Icons.Default.Storage
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = title,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // User Management Tab
                        UserManagementSection(
                            securityManager = securityManager,
                            db = db,
                            loggedInUsername = loggedInUsername,
                            loggedInFullName = loggedInFullName,
                            currentRole = currentRole
                        )
                    }
                    1 -> {
                        // Store Profile Tab
                        StoreProfileSection(
                            storeProfile = storeProfile,
                            securityManager = securityManager
                        )
                    }
                    2 -> {
                        // Backup & Sync Tab
                        BackupAndSyncSection(
                            syncStatus = syncStatus,
                            lastSyncTime = lastSyncTime,
                            securityManager = securityManager
                        )
                    }
                    3 -> {
                        // General & Printers Tab
                        GeneralAndPrintersSection(
                            isDarkTheme = isDarkTheme,
                            onToggleDarkTheme = onToggleDarkTheme
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated User Management Panel for Admin
 */
@Composable
private fun UserManagementSection(
    securityManager: AppSecurityAndSettingsManager,
    db: AppDatabase,
    loggedInUsername: String,
    loggedInFullName: String,
    currentRole: UserRole
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val systemUsers by db.userDao().getAllUsers().collectAsStateWithLifecycle(emptyList())

    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToChangePassword by remember { mutableStateOf<UserEntity?>(null) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    // Current User Profile & Quick Password Change Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = PersianGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "کاربر وارد شده: $loggedInFullName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "نام کاربری: $loggedInUsername | سطح دسترسی: ${currentRole.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val activeUser = db.userDao().getUserByUsername(loggedInUsername)
                        if (activeUser != null) {
                            userToChangePassword = activeUser
                        } else {
                            Toast.makeText(context, "اطلاعات کاربر در دیتابیس یافت نشد.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("change_current_user_password_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تغییر رمز عبور حساب جاری ($loggedInUsername)", fontWeight = FontWeight.Bold)
            }
        }
    }

    // User Accounts Management List Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "کاربران سیستم و سطوح دسترسی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "تعریف فروشنده / صندوق‌دار و ویزیتور اختصاصی",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showAddUserDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_new_user_button"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("کاربر جدید", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (systemUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ کاربری یافت نشد. لطفاً از دکمه «کاربر جدید» استفاده کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    systemUsers.forEach { user ->
                        val roleObj = UserRole.fromString(user.role)
                        val isPrimaryAdmin = user.username.equals("admin", ignoreCase = true)
                        val roleColor = when (roleObj) {
                            UserRole.ADMIN -> PersianGold
                            UserRole.POS_USER -> MaterialTheme.colorScheme.primary
                            UserRole.VISITOR -> Color(0xFF7E57C2)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (user.isActive) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(roleColor.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (roleObj) {
                                                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                                    UserRole.POS_USER -> Icons.Default.PointOfSale
                                                    UserRole.VISITOR -> Icons.Default.Badge
                                                },
                                                contentDescription = null,
                                                tint = roleColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = user.fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(${user.username})",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = roleObj.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = roleColor
                                                )
                                                if (user.mobile.isNotBlank()) {
                                                    Text(
                                                        text = " • موبایل: ${user.mobile}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Status Badge (Active / Inactive)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (user.isActive) AccountingDebitGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (user.isActive) "فعال" else "غیرفعال",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.isActive) AccountingDebitGreen else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Toggle (Cannot deactivate primary admin)
                                    if (!isPrimaryAdmin) {
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    val newStatus = !user.isActive
                                                    db.userDao().updateUserStatus(user.id, newStatus)
                                                    val statusLabel = if (newStatus) "فعال" else "غیرفعال"
                                                    Toast.makeText(context, "وضعیت کاربر به «$statusLabel» تغییر یافت.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (user.isActive) MaterialTheme.colorScheme.error else AccountingDebitGreen
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (user.isActive) "غیرفعال‌سازی" else "فعال‌سازی",
                                                fontSize = 11.sp,
                                                color = if (user.isActive) MaterialTheme.colorScheme.error else AccountingDebitGreen
                                            )
                                        }
                                    }

                                    // Change Password Button
                                    TextButton(
                                        onClick = { userToChangePassword = user },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تغییر رمز", fontSize = 11.sp)
                                    }

                                    // Edit User Button (Only for non-primary admin or editing name/mobile)
                                    TextButton(
                                        onClick = { userToEdit = user },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ویرایش", fontSize = 11.sp)
                                    }

                                    // Delete User Button (Forbidden for primary admin)
                                    if (!isPrimaryAdmin) {
                                        IconButton(
                                            onClick = { userToDelete = user },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف کاربر",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
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

    // RBAC Security Matrix Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PersianGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ماتریس سطوح دسترسی سازمانی (RBAC):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "۱. مدیر کل (Admin):\n" +
                        "   • دسترسی کامل، نامحدود و بدون قفل به تمام ماژول‌ها، حسابداری، خزانه‌داری، گزارش سود، انبارداری و تنظیمات.\n" +
                        "   • تنها نقشی که مجاز به تعریف، ویرایش، تغییر رمز و غیرفعال‌سازی سایر کاربران است.\n\n" +
                        "۲. فروشنده / صندوق‌دار (POS User):\n" +
                        "   • دسترسی به صندوق فروشگاهی (POS)، صدور و ثبت فاکتورهای فروش و مشاهده موجودی کالاها.\n" +
                        "   • بدون دسترسی به حسابداری، دفاتر مالی، خزانه‌داری، هزینه‌ها، سود و تنظیمات سیستم.\n\n" +
                        "۳. ویزیتور و بازاریاب (Visitor):\n" +
                        "   • دسترسی به مشاهده کاتالوگ محصولات و قیمت‌های همکار/مصرف‌کننده.\n" +
                        "   • مجاز به ثبت پیش‌فاکتور (سفارش مشتری) بدون دسترسی به خزانه‌داری یا دخل صندوق.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }

    // Dialog: Add New User
    if (showAddUserDialog) {
        AddUserDialog(
            db = db,
            onDismiss = { showAddUserDialog = false },
            onUserCreated = {
                showAddUserDialog = false
                Toast.makeText(context, "کاربر جدید با موفقیت ایجاد شد.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Change Password
    userToChangePassword?.let { user ->
        ChangePasswordDialog(
            user = user,
            db = db,
            onDismiss = { userToChangePassword = null },
            onPasswordChanged = {
                userToChangePassword = null
                Toast.makeText(context, "رمز عبور با موفقیت به‌روزرسانی شد.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Edit User Details
    userToEdit?.let { user ->
        EditUserDialog(
            user = user,
            db = db,
            onDismiss = { userToEdit = null },
            onUserUpdated = {
                userToEdit = null
                Toast.makeText(context, "اطلاعات کاربر به‌روزرسانی شد.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Delete User Confirmation
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("تایید حذف کاربر", fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف دائم کاربر «${user.fullName} (${user.username})» اطمینان دارید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            db.userDao().deleteUser(user)
                            userToDelete = null
                            Toast.makeText(context, "کاربر با موفقیت حذف گردید.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف کاربر")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

/**
 * Dialog to add a new user (Seller, Visitor, Admin)
 */
@Composable
private fun AddUserDialog(
    db: AppDatabase,
    onDismiss: () -> Unit,
    onUserCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var mobile by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.POS_USER) }
    var isActive by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعریف کاربر جدید سیستم", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "اطلاعات هویتی و رمز عبور اختصاصی کاربر را وارد کنید:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("نام و نام خانوادگی") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("نام کاربری (حروف لاتین، مثلا seller1)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("رمز عبور اختصاصی") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "مخفی‌سازی رمز" else "نمایش رمز"
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("شماره موبایل (اختیاری)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "انتخاب نقش و سطح دسترسی:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedRole == UserRole.POS_USER,
                        onClick = { selectedRole = UserRole.POS_USER },
                        label = { Text("فروشنده / صندوق‌دار", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedRole == UserRole.VISITOR,
                        onClick = { selectedRole = UserRole.VISITOR },
                        label = { Text("ویزیتور", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("وضعیت حساب (فعال برای ورود به برنامه):", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = fullName.trim()
                    val trimmedUser = username.trim().lowercase()
                    val trimmedPass = password.trim()

                    if (trimmedName.isBlank() || trimmedUser.isBlank() || trimmedPass.isBlank()) {
                        errorMessage = "لطفاً نام، نام کاربری و رمز عبور را وارد کنید."
                        return@Button
                    }
                    if (trimmedPass.length < 3) {
                        errorMessage = "رمز عبور باید حداقل ۳ کاراکتر باشد."
                        return@Button
                    }

                    isSubmitting = true
                    scope.launch {
                        val existing = db.userDao().getUserByUsername(trimmedUser)
                        if (existing != null) {
                            errorMessage = "این نام کاربری قبلاً در سیستم ثبت شده است."
                            isSubmitting = false
                        } else {
                            val newUser = UserEntity(
                                username = trimmedUser,
                                passwordHash = trimmedPass,
                                fullName = trimmedName,
                                role = selectedRole.name,
                                mobile = mobile.trim(),
                                isActive = isActive
                            )
                            db.userDao().insertUser(newUser)
                            isSubmitting = false
                            onUserCreated()
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ثبت کاربر جدید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

/**
 * Dialog to change a user's password with eye visibility toggle
 */
@Composable
private fun ChangePasswordDialog(
    user: UserEntity,
    db: AppDatabase,
    onDismiss: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var newPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = PersianGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تغییر رمز عبور", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "کاربر: ${user.fullName} (${user.username})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("رمز عبور جدید") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "مخفی کردن رمز" else "نمایش رمز"
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = newPassword.trim()
                    if (p.length < 3) {
                        errorMessage = "رمز عبور جدید باید حداقل ۳ کاراکتر باشد."
                        return@Button
                    }
                    isSubmitting = true
                    scope.launch {
                        db.userDao().updatePassword(user.id, p)
                        isSubmitting = false
                        onPasswordChanged()
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ذخیره رمز جدید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

/**
 * Dialog to edit user details (full name, mobile, role)
 */
@Composable
private fun EditUserDialog(
    user: UserEntity,
    db: AppDatabase,
    onDismiss: () -> Unit,
    onUserUpdated: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf(user.fullName) }
    var mobile by remember { mutableStateOf(user.mobile) }
    var selectedRole by remember { mutableStateOf(UserRole.fromString(user.role)) }
    var isActive by remember { mutableStateOf(user.isActive) }
    var isSubmitting by remember { mutableStateOf(false) }
    val isPrimaryAdmin = user.username.equals("admin", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ویرایش اطلاعات کاربر", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "نام کاربری (غیرقابل تغییر): ${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("نام و نام خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("شماره همراه") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (!isPrimaryAdmin) {
                    Text(
                        text = "سطح دسترسی و نقش:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedRole == UserRole.POS_USER,
                            onClick = { selectedRole = UserRole.POS_USER },
                            label = { Text("فروشنده / صندوق‌دار", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.VISITOR,
                            onClick = { selectedRole = UserRole.VISITOR },
                            label = { Text("ویزیتور", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("وضعیت فعال‌بودن حساب:", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.trim().isBlank()) return@Button
                    isSubmitting = true
                    scope.launch {
                        val updated = user.copy(
                            fullName = fullName.trim(),
                            mobile = mobile.trim(),
                            role = if (isPrimaryAdmin) UserRole.ADMIN.name else selectedRole.name,
                            isActive = if (isPrimaryAdmin) true else isActive
                        )
                        db.userDao().updateUser(updated)
                        isSubmitting = false
                        onUserUpdated()
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ذخیره تغییرات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

/**
 * Store Profile Section
 */
@Composable
private fun StoreProfileSection(
    storeProfile: StoreProfile,
    securityManager: AppSecurityAndSettingsManager
) {
    val context = LocalContext.current
    var name by remember(storeProfile) { mutableStateOf(storeProfile.name) }
    var managerName by remember(storeProfile) { mutableStateOf(storeProfile.managerName) }
    var phone by remember(storeProfile) { mutableStateOf(storeProfile.phone) }
    var mobile by remember(storeProfile) { mutableStateOf(storeProfile.mobile) }
    var address by remember(storeProfile) { mutableStateOf(storeProfile.address) }
    var nationalCode by remember(storeProfile) { mutableStateOf(storeProfile.nationalCode) }
    var defaultBankCard by remember(storeProfile) { mutableStateOf(storeProfile.defaultBankCard) }
    var defaultSheba by remember(storeProfile) { mutableStateOf(storeProfile.defaultSheba) }
    var taxRate by remember(storeProfile) { mutableStateOf(storeProfile.taxRatePercent.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "مشخصات بازرگانی و سربرگ فاکتور", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام فروشگاه / شرکت بازرگانی") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = managerName,
                    onValueChange = { managerName = it },
                    label = { Text("نام مدیریت") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("تلفن ثابت") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("شماره همراه") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = nationalCode,
                    onValueChange = { nationalCode = it },
                    label = { Text("شناسه / کد ملی") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("آدرس دقیق فروشگاه و انبار") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = defaultBankCard,
                onValueChange = { defaultBankCard = it },
                label = { Text("شماره کارت واریزی پیش‌فرض (پاورقی فاکتور)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = defaultSheba,
                onValueChange = { defaultSheba = it },
                label = { Text("شماره شبا حساب بانکی") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = taxRate,
                onValueChange = { taxRate = it },
                label = { Text("نرخ مالیات بر ارزش افزوده (درصد)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val profile = storeProfile.copy(
                        name = name.ifBlank { "حسابداری و بازرگانی گلی" },
                        managerName = managerName,
                        phone = phone,
                        mobile = mobile,
                        address = address,
                        nationalCode = nationalCode,
                        defaultBankCard = defaultBankCard,
                        defaultSheba = defaultSheba,
                        taxRatePercent = taxRate.toIntOrNull() ?: 10
                    )
                    securityManager.updateProfile(profile)
                    Toast.makeText(context, "اطلاعات فروشگاه با موفقیت ذخیره شد.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_store_profile_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره تغییرات مشخصات فروشگاه")
            }
        }
    }
}

/**
 * Backup and Cloud Sync Section
 */
@Composable
private fun BackupAndSyncSection(
    syncStatus: CloudSyncStatus,
    lastSyncTime: String,
    securityManager: AppSecurityAndSettingsManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = DatabaseBackupHelper.restoreDatabaseFromFile(context, uri)
                if (result.isSuccess) {
                    Toast.makeText(context, "بازیابی اطلاعات از فایل پشتیبان با موفقیت انجام شد.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "خطا در بازیابی فایل: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Database Backup & Restore Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = PersianGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "پشتیبان‌گیری و بازیابی پایگاه داده (Backup & Restore)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ایجاد فایل پشتیبان از کلیه حساب‌ها، فاکتورها، انبار، کاربران و اسناد حسابداری بر روی حافظه دستگاه.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            val result = DatabaseBackupHelper.createDatabaseBackup(context)
                            if (result.isSuccess) {
                                val backupFile = result.getOrNull()!!
                                Toast.makeText(context, "فایل پشتیبان ساخته شد: ${backupFile.name}", Toast.LENGTH_SHORT).show()
                                DatabaseBackupHelper.shareBackupFile(context, backupFile)
                            } else {
                                Toast.makeText(context, "خطا در ایجاد پشتیبان: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("create_backup_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تهیه پشتیبان (Backup)")
                }

                OutlinedButton(
                    onClick = { restoreLauncher.launch("*/*") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("restore_backup_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("بازیابی اطلاعات (Restore)")
                }
            }
        }
    }

    // Cloud Sync Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = AccountingDebitGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "پایگاه داده و همگام‌سازی ابری",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "وضعیت: ${syncStatus.title}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (syncStatus == CloudSyncStatus.ONLINE_SYNCED) AccountingDebitGreen else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "آخرین همگام‌سازی: $lastSyncTime",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    isSyncing = true
                    securityManager.setSyncStatus(CloudSyncStatus.SYNCING)
                    scope.launch {
                        delay(1000)
                        val now = JalaliDate.now().format(includeTime = true)
                        securityManager.setSyncStatus(CloudSyncStatus.ONLINE_SYNCED, now)
                        isSyncing = false
                        Toast.makeText(context, "همگام‌سازی با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_cloud_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSyncing) "در حال همگام‌سازی..." else "همگام‌سازی با فضای ابری")
            }
        }
    }
}

/**
 * General Settings and Bluetooth Printers Section
 */
@Composable
private fun GeneralAndPrintersSection(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // Dark Mode Switch
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "حالت تیره (Dark Mode)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = "طراحی تیره و مناسب نور کم", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = isDarkTheme,
                onCheckedChange = onToggleDarkTheme,
                modifier = Modifier.testTag("toggle_dark_theme_switch")
            )
        }
    }

    // Bluetooth Thermal Printers
    val pairedPrinters = remember { BluetoothThermalPrinter.getPairedPrinters(context) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = AccountingDebitGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "پرینترهای حرارتی بلوتوثی (ESC/POS)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "دستگاه‌های جفت‌شده با گوشی جهت چاپ سریع فاکتورهای حرارتی ۵۸ و ۸۰ میلی‌متری:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            if (pairedPrinters.isEmpty()) {
                Text(
                    text = "هیچ پرینتر بلوتوثی جفت‌شده‌ای یافت نشد. می‌توانید از بخش تنظیمات بلوتوث اندروید اقدام به Pair نمایید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            } else {
                pairedPrinters.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🖨 ${p.name}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(p.address, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
