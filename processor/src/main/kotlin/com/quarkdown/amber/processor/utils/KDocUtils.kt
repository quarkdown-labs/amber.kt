package com.quarkdown.amber.processor.utils

/**
 * Utilities for KDoc.
 */
object KDocUtils {
    /**
     * Generates KDoc comment from the given content.
     * Each line in the content will be prefixed with ` * ` and wrapped in `/** ... */`.
     * @param content The content of the KDoc comment.
     * @return The formatted KDoc comment.
     */
    fun generate(content: String): String {
        val lines = content.trimIndent().lines()
        return buildString {
            appendLine("/**")
            for (line in lines) {
                appendLine(" * $line".trimEnd())
            }
            append(" */")
        }
    }
}
