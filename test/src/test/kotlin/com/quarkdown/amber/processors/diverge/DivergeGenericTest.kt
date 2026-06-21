package com.quarkdown.amber.processors.diverge

import com.quarkdown.amber.annotations.Diverge
import kotlin.test.Test
import kotlin.test.assertEquals

class GenericBox<T>(
    @Diverge val value: T,
    val label: String,
)

class Bounded<T : Number>(
    @Diverge val value: T,
)

class DivergeGenericTest {
    @Test
    fun `diverges a generic class`() {
        val box = GenericBox(value = "hello", label = "greeting")
        val updated = box.diverge(value = "world")
        assertEquals("world", updated.value)
        assertEquals("greeting", updated.label)
    }

    @Test
    fun `diverges a generic class with a bound`() {
        val bounded: Bounded<Int> = Bounded(value = 5)
        val updated = bounded.diverge(value = 10)
        assertEquals(10, updated.value)
    }
}
