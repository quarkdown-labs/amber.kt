package com.quarkdown.amber.kmp

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.amber.annotations.ExportResource
import com.quarkdown.amber.annotations.Mergeable
import com.quarkdown.amber.annotations.NestedData

@NestedData
data class Config(
    val app: AppConfig,
)

data class AppConfig(
    val theme: String,
)

@Mergeable
data class Preferences(
    val theme: String? = null,
    val fontSize: Int? = null,
)

class Person(
    val name: String,
    @Diverge val age: Int,
)

@ExportResource("/kmp/greeting.txt")
object Assets
