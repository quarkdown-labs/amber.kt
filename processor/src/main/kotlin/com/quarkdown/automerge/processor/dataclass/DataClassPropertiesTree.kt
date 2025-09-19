package com.quarkdown.automerge.processor.dataclass

import com.google.devtools.ksp.symbol.KSClassDeclaration

sealed interface PropertyNode {
    val name: String
    val type: String
    val nullable: Boolean
}

data class LeafPropertyNode(
    override val name: String,
    override val type: String,
    override val nullable: Boolean,
) : PropertyNode

data class DataClassPropertyNode(
    override val name: String,
    override val type: String,
    override val nullable: Boolean,
    val children: List<PropertyNode>,
) : PropertyNode

fun KSClassDeclaration.buildDataClassPropertiesTree(): DataClassPropertyNode {
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
                val children = buildChildrenForClass(typeDeclaration)
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
