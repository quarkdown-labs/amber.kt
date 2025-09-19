package com.quarkdown.automerge.processors.nesteddata

import com.quarkdown.automerge.annotations.NestedData

@NestedData
data class Config(
    val id: Int,
    val app: AppConfig,
    val fallbackApp: AppConfig,
    val notifications: NotificationConfig,
    val io: IoConfig,
    val a: A,
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

data class A(
    val b: B,
) {
    data class B(
        val c: C,
    ) {
        data class C(
            val d: D?,
        ) {
            data class D(
                val e: E?,
            ) {
                data class E(
                    val value: Int,
                )
            }
        }
    }
}
