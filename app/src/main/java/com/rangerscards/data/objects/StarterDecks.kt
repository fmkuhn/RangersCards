package com.rangerscards.data.objects

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class StarterDeck(
    val meta: JsonElement,
    val foc: Int,
    val spi: Int,
    val awa: Int,
    val fit: Int,
    val slots: JsonElement,
)

object StarterDecks {
    val starterDecks by lazy {
        listOf(
            StarterDeck(
                meta = buildJsonObject {
                    put("role", "01037")
                    put("background", "traveler")
                    put("specialty", "explorer")
                },
                foc = 1,
                spi = 2,
                awa = 2,
                fit = 3,
                slots = buildJsonObject {
                    put("01001", 2)
                    put("01039", 2)
                    put("01056", 2)
                    put("01099", 2)
                    put("01005", 2)
                    put("01101", 2)
                    put("01105", 2)
                    put("01003", 2)
                    put("01044", 2)
                    put("01006", 2)
                    put("01093", 2)
                    put("01008", 2)
                    put("01048", 2)
                    put("01042", 2)
                    put("01043", 2)
                }
            ),
            StarterDeck(
                meta = buildJsonObject {
                    put("role", "01066")
                    put("background", "shepherd")
                    put("specialty", "conciliator")
                },
                foc = 2,
                spi = 3,
                awa = 1,
                fit = 2,
                slots = buildJsonObject {
                    put("01023", 2)
                    put("01073", 2)
                    put("01104", 2)
                    put("01078", 2)
                    put("01026", 2)
                    put("01107", 2)
                    put("01095", 2)
                    put("01067", 2)
                    put("01097", 2)
                    put("01022", 2)
                    put("01070", 2)
                    put("01025", 2)
                    put("01027", 2)
                    put("01077", 2)
                    put("01018", 2)
                }
            ),
            StarterDeck(
                meta = buildJsonObject {
                    put("role", "01079")
                    put("background", "forager")
                    put("specialty", "shaper")
                },
                foc = 2,
                spi = 1,
                awa = 3,
                fit = 2,
                slots = buildJsonObject {
                    put("01102", 2)
                    put("01084", 2)
                    put("01081", 2)
                    put("01085", 2)
                    put("01034", 2)
                    put("01029", 2)
                    put("01090", 2)
                    put("01031", 2)
                    put("01100", 2)
                    put("01094", 2)
                    put("01083", 2)
                    put("01082", 2)
                    put("01028", 2)
                    put("01106", 2)
                    put("01035", 2)
                }
            ),
            StarterDeck(
                meta = buildJsonObject {
                    put("role", "01051")
                    put("background", "artisan")
                    put("specialty", "artificer")
                },
                foc = 3,
                spi = 2,
                awa = 2,
                fit = 1,
                slots = buildJsonObject {
                    put("01062", 2)
                    put("01061", 2)
                    put("01012", 2)
                    put("01059", 2)
                    put("01060", 2)
                    put("01096", 2)
                    put("01011", 2)
                    put("01007", 2)
                    put("01017", 2)
                    put("01013", 2)
                    put("01108", 2)
                    put("01015", 2)
                    put("01098", 2)
                    put("01103", 2)
                    put("01053", 2)
                }
            ),
        )
    }
}