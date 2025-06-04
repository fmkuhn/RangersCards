package com.rangerscards.data

data class CardFilterOptions(
    val searchQuery: String = "",
    val types: List<String> = emptyList(),
    val traits:  List<String> = emptyList(),
    val sets: List<String> = emptyList(),
    val costRange: IntRange = -2..10,
    val approaches: Approaches = Approaches(),
    val packs: List<String> = emptyList(),
    val aspectRequirements: AspectRequirements = AspectRequirements()
)

data class Approaches(
    val conflict: Int? = null,
    val reason: Int? = null,
    val exploration: Int? = null,
    val connection: Int? = null
)

data class AspectRequirements(
    val awa: Int? = null,
    val spi: Int? = null,
    val foc: Int? = null,
    val fit: Int? = null
)