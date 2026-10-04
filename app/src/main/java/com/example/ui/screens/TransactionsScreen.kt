package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.ui.components.ReclassifyCategoryDialog
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium

@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: TransactionCategory?,
    onSelectCategory: (TransactionCategory?) -> Unit,
    selectedType: TransactionType?,
    onSelectType: (TransactionType?) -> Unit,
    onReclassifyCategory: (transactionId: Long, merchant: String, newCategory: TransactionCategory) -> Unit,
    onOpenAddTransaction: () -> Unit,
    onOpenStatementUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reclassifyTarget by remember { mutableStateOf<Transaction?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedCategory, selectedType) {
        transactions.filter { tx ->
            val matchQuery = searchQuery.isBlank() ||
                tx.merchant.contains(searchQuery, ignoreCase = true) ||
                tx.description.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == null || tx.category == selectedCategory
            val matchType = selectedType == null || tx.type == selectedType
            matchQuery && matchCat && matchType
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Transactions",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateDark
                        )
                        Text(
                            text = "${filteredTransactions.size} transactions categorized",
                            fontSize = 12.sp,
                            color = SlateMedium
                        )
                    }

                    Row {
                        IconButton(onClick = onOpenStatementUpload) {
                            Icon(Icons.Default.UploadFile, contentDescription = "Upload Statement (PDF/CSV)", tint = PrimaryNavy)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search merchant or description...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateLight) },
                    modifier = Modifier.fillMaxWidth().testTag("tx_search_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Category Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onSelectCategory(null) },
                            color = if (selectedCategory == null) PrimaryNavy else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "All Categories",
                                fontSize = 11.sp,
                                fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedCategory == null) Color.White else SlateDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    items(TransactionCategory.entries) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onSelectCategory(if (isSelected) null else cat) },
                            color = if (isSelected) PrimaryNavy else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else SlateDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Type Filter Chips (All, Debit, Credit)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectType(null) },
                        color = if (selectedType == null) SlateDark else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                    ) {
                        Text(
                            text = "All Types",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == null) Color.White else SlateMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectType(TransactionType.DEBIT) },
                        color = if (selectedType == TransactionType.DEBIT) SlateDark else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                    ) {
                        Text(
                            text = "Expenses (Debits)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.DEBIT) Color.White else SlateMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectType(TransactionType.CREDIT) },
                        color = if (selectedType == TransactionType.CREDIT) EmeraldGreen else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                    ) {
                        Text(
                            text = "Inflows (Credits)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.CREDIT) Color.White else SlateMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Transactions list
            items(filteredTransactions) { tx ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        .clickable { reclassifyTarget = tx }
                        .testTag("tx_item_${tx.id}"),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (tx.isDebit) Color(0xFFF1F5F9) else EmeraldGreenSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tx.merchant.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.isDebit) SlateDark else EmeraldGreen,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tx.merchant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SlateDark
                                    )
                                    Text(
                                        text = tx.description,
                                        fontSize = 11.sp,
                                        color = SlateMedium,
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = tx.formattedAmount(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.isDebit) SlateDark else EmeraldGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable { reclassifyTarget = tx }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tx.category.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SlateDark
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(Icons.Default.Edit, contentDescription = "Change Category", modifier = Modifier.size(10.dp), tint = SlateMedium)
                                    }
                                }

                                if (tx.isRecurring) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE0E7FF)
                                    ) {
                                        Text(
                                            text = "Recurring",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3730A3),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${tx.date} • ${tx.accountName.take(16)}",
                                fontSize = 10.sp,
                                color = SlateLight
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // FAB to Add Transaction
        FloatingActionButton(
            onClick = onOpenAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_transaction_fab"),
            containerColor = PrimaryNavy,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }

        // Reclassify Dialog
        reclassifyTarget?.let { tx ->
            ReclassifyCategoryDialog(
                transaction = tx,
                onDismiss = { reclassifyTarget = null },
                onSaveCategory = { newCat ->
                    onReclassifyCategory(tx.id, tx.merchant, newCat)
                }
            )
        }
    }
}
