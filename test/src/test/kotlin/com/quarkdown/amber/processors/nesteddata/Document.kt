package com.quarkdown.amber.processors.nesteddata

import com.quarkdown.amber.annotations.NestedData

@NestedData
data class Document(
    val layout: Layout,
)

data class Layout(
    val margins: Margins? = null,
)

data class Margins(
    val top: Size,
    val bottom: Size,
    val left: Size,
    val right: Size,
)

data class Size(
    val value: Int,
)
