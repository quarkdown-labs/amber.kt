package com.quarkdown.automerge.processors.nesteddata

import com.quarkdown.automerge.annotations.NestedData

@NestedData
data class Config(
    val id: Int,
    val app: AppConfig,
    val notifications: NotificationConfig,
    val io: IoConfig,
)

data class AppConfig(
    val theme: String,
)

data class NotificationConfig(
    val email: Boolean,
    val push: Boolean,
)

data class IoConfig(
    val source: SourceIoConfig,
    val output: OutputIoConfig?,
)

data class SourceIoConfig(
    val sourceDir: String,
)

data class OutputIoConfig(
    val outputDir: String,
)
