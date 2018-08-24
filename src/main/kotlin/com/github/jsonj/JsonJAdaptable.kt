package com.github.jsonj

interface JsonJAdaptable {
    fun asJsonObject(): JsonObject {
        return JsonObject().fill(this)
    }
}