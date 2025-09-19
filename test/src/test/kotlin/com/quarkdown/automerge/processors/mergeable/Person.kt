package com.quarkdown.automerge.processors.mergeable

import com.quarkdown.automerge.annotations.Mergeable

@Mergeable
data class Person(
    val name: String?,
    val age: Int?,
)
