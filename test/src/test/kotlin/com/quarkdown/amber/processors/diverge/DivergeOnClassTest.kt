package com.quarkdown.amber.processors.diverge

import com.quarkdown.amber.annotations.Diverge
import kotlin.test.Test
import kotlin.test.assertEquals

@Diverge
class Config(
    val host: String,
    val port: Int,
)

class DivergeOnClassTest {
    @Test
    fun `class-level diverge marks every param`() {
        val config = Config(host = "localhost", port = 8080)
        val updated = config.diverge(port = 9090)
        assertEquals("localhost", updated.host)
        assertEquals(9090, updated.port)
    }

    @Test
    fun `class-level diverge with no args returns equal copy`() {
        val config = Config(host = "localhost", port = 8080)
        val copy = config.diverge()
        assertEquals(config.host, copy.host)
        assertEquals(config.port, copy.port)
    }
}
