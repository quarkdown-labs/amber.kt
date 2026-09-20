package com.quarkdown.amber.processors.exportresource

import java.io.File

/**
 * Name of the KSP option that lists the resource roots of the project, separated by
 * [File.pathSeparator]. The Amber Gradle plugin fills it in from the project's source sets.
 */
const val RESOURCE_ROOTS_OPTION: String = "amber.resourceRoots"

/**
 * Locates resource files on disk while compiling, since KSP itself offers no access to the
 * resources of the module being processed: their roots are supplied by the build, through the
 * [RESOURCE_ROOTS_OPTION] option.
 */
class ResourceLocator(
    options: Map<String, String>,
) {
    private val roots: List<File> =
        options[RESOURCE_ROOTS_OPTION]
            .orEmpty()
            .split(File.pathSeparator)
            .filter { it.isNotBlank() }
            .map(::File)

    /**
     * Finds the file a resource [path] points to, mirroring `Class.getResource` semantics: an
     * absolute path is resolved against the resource roots, a relative one against the directory
     * matching [packageName].
     *
     * @param path Path of the resource, absolute (leading `/`) or relative to [packageName].
     * @param packageName Package of the annotated symbol, base of relative paths.
     * @return The located, existing file.
     * @throws IllegalStateException if no root contains the resource, or if the path escapes its root.
     */
    fun locate(
        path: String,
        packageName: String,
    ): File {
        val relativePath = path.toRootRelativePath(packageName)
        val (root, file) =
            roots
                .map { it.canonicalFile to it.resolve(relativePath).canonicalFile }
                .firstOrNull { (_, file) -> file.isFile }
                ?: error("resource '$path' not found. Looked into: ${roots.joinToString().ifEmpty { "no resource root" }}")

        // A path may well climb out of its root through '..' or a symlink: only what the build
        // declares as a resource may be embedded.
        check(file.startsWith(root)) { "resource '$path' lies outside of its resource root $root" }
        return file
    }

    /** This resource path, made relative to a resource root: relative paths are prefixed with their package directory. */
    private fun String.toRootRelativePath(packageName: String): String =
        when {
            startsWith("/") -> removePrefix("/")
            packageName.isEmpty() -> this
            else -> packageName.replace('.', '/') + "/" + this
        }
}
