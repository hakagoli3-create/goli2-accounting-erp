package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.database.AppDatabase
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.CloudSyncStatus
import com.example.data.model.UserRole
import com.example.ui.screens.AccountingScreen
import com.example.ui.screens.CrmScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.IncomeScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoiceScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TreasuryScreen
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.GoliAccountingTheme
import com.example.ui.theme.MatteGold
import com.example.ui.theme.MatteGoldContainer
import com.example.ui.theme.NavyBrand
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PersianGold
import com.example.ui.theme.StatusInfoBlue
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.ui.viewmodel.PartyViewModel
import com.example.ui.viewmodel.TreasuryViewModel
import kotlinx.coroutines.launch

/**
 * دسته‌بندی‌های موضوعی بخش‌های سامانه جامع حسابداری گلی
 */
enum class ScreenCategory(val titleFa: String, val icon: ImageVector) {
    OPERATIONS("عملیات و فروشگاه", Icons.Default.Storefront),
    INVENTORY("انبار و کالا", Icons.Default.Inventory2),
    FINANCE("مالی و خزانه‌داری", Icons.Default.AccountBalance),
    MANAGEMENT("مدیریت و سیستم", Icons.Default.Settings)
}

/**
 * معرفی بخش‌های ده‌گانه نرم‌افزار به همراه عنوان، آیکون یکپارچه، زیرعنوان و دسته‌بندی
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val subtitle: String = "",
    val category: ScreenCategory = ScreenCategory.OPERATIONS,
    val accentColor: Color = NavyPrimary
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "داشبورد",
        icon = Icons.Default.Dashboard,
        subtitle = "شاخص‌های کلیدی، خلاصه مالی و فروش",
        category = ScreenCategory.OPERATIONS,
        accentColor = NavyPrimary
    )

    data object Pos : Screen(
        route = "pos",
        title = "صندوق POS",
        icon = Icons.Default.PointOfSale,
        subtitle = "فروش سریع فروشگاهی، بارکد و تسویه",
        category = ScreenCategory.OPERATIONS,
        accentColor = PersianGold
    )

    data object Invoices : Screen(
        route = "invoices",
        title = "فاکتورها",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        subtitle = "صدور، چاپ حرارتی، A4 و تسویه فاکتور",
        category = ScreenCategory.OPERATIONS,
        accentColor = StatusInfoBlue
    )

    data object Inventory : Screen(
        route = "inventory",
        title = "انبار و کالا",
        icon = Icons.Default.Inventory2,
        subtitle = "موجودی انبارها، حواله انتقال و کاردکس",
        category = ScreenCategory.INVENTORY,
        accentColor = Color(0xFF00796B)
    )

    data object Treasury : Screen(
        route = "treasury",
        title = "خزانه‌داری",
        icon = Icons.Default.AccountBalance,
        subtitle = "حساب‌های بانکی، دخل صندوق و چک‌های صیادی",
        category = ScreenCategory.FINANCE,
        accentColor = Color(0xFF673AB7)
    )

    data object Accounting : Screen(
        route = "accounting",
        title = "حسابداری",
        icon = Icons.AutoMirrored.Filled.Assignment,
        subtitle = "ثبت اسناد دوبل، دفاتر کل و معین و تراز آزمایشی",
        category = ScreenCategory.FINANCE,
        accentColor = Color(0xFF1976D2)
    )

    data object Incomes : Screen(
        route = "incomes",
        title = "درآمدها",
        icon = Icons.Default.TrendingUp,
        subtitle = "ثبت و پیگیری درآمدهای متفرقه و سود بانکی",
        category = ScreenCategory.FINANCE,
        accentColor = AccountingDebitGreen
    )

    data object Expenses : Screen(
        route = "expenses",
        title = "هزینه‌ها",
        icon = Icons.Default.TrendingDown,
        subtitle = "سرفصل‌ها و مدیریت هزینه‌های جاری شرکت",
        category = ScreenCategory.FINANCE,
        accentColor = AccountingCreditRed
    )

    data object Crm : Screen(
        route = "crm",
        title = "مشتریان",
        icon = Icons.Default.People,
        subtitle = "طرف‌های حساب، مشتریان، تامین‌کنندگان و سقف اعتبار",
        category = ScreenCategory.MANAGEMENT,
        accentColor = Color(0xFFE65100)
    )

    data object Settings : Screen(
        route = "settings",
        title = "تنظیمات",
        icon = Icons.Default.Settings,
        subtitle = "پروفایل فروشگاه، چندارزی، کاربران و نسخه پشتیبان",
        category = ScreenCategory.MANAGEMENT,
        accentColor = Color(0xFF455A64)
    )

    companion object {
        val ALL_SCREENS: List<Screen> = listOf(
            Dashboard,
            Pos,
            Invoices,
            Inventory,
            Treasury,
            Accounting,
            Incomes,
            Expenses,
            Crm,
            Settings
        )

        fun findByRoute(route: String?): Screen {
            return ALL_SCREENS.firstOrNull { it.route.equals(route, ignoreCase = true) } ?: Dashboard
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val securityManager = AppSecurityAndSettingsManager.getInstance(this)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            GoliAccountingTheme(darkTheme = isDarkTheme) {
                MainAppContent(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDarkTheme = it },
                    securityManager = securityManager
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    securityManager: AppSecurityAndSettingsManager
) {
    val context = LocalContext.current
    val factory = remember { AppViewModelFactory(context) }
    val db = remember { AppDatabase.getInstance(context) }

    val isLoggedIn by securityManager.isLoggedIn.collectAsStateWithLifecycle()

    // نمایش صفحه ورود در صورت احراز هویت نشدن
    if (!isLoggedIn) {
        LoginScreen(
            securityManager = securityManager,
            db = db,
            onLoginSuccess = {}
        )
        return
    }

    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()
    val loggedInUsername by securityManager.loggedInUsername.collectAsStateWithLifecycle()
    val loggedInFullName by securityManager.loggedInFullName.collectAsStateWithLifecycle()
    val syncStatus by securityManager.syncStatus.collectAsStateWithLifecycle()
    val storeProfile by securityManager.storeProfile.collectAsStateWithLifecycle()

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val accountingViewModel: AccountingViewModel = viewModel(factory = factory)
    val inventoryViewModel: InventoryViewModel = viewModel(factory = factory)
    val invoiceViewModel: InvoiceViewModel = viewModel(factory = factory)
    val treasuryViewModel: TreasuryViewModel = viewModel(factory = factory)
    val partyViewModel: PartyViewModel = viewModel(factory = factory)
    val expenseAndIncomeViewModel: com.example.ui.viewmodel.ExpenseAndIncomeViewModel = viewModel(factory = factory)

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val currentScreen = remember(currentRoute) { Screen.findByRoute(currentRoute) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showQuickModulesSheet by remember { mutableStateOf(false) }

    // فیلتر کردن صفحات بر اساس نقش کاربر (RBAC)
    val allowedDrawerScreens = remember(currentRole) {
        when (currentRole) {
            UserRole.ADMIN -> Screen.ALL_SCREENS
            UserRole.POS_USER -> listOf(
                Screen.Dashboard,
                Screen.Pos,
                Screen.Invoices,
                Screen.Inventory
            )
            UserRole.VISITOR -> listOf(
                Screen.Dashboard,
                Screen.Invoices,
                Screen.Inventory
            )
        }
    }

    // ۴ آیتم پرکاربرد برای نوار پایین موبایل بر اساس نقش
    val primaryBottomScreens = remember(currentRole) {
        when (currentRole) {
            UserRole.ADMIN -> listOf(
                Screen.Dashboard,
                Screen.Pos,
                Screen.Invoices,
                Screen.Treasury
            )
            UserRole.POS_USER -> listOf(
                Screen.Dashboard,
                Screen.Pos,
                Screen.Invoices,
                Screen.Inventory
            )
            UserRole.VISITOR -> listOf(
                Screen.Dashboard,
                Screen.Invoices,
                Screen.Inventory
            )
        }
    }

    fun navigateToScreen(targetRoute: String) {
        if (currentRoute == targetRoute) return
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerTonalElevation = 6.dp,
                    modifier = Modifier.width(320.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // ==========================================
                        // هدر فاخر کشویی با هویت سازمانی گلی
                        // ==========================================
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(NavyDark, NavyPrimary, Color(0xFF1E2538))
                                        )
                                    )
                                    .padding(horizontal = 20.dp, vertical = 22.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .border(BorderStroke(2.dp, PersianGold), CircleShape)
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.app_logo),
                                                contentDescription = "لوگوی برنامه",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = storeProfile.name.ifBlank { "مدیریت حسابداری گلی" },
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = Color.White
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "سامانه یکپارچه مالی و بازرگانی",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    color = PersianGold
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // کارت مشخصات کاربر و سطح دسترسی
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(MatteGoldContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = NavyPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = loggedInFullName.ifBlank { "کاربر سیستم" },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 13.sp
                                                    )
                                                )
                                                Text(
                                                    text = "@$loggedInUsername • ${currentRole.title}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                    )
                                                )
                                            }

                                            // نشان آنلاین / آفلاین
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (syncStatus == CloudSyncStatus.ONLINE_SYNCED) AccountingDebitGreen else PersianGold,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // دکمه خروج
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            securityManager.logout()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("logout_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, AccountingCreditRed.copy(alpha = 0.6f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFFFB4AB)
                                        ),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = AccountingCreditRed
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "خروج امن از حساب کاربری",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // لیست بخش‌های ده‌گانه بر اساس دسته‌بندی
                        // ==========================================
                        ScreenCategory.entries.forEach { category ->
                            val categoryScreens = allowedDrawerScreens.filter { it.category == category }
                            if (categoryScreens.isNotEmpty()) {
                                item {
                                    DrawerCategoryHeader(category = category)
                                }

                                items(categoryScreens) { screen ->
                                    val isSelected = currentRoute == screen.route
                                    NavigationDrawerItem(
                                        label = {
                                            Column {
                                                Text(
                                                    text = screen.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 14.sp
                                                    )
                                                )
                                                Text(
                                                    text = screen.subtitle,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 10.sp,
                                                        color = if (isSelected) PersianGold else TextDarkSecondary
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        },
                                        icon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        if (isSelected) PersianGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.title,
                                                    tint = if (isSelected) PersianGold else screen.accentColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        badge = {
                                            if (isSelected) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = PersianGold
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = NavyPrimary,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(
                                                            text = "فعال",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = NavyPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navigateToScreen(screen.route)
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = NavyPrimary,
                                            unselectedContainerColor = Color.Transparent,
                                            selectedTextColor = Color.White,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            selectedIconColor = PersianGold,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 2.dp)
                                            .testTag("drawer_item_${screen.route}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    val isSecondaryActive = currentRoute !in primaryBottomScreens.map { it.route }

                    Surface(
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, CleanBorder),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            // ۴ دکمه اصلی ناوبری
                            primaryBottomScreens.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = { navigateToScreen(screen.route) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = NavyPrimary,
                                        indicatorColor = NavyPrimary,
                                        unselectedIconColor = TextDarkSecondary,
                                        unselectedTextColor = TextDarkSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_${screen.route}")
                                )
                            }

                            // دکمه پنجم: دسترسی سریع به تمام ۱۰ بخش سیستم
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (isSecondaryActive) currentScreen.icon else Icons.Default.GridView,
                                        contentDescription = "تمام بخش‌ها"
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (isSecondaryActive) currentScreen.title else "بخش‌ها",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSecondaryActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selected = isSecondaryActive,
                                onClick = {
                                    showQuickModulesSheet = true
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = PersianGold,
                                    indicatorColor = PersianGold,
                                    unselectedIconColor = TextDarkSecondary,
                                    unselectedTextColor = TextDarkSecondary
                                ),
                                modifier = Modifier.testTag("nav_item_all_modules")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigateToRoute = { route -> navigateToScreen(route) }
                            )
                        }

                        composable(Screen.Pos.route) {
                            if (currentRole.canAccessPos) {
                                PosScreen(viewModel = invoiceViewModel)
                            } else {
                                AccessDeniedCard(
                                    role = currentRole,
                                    onBackToDashboard = { navigateToScreen(Screen.Dashboard.route) }
                                )
                            }
                        }

                        composable(Screen.Invoices.route) {
                            InvoiceScreen(viewModel = invoiceViewModel)
                        }

                        composable(Screen.Inventory.route) {
                            InventoryScreen(viewModel = inventoryViewModel)
                        }

                        composable(Screen.Treasury.route) {
                            if (currentRole.canAccessTreasury) {
                                TreasuryScreen(viewModel = treasuryViewModel)
                            } else {
                                AccessDeniedCard(
                                    role = currentRole,
                                    onBackToDashboard = { navigateToScreen(Screen.Dashboard.route) }
                                )
                            }
                        }

                        composable(Screen.Accounting.route) {
                            if (currentRole.canAccessAccounting) {
                                AccountingScreen(viewModel = accountingViewModel)
                            } else {
                                AccessDeniedCard(
                                    role = currentRole,
                                    onBackToDashboard = { navigateToScreen(Screen.Dashboard.route) }
                                )
                            }
                        }

                        composable(Screen.Incomes.route) {
                            if (currentRole.canAccessExpensesAndIncomes) {
                                IncomeScreen(viewModel = expenseAndIncomeViewModel)
                            } else {
                                AccessDeniedCard(
                                    role = currentRole,
                                    onBackToDashboard = { navigateToScreen(Screen.Dashboard.route) }
                                )
                            }
                        }

                        composable(Screen.Expenses.route) {
                            if (currentRole.canAccessExpensesAndIncomes) {
                                ExpenseScreen(viewModel = expenseAndIncomeViewModel)
                            } else {
                                AccessDeniedCard(
                                    role = currentRole,
                                    onBackToDashboard = { navigateToScreen(Screen.Dashboard.route) }
                                )
                            }
                        }

                        composable(Screen.Crm.route) {
                            if (currentRole == UserRole.ADMIN) {
                                CrmScreen(viewModel = partyViewModel)
                            } else {
                                AccessDeniedCard(
                                    role = currentRole,
                                    onBackToDashboard = { navigateToScreen(Screen.Dashboard.route) }
                                )
                            }
                        }

                        composable(Screen.Settings.route) {
                            if (currentRole.canAccessSettings) {
                                SettingsScreen(
                                    securityManager = securityManager,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = onToggleDarkTheme
                                )
                            } else {
                                AccessDeniedCard(
                                    role = currentRole,
                                    onBackToDashboard = { navigateToScreen(Screen.Dashboard.route) }
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // پنجره شیت دسترسی سریع به تمام ۱۰ بخش سیستم
            // ==========================================
            if (showQuickModulesSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showQuickModulesSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
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
                                        imageVector = Icons.Default.Widgets,
                                        contentDescription = null,
                                        tint = PersianGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "تمام بخش‌های نرم‌افزار گلی",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = NavyPrimary
                                    )
                                    Text(
                                        text = "دسترسی سریع به ۱۰ ماژول تخصصی سامانه",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = TextDarkSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    showQuickModulesSheet = false
                                    scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "منوی کشویی کامل",
                                    tint = NavyPrimary
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = CleanBorder
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                        ) {
                            items(Screen.ALL_SCREENS) { screen ->
                                val isAllowed = screen in allowedDrawerScreens
                                val isCurrent = currentRoute == screen.route

                                QuickModuleCard(
                                    screen = screen,
                                    isAllowed = isAllowed,
                                    isCurrent = isCurrent,
                                    onClick = {
                                        if (isAllowed) {
                                            showQuickModulesSheet = false
                                            navigateToScreen(screen.route)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // دکمه باز کردن منوی کشویی و تنظیمات کاربر
                        Button(
                            onClick = {
                                showQuickModulesSheet = false
                                scope.launch { drawerState.open() }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = null, tint = PersianGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مشاهده منوی کشویی، اطلاعات حساب و خروج",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerCategoryHeader(category: ScreenCategory) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = PersianGold,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.titleFa,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = TextDarkSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = CleanBorder)
    }
}

@Composable
private fun QuickModuleCard(
    screen: Screen,
    isAllowed: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isCurrent) PersianGold else CleanBorder
    val borderWidth = if (isCurrent) 2.dp else 1.dp
    val containerColor = if (isCurrent) MatteGoldContainer else if (isAllowed) CleanSurface else CleanBackground.copy(alpha = 0.5f)

    Card(
        onClick = onClick,
        enabled = isAllowed,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isAllowed) screen.accentColor.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (!isAllowed) Icons.Default.Lock else screen.icon,
                    contentDescription = null,
                    tint = if (!isAllowed) Color.Gray else screen.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = if (isAllowed) NavyPrimary else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PersianGold, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (!isAllowed) "دسترسی محدود" else screen.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = if (isAllowed) TextDarkSecondary else Color.Gray
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AccessDeniedCard(
    role: UserRole,
    onBackToDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, CleanBorder),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(AccountingCreditRed.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AccountingCreditRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "عدم دسترسی به این بخش (محدودیت سازمانی)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "نقش فعال شما «${role.title}» است و دسترسی به این بخش به دلیل محدودیت‌های امنیتی سازمانی مسدود می‌باشد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onBackToDashboard,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بازگشت به داشبورد اصلی", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
