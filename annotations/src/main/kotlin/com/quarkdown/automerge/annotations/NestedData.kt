package com.quarkdown.automerge.annotations

/**
 * Marks a Kotlin data class to generate a `copyNested(...)` extension that allows updating
 * nested properties using underscore-separated parameter names.
 *
 * Example: for `data class Config(val app: App(val theme: String))`, the processor will generate
 * a function `fun Config.copyNested(app_theme: Any? = null, app: App = this.app, ...): Config` so
 * you can call `config.copyNested(app_theme = "dark")` or `config.copyNested(app_theme = config.app.copy(theme = "dark"))`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NestedData
