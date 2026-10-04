package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import com.example.data.model.AssetType
import com.example.data.model.GoalCategory
import com.example.data.model.LiabilityType
import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.repository.CsvParser
import com.example.data.repository.PdfStatementParser
import com.example.data.repository.StatementExtractor
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (merchant: String, desc: String, amount: Double, type: TransactionType, cat: TransactionCategory, account: String, isRecurring: Boolean) -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isDebit by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD) }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var accountName by remember { mutableStateOf("GTBank Naira Checking") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Transaction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Type Toggle (Debit vs Credit)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isDebit = true },
                        modifier = Modifier.weight(1f).testTag("type_debit_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDebit) PrimaryNavy else Color.Transparent,
                            contentColor = if (isDebit) Color.White else SlateMedium
                        )
                    ) {
                        Text("Expense / Debit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { isDebit = false },
                        modifier = Modifier.weight(1f).testTag("type_credit_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isDebit) EmeraldGreen else Color.Transparent,
                            contentColor = if (!isDebit) Color.White else SlateMedium
                        )
                    ) {
                        Text("Income / Credit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₦)") },
                    modifier = Modifier.fillMaxWidth().testTag("amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Beneficiary") },
                    placeholder = { Text("e.g. Chowdeck, TotalEnergies, Paystack") },
                    modifier = Modifier.fillMaxWidth().testTag("merchant_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Narration") },
                    placeholder = { Text("e.g. Lunch delivery, fuel refill") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownOpen,
                    onExpandedChange = { isCategoryDropdownOpen = !isCategoryDropdownOpen }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownOpen,
                        onDismissRequest = { isCategoryDropdownOpen = false }
                    ) {
                        TransactionCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    isCategoryDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isRecurring = !isRecurring }
                ) {
                    Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark as recurring monthly payment",
                        fontSize = 13.sp,
                        color = SlateMedium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && merchant.isNotBlank()) {
                            onConfirm(
                                merchant,
                                desc.ifBlank { merchant },
                                amount,
                                if (isDebit) TransactionType.DEBIT else TransactionType.CREDIT,
                                selectedCategory,
                                accountName,
                                isRecurring
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_transaction_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Transaction", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatementUploadDialog(
    onDismiss: () -> Unit,
    onExtractAndImport: (rawContent: String, isCsv: Boolean, fileName: String) -> StatementExtractor.StatementExtractionResult
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileType by remember { mutableStateOf("PDF") }
    var rawStatementText by remember { mutableStateOf("") }
    var extractionResult by remember { mutableStateOf<StatementExtractor.StatementExtractionResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val samplePdfText = """
GUARANTY TRUST BANK PLC
ACCOUNT STATEMENT: 0128943892 (AYODELE ELEBUTE)
PERIOD: 01-SEP-2026 TO 15-SEP-2026
CURRENCY: NGN

01-09-2026 PAYROLL CREDIT / TECH CORP SALARY 950000.00 CR
02-09-2026 POS DEBIT / SHOPRITE MALL IKEJA 42000.00 DR
04-09-2026 WEB PAYMENT / CHOWDECK LAGOS 14500.00 DR
05-09-2026 ELECTRONIC LEVY CBN STAMP DUTY 50.00 DR
07-09-2026 POS DEBIT / TOTALENERGIES FILLING STN 45000.00 DR
10-09-2026 MONTHLY DEBIT / MultiChoice DSTV 19800.00 DR
12-09-2026 BANK CHARGES / SMS NOTIFICATION LEVY 1200.00 DR
14-09-2026 POS DEBIT / DOMINOS PIZZA VI 16800.00 DR
    """.trimIndent()

    val sampleCsvText = """
Date,Description,Merchant,Debit,Credit,Balance
2026-09-12,TRANSFER TO CHOWDECK LAGOS,Chowdeck,12500,,485000
2026-09-13,POS PURCHASE TOTALENERGIES VI,TotalEnergies,35000,,450000
2026-09-13,CARD SUB NETFLIX PREMIUM,Netflix,5000,,445000
2026-09-14,MONTHLY PAYROLL INFLOW,Acme Corp,,950000,1395000
2026-09-14,USSD STAMP DUTY CHARGE,CBN / NIBSS,50,,1394950
2026-09-15,POS TRANSACTION SPAR LEKKI,Spar,28500,,1366450
    """.trimIndent()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            try {
                var fileName = "bank_statement"
                if (uri.scheme == "content") {
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val colIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (colIdx != -1) fileName = it.getString(colIdx)
                        }
                    }
                } else {
                    uri.path?.substringAfterLast('/')?.let { fileName = it }
                }

                selectedFileName = fileName
                val isPdf = fileName.endsWith(".pdf", ignoreCase = true) ||
                    (context.contentResolver.getType(uri)?.contains("pdf", ignoreCase = true) == true)
                val isCsv = fileName.endsWith(".csv", ignoreCase = true) ||
                    (context.contentResolver.getType(uri)?.contains("csv", ignoreCase = true) == true)

                selectedFileType = if (isPdf) "PDF" else if (isCsv) "CSV" else "Document"

                val content = if (isPdf) {
                    PdfStatementParser.extractTextFromUri(context, uri)
                } else {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                }

                rawStatementText = content
                val res = onExtractAndImport(content, isCsv, fileName)
                extractionResult = res
            } catch (e: Exception) {
                extractionResult = StatementExtractor.StatementExtractionResult(
                    bankName = "Unknown",
                    accountNumber = "-",
                    openingBalance = 0.0,
                    closingBalance = 0.0,
                    transactions = emptyList(),
                    fileType = selectedFileType,
                    rawTextPreview = "",
                    errorMessage = "Failed to read file: ${e.localizedMessage}"
                )
            } finally {
                isProcessing = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Upload Statement",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Text(
                                text = "Manual PDF or CSV Import",
                                fontSize = 11.sp,
                                color = SlateMedium
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMedium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Upload File vs Paste Text
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = PrimaryNavy,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Choose File (PDF/CSV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Paste Text", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTabIndex == 0) {
                    // Upload File Mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                            .clickable {
                                filePickerLauncher.launch(
                                    arrayOf("application/pdf", "text/csv", "text/comma-separated-values", "text/plain", "*/*")
                                )
                            },
                        color = Color(0xFFF8FAFC)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Select Bank Statement File",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SlateDark
                            )
                            Text(
                                text = "Supports PDF e-statements & CSV spreadsheets from GTBank, Zenith, Access, Kuda, First Bank, UBA, etc.",
                                fontSize = 12.sp,
                                color = SlateMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    filePickerLauncher.launch(
                                        arrayOf("application/pdf", "text/csv", "text/comma-separated-values", "text/plain", "*/*")
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Browse Device Files", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Paste Text Mode
                    Column {
                        OutlinedTextField(
                            value = rawStatementText,
                            onValueChange = { rawStatementText = it },
                            label = { Text("Paste Statement or CSV Content") },
                            placeholder = { Text("Paste extracted text or CSV rows here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 160.dp),
                            maxLines = 8
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    rawStatementText = samplePdfText
                                    selectedFileType = "PDF"
                                    selectedFileName = "Sample_GTBank_Statement.pdf"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sample PDF", fontSize = 11.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    rawStatementText = sampleCsvText
                                    selectedFileType = "CSV"
                                    selectedFileName = "Sample_Statement.csv"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sample CSV", fontSize = 11.sp, maxLines = 1)
                            }

                            Button(
                                onClick = {
                                    if (rawStatementText.isNotBlank()) {
                                        val isCsv = rawStatementText.contains(",") && rawStatementText.lines().firstOrNull()?.contains("date", true) == true
                                        extractionResult = onExtractAndImport(
                                            rawStatementText,
                                            isCsv,
                                            selectedFileName ?: if (isCsv) "statement.csv" else "statement.pdf"
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                shape = RoundedCornerShape(8.dp),
                                enabled = rawStatementText.isNotBlank()
                            ) {
                                Text("Parse", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Selected File Banner
                if (selectedFileName != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (selectedFileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                                contentDescription = null,
                                tint = if (selectedFileType == "PDF") Color(0xFFDC2626) else EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFileName ?: "Statement",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SlateDark,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Format: $selectedFileType",
                                    fontSize = 10.sp,
                                    color = SlateMedium
                                )
                            }
                        }
                    }
                }

                // Extraction Result Feedback
                extractionResult?.let { res ->
                    Spacer(modifier = Modifier.height(14.dp))
                    if (res.transactions.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFECFDF5),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Statement Parsed Successfully",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EmeraldGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bank: ${res.bankName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateDark
                                )
                                Text(
                                    text = "Imported ${res.transactions.size} transactions into FinAudit database.",
                                    fontSize = 11.sp,
                                    color = SlateMedium
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View Audit Results", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "No Transactions Found",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = res.errorMessage ?: "Ensure your PDF or CSV contains dated line items with debit/credit amounts.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309)
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

// Retain CsvImportDialog as compatibility alias delegating to StatementUploadDialog
@Composable
fun CsvImportDialog(
    onDismiss: () -> Unit,
    onImportParsed: (String) -> CsvParser.ParseResult
) {
    StatementUploadDialog(
        onDismiss = onDismiss,
        onExtractAndImport = { rawContent, isCsv, fileName ->
            val parsed = onImportParsed(rawContent)
            StatementExtractor.StatementExtractionResult(
                bankName = "CSV Import",
                accountNumber = "-",
                openingBalance = 0.0,
                closingBalance = 0.0,
                transactions = parsed.transactions,
                fileType = "CSV",
                rawTextPreview = rawContent.take(100),
                errorMessage = parsed.errorMessage
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReclassifyCategoryDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSaveCategory: (TransactionCategory) -> Unit
) {
    var selectedCat by remember { mutableStateOf(transaction.category) }
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Correct Transaction Category",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "FinAudit AI learns from your corrections. Changing this will update \"${transaction.merchant}\" and remember for all future transactions.",
                    fontSize = 12.sp,
                    color = SlateMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCat.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select New Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        TransactionCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCat = cat
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveCategory(selectedCat)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update & Learn", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AssetType, value: Double, institution: String, isPotential: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AssetType.SAVINGS_ACCOUNT) }
    var institution by remember { mutableStateOf("") }
    var isPotential by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Asset",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name") },
                    placeholder = { Text("e.g. Treasury Bills, Land, Stock Portfolio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Estimated Value (₦)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asset Class") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        AssetType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Institution / Custodian") },
                    placeholder = { Text("e.g. Cowrywise, Bamboo, Central Bank") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPotential = !isPotential }
                ) {
                    Checkbox(checked = isPotential, onCheckedChange = { isPotential = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Potential Asset — Confirm Required",
                        fontSize = 12.sp,
                        color = SlateMedium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val v = valueText.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && v > 0) {
                            onConfirm(name, selectedType, v, institution, isPotential)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Asset", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLiabilityDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: LiabilityType, outstanding: Double, monthly: Double, interest: Double?, lender: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var outstandingText by remember { mutableStateOf("") }
    var monthlyText by remember { mutableStateOf("") }
    var interestText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(LiabilityType.PERSONAL_LOAN) }
    var lender by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Liability / Debt",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Debt Name") },
                    placeholder = { Text("e.g. Carbon Loan, CredPal BNPL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = outstandingText,
                    onValueChange = { outstandingText = it },
                    label = { Text("Outstanding Balance (₦)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = monthlyText,
                    onValueChange = { monthlyText = it },
                    label = { Text("Monthly Repayment (₦)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Debt Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        LiabilityType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = interestText,
                    onValueChange = { interestText = it },
                    label = { Text("Annual Interest Rate % (Optional)") },
                    placeholder = { Text("e.g. 18.5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val out = outstandingText.toDoubleOrNull() ?: 0.0
                        val m = monthlyText.toDoubleOrNull() ?: 0.0
                        val rate = interestText.toDoubleOrNull()
                        if (name.isNotBlank() && out > 0) {
                            onConfirm(name, selectedType, out, m, rate, lender)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Liability", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: GoalCategory, target: Double, current: Double, months: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var currentText by remember { mutableStateOf("") }
    var monthsText by remember { mutableStateOf("12") }
    var selectedCat by remember { mutableStateOf(GoalCategory.EMERGENCY_FUND) }
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create Savings Goal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. 6-Month Emergency Buffer, Rent Pool") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCat.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Goal Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        GoalCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCat = cat
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount (₦)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    label = { Text("Current Savings (₦)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = monthsText,
                    onValueChange = { monthsText = it },
                    label = { Text("Target Horizon (Months)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val target = targetText.toDoubleOrNull() ?: 0.0
                        val current = currentText.toDoubleOrNull() ?: 0.0
                        val months = monthsText.toIntOrNull() ?: 12
                        if (title.isNotBlank() && target > 0) {
                            onConfirm(title, selectedCat, target, current, months)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Goal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
