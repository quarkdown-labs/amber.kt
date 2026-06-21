package com.quarkdown.amber.annotations

/**
 * Marks one or more primary-constructor parameters of a class as "divergeable" — i.e.,
 * exposed as parameters of a generated `diverge(...)` extension that returns a copy of
 * the receiver with the marked parameters replaced.
 *
 * Behaviour is similar to a data class `copy(...)`, but works on any class (not just data
 * classes) and only the *marked* parameters appear in the generated function's signature.
 *
 * Placement:
 * - On a single constructor parameter: only that parameter is divergeable.
 *   `class Person(val name: String, @Diverge val age: Int)`
 * - On the primary constructor: every parameter is divergeable.
 *   `class Person @Diverge constructor(val name: String, val age: Int)`
 * - On the class itself: every parameter of the primary constructor is divergeable.
 *   `@Diverge class Person(val name: String, val age: Int)`
 *
 * What gets generated, given marked parameters `p1, p2`:
 * ```
 * fun <ClassName>.diverge(
 *     p1: P1 = this.p1,
 *     p2: P2 = this.p2,
 * ): <ClassName> = <ClassName>(
 *     // all primary-ctor params, marked or not — non-marked ones come from `this`
 * )
 * ```
 *
 * Calling `.diverge()` with no arguments returns a structural copy of the receiver.
 *
 * Constraints:
 * - The class must have a primary constructor.
 * - All primary-constructor parameters must be declared `val` or `var`, since the
 *   generator needs to read each value from `this` to reconstruct.
 * - When applied on a constructor, that constructor must be the primary one.
 *
 * Notes:
 * - Annotation retention is SOURCE; it does not exist at runtime.
 * - The generated file is `<ClassName>_Diverge.kt` in the class's package.
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.SOURCE)
annotation class Diverge
