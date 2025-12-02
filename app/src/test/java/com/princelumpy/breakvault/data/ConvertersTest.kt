package com.princelumpy.breakvault.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromStringList converts list to JSON string`() {
        val list = listOf("Move1", "Move2")
        val result = converters.fromStringList(list)
        assertEquals("[\"Move1\",\"Move2\"]", result)
    }

    @Test
    fun `toStringList converts JSON string to list`() {
        val json = "[\"Move1\",\"Move2\"]"
        val result = converters.toStringList(json)
        assertEquals(listOf("Move1", "Move2"), result)
    }

    @Test
    fun `toStringList returns empty list on invalid JSON`() {
        val invalidJson = "{invalid}"
        val result = converters.toStringList(invalidJson)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `toStringList returns empty list on empty string`() {
        val result = converters.toStringList("")
        assertEquals(emptyList<String>(), result)
    }
}
