package com.quarkdown.automerge.annotations

/**
 * Marks a Kotlin data class as merge-capable and triggers generation of a
 * `<T>.merge(other: T): T` extension function via the automerge KSP processor.
 *
 * What gets generated:
 * - A file named `<ClassName>_AutoMerge.kt` in the same package.
 * - An extension function with the signature: `fun <ClassName>.merge(other: <ClassName>?): <ClassName>`.
 * - Merge semantics per property:
 *   - For nullable properties: `this.prop ?: other.prop` (uses `other` as fallback).
 *   - For non-nullable properties: `this.prop` (keeps the current value).
 * - If `other` is null, the result is a copy of `this`.
 *
 * Constraints:
 * - Can only be applied to data classes. The processor will report an error otherwise.
 * - Only properties declared in the primary constructor participate in the merge (mirrors `copy`).
 *
 * Notes:
 * - Annotation retention is SOURCE; it does not exist at runtime.
 * - Target is CLASS, to be placed on the data class declaration itself.
 *
 * Example:
 * ```kotlin
 * @Mergeable
 * data class Person(
 *     val id: String,            // non-nullable: kept from `this`
 *     val nickname: String?,     // nullable: may fall back to `other.nickname`
 * )
 *
 * // Generated usage
 * val a = Person(id = "1", nickname = null)
 * val b = Person(id = "2", nickname = "gio")
 * val merged = a.merge(b) // => Person(id = "1", nickname = "gio")
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Mergeable
