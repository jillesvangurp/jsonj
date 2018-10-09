package com.github.jsonj

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import java.math.BigDecimal
import java.math.BigInteger

enum class MyEnum { FOO, BAR }

data class Foo(
    val message: String,
    val value: Int,
    val value2: Long,
    val value3: Float,
    val value4: Double,
    val value5: BigInteger,
    val value6: BigDecimal,
    val maybe: Boolean,
    val numnun: MyEnum
) : JsonJAdaptable

class JsonJAdaptableTest {
    @Test
    fun shouldConvert() {
        val foo = Foo(
            "meaning",
            42,
            42,
            42.0f,
            42.0,
            BigInteger.valueOf(2).pow(128),
            BigDecimal.valueOf(42.666).pow(100),
            true,
            MyEnum.FOO
        )
        val json = foo.asJsonObject()
        assertThat(json.construct(Foo::class)).isEqualTo(foo)
    }
}