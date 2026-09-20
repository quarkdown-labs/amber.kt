package com.quarkdown.amber.processors.exportresource

import com.quarkdown.amber.annotations.ExportResource
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

@ExportResource("package_relative.txt")
object ExportPackageRelativeResource

class ExportPackageRelativeResourceTest {
    @Test
    fun `exports a resource located in the annotated class's package`() {
        assertEquals("Sibling of the annotated class.", ExportPackageRelativeResource.packageRelative)
    }
}
