package com.quarkdown.automerge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PersonTest {
    @Test
    fun `merges nullable from fallback when primary is null`() {
        val a = Person(name = null, age = 20)
        val b = Person(name = "Jane", age = null)
        val merged = a.merge(b)
        assertEquals("Jane", merged.name)
        assertEquals(20, merged.age)
    }

    @Test
    fun `keeps primary when both non-null`() {
        val a = Person(name = "John", age = 30)
        val b = Person(name = "Jane", age = 25)
        val merged = a.merge(b)
        assertEquals("John", merged.name)
        assertEquals(30, merged.age)
    }

    @Test
    fun `keeps primary when primary non-null and fallback null`() {
        val a = Person(name = "John", age = 30)
        val b = Person(name = null, age = null)
        val merged = a.merge(b)
        assertEquals("John", merged.name)
        assertEquals(30, merged.age)
    }

    @Test
    fun `null when both null`() {
        val a = Person(name = null, age = null)
        val b = Person(name = null, age = null)
        val merged = a.merge(b)
        assertNull(merged.name)
        assertNull(merged.age)
    }

    @Test
    fun `mixed fields merge independently`() {
        val a = Person(name = null, age = 40)
        val b = Person(name = "Jane", age = null)
        val merged = a.merge(b)
        assertEquals("Jane", merged.name)
        assertEquals(40, merged.age)
    }

    @Test
    fun `chain merge picks first non-null from left to right`() {
        val a = Person(name = null, age = null)
        val b = Person(name = null, age = 18)
        val c = Person(name = "Cara", age = null)
        val merged = a.merge(b).merge(c)
        // First merge: name=null, age=18
        // Second merge with c: name becomes "Cara" (fallback), age remains 18 (primary non-null)
        assertEquals("Cara", merged.name)
        assertEquals(18, merged.age)
    }

    @Test
    fun `originals are unchanged`() {
        val a = Person(name = null, age = 20)
        val b = Person(name = "Jane", age = 30)
        val merged = a.merge(b)
        assertEquals(null, a.name)
        assertEquals(20, a.age)
        assertEquals("Jane", b.name)
        assertEquals(30, b.age)
        assertEquals("Jane", merged.name)
        assertEquals(20, merged.age)
    }
}
