package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.GoliAccountingTheme
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.ui.viewmodel.PartyViewModel
import com.example.ui.viewmodel.TreasuryViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.Dashboard)
    data object Pos : Screen("pos", "صندوق POS", Icons.Default.PointOfSale)
    data object Invoices : Screen("invoices", "فاکتورها", Icons.AutoMirrored.Filled.ReceiptLong)
    data object Expenses : Screen("expenses", "هزینه‌ها", Icons.Default.TrendingDown)
    data object Incomes : Screen("incomes", "درآمدها", Icons.Default.TrendingUp)
    data object Accounting : Screen("accounting", "حسابداری", Icons.AutoMirrored.Filled.Assignment)
    data object Treasury : Screen("treasury", "خزانه‌داری", Icons.Default.AccountBalance)
    data object Inventory : Screen("inventory", "انبار و کالا", Icons.Default.Inventory2)
    data object Crm : Screen("crm", "مشتریان", Icons.Default.People)
    data object Settings : Screen("settings", "تنظیمات", Icons.Default.Settings)
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

    // LoginScreen as start destination when not authenticated
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

    // RBAC: Dynamic screens based on user role
    // 1) Admin: Full access to all screens, settings and CRUD
    // 2) POS User (Seller): Limited to issuing invoices, viewing products, and cash desk POS
    // 3) Visitor: Limited to catalog and pre-invoices/orders
    val allowedDrawerScreens = when (currentRole) {
        UserRole.ADMIN -> listOf(
            Screen.Dashboard,
            Screen.Pos,
            Screen.Invoices,
            Screen.Expenses,
            Screen.Incomes,
            Screen.Accounting,
            Screen.Inventory,
            Screen.Treasury,
            Screen.Crm,
            Screen.Settings
        )
        UserRole.POS_USER -> listOf(
            Screen.Dashboard,
            Screen.Pos,
            Screen.Invoices,
            Screen.Inventory,
            Screen.Crm
        )
        UserRole.VISITOR -> listOf(
            Screen.Dashboard,
            Screen.Invoices,
            Screen.Inventory,
            Screen.Crm
        )
    }

    val bottomNavItems = when (currentRole) {
        UserRole.ADMIN -> listOf(
            Screen.Dashboard,
            Screen.Pos,
            Screen.Invoices,
            Screen.Expenses,
            Screen.Treasury
        )
        UserRole.POS_USER -> listOf(
            Screen.Dashboard,
            Screen.Pos,
            Screen.Invoices,
            Screen.Inventory,
            Screen.Crm
        )
        UserRole.VISITOR -> listOf(
            Screen.Dashboard,
            Screen.Invoices,
            Screen.Inventory,
            Screen.Crm
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = storeProfile.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "کاربر جاری: $loggedInFullName ($loggedInUsername)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // RBAC Active Role Indicator with Role-Specific Badge
                    val roleBadgeColor = when (currentRole) {
                        UserRole.ADMIN -> PersianGold
                        UserRole.POS_USER -> MaterialTheme.colorScheme.primary
                        UserRole.VISITOR -> Color(0xFF7E57C2)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (currentRole) {
                                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                    UserRole.POS_USER -> Icons.Default.PointOfSale
                                    UserRole.VISITOR -> Icons.Default.Badge
                                },
                                contentDescription = null,
                                tint = roleBadgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "سطح دسترسی: ${currentRole.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    // Cloud Sync Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (syncStatus == CloudSyncStatus.ONLINE_SYNCED) AccountingDebitGreen else PersianGold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = syncStatus.title,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch { drawerState.close() }
                            securityManager.logout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logout_button"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خروج از حساب (Logout)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                allowedDrawerScreens.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_${screen.route}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
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
                            onNavigateToRoute = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(Screen.Pos.route) {
                        if (currentRole.canAccessPos) {
                            PosScreen(viewModel = invoiceViewModel)
                        } else {
                            AccessDeniedCard(
                                role = currentRole,
                                onBackToDashboard = { navController.navigate(Screen.Dashboard.route) }
                            )
                        }
                    }
                    composable(Screen.Invoices.route) {
                        InvoiceScreen(viewModel = invoiceViewModel)
                    }
                    composable(Screen.Expenses.route) {
                        if (currentRole.canAccessExpensesAndIncomes) {
                            ExpenseScreen(viewModel = expenseAndIncomeViewModel)
                        } else {
                            AccessDeniedCard(
                                role = currentRole,
                                onBackToDashboard = { navController.navigate(Screen.Dashboard.route) }
                            )
                        }
                    }
                    composable(Screen.Incomes.route) {
                        if (currentRole.canAccessExpensesAndIncomes) {
                            IncomeScreen(viewModel = expenseAndIncomeViewModel)
                        } else {
                            AccessDeniedCard(
                                role = currentRole,
                                onBackToDashboard = { navController.navigate(Screen.Dashboard.route) }
                            )
                        }
                    }
                    composable(Screen.Accounting.route) {
                        if (currentRole.canAccessAccounting) {
                            AccountingScreen(viewModel = accountingViewModel)
                        } else {
                            AccessDeniedCard(
                                role = currentRole,
                                onBackToDashboard = { navController.navigate(Screen.Dashboard.route) }
                            )
                        }
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
                                onBackToDashboard = { navController.navigate(Screen.Dashboard.route) }
                            )
                        }
                    }
                    composable(Screen.Crm.route) {
                        CrmScreen(viewModel = partyViewModel)
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
                                onBackToDashboard = { navController.navigate(Screen.Dashboard.route) }
                            )
                        }
                    }
                }
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
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "عدم دسترسی به این بخش (محدودیت RBAC)",
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
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بازگشت به داشبورد")
                }
            }
        }
    }
}
