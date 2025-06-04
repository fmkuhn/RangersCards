package com.rangerscards.data

data class CardFilterOptions(
    val searchQuery: String = "",
    val types: List<String> = emptyList(),
    val traits:  List<String> = emptyList(),
    val sets: List<String> = emptyList(),
    val costRange: IntRange? = null,
    val approaches: Approaches = Approaches(),
    val packs: List<String> = emptyList(),
    val aspectRequirements: AspectRequirements = AspectRequirements()
)

data class Approaches(
    val conflict: Boolean = false,
    val reason: Boolean = false,
    val exploration: Boolean = false,
    val connection: Boolean = false
)

data class AspectRequirements(
    val awa: Int? = null,
    val spi: Int? = null,
    val foc: Int? = null,
    val fit: Int? = null
)

object CardFilters {
    fun buildFiltersClause(filterOptions: CardFilterOptions): String {
        val clauses = mutableListOf<String>()

        // 1) Filter by exact type_id (IN (...))
        if (filterOptions.types.isNotEmpty()) {
            // e.g. type_id IN ('gear', 'role')
            val escaped = filterOptions.types.joinToString(", ") { "'${it.replace("'", "''")}'" }
            clauses += "type_id IN ($escaped)"
        }

        // 2) Filter by traits (check if the traits column CONTAINS any of the user’s selected traits)
        //    e.g. if traits = ["Wisdom", "Trail"]: (real_traits LIKE '%Wisdom%' OR real_traits LIKE '%Trail%')
        if (filterOptions.traits.isNotEmpty()) {
            val sub = filterOptions.traits.joinToString(" OR ") {
                "real_traits LIKE '%${it.replace("'", "''")}%'"
            }
            clauses += "($sub)"
        }

        // 3) Filter by set_id (IN (...))
        if (filterOptions.sets.isNotEmpty()) {
            val escaped = filterOptions.sets.joinToString(", ") { "'${it.replace("'", "''")}'" }
            clauses += "set_id IN ($escaped)"
        }

        // 4) Filter by costRange (INTEGER BETWEEN min AND max), only if range is set
        if (filterOptions.costRange != null) {
            val minCost = filterOptions.costRange.first
            val maxCost = filterOptions.costRange.last
            clauses += "cost BETWEEN $minCost AND $maxCost"
        }

        // 5) Filter by Approaches.
        //    If any of the four is true, build a single "( ... OR ... )" clause:
        val approachChecks = mutableListOf<String>()
        if (filterOptions.approaches.conflict) {
            approachChecks += "approach_conflict >= 1"
        }
        if (filterOptions.approaches.reason) {
            approachChecks += "approach_reason >= 1"
        }
        if (filterOptions.approaches.exploration) {
            approachChecks += "approach_exploration >= 1"
        }
        if (filterOptions.approaches.connection) {
            approachChecks += "approach_connection >= 1"
        }
        if (approachChecks.isNotEmpty()) {
            // Combine all selected approach‐checks with OR
            val combined = approachChecks.joinToString(" OR ")
            clauses += "($combined)"
        }

        // 6) Filter by AspectRequirements: combine into one "(… OR …)" clause
        val aspectChecks = mutableListOf<String>()
        filterOptions.aspectRequirements.awa?.let { awaValue ->
            aspectChecks += "(aspect_id = 'AWA' AND level = $awaValue)"
        }
        filterOptions.aspectRequirements.spi?.let { spiValue ->
            aspectChecks += "(aspect_id = 'SPI' AND level = $spiValue)"
        }
        filterOptions.aspectRequirements.foc?.let { focValue ->
            aspectChecks += "(aspect_id = 'FOC' AND level = $focValue)"
        }
        filterOptions.aspectRequirements.fit?.let { fitValue ->
            aspectChecks += "(aspect_id = 'FIT' AND level = $fitValue)"
        }
        if (aspectChecks.isNotEmpty()) {
            val combined = aspectChecks.joinToString(" OR ")
            clauses += "($combined)"
        }

        // If collected zero clauses, return an empty string
        if (clauses.isEmpty()) return ""

        // Otherwise join all clauses with " AND " and prefix with " AND "
        return clauses.joinToString(separator = " AND ")
    }
}