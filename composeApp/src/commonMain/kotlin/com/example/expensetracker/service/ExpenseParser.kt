package com.example.expensetracker.service

import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory

/**
 * Simple rule-based parser for extracting expense data from text
 * This is a POC - we'll evaluate if it's "good enough" or needs AI
 */
object ExpenseParser {

    /**
     * Parse transcribed text into expense data
     */
    fun parse(text: String): ParsedExpenseData {
        val lowerText = text.lowercase()

        return ParsedExpenseData(
            amount = extractAmount(lowerText),
            currency = extractCurrency(lowerText),
            category = extractCategory(lowerText),
            description = extractDescription(text, lowerText),
            rawText = text
        )
    }

    /**
     * Extract amount from text
     * Handles: "50", "50.00", "fifty", "50 euros"
     */
    private fun extractAmount(text: String): Double? {
        // Pattern 1: Numeric amounts
        val numericPattern = Regex("""(\d+(?:[.,]\d{1,2})?)""")
        val numericMatch = numericPattern.find(text)
        if (numericMatch != null) {
            return numericMatch.value.replace(",", ".").toDoubleOrNull()
        }

        // Pattern 2: Spelled out numbers (basic)
        val wordToNumber = mapOf(
            "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0, "five" to 5.0,
            "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
            "fifteen" to 15.0, "twenty" to 20.0, "thirty" to 30.0, "forty" to 40.0,
            "fifty" to 50.0, "sixty" to 60.0, "seventy" to 70.0, "eighty" to 80.0,
            "ninety" to 90.0, "hundred" to 100.0
        )

        for ((word, value) in wordToNumber) {
            if (text.contains(word)) {
                return value
            }
        }

        return null
    }

    /**
     * Extract currency from text
     * Handles: "euros", "dollars", "pounds", "€", "$", "£"
     */
    private fun extractCurrency(text: String): Currency? {
        return when {
            // Symbols
            text.contains("€") -> Currency.EUR
            text.contains("$") -> Currency.USD
            text.contains("£") -> Currency.GBP
            text.contains("¥") -> Currency.JPY

            // Words
            text.contains("euro") -> Currency.EUR
            text.contains("dollar") -> Currency.USD
            text.contains("buck") -> Currency.USD
            text.contains("pound") -> Currency.GBP
            text.contains("yen") -> Currency.JPY
            text.contains("franc") -> Currency.CHF

            else -> null
        }
    }

    /**
     * Extract category from text
     * Looks for category keywords
     */
    private fun extractCategory(text: String): ExpenseCategory? {
        // FOOD keywords
        val foodKeywords = listOf(
            "food", "lunch", "dinner", "breakfast", "meal", "restaurant",
            "cafe", "coffee", "snack", "grocery", "groceries",
            "subway", "mcdonald", "burger", "pizza"
        )

        // TRAVEL keywords
        val travelKeywords = listOf(
            "travel", "transport", "gas", "petrol", "fuel", "parking",
            "train", "bus", "flight", "taxi", "uber", "car"
        )

        // UTILITIES keywords
        val utilitiesKeywords = listOf(
            "utilities", "utility", "electric", "electricity", "water",
            "internet", "phone", "bill", "subscription"
        )

        return when {
            foodKeywords.any { text.contains(it) } -> ExpenseCategory.FOOD
            travelKeywords.any { text.contains(it) } -> ExpenseCategory.TRAVEL
            utilitiesKeywords.any { text.contains(it) } -> ExpenseCategory.UTILITIES
            else -> null
        }
    }

    /**
     * Extract description from text
     * Tries to clean up the text into a readable description
     */
    private fun extractDescription(originalText: String, lowerText: String): String {
        // Remove category mentions
        var cleaned = originalText

        val categoryWords = listOf(
            "food category", "travel category", "utilities category",
            "category", "amount", "expense"
        )

        categoryWords.forEach { word ->
            cleaned = cleaned.replace(word, "", ignoreCase = true)
        }

        // Remove currency mentions
        val currencyWords = listOf("euros", "dollars", "pounds", "bucks")
        currencyWords.forEach { word ->
            cleaned = cleaned.replace(word, "", ignoreCase = true)
        }

        // Remove amount if at the beginning
        cleaned = cleaned.replace(Regex("""^\d+[.,]?\d*\s*"""), "")

        // Clean up whitespace
        cleaned = cleaned.trim().replace(Regex("""\s+"""), " ")

        // Capitalize first letter
        return cleaned.replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * Result of parsing transcribed text
 */
data class ParsedExpenseData(
    val amount: Double?,
    val currency: Currency?,
    val category: ExpenseCategory?,
    val description: String,
    val rawText: String
) {
    /**
     * Calculate a simple "completeness" score
     * 1.0 = all fields extracted, 0.0 = nothing extracted
     */
    val completeness: Float
        get() {
            var score = 0f
            if (amount != null) score += 0.4f  // Most important
            if (currency != null) score += 0.2f
            if (category != null) score += 0.2f
            if (description.isNotBlank()) score += 0.2f
            return score
        }

    /**
     * Is this data "good enough" to show to user?
     */
    val isUsable: Boolean
        get() = amount != null && description.isNotBlank()
}

