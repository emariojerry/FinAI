package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddAssetDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AddLiabilityDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.StatementUploadDialog
import com.example.ui.screens.AssetsLiabilitiesScreen
import com.example.ui.screens.AuditScreen
import com.example.ui.screens.AuditorChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.TaxFilingScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    DASHBOARD("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    AUDIT("Audit", Icons.Filled.Insights, Icons.Outlined.Insights),
    TRANSACTIONS("Activity", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    TAX("Tax", Icons.Filled.Calculate, Icons.Outlined.Calculate),
    NET_WORTH("Net Worth", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    SAVINGS("Goals", Icons.Filled.Savings, Icons.Outlined.Savings),
    AI_ADVISOR("Auditor AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
}

@Composable
fun FinAuditApp(
    viewModel: FinanceViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Dialog Visibility States
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showStatementUploadDialog by remember { mutableStateOf(false) }
    var showAddAssetDialog by remember { mutableStateOf(false) }
    var showAddLiabilityDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // State Collection
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val liabilities by viewModel.liabilities.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val audit by viewModel.audit.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAuditorThinking by viewModel.isAuditorThinking.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val taxAssessment by viewModel.taxAssessment.collectAsState()
    val taxInputState by viewModel.taxInputState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                ScreenTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) PrimaryNavy else SlateMedium
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) PrimaryNavy else SlateMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFF1F5F9),
                            selectedIconColor = PrimaryNavy,
                            unselectedIconColor = SlateMedium,
                            selectedTextColor = PrimaryNavy,
                            unselectedTextColor = SlateMedium
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.DASHBOARD -> DashboardScreen(
                    user = userProfile,
                    audit = audit,
                    transactions = transactions,
                    alerts = alerts,
                    onNavigateToAudit = { currentTab = ScreenTab.AUDIT },
                    onNavigateToTransactions = { currentTab = ScreenTab.TRANSACTIONS },
                    onNavigateToTax = { currentTab = ScreenTab.TAX },
                    onOpenAddTransaction = { showAddTransactionDialog = true },
                    onOpenStatementUpload = { showStatementUploadDialog = true }
                )

                ScreenTab.AUDIT -> AuditScreen(
                    audit = audit,
                    onNavigateToChat = { currentTab = ScreenTab.AI_ADVISOR }
                )

                ScreenTab.TRANSACTIONS -> TransactionsScreen(
                    transactions = transactions,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedCategory = selectedCategoryFilter,
                    onSelectCategory = { viewModel.setCategoryFilter(it) },
                    selectedType = selectedTypeFilter,
                    onSelectType = { viewModel.setTypeFilter(it) },
                    onReclassifyCategory = { txId, merchant, cat ->
                        viewModel.reclassifyTransaction(txId, merchant, cat)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Updated category and saved rule for \"$merchant\"")
                        }
                    },
                    onOpenAddTransaction = { showAddTransactionDialog = true },
                    onOpenStatementUpload = { showStatementUploadDialog = true }
                )

                ScreenTab.TAX -> TaxFilingScreen(
                    assessment = taxAssessment,
                    inputState = taxInputState,
                    onUpdateInput = { viewModel.updateTaxInput(it) },
                    onSyncWithStatement = {
                        viewModel.syncTaxWithDetectedIncome()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Synced annual gross income with detected salary inflows!")
                        }
                    },
                    onMarkFilingStatus = { status ->
                        viewModel.setTaxFilingStatus(status)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Tax filing status marked: ${status.label}")
                        }
                    },
                    onGenerateReport = { viewModel.generateFilingReport() }
                )

                ScreenTab.NET_WORTH -> AssetsLiabilitiesScreen(
                    user = userProfile,
                    assets = assets,
                    liabilities = liabilities,
                    onAddAsset = { showAddAssetDialog = true },
                    onConfirmPotentialAsset = { assetId ->
                        viewModel.confirmPotentialAsset(assetId)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Asset confirmed and added to Net Worth calculation!")
                        }
                    },
                    onDeleteAsset = { viewModel.deleteAsset(it) },
                    onAddLiability = { showAddLiabilityDialog = true },
                    onDeleteLiability = { viewModel.deleteLiability(it) }
                )

                ScreenTab.SAVINGS -> SavingsGoalsScreen(
                    goals = savingsGoals,
                    audit = audit,
                    onAddGoal = { showAddGoalDialog = true },
                    onAddProgress = { goalId, amount ->
                        viewModel.updateGoalProgress(goalId, amount)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("₦50,000 progress recorded!")
                        }
                    }
                )

                ScreenTab.AI_ADVISOR -> AuditorChatScreen(
                    messages = chatMessages,
                    isThinking = isAuditorThinking,
                    onSendMessage = { viewModel.sendMessageToAuditor(it) }
                )
            }
        }

        // Dialogs
        if (showAddTransactionDialog) {
            AddTransactionDialog(
                onDismiss = { showAddTransactionDialog = false },
                onConfirm = { merchant, desc, amount, type, cat, account, isRecurring ->
                    viewModel.addManualTransaction(merchant, desc, amount, type, cat, account, isRecurring)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaction recorded successfully!")
                    }
                }
            )
        }

        if (showStatementUploadDialog) {
            StatementUploadDialog(
                onDismiss = { showStatementUploadDialog = false },
                onExtractAndImport = { content, isCsv, fileName ->
                    val res = viewModel.importStatement(content, isCsv, fileName)
                    coroutineScope.launch {
                        if (res.transactions.isNotEmpty()) {
                            snackbarHostState.showSnackbar("Extracted ${res.transactions.size} transactions from ${res.bankName}!")
                        } else if (res.errorMessage != null) {
                            snackbarHostState.showSnackbar("Upload note: ${res.errorMessage}")
                        }
                    }
                    res
                }
            )
        }

        if (showAddAssetDialog) {
            AddAssetDialog(
                onDismiss = { showAddAssetDialog = false },
                onConfirm = { name, type, value, institution, isPotential ->
                    viewModel.addAsset(name, type, value, institution, isPotential)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Asset \"$name\" saved!")
                    }
                }
            )
        }

        if (showAddLiabilityDialog) {
            AddLiabilityDialog(
                onDismiss = { showAddLiabilityDialog = false },
                onConfirm = { name, type, outstanding, monthly, interest, lender ->
                    viewModel.addLiability(name, type, outstanding, monthly, interest, lender)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Liability \"$name\" recorded!")
                    }
                }
            )
        }

        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { title, cat, target, current, months ->
                    viewModel.addSavingsGoal(title, cat, target, current, months)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Goal \"$title\" created!")
                    }
                }
            )
        }
    }
}
