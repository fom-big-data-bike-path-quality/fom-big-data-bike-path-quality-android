package de.florianschwanz.bikepathquality.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant


class InstantTypeConverter : JsonSerializer<Instant?>,
    JsonDeserializer<Instant?> {
    override fun serialize(
        src: Instant?,
        srcType: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.toEpochMilli())
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type?,
        context: JsonDeserializationContext?
    ): Instant {
        return Instant.ofEpochMilli(json.asLong)
    }
}