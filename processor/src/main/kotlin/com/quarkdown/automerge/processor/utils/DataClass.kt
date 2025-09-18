package com.quarkdown.automerge.processor.utils

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

/** True if this class is declared as a Kotlin data class. */
val KSClassDeclaration.isDataClass: Boolean
    get() = Modifier.DATA in this.modifiers

/**
 * Returns the properties that belong to the primary constructor of the data class, keeping
 * their declaration order. If the receiver is not a data class, an empty list is returned.
 */
val KSClassDeclaration.dataClassProperties: List<KSPropertyDeclaration>
    get() {
        if (!isDataClass) return emptyList()

        val constructorProperties: Set<String> =
            primaryConstructor
                ?.parameters
                ?.mapNotNull { it.name?.asString() }
                ?.toSet()
                ?: emptySet()

        return getDeclaredProperties()
            .filter { p -> p.simpleName.asString() in constructorProperties }
            .toList()
    }
