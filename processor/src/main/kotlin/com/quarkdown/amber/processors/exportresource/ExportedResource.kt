package com.quarkdown.amber.processors.exportresource

/**
 * A resource read at compile time, ready to be exported as a property of the annotated class.
 *
 * @param propertyName Name of the property the content will be exposed through.
 * @param path Path of the resource, as written in the annotation. Kept for documentation purposes.
 * @param content Text content of the resource.
 */
data class ExportedResource(
    val propertyName: String,
    val path: String,
    val content: String,
) {
    init {
        check(propertyName.isValidPropertyName) {
            "'$propertyName' cannot name the property exporting resource '$path': " +
                "please supply a valid Kotlin identifier via @ExportResource(name = ...)"
        }
    }
}

/** Kotlin hard keywords, which cannot name a declaration. */
private val KEYWORDS =
    setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
        "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
        "true", "try", "typealias", "typeof", "val", "var", "when", "while",
    )

/** Shape of an identifier that needs no backtick escaping. */
private val IDENTIFIER = Regex("[\\p{L}_][\\p{L}\\p{N}_]*")

/** Whether this string can name a generated property as it is. */
private val String.isValidPropertyName: Boolean
    get() = matches(IDENTIFIER) && this !in KEYWORDS

/** Characters that separate words in a resource file name. */
private val WORD_SEPARATORS = Regex("[^A-Za-z0-9]+")

/**
 * Property name inferred from this resource path when the annotation does not supply one:
 * the file name, stripped of its directories and extension, camel-cased.
 * For instance, `/dir/my_file-name.txt` yields `myFileName`.
 *
 * The outcome is not guaranteed to be a usable name: [ExportedResource] validates it, as it does
 * for names supplied by hand.
 */
val String.inferredPropertyName: String
    get() =
        substringAfterLast('/')
            .substringBeforeLast('.')
            .split(WORD_SEPARATORS)
            .filter { it.isNotEmpty() }
            .mapIndexed { index, word ->
                when (index) {
                    0 -> word.replaceFirstChar(Char::lowercaseChar)
                    else -> word.replaceFirstChar(Char::uppercaseChar)
                }
            }.joinToString("")
