package com.quarkdown.automerge

import com.quarkdown.automerge.annotations.Mergeable

@Mergeable
data class Person(
    val name: String?,
    val age: Int?,
)
