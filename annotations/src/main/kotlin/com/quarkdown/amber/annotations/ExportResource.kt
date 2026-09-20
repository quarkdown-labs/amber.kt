package com.quarkdown.amber.annotations

/**
 * Embeds the content of a classpath resource into the source code at compile time, exposing it
 * as a generated `String` extension property on the annotated class or object.
 *
 * The resource is read by the Amber KSP processor while compiling, and its text is inlined in the
 * generated file: no file or classloader access happens at runtime.
 *
 * Path resolution mirrors `Class.getResource`:
 * - An absolute path (leading `/`) is resolved against the resource roots of the project.
 * - A relative path is resolved against the directory matching the annotated class's package.
 *
 * The annotation is repeatable: each occurrence contributes one property to the same generated file.
 *
 * ```kotlin
 * @ExportResource("/templates/page.html")
 * @ExportResource("/templates/page.css", name = "style")
 * object Templates
 *
 * Templates.page      // content of /templates/page.html, inferred from the file name
 * Templates.style     // content of /templates/page.css
 * ```
 *
 * @param path Path of the resource to export, as seen from the classpath.
 * @param name Name of the generated property. If blank, it is inferred by camel-casing the
 *             resource's file name without its extension (`my_file-name.txt` -> `myFileName`).
 *
 * Constraints:
 * - The resource must exist at compile time and contain UTF-8 text.
 * - Two resources exported by the same class must not resolve to the same property name.
 *
 * Notes:
 * - Annotation retention is SOURCE; it does not exist at runtime.
 * - The generated file is `<ClassName>_ExportResource.kt` in the class's package.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class ExportResource(
    val path: String,
    val name: String = "",
)
