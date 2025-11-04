package com.example.expensetracker.model

/**
 * Enum representing supported currencies
 * Each currency has a symbol and code
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    JPY("JPY", "¥", "Japanese Yen"),
    CHF("CHF", "Fr", "Swiss Franc"),
    CAD("CAD", "C$", "Canadian Dollar"),
    AUD("AUD", "A$", "Australian Dollar"),
    CNY("CNY", "¥", "Chinese Yuan"),
    INR("INR", "₹", "Indian Rupee"),
    SEK("SEK", "kr", "Swedish Krona"),
    NOK("NOK", "kr", "Norwegian Krone"),
    DKK("DKK", "kr", "Danish Krone");

    companion object {
        fun fromCode(code: String): Currency {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }

    /**
     * Formats an amount with this currency
     * @param amount The amount to format
     * @return Formatted string like "$32.50" or "€25.99"
     */
    fun format(amount: Double): String {
        return when (this) {
            JPY, CNY -> "${symbol}${amount.toInt()}" // No decimals for yen/yuan
            else -> {
                // Format to 2 decimal places (multiplatform compatible)
                val integerPart = amount.toInt()
                val fractionalPart = ((amount - integerPart) * 100).toInt().let {
                    if (it < 10) "0$it" else "$it"
                }
                "$symbol$integerPart.$fractionalPart"
            }
        }
    }
}

