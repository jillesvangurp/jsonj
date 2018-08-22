package com.github.jsonj

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jsonj.tools.JsonBuilder.`object`
import com.github.jsonj.tools.JsonBuilder.array
import com.github.jsonj.tools.JsonBuilder.field
import com.github.jsonj.tools.JsonBuilder.fromObject
import com.github.jsonj.tools.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ObjectMapperTest {
    lateinit var objectMapper: ObjectMapper
    lateinit var parser: JsonParser

    @BeforeMethod
    fun before() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        parser = JsonParser()
    }

    @DataProvider
    fun jsonElements(): Array<Array<Any>> {
        return arrayOf(
            arrayOf<Any>(
                `object`(
                    field("meaning_of_life", 42),
                    field("a", 42.0),
                    field("b", true),
                    field("c", array(42, "foo", 3.14, true, null)),
                    field("d", `object`(field("a", 1)))
                )
            ),
            arrayOf<Any>(array(42, "foo", 3.14, true, null)),
            arrayOf<Any>(fromObject(42)),
            arrayOf<Any>(fromObject("hello world"))
        )
    }

    @Test(dataProvider = "jsonElements")
    fun shouldSurviveSerializeDeserializeAndBeEqual(element: JsonElement) {
        val serialized = objectMapper.writeValueAsString(element)
        assertThat(parser.parse(serialized)).isEqualTo(element)
        val deSerialized = objectMapper.readValue<JsonElement>(serialized, JsonElement::class.java)
        assertThat(deSerialized).isEqualTo(element)

        if (element.isArray) {
            val e = objectMapper.readValue<JsonArray>(serialized, JsonArray::class.java)
            assertThat(e).isEqualTo(element)
        }

        if (element.isObject) {
            val e = objectMapper.readValue<JsonObject>(serialized, JsonObject::class.java)
            assertThat<String, JsonElement>(e).isEqualTo(element)
        }

        if (element.isPrimitive) {
            val e = objectMapper.readValue<JsonPrimitive>(serialized, JsonPrimitive::class.java)
            assertThat(e).isEqualTo(element)
        }
    }

    data class Foo(
        var attribute: String? = null,
        var anotherAttribute: Int = 0,
        var nestedJsonj: JsonObject? = null
    )

    @Test
    fun shouldMixJsonjWithPojos() {
        val foo = Foo()
        foo.attribute = "Hi wrld"
        foo.anotherAttribute = 42
        val nested = `object`(
            field("meaning_of_life", 42),
            field("a", 42.0),
            field("b", true),
            field("c", array(42, "foo", 3.14, true, null)),
            field("d", `object`(field("a", 1)))
        )
        foo.nestedJsonj = nested

        val serialized = objectMapper.writeValueAsString(foo)
        val `object` = parser.parseObject(serialized)
        assertThat(`object`.getString("attribute")).isEqualTo("Hi wrld")
        assertThat(`object`.getInt("anotherAttribute")).isEqualTo(42)
        assertThat(`object`.getObject("nestedJsonj")).isEqualTo(nested)
    }

    data class Bar(@JsonIgnore val obj: JsonObject = JsonObject(), val foo: Int, val bar: String) : JsonDataObject {
        override fun getJsonObject(): JsonObject {
            return obj
        }
    }

    @Test
    fun `should serialize and deserialize JsonDataObject with non jsonj fields using objectMapper`() {
        val bar = Bar(`object`(field("message", "hi wrld")), 42, "test")
        val serialized = objectMapper.writeValueAsString(bar)
        assertThat(serialized).isEqualTo("""{"foo":42,"bar":"test","message":"hi wrld"}""")
        val bar2 = objectMapper.readValue<Bar>(serialized, Bar::class.java)
        assertThat(objectMapper.writeValueAsString(bar2)).isEqualTo(serialized)
    }
}
