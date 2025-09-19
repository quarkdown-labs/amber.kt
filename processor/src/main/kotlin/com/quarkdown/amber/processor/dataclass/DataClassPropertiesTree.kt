package com.quarkdown.amber.processor.dataclass

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Represents a node in a hierarchical tree of data class properties.
 */
sealed interface PropertyNode {
    /** The name of the property as declared in the source code. */
    val name: String

    /** The fully qualified type name of the property. */
    val type: String

    /** Whether the property type is nullable (marked with '?'). */
    val nullable: Boolean
}

/**
 * Represents a leaf node in the property tree (non-data class properties).
 *
 * @property name The property name
 * @property type The fully qualified type name
 * @property nullable Whether the property is nullable
 */
data class LeafPropertyNode(
    override val name: String,
    override val type: String,
    override val nullable: Boolean,
) : PropertyNode

/**
 * Represents a data class node in the property tree with nested children.
 *
 * @property name The property name
 * @property type The fully qualified type name of the data class
 * @property nullable Whether the property is nullable
 * @property children List of child property nodes contained within this data class
 */
data class DataClassPropertyNode(
    override val name: String,
    override val type: String,
    override val nullable: Boolean,
    val children: List<PropertyNode>,
) : PropertyNode

/**
 * Builds a hierarchical tree representation of all properties in a data class.
 *
 * The function includes built-in cycle detection to prevent infinite recursion
 * when data classes reference each other.
 *
 * @return Root node of the property tree representing the entire data class structure
 */
fun KSClassDeclaration.buildDataClassPropertiesTree(): DataClassPropertyNode {
    /**
     * Recursively builds child nodes for a given data class.
     *
     * @param classDeclaration The class to analyze
     * @param visited Set of already-visited class names to prevent cycles
     * @return List of property nodes representing the class's properties
     */
    fun buildChildrenForClass(
        classDeclaration: KSClassDeclaration,
        visited: MutableSet<String> = mutableSetOf(),
    ): List<PropertyNode> {
        val className = classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()

        // Keeping track of visited class qualified names to avoid infinite recursion.
        if (!visited.add(className)) return emptyList()

        return classDeclaration.dataClassProperties.map { property ->
            val propertyType = property.type.resolve()
            val typeDeclaration = propertyType.declaration
            val propertyTypeName = propertyType.declaration.qualifiedName?.asString() ?: propertyType.declaration.simpleName.asString()
            val propertyName = property.simpleName.asString()
            val isNullable = propertyType.isMarkedNullable

            if (typeDeclaration is KSClassDeclaration && typeDeclaration.isDataClass) {
                // Recursively build children for nested data classes.
                val children = buildChildrenForClass(typeDeclaration, visited.toMutableSet())
                DataClassPropertyNode(
                    name = propertyName,
                    type = propertyTypeName,
                    nullable = isNullable,
                    children = children,
                )
            } else {
                LeafPropertyNode(
                    name = propertyName,
                    type = propertyTypeName,
                    nullable = isNullable,
                )
            }
        }
    }

    val root = this
    val rootTypeName = root.qualifiedName?.asString() ?: root.simpleName.asString()
    val children = buildChildrenForClass(root)
    return DataClassPropertyNode(root.simpleName.asString(), rootTypeName, false, children)
}

/**
 * Performs a depth-first search traversal of the property tree.
 *
 * @param skipRoot Whether to skip applying the action to the root node
 * @param action The function to apply to each visited node
 */
fun PropertyNode.dfs(
    skipRoot: Boolean = false,
    action: (PropertyNode) -> Unit,
) {
    if (!skipRoot) {
        action(this)
    }
    if (this is DataClassPropertyNode) {
        children.forEach { it.dfs(skipRoot = false, action) }
    }
}
