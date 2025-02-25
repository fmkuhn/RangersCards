package com.rangerscards.data.objects

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object JsonElementConverter {
    @TypeConverter
    fun fromJsonElement(jsonElement: JsonElement?): String? {
        return jsonElement?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toJsonElement(json: String?): JsonElement? {
        return json?.let { Json.decodeFromString(it) }
    }
}