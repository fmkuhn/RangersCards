package com.rangerscards.data

object TimestampNormilizer {
    // It ensures the fraction has exactly 3 digits.
    fun fixFraction(dateString: String?): String? {
        if (dateString == null) return null
        // Regex groups:
        // 1. The part before the fraction (including the dot)
        // 2. The fractional digits
        // 3. The rest (timezone information)
        val regex = """(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.)(\d+)(.*)""".toRegex()
        return regex.replace(dateString) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val fraction = matchResult.groupValues[2]
            val suffix = matchResult.groupValues[3]
            // Truncate to the first 3 digits or pad with zeros if needed.
            val fixedFraction = when {
                fraction.length > 3 -> fraction.substring(0, 3)
                fraction.length < 3 -> fraction.padEnd(3, '0')
                else -> fraction
            }
            prefix + fixedFraction + suffix
        }
    }
}