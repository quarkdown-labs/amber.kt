package com.quarkdown.amber.processors.exportresource

import com.quarkdown.amber.annotations.ExportResource
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

@ExportResource("/exportresource/lorem_ipsum-content.txt", name = "lorem")
object ExportResourceSingleExplicitName

@ExportResource("/exportresource/lorem_ipsum-content.txt")
object ExportResourceSingleImplicitName

@ExportResource("/exportresource/lorem_ipsum-content.txt", name = "lorem")
@ExportResource("/exportresource/lorem_ipsum-content.txt")
object ExportMultipleResources

private const val CONTENT = "Lorem ipsum\ndolor sit amet."

class ExportResourceTest {
    @Test
    fun `exports a single txt resource by explicit name`() {
        assertEquals(CONTENT, ExportResourceSingleExplicitName.lorem)
    }

    @Test
    fun `exports a single txt resource by implicit name`() {
        assertEquals(CONTENT, ExportResourceSingleImplicitName.loremIpsumContent)
    }

    @Test
    fun `exports multiple txt resources`() {
        assertEquals(CONTENT, ExportMultipleResources.lorem)
        assertEquals(CONTENT, ExportMultipleResources.loremIpsumContent)
    }
}

object ExportResourceOuter {
    @ExportResource("/exportresource/lorem_ipsum-content.txt", name = "lorem")
    object Nested
}

class NestedExportResourceTest {
    @Test
    fun `exports a resource from a nested object`() {
        assertEquals(CONTENT, ExportResourceOuter.Nested.lorem)
    }
}
