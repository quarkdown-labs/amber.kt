package com.example.sample

import kotlin.test.Test
import kotlin.test.assertEquals

class PersonTest {
    @Test
    fun testMerge() {
        val a = Person(name = null, age = 20)
        val b = Person(name = "Jane", age = null)
        val merged = a.merge(b)
        assertEquals("Jane", merged.name)
        assertEquals(20, merged.age)
    }
}
