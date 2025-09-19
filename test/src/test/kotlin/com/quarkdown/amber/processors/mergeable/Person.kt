package com.quarkdown.amber.processors.mergeable

import com.quarkdown.amber.annotations.Mergeable

@Mergeable
data class Person(
    val name: String?,
    val age: Int?,
)
