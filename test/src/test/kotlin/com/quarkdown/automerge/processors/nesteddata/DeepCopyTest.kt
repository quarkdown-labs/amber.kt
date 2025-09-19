package com.quarkdown.automerge.processors.nesteddata

import kotlin.test.Test
import kotlin.test.assertEquals

class DeepCopyTest {
    private val config =
        Config(
            id = 1,
            app = AppConfig(theme = "light"),
            notifications = NotificationConfig(email = true, push = true),
            io =
                IoConfig(
                    source = SourceIoConfig(sourceDir = "/src"),
                    output = OutputIoConfig(outputDir = "/out"),
                ),
        )

    @Test
    fun `copies non-nested values`() {
        val new = config.deepCopy(id = 2)
        assertEquals(2, new.id)
        assertEquals(config.app, new.app)
        assertEquals(config.notifications, new.notifications)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies one-level nested values`() {
        val new = config.deepCopy(app_theme = "dark")
        assertEquals(1, new.id)
        assertEquals("dark", new.app.theme)
        assertEquals(config.notifications, new.notifications)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies multiple one-level nested values of different fields`() {
        val new =
            config.deepCopy(
                app_theme = "dark",
                notifications_email = false,
            )
        assertEquals(1, new.id)
        assertEquals("dark", new.app.theme)
        assertEquals(false, new.notifications.email)
        assertEquals(true, new.notifications.push)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies multiple one-level nested values of the same field`() {
        val new =
            config.deepCopy(
                notifications_email = false,
                notifications_push = true,
            )
        assertEquals(1, new.id)
        assertEquals(config.app, new.app)
        assertEquals(false, new.notifications.email)
        assertEquals(true, new.notifications.push)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies two-level nested values`() {
        val new = config.deepCopy(io_source_sourceDir = "/newSrc")
        assertEquals(1, new.id)
        assertEquals(config.app, new.app)
        assertEquals(config.notifications, new.notifications)
        assertEquals("/newSrc", new.io.source.sourceDir)
        assertEquals("/out", new.io.output?.outputDir)
    }

    @Test
    fun `copies values from different levels of nesting`() {
        val new =
            config.deepCopy(
                id = 2,
                app_theme = "dark",
                io_output_outputDir = "/newOut",
            )
        assertEquals(2, new.id)
        assertEquals("dark", new.app.theme)
        assertEquals(config.notifications, new.notifications)
        assertEquals("/src", new.io.source.sourceDir)
        assertEquals("/newOut", new.io.output?.outputDir)
    }
}
