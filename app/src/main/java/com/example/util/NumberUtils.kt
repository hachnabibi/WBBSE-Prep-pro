package com.example.util

import java.util.Locale

/**
 * Formats numbers into compact representations like 1K, 1.5K, 10K, 1M, etc.
 * E.g., 1000 -> "1K", 1500 -> "1.5K", 10000 -> "10K", 1000000 -> "1M"
 */
fun formatCompactNumber(number: Long, isBengali: Boolean = false): String {
    val formatted = when {
        number >= 1_000_000_000L -> String.format(Locale.US, "%.1fB", number / 1_000_000_000.0).replace(".0B", "B")
        number >= 1_000_000L -> String.format(Locale.US, "%.1fM", number / 1_000_000.0).replace(".0M", "M")
        number >= 1_000L -> String.format(Locale.US, "%.1fK", number / 1_000.0).replace(".0K", "K")
        else -> number.toString()
    }
    return if (isBengali) {
        formatBengaliCompactNumber(formatted)
    } else {
        formatted
    }
}

fun formatCompactNumber(number: Int, isBengali: Boolean = false): String {
    return formatCompactNumber(number.toLong(), isBengali)
}

fun formatCompactNumber(number: Double, isBengali: Boolean = false): String {
    return formatCompactNumber(number.toLong(), isBengali)
}

private fun formatBengaliCompactNumber(input: String): String {
    val banglaDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )
    return input.map { banglaDigits[it] ?: it }.joinToString("")
}
