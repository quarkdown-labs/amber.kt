package com.example.sample

import com.quarkdown.automerge.annotations.AutoMerge

@AutoMerge
data class Person(
    val name: String?,
    val age: Int?,
)
