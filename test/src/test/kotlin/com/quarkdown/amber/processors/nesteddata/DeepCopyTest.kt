package com.quarkdown.amber.processors.nesteddata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepCopyTest {
    private val config =
        Config(
            id = 1,
            app = AppConfig(theme = "light"),
            fallbackApp = AppConfig(theme = "dark"),
            notifications = NotificationConfig(email = true, push = true),
            io =
                IoConfig(
                    source = SourceIoConfig(sourceDir = "/src"),
                    output = OutputIoConfig(outputDir = "/out"),
                ),
            a =
                A(
                    A.B(
                        A.B.C(
                            A.B.C.D(
                                A.B.C.D
                                    .E(value = 42),
                            ),
                        ),
                    ),
                ),
        )

    @Test
    fun `deep-copies`() {
        val new = config.deepCopy()
        assertEquals(config, new)
        assertEquals(config.app, new.app)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(config.notifications, new.notifications)
        assertEquals(config.io, new.io)
        assertEquals(config.a, new.a)
        // Ensure different instances
        assert(config !== new)
        assert(config.app !== new.app)
        assert(config.fallbackApp !== new.fallbackApp)
        assert(config.notifications !== new.notifications)
        assert(config.io !== new.io)
        assert(config.io.source !== new.io.source)
        assert(config.io.output !== new.io.output)
        assert(config.a !== new.a)
    }

    @Test
    fun `copies non-nested values`() {
        val new = config.deepCopy(id = 2)
        assertEquals(2, new.id)
        assertEquals(config.app, new.app)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(config.notifications, new.notifications)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies one-level nested values`() {
        val new = config.deepCopy(appTheme = "dark")
        assertEquals(1, new.id)
        assertEquals("dark", new.app.theme)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(config.notifications, new.notifications)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies multiple one-level nested values of different fields`() {
        val new =
            config.deepCopy(
                appTheme = "dark",
                notificationsEmail = false,
            )
        assertEquals(1, new.id)
        assertEquals("dark", new.app.theme)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(false, new.notifications.email)
        assertEquals(true, new.notifications.push)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies multiple one-level nested values of the same field`() {
        val new =
            config.deepCopy(
                notificationsEmail = false,
                notificationsPush = true,
            )
        assertEquals(1, new.id)
        assertEquals(config.app, new.app)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(false, new.notifications.email)
        assertEquals(true, new.notifications.push)
        assertEquals(config.io, new.io)
    }

    @Test
    fun `copies two-level nested values`() {
        val new = config.deepCopy(ioSourceSourceDir = "/newSrc")
        assertEquals(1, new.id)
        assertEquals(config.app, new.app)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(config.notifications, new.notifications)
        assertEquals("/newSrc", new.io.source.sourceDir)
        assertEquals("/out", new.io.output?.outputDir)
    }

    @Test
    fun `copies values from different levels of nesting`() {
        val new =
            config.deepCopy(
                id = 2,
                appTheme = "dark",
                ioOutputOutputDir = "/newOut",
            )
        assertEquals(2, new.id)
        assertEquals("dark", new.app.theme)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(config.notifications, new.notifications)
        assertEquals("/src", new.io.source.sourceDir)
        assertEquals("/newOut", new.io.output?.outputDir)
    }

    @Test
    fun `copies deeply nested values`() {
        val new = config.deepCopy(aBCDEValue = 100)
        assertEquals(1, new.id)
        assertEquals(config.app, new.app)
        assertEquals(config.fallbackApp, new.fallbackApp)
        assertEquals(config.notifications, new.notifications)
        assertEquals(config.io, new.io)
        assertEquals(
            100,
            new.a.b.c.d
                ?.e
                ?.value,
        )
    }

    @Test
    fun `copies deeply nested, null value`() {
        val new = config.deepCopy(aBCD = null)
        assertEquals(null, new.a.b.c.d)
    }

    @Test
    fun `discards deeply nested, non-null null value`() {
        val new = config.deepCopy(aBCDEValue = null)
        assertEquals(config.a, new.a)
        assertEquals(config.a.b, new.a.b)
        assertEquals(config.a.b.c, new.a.b.c)
        assertEquals(config.a.b.c.d, new.a.b.c.d)
        assertEquals(
            config.a.b.c.d
                ?.e,
            new.a.b.c.d
                ?.e,
        )
        assertEquals(
            config.a.b.c.d
                ?.e
                ?.value,
            new.a.b.c.d
                ?.e
                ?.value,
        )
    }

    @Test
    fun `copies generic properties`() {
        val alphabet =
            Alphabet(
                letters = listOf('A', 'B', 'C'),
                anyLetters = listOf('D', 'E', 'F'),
                lettersToIndex = mapOf('G' to 7, 'H' to 8, 'I' to 9),
            )
        val new =
            alphabet.deepCopy(
                letters = listOf('X', 'Y', 'Z'),
                anyLetters = listOf('1', '2', '3'),
                lettersToIndex = mapOf('4' to 4, '5' to 5, '6' to 6),
            )
        assertEquals(listOf('X', 'Y', 'Z'), new.letters)
        assertEquals(listOf('1', '2', '3'), new.anyLetters)
        assertEquals(mapOf('4' to 4, '5' to 5, '6' to 6), new.lettersToIndex)
    }

    @Test
    fun `cannot copy non-nullable properties of null property`() {
        val document = Document(Layout())
        assertEquals(null, document.layout.margins)
        val new = document.deepCopy(layoutMarginsTop = Size(10))
        assertNull(
            new.layout
                .margins
                ?.top
                ?.value,
        )
    }

    @Test
    fun `copies non-nullable properties of null property if the parent is set, separately`() {
        val document =
            Document(Layout()).deepCopy(
                layoutMargins =
                    Margins(
                        top = Size(1),
                        bottom = Size(1),
                        left = Size(1),
                        right = Size(1),
                    ),
            )

        val new = document.deepCopy(layoutMarginsTop = Size(10))
        assertEquals(
            10,
            new.layout
                .margins
                ?.top
                ?.value,
        )
    }

    @Test
    fun `copies non-nullable properties of null property if the parent is set, all-in-one`() {
        val document = Document(Layout())
        val new =
            document.deepCopy(
                layoutMargins =
                    Margins(
                        top = Size(1),
                        bottom = Size(1),
                        left = Size(1),
                        right = Size(1),
                    ),
                layoutMarginsTop = Size(10),
            )
        assertEquals(
            10,
            new.layout
                .margins
                ?.top
                ?.value,
        )
    }
}
