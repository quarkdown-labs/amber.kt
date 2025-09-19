package com.quarkdown.amber.processors.nesteddata

import com.quarkdown.amber.annotations.NestedData

@NestedData
data class Alphabet(
    val letters: List<Char>,
    val anyLetters: List<*>?,
    val lettersToIndex: Map<Char, Int>,
)
