package com.quarkdown.amber.processors.diverge

import com.quarkdown.amber.annotations.Diverge
import kotlin.test.Test
import kotlin.test.assertEquals

class Box
    @Diverge
    constructor(
        val width: Int,
        val height: Int,
    )

class DivergeOnConstructorTest {
    @Test
    fun `constructor-level diverge marks every param`() {
        val box = Box(width = 10, height = 20)
        val resized = box.diverge(width = 100, height = 200)
        assertEquals(100, resized.width)
        assertEquals(200, resized.height)
    }

    @Test
    fun `constructor-level diverge supports partial override`() {
        val box = Box(width = 10, height = 20)
        val taller = box.diverge(height = 999)
        assertEquals(10, taller.width)
        assertEquals(999, taller.height)
    }
}
