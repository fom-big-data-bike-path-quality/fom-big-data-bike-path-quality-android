package de.florianschwanz.bikepathquality.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*


class UuidTypeConverter : JsonSerializer<UUID?>, JsonDeserializer<UUID?> {

    override fun serialize(
        src: UUID?,
        srcType: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.toString())
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type?,
        context: JsonDeserializationContext?
    ): UUID {
        return UUID.fromString(json.asString)
    }
}