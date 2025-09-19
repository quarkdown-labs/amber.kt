package com.quarkdown.amber.processor.utils

import com.google.devtools.ksp.symbol.KSType

/**
 * Returns a codegen-ready string representation of the [KSType], including its generic type
 * arguments if present.
 *
 * For example, a type representing `Map<String, List<Int>>` would return the string
 * `"kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.Int>>"`.
 */
val KSType.formattedName: String
    get() {
        fun formatTypeName(type: KSType): String {
            val base =
                type.declaration.qualifiedName?.asString()
                    ?: type.declaration.simpleName.asString()

            if (type.arguments.isEmpty()) {
                return base
            }

            val args =
                type.arguments.map { arg ->
                    arg.type?.resolve()?.let { formatTypeName(it) } ?: "*"
                }

            return "$base<${args.joinToString(", ")}>"
        }

        return formatTypeName(this)
    }
