package com.quarkdown.amber.processors.diverge

import com.quarkdown.amber.annotations.Diverge
import kotlin.test.Test
import kotlin.test.assertEquals

class Person(
    val name: String,
    @Diverge val age: Int,
    val city: String,
)

class DivergeTest {
    @Test
    fun `diverges only the marked param`() {
        val person = Person("Alice", 30, "New York")
        val updated = person.diverge(age = 31)
        assertEquals("Alice", updated.name)
        assertEquals(31, updated.age)
        assertEquals("New York", updated.city)
    }

    @Test
    fun `diverge with no args returns equal copy`() {
        val person = Person("Alice", 30, "New York")
        val copy = person.diverge()
        assertEquals(person.name, copy.name)
        assertEquals(person.age, copy.age)
        assertEquals(person.city, copy.city)
    }
}
