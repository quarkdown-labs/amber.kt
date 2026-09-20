package com.quarkdown.amber.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises, from common code, the declarations Amber generates out of common code: the very point
 * of multiplatform support is that these resolve on every target.
 */
class AmberCommonTest {
    @Test
    fun `deep-copies a nested data class`() {
        val config = Config(AppConfig(theme = "light"))
        assertEquals("dark", config.deepCopy(appTheme = "dark").app.theme)
    }

    @Test
    fun `merges a data class`() {
        val merged = Preferences(theme = "dark").merge(Preferences(theme = "light", fontSize = 16))
        assertEquals("dark", merged.theme)
        assertEquals(16, merged.fontSize)
    }

    @Test
    fun `diverges a class`() {
        val person = Person("Alice", 30).diverge(age = 31)
        assertEquals("Alice", person.name)
        assertEquals(31, person.age)
    }

    @Test
    fun `exports a resource`() {
        assertEquals("Hello from a common resource.", Assets.greeting)
    }
}
