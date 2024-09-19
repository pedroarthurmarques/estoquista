package br.com.pedroamarques.estoquista.factory

import com.google.gson.*
import timber.log.Timber
import java.lang.reflect.Type
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

public class BigDecimalSerializer: JsonSerializer<BigDecimal>, JsonDeserializer<BigDecimal> {
    override fun serialize(src: BigDecimal?, typeOfSrc: Type?, context: JsonSerializationContext? ): JsonElement {
        if (src == null) {
            return JsonNull()
        }

        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext? ): BigDecimal? {
        if (json == null) {
            return null
        }

        try {
            if (json.asBigDecimal != null) {
                return json.asBigDecimal
            } else {
                throw Exception("asDouble")
            }
        } catch (ex: Exception) {
            try {
                if (json.asDouble != null) {
                    return BigDecimal(json.asDouble)
                } else {
                    throw Exception("asInt")
                }
            } catch (ex: Exception) {
                try {
                    if (json.asInt != null) {
                        return BigDecimal(json.asInt)
                    } else {
                        throw Exception("asString")
                    }
                } catch (ex: Exception) {
                    return try {
                        if (json.asString != null) {
                            val valor = json.asString?.replace(regex = Regex("[^0-9.]"), replacement = "")
                            BigDecimal(valor)
                        } else {
                            null
                        }
                    } catch (ex: Exception) {
                        null
                    }
                }
            }
        }
    }
}