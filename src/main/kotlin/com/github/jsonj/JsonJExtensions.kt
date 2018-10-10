package com.github.jsonj

import com.github.jsonj.tools.JsonBuilder
import com.github.jsonj.tools.JsonBuilder.array
import com.github.jsonj.tools.JsonBuilder.nullValue
import com.github.jsonj.tools.JsonBuilder.primitive
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.cast
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.jvmErasure

fun obj(init: JsonObject.() -> Unit): JsonObject {
    val newObject = JsonObject()
    newObject.init()
    return newObject
}

fun arr(init: JsonArray.() -> Unit): JsonArray {
    val newObject = JsonArray()
    newObject.init()
    return newObject
}

fun JsonObject.field(key: String, vararg values: Any) {
    when (values.size) {
        0 -> put(key, JsonBuilder.nullValue())
        1 -> put(key, values[0])
        else -> put(key, JsonBuilder.array(*values))
    }
}

fun JsonObject.arrayField(key: String, vararg values: Any) {
    put(key, JsonBuilder.array(*values))
}

/**
 * @param property name
 * @param ignoreCase if true ignore case
 * @param ignoreUnderscores if true ignore underscores
 * @return the value of the first field matching the name or null
 */
fun JsonObject.flexGet(name: String, ignoreCase: Boolean = true, ignoreUnderscores: Boolean = true): JsonElement? {
    val key = keys.filter { normalize(it, ignoreCase, ignoreUnderscores) == normalize(name, ignoreCase, ignoreUnderscores) }
        .firstOrNull()
    return if (key != null) {
        val value = get(key)
        if (value?.isNull() == true) {
            // handle json null as null
            null
        } else {
            value
        }
    } else {
        null
    }
}

fun <T : Enum<*>> enumVal(clazz: KClass<T>, value: String): T? {
    val enumConstants = clazz.java.enumConstants
    return enumConstants.filter { value == it.name }.first()
}

/**
 * @param clazz a kotlin class
 * @return a new instance of clazz populated with values from the json object matched on the property names (ignoring case and underscores).
 */
fun <T : Any> JsonObject.construct(clazz: KClass<T>): T {
    val primaryConstructor = clazz.primaryConstructor
    val paramz: MutableMap<KParameter, Any?> = mutableMapOf()

    primaryConstructor!!.parameters.forEach {
        val name = it.name.orEmpty()
        val nonNullableType = it.type.withNullability(false)
        if (nonNullableType.isSubtypeOf(Int::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asInt())
        } else if (nonNullableType.isSubtypeOf(Long::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asLong())
        } else if (nonNullableType.isSubtypeOf(Float::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asFloat())
        } else if (nonNullableType.isSubtypeOf(Double::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asDouble())
        } else if (nonNullableType.isSubtypeOf(BigInteger::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asNumber())
        } else if (nonNullableType.isSubtypeOf(BigDecimal::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asNumber())
        } else if (nonNullableType.isSubtypeOf(Long::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asLong())
        } else if (nonNullableType.isSubtypeOf(String::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asString())
        } else if (nonNullableType.isSubtypeOf(Boolean::class.starProjectedType)) {
            paramz.put(it, flexGet(name)?.asBoolean())
        } else if (nonNullableType.isSubtypeOf(Enum::class.starProjectedType)) {
            val enumName = flexGet(name)?.asString()
            if (enumName != null) {
                @Suppress("UNCHECKED_CAST") // we already checked but too hard for Kotlin to figure out
                paramz.put(it, enumVal(it.type.jvmErasure as KClass<Enum<*>>, enumName))
            }
        } else {
            paramz.put(it, flexGet(name)?.asObject()?.construct(it.type.jvmErasure))
        }
    }

    return primaryConstructor.callBy(paramz)
}

/**
 * Attempt to populate the json object using the properties of the provided object. Field names are lower cased and underscored.
 * @param obj an object
 */
fun <T : Any> JsonObject.fill(obj: T): JsonObject {
    val clazz = obj::class

    for (memberProperty in clazz.memberProperties) {
        val propertyName = memberProperty.name
        val jsonName = toUnderscore(propertyName)

        val value = memberProperty.getter.call(obj)
        if (memberProperty.returnType.isSubtypeOf(Enum::class.starProjectedType)) {
            val enumValue = value as Enum<*>
            put(jsonName, enumValue.name)
        } else {
            val returnType = memberProperty.returnType
            val jsonElement: JsonElement = jsonElement(returnType, value)

            put(jsonName, jsonElement)
        }
    }
    return this
}

private fun normalize(input: String, lower: Boolean = true, ignoreUnderscores: Boolean = true): String {
    val normalized = if (ignoreUnderscores) input.replace("_", "") else input
    return if (lower) normalized.toLowerCase(Locale.ROOT) else normalized
}

private fun toUnderscore(propertyName: String): String {
    return StringUtils.splitByCharacterTypeCamelCase(propertyName)
        .filter { !it.equals("_") } // avoid getting ___
        .map { it.toLowerCase(Locale.ROOT) }
        .joinToString("_")
}

private fun jsonElement(returnType: KType, value: Any?): JsonElement {
    val jsonElement: JsonElement
    if (value == null) {
        return nullValue()
    }
    val nonNullableReturnType = returnType.withNullability(false)
    if (nonNullableReturnType.isSubtypeOf(Number::class.starProjectedType) ||
        nonNullableReturnType.isSubtypeOf(String::class.starProjectedType) ||
        nonNullableReturnType.isSubtypeOf(Boolean::class.starProjectedType)
    ) {
        jsonElement = primitive(value)
    } else if (nonNullableReturnType.isSubtypeOf(Collection::class.starProjectedType)) {
        val arr = array()
        Collection::class.cast(value).forEach {
            if (it != null) {
                arr.add(jsonElement(it::class.starProjectedType, it))
            }
        }
        jsonElement = arr
    } else if (nonNullableReturnType.isSubtypeOf(Map::class.starProjectedType)) {
        val newObj = JsonObject()
        Map::class.cast(value).forEach {
            if (it.key != null) {
                if (it.value != null) {
                    newObj.put(toUnderscore(it.key.toString()), jsonElement(it::class.starProjectedType, it.value))
                }
            }
        }
        jsonElement = newObj
    } else {
        val newObj = JsonObject()
        newObj.fill(value)
        jsonElement = newObj
    }
    return jsonElement
}
