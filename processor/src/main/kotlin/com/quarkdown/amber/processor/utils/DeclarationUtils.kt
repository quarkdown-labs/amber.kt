package com.quarkdown.amber.processor.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

/** Declarations enclosing this one, from the outermost inwards. Empty for a top-level declaration. */
val KSDeclaration.enclosingDeclarations: List<KSDeclaration>
    get() = generateSequence(parentDeclaration) { it.parentDeclaration }.toList().reversed()

/**
 * Name this declaration is referenced by from code generated in its own package, i.e. its simple
 * name qualified by the declarations it is nested in: `Assets` when top-level, `Outer.Assets` when
 * nested in `Outer`. Unlike the simple name alone, it resolves at package scope.
 */
val KSClassDeclaration.packageScopedName: String
    get() = (enclosingDeclarations + this).joinToString(".") { it.simpleName.asString() }

/**
 * Name of the file generated for this declaration, without extension: its nesting chain joined by
 * underscores, so that same-named declarations nested in different parents do not collide.
 */
val KSClassDeclaration.generatedFileBaseName: String
    get() = (enclosingDeclarations + this).joinToString("_") { it.simpleName.asString() }
