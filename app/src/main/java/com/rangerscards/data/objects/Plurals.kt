package com.rangerscards.data.objects

object Plurals {
    fun getPlural(lang: String, plurals: String, count: Int): String {
        // Split the input string by commas and trim any extra spaces.
        val forms = plurals.split(",")

        return when (lang) {
            "ru" -> {
                // Ensure we have exactly three forms for Russian.
                if (forms.size != 3) {
                    // Fallback: return the first form if the count of forms is not three.
                    forms.firstOrNull() ?: ""
                } else {
                    when {
                        (count % 10 == 1 && count % 100 != 11) -> forms[0]
                        (count % 10 in 2..4 && (count % 100 < 10 || count % 100 >= 20)) -> forms[1]
                        else -> forms[2]
                    }
                }
            }
            // For languages like English, German, Italian, French, and as a default:
            else -> {
                // If count is not 1 and there is a plural form available, use it.
                if (count != 1 && forms.size > 1) forms[1] else forms.firstOrNull() ?: ""
            }
        }
    }
}