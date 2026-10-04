package com.example.data.repository

import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvParser {

    data class ParseResult(
        val transactions: List<Transaction>,
        val detectedColumns: List<String>,
        val rowCount: Int,
        val errorMessage: String? = null
    )

    fun parseCsv(csvText: String, learnedRules: Map<String, TransactionCategory> = emptyMap()): ParseResult {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return ParseResult(emptyList(), emptyList(), 0, "CSV content is empty")
        }

        val headerLine = lines[0]
        val headers = parseCsvRow(headerLine).map { it.trim().lowercase() }

        // Auto-detect column indices
        var dateIdx = headers.indexOfFirst { it.contains("date") || it.contains("time") }
        var descIdx = headers.indexOfFirst { it.contains("desc") || it.contains("narration") || it.contains("details") || it.contains("remark") }
        var merchantIdx = headers.indexOfFirst { it.contains("merchant") || it.contains("beneficiary") || it.contains("recipient") || it.contains("sender") }
        var debitIdx = headers.indexOfFirst { it.contains("debit") || it.contains("withdrawal") || it.contains("out") }
        var creditIdx = headers.indexOfFirst { it.contains("credit") || it.contains("deposit") || it.contains("in") }
        var amountIdx = headers.indexOfFirst { it.contains("amount") || it.contains("val") }
        var typeIdx = headers.indexOfFirst { it.contains("type") }

        if (descIdx == -1 && headers.isNotEmpty()) {
            descIdx = 1.coerceAtMost(headers.size - 1)
        }
        if (dateIdx == -1) dateIdx = 0

        val transactions = mutableListOf<Transaction>()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        for (i in 1 until lines.size) {
            val row = parseCsvRow(lines[i])
            if (row.isEmpty()) continue

            val rawDate = if (dateIdx in row.indices) row[dateIdx] else todayStr
            val rawDesc = if (descIdx in row.indices) row[descIdx] else "Transaction"
            val rawMerchant = if (merchantIdx in row.indices && row[merchantIdx].isNotEmpty()) row[merchantIdx] else extractMerchantFromDesc(rawDesc)

            var amount = 0.0
            var type = TransactionType.DEBIT

            if (debitIdx != -1 && debitIdx in row.indices && row[debitIdx].isNotEmpty()) {
                val parsed = parseAmount(row[debitIdx])
                if (parsed > 0) {
                    amount = parsed
                    type = TransactionType.DEBIT
                }
            }

            if (amount == 0.0 && creditIdx != -1 && creditIdx in row.indices && row[creditIdx].isNotEmpty()) {
                val parsed = parseAmount(row[creditIdx])
                if (parsed > 0) {
                    amount = parsed
                    type = TransactionType.CREDIT
                }
            }

            if (amount == 0.0 && amountIdx != -1 && amountIdx in row.indices) {
                val parsed = parseAmount(row[amountIdx])
                amount = Math.abs(parsed)
                if (row[amountIdx].startsWith("-") || (typeIdx in row.indices && row[typeIdx].contains("dr", ignoreCase = true))) {
                    type = TransactionType.DEBIT
                } else if (typeIdx in row.indices && row[typeIdx].contains("cr", ignoreCase = true)) {
                    type = TransactionType.CREDIT
                }
            }

            if (amount <= 0) continue

            val (category, confidence) = CategoryClassifier.classify(rawMerchant, rawDesc, learnedRules)
            val isBankFee = category == TransactionCategory.BANK_FEES || rawDesc.contains("stamp duty", true) || rawDesc.contains("sms fee", true)

            transactions.add(
                Transaction(
                    date = rawDate.ifEmpty { todayStr },
                    merchant = rawMerchant,
                    description = rawDesc,
                    amount = amount,
                    currency = "NGN",
                    type = type,
                    category = category,
                    isBankFee = isBankFee,
                    isRecurring = isRecurringMerchant(rawMerchant),
                    aiConfidence = confidence,
                    accountName = "Imported CSV Statement"
                )
            )
        }

        return ParseResult(transactions, headers, transactions.size, null)
    }

    private fun parseCsvRow(row: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        for (char in row) {
            when (char) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        current.append(char)
                    } else {
                        result.add(current.toString().trim())
                        current.clear()
                    }
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    private fun parseAmount(raw: String): Double {
        val clean = raw.replace("₦", "").replace("$", "").replace(",", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    private fun extractMerchantFromDesc(desc: String): String {
        val words = desc.split(" ", "/", "-", "_").filter { it.isNotBlank() }
        return if (words.isNotEmpty()) words.take(2).joinToString(" ") else "Merchant"
    }

    private fun isRecurringMerchant(merchant: String): Boolean {
        val lower = merchant.lowercase()
        return lower.contains("netflix") || lower.contains("spotify") || lower.contains("dstv") || lower.contains("gym") || lower.contains("apple") || lower.contains("google")
    }
}
