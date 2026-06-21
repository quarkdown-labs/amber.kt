package com.quarkdown.amber.processors.diverge

import com.quarkdown.amber.annotations.Diverge
import kotlin.test.Test
import kotlin.test.assertEquals

class Point(
    @Diverge val x: Int,
    @Diverge val y: Int,
    val label: String,
)

class DivergeMultiplePropsTest {
    @Test
    fun `diverge one marked param at a time`() {
        val point = Point(x = 1, y = 2, label = "origin")
        val movedX = point.diverge(x = 10)
        assertEquals(10, movedX.x)
        assertEquals(2, movedX.y)
        assertEquals("origin", movedX.label)
    }

    @Test
    fun `diverge multiple marked params at once`() {
        val point = Point(x = 1, y = 2, label = "origin")
        val moved = point.diverge(x = 10, y = 20)
        assertEquals(10, moved.x)
        assertEquals(20, moved.y)
        assertEquals("origin", moved.label)
    }
}
