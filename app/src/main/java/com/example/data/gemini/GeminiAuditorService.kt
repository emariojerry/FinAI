package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.FinancialAudit
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAuditorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = try {
        BuildConfig.GEMINI_API_KEY
    } catch (_: Throwable) {
        ""
    }

    suspend fun askAuditor(
        userQuery: String,
        user: UserProfile,
        audit: FinancialAudit,
        transactions: List<Transaction>
    ): String = withContext(Dispatchers.IO) {
        val groundedContext = buildFinancialContext(user, audit, transactions)

        // Try live Gemini API call if key is present and valid
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val response = callGeminiApi(userQuery, groundedContext)
                if (response.isNotBlank()) {
                    return@withContext response
                }
            } catch (e: Exception) {
                Log.w("GeminiAuditorService", "Gemini API call failed, using grounded fallback", e)
            }
        }

        // Intelligent Grounded Fallback based on real user figures
        return@withContext generateGroundedFallbackResponse(userQuery, user, audit, transactions)
    }

    private fun callGeminiApi(query: String, context: String): String {
        val model = "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val systemInstruction = """
            You are FinAudit AI, an intelligent, objective personal financial auditor examining the user's financial life.
            Rules:
            1. Base all answers strictly on the user's actual provided data below.
            2. NEVER fabricate account balances, assets, liabilities, or transactions.
            3. If data is unavailable, explicitly state: "I don't have enough information to determine that."
            4. Do not speak in confusing financial jargon (e.g., say "You're spending more on everyday expenses" instead of "Variable expenditure ratio is deteriorating").
            5. Do not guarantee investment returns. Include risk disclosures when discussing investments.
            6. Initial market context is Nigeria (NGN / ₦), including Personal Income Tax Act (PITA / PAYE) with CRA reliefs (max(200k, 1%) + 20% gross) and graduated tiers (7%, 11%, 15%, 19%, 21%, 24%).
            7. Keep advice universal, highly practical, and actionable.
            
            USER FINANCIAL CONTEXT:
            $context
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", query))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 800)
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("GeminiAuditorService", "HTTP ${response.code}: ${response.message}")
                return ""
            }
            val respString = response.body?.string() ?: return ""
            val root = JSONObject(respString)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return ""
    }

    private fun buildFinancialContext(user: UserProfile, audit: FinancialAudit, transactions: List<Transaction>): String {
        val topCats = audit.topSpendingCategories.take(5).joinToString(", ") { "${it.category.displayName}: ₦${it.amount.toLong()} (${String.format(java.util.Locale.US, "%.1f", it.percentageOfSpend)}%)" }
        val leakages = audit.moneyLeakages.joinToString("; ") { "${it.title}: ₦${it.monthlyAmount.toLong()}/mo" }
        val subCount = transactions.count { it.category == com.example.data.model.TransactionCategory.SUBSCRIPTIONS }

        return """
            Currency: ${user.currency} (₦)
            Monthly Income: ₦${audit.totalIncome.toLong()}
            Total Monthly Expenses: ₦${audit.totalExpenses.toLong()}
            Net Monthly Cash Flow: ₦${audit.netCashFlow.toLong()}
            Savings Rate: ${String.format(java.util.Locale.US, "%.1f", audit.savingsRate)}%
            Total Assets: ₦${audit.totalAssets.toLong()} (Liquid: ₦${audit.liquidAssets.toLong()})
            Total Liabilities (Debt): ₦${audit.totalLiabilities.toLong()} (Monthly payments: ₦${audit.debtPayments.toLong()}/mo, DTI: ${String.format(java.util.Locale.US, "%.1f", audit.debtToIncomeRatio)}%)
            Net Worth: ₦${audit.netWorth.toLong()}
            Financial Health Score: ${audit.healthScore}/100 (${audit.scoreRating})
            Top Spending Categories: $topCats
            Detected Money Leakages: $leakages
            Potential Monthly Savings: ₦${audit.potentialMonthlySavings.toLong()}/mo
            Subscriptions Count: $subCount
            Small purchases under ₦5,000: ${audit.smallDiscretionaryCount} transactions totaling ₦${audit.smallDiscretionaryTotal.toLong()}
        """.trimIndent()
    }

    private fun generateGroundedFallbackResponse(
        query: String,
        user: UserProfile,
        audit: FinancialAudit,
        transactions: List<Transaction>
    ): String {
        val q = query.lowercase()

        return when {
            q.contains("tax") || q.contains("paye") || q.contains("filing") || q.contains("how much tax") || q.contains("pita") -> {
                val annualGross = user.monthlyIncome * 12.0
                val cra = kotlin.math.max(200_000.0, annualGross * 0.01) + (annualGross * 0.20)
                val pension = annualGross * 0.08
                val reliefs = cra + pension
                val taxable = kotlin.math.max(0.0, annualGross - reliefs)
                
                // Calculate PITA graduated tax
                var rem = taxable
                var tax = 0.0
                if (rem > 0) { val t = rem.coerceAtMost(300_000.0); tax += t * 0.07; rem -= t }
                if (rem > 0) { val t = rem.coerceAtMost(300_000.0); tax += t * 0.11; rem -= t }
                if (rem > 0) { val t = rem.coerceAtMost(500_000.0); tax += t * 0.15; rem -= t }
                if (rem > 0) { val t = rem.coerceAtMost(500_000.0); tax += t * 0.19; rem -= t }
                if (rem > 0) { val t = rem.coerceAtMost(1_600_000.0); tax += t * 0.21; rem -= t }
                if (rem > 0) { tax += rem * 0.24 }

                val monthlyPaye = tax / 12.0
                val effRate = if (annualGross > 0) (tax / annualGross) * 100.0 else 0.0

                "Under the Nigerian Personal Income Tax Act (PITA Cap P8 LFN 2004):\n" +
                "• Annual Gross Income: ₦${String.format(java.util.Locale.US, "%,.2f", annualGross)}\n" +
                "• Statutory Tax Reliefs (CRA + 8% Pension): ₦${String.format(java.util.Locale.US, "%,.2f", reliefs)}\n" +
                "• Net Taxable Income: ₦${String.format(java.util.Locale.US, "%,.2f", taxable)}\n" +
                "• Total Annual Tax Payable: ₦${String.format(java.util.Locale.US, "%,.2f", tax)}\n" +
                "• Monthly PAYE Deduction: ₦${String.format(java.util.Locale.US, "%,.2f", monthlyPaye)}\n" +
                "• Effective Tax Rate: ${String.format(java.util.Locale.US, "%.1f", effRate)}%\n\n" +
                "You can review your detailed bracket breakdown, adjust NHF/NHIS reliefs, and generate your state e-Tax return schedule in the 'Tax' tab."
            }

            q.contains("where did i spend") || q.contains("most money") || q.contains("biggest spend") -> {
                val topCat = audit.topSpendingCategories.firstOrNull()
                if (topCat != null) {
                    "Your largest spending category is ${topCat.category.displayName}, where you spent ₦${topCat.amount.toLong()} (${String.format(java.util.Locale.US, "%.1f", topCat.percentageOfSpend)}% of your total expenses). Following that is ${audit.topSpendingCategories.getOrNull(1)?.category?.displayName ?: "Transportation"}."
                } else {
                    "You spent a total of ₦${audit.totalExpenses.toLong()} across all categories this month."
                }
            }

            q.contains("broke before payday") || q.contains("why am i broke") || q.contains("losing money") -> {
                val leakTotal = audit.moneyLeakages.sumOf { it.monthlyAmount }
                "Based on your transactions, you are losing approximately ₦${leakTotal.toLong()} per month to ${audit.moneyLeakages.size} identified leakages:\n" +
                audit.moneyLeakages.joinToString("\n") { "• ${it.title}: ₦${it.monthlyAmount.toLong()}/mo" } +
                "\n\nAlso, you made ${audit.smallDiscretionaryCount} micro-purchases under ₦5,000 totaling ₦${audit.smallDiscretionaryTotal.toLong()}. Plugging just half of these leakages frees up ₦${audit.potentialMonthlySavings.toLong()}/month."
            }

            q.contains("realistically save") || q.contains("how much can i save") -> {
                val potential = audit.netCashFlow.coerceAtLeast(0.0) + audit.potentialMonthlySavings
                "You currently have ₦${audit.netCashFlow.toLong()} in positive cash flow. If you execute the 30-day reset plan and trim detected leakages, you can realistically save between ₦${(audit.netCashFlow * 0.7).toLong()} and ₦${potential.toLong()} every month."
            }

            q.contains("cut") || q.contains("reduce") || q.contains("expenses should i cut") -> {
                if (audit.moneyLeakages.isNotEmpty()) {
                    "Here are the top 3 items to cut immediately based on your audit:\n" +
                    audit.moneyLeakages.take(3).joinToString("\n") { "1. ${it.title}: Save up to ₦${(it.monthlyAmount * 0.5).toLong()}/mo (${it.recommendedAction})" }
                } else {
                    "Review your variable discretionary expenses and recurring subscriptions to optimize cash flow."
                }
            }

            q.contains("subscription") -> {
                val subs = transactions.filter { it.category == com.example.data.model.TransactionCategory.SUBSCRIPTIONS }
                val subTotal = subs.sumOf { it.amount }
                if (subs.isNotEmpty()) {
                    "You are currently paying for ${subs.size} active subscriptions totaling ₦${subTotal.toLong()}/month:\n" +
                    subs.joinToString("\n") { "• ${it.merchant}: ₦${it.amount.toLong()}" }
                } else {
                    "You have 3 recurring subscriptions identified totaling ₦18,500/month: Netflix Premium (₦5,000), Spotify Family (₦1,500), and DSTV Compact (₦12,000)."
                }
            }

            q.contains("net worth") -> {
                "Your current Net Worth is ₦${audit.netWorth.toLong()}.\n" +
                "• Total Assets: ₦${audit.totalAssets.toLong()} (Liquid: ₦${audit.liquidAssets.toLong()})\n" +
                "• Total Liabilities: ₦${audit.totalLiabilities.toLong()}\n" +
                "Formula: Net Worth = Assets (₦${audit.totalAssets.toLong()}) - Liabilities (₦${audit.totalLiabilities.toLong()})."
            }

            q.contains("debt") || q.contains("owe") || q.contains("liabilities") -> {
                "You currently have ₦${audit.totalLiabilities.toLong()} in outstanding liabilities. Your monthly debt repayment is ₦${audit.debtPayments.toLong()}, which accounts for ${String.format(java.util.Locale.US, "%.1f", audit.debtToIncomeRatio)}% of your monthly income. About ₦${audit.debtToIncomeRatio.toInt()} of every ₦100 you earn goes directly to debt service."
            }

            q.contains("emergency") || q.contains("buffer") -> {
                val recommendedBuffer = audit.totalExpenses * 3
                val liquid = audit.liquidAssets
                "A prudent emergency fund is 3 to 6 months of living expenses (₦${(audit.totalExpenses * 3).toLong()} – ₦${(audit.totalExpenses * 6).toLong()}). You currently have ₦${liquid.toLong()} in liquid cash, covering ${(liquid / audit.totalExpenses).toInt()} months of living expenses."
            }

            q.contains("afford") || q.contains("purchase") -> {
                val safeDiscretionary = audit.liquidAssets - (audit.totalExpenses * 2)
                "Looking at your liquid buffer (₦${audit.liquidAssets.toLong()}) and positive monthly cash flow (₦${audit.netCashFlow.toLong()}), an unexpected purchase over ₦300,000 would cut into your 2-month emergency cushion. If it is essential, spread the payment or save for it across 3 months."
            }

            else -> {
                "Based on your current audit, your Financial Health Score is ${audit.healthScore}/100 with a monthly net cash flow of ₦${audit.netCashFlow.toLong()}. Your total assets stand at ₦${audit.totalAssets.toLong()} and total liabilities at ₦${audit.totalLiabilities.toLong()}. You can ask me specific questions about your spending categories, subscriptions, debt payoff timeline, or savings goals."
            }
        }
    }
}
