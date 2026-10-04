package com.example.data.repository

import com.example.data.model.TransactionCategory

object CategoryClassifier {

    private val defaultRules: Map<String, TransactionCategory> = mapOf(
        // Food & Dining
        "supermarket" to TransactionCategory.FOOD,
        "grocery" to TransactionCategory.FOOD,
        "restaurant" to TransactionCategory.FOOD,
        "eatery" to TransactionCategory.FOOD,
        "chowdeck" to TransactionCategory.FOOD,
        "glovo" to TransactionCategory.FOOD,
        "domino" to TransactionCategory.FOOD,
        "kfc" to TransactionCategory.FOOD,
        "shoprite" to TransactionCategory.FOOD,
        "spar" to TransactionCategory.FOOD,
        "hubmart" to TransactionCategory.FOOD,
        "buka" to TransactionCategory.FOOD,
        "cafe" to TransactionCategory.FOOD,
        "dining" to TransactionCategory.FOOD,

        // Transportation
        "uber" to TransactionCategory.TRANSPORTATION,
        "bolt" to TransactionCategory.TRANSPORTATION,
        "fuel" to TransactionCategory.TRANSPORTATION,
        "filling station" to TransactionCategory.TRANSPORTATION,
        "totalenergies" to TransactionCategory.TRANSPORTATION,
        "nnpc" to TransactionCategory.TRANSPORTATION,
        "oando" to TransactionCategory.TRANSPORTATION,
        "transit" to TransactionCategory.TRANSPORTATION,
        "airpeace" to TransactionCategory.TRANSPORTATION,
        "airline" to TransactionCategory.TRANSPORTATION,

        // Housing & Utilities
        "rent" to TransactionCategory.HOUSING,
        "estate dues" to TransactionCategory.HOUSING,
        "ikedc" to TransactionCategory.UTILITIES,
        "ekedc" to TransactionCategory.UTILITIES,
        "electricity" to TransactionCategory.UTILITIES,
        "power" to TransactionCategory.UTILITIES,
        "water board" to TransactionCategory.UTILITIES,
        "waste" to TransactionCategory.UTILITIES,

        // Subscriptions
        "netflix" to TransactionCategory.SUBSCRIPTIONS,
        "spotify" to TransactionCategory.SUBSCRIPTIONS,
        "dstv" to TransactionCategory.SUBSCRIPTIONS,
        "gotv" to TransactionCategory.SUBSCRIPTIONS,
        "apple.com" to TransactionCategory.SUBSCRIPTIONS,
        "google storage" to TransactionCategory.SUBSCRIPTIONS,
        "youtube premium" to TransactionCategory.SUBSCRIPTIONS,
        "gym" to TransactionCategory.SUBSCRIPTIONS,
        "showmax" to TransactionCategory.SUBSCRIPTIONS,

        // Bank Fees
        "sms charges" to TransactionCategory.BANK_FEES,
        "stamp duty" to TransactionCategory.BANK_FEES,
        "transfer fee" to TransactionCategory.BANK_FEES,
        "maintenance fee" to TransactionCategory.BANK_FEES,
        "atm fee" to TransactionCategory.BANK_FEES,
        "vat on charges" to TransactionCategory.BANK_FEES,

        // Healthcare
        "pharmacy" to TransactionCategory.HEALTHCARE,
        "medplus" to TransactionCategory.HEALTHCARE,
        "healthplus" to TransactionCategory.HEALTHCARE,
        "hospital" to TransactionCategory.HEALTHCARE,
        "clinic" to TransactionCategory.HEALTHCARE,

        // Debt
        "loan" to TransactionCategory.DEBT,
        "carbon" to TransactionCategory.DEBT,
        "fairmoney" to TransactionCategory.DEBT,
        "renmoney" to TransactionCategory.DEBT,
        "credpal" to TransactionCategory.DEBT,
        "bnpl" to TransactionCategory.DEBT,

        // Shopping
        "jumia" to TransactionCategory.SHOPPING,
        "konga" to TransactionCategory.SHOPPING,
        "amazon" to TransactionCategory.SHOPPING,
        "boutique" to TransactionCategory.SHOPPING,
        "clothing" to TransactionCategory.SHOPPING,

        // Savings & Investment
        "cowrywise" to TransactionCategory.SAVINGS,
        "piggyvest" to TransactionCategory.SAVINGS,
        "bamboo" to TransactionCategory.INVESTMENT,
        "chaka" to TransactionCategory.INVESTMENT,
        "trove" to TransactionCategory.INVESTMENT,
        "treasury" to TransactionCategory.INVESTMENT,
        "mutual fund" to TransactionCategory.INVESTMENT,

        // Income / Salary
        "salary" to TransactionCategory.BUSINESS,
        "payroll" to TransactionCategory.BUSINESS,
        "consulting fee" to TransactionCategory.BUSINESS,
        "dividend" to TransactionCategory.INVESTMENT
    )

    fun classify(
        merchant: String,
        description: String,
        learnedRules: Map<String, TransactionCategory> = emptyMap()
    ): Pair<TransactionCategory, Float> {
        val combined = "$merchant $description".lowercase()

        // 1. Check user's learned rules first (highest confidence)
        for ((pattern, category) in learnedRules) {
            if (combined.contains(pattern.lowercase())) {
                return category to 0.99f
            }
        }

        // 2. Check default keyword rules
        for ((pattern, category) in defaultRules) {
            if (combined.contains(pattern)) {
                return category to 0.92f
            }
        }

        // 3. Fallback
        return TransactionCategory.OTHER to 0.50f
    }
}
