package com.example.myeduapp.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Decodes API ids that may arrive as integers or hashid strings.
 */
object JsonFlexibleIdSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonNull) return ""
            if (element is JsonPrimitive) {
                return element.content
            }
        }
        return decoder.decodeString()
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

/** Decodes API values that may arrive as strings, numbers, or booleans. */
object JsonFlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonNull) return ""
            if (element is JsonPrimitive) {
                return element.content
            }
        }
        return decoder.decodeString()
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

/** Decodes booleans that may arrive as true/false, 1/0, or "yes"/"no". */
object JsonFlexibleBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                return when (element.content.lowercase()) {
                    "1", "true", "yes" -> true
                    "0", "false", "no" -> false
                    else -> element.content.toBooleanStrictOrNull() ?: false
                }
            }
        }
        return decoder.decodeBoolean()
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}

/** Decodes integers that may arrive as numbers or numeric strings. */
object JsonFlexibleIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonNull) return 0
            if (element is JsonPrimitive) {
                return element.content.toDoubleOrNull()?.toInt()
                    ?: element.content.toIntOrNull()
                    ?: 0
            }
        }
        return decoder.decodeInt()
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}

/** Nullable variant for optional numeric API fields. */
object JsonFlexibleNullableIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleNullableInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonNull) return null
            if (element is JsonPrimitive) {
                return element.content.toDoubleOrNull()?.toInt()
                    ?: element.content.toIntOrNull()
            }
        }
        return decoder.decodeInt()
    }

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) encoder.encodeNull() else encoder.encodeInt(value)
    }
}
