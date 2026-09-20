package com.quarkdown.amber.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter

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

/**
 * Returns a representation of the [KSType] that can be pasted as a type *use* in generated code.
 *
 * Unlike [formattedName], it refers to type parameters by their short name and carries nullability over, e.g. `kotlin.String?`.
 */
val KSType.typeUseName: String
    get() {
        val base =
            when (val declaration = this.declaration) {
                is KSTypeParameter -> declaration.name.asString()
                else -> declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
            }

        val rendered =
            when {
                arguments.isEmpty() -> base
                else -> "$base<${arguments.joinToString(", ") { it.type?.resolve()?.typeUseName ?: "*" }}>"
            }

        return if (isMarkedNullable) "$rendered?" else rendered
    }
