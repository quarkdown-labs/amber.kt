package com.quarkdown.amber.processors.diverge

import com.quarkdown.amber.annotations.Diverge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Labeled(
    @Diverge val label: String?,
    val id: Int,
)

class DivergeNullableTest {
    @Test
    fun `diverge replaces null with a value`() {
        val labeled = Labeled(label = null, id = 1)
        val updated = labeled.diverge(label = "named")
        assertEquals("named", updated.label)
        assertEquals(1, updated.id)
    }

    @Test
    fun `diverge accepts null override`() {
        val labeled = Labeled(label = "named", id = 1)
        val updated = labeled.diverge(label = null)
        assertNull(updated.label)
    }

    @Test
    fun `diverge with no args preserves null`() {
        val labeled = Labeled(label = null, id = 1)
        val copy = labeled.diverge()
        assertNull(copy.label)
        assertEquals(1, copy.id)
    }
}
