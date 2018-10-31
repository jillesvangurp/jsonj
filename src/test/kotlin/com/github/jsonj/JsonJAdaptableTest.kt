package com.github.jsonj

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import java.math.BigDecimal
import java.math.BigInteger

enum class MyEnum { FOO, BAR }

data class Foo(
    val message: String,
    val message2: String?,
    val message3: String?,
    val value: Int,
    val value2: Long,
    val value3: Float,
    val value4: Double,
    val value5: BigInteger,
    val value6: BigDecimal,
    val maybe: Boolean,
    val numnun: MyEnum,
    val jsonObject: JsonObject
) : JsonJAdaptable {
    val synthetic = { message.reversed() }
    val lzy by lazy { message.reversed() }
}

class JsonJAdaptableTest {
    @Test
    fun shouldConvert() {
        val foo = Foo(
            message = "meaning",
            message2 = "foo",
            message3 = null,
            value = 42,
            value2 = 42,
            value3 = 42.0f,
            value4 = 42.0,
            value5 = BigInteger.valueOf(2).pow(128),
            value6 = BigDecimal.valueOf(42.666).pow(100),
            maybe = true,
            numnun = MyEnum.FOO,
            jsonObject = obj { field("foo", "bar") }
        )
        val json = foo.asJsonObject()
        println(json.prettyPrint())
        // extra fields get ignored
        json.put("idontexistasafield", 42)
        val reconstructed = json.construct(Foo::class)
        assertThat(foo.message2).isNotBlank()
        assertThat(foo.message3).isNull()
        assertThat(json.get("message3")).isNull()
        assertThat(reconstructed.message2).isEqualTo("foo")
        assertThat(reconstructed.message3).isNull()
        assertThat(reconstructed).isEqualTo(foo)
    }
}