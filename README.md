# kotlin-automerge

AutoMerge is a tiny, compile-time, reflectionless Kotlin library that makes it possible to merge instances of the same data class,
in an immutable and user-friendly way.

The library was developed out of necessity for the [Quarkdown typesetting system](https://github.com/iamgio/quarkdown):
it's currently available only for Kotlin/JVM, though Kotlin Multiplatform would be easy to support.  
Contributions towards multiplatform support are welcome.

## Installation

```kotlin
plugins {
    id("com.quarkdown.automerge") version "<version>"
}

repositories {
    mavenCentral()
}
```

Also make sure Maven central is enabled as a plugin repository in your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

The plugin will automatically include the compile-time `processor` and the runtime `annotations` modules,
along with linking up the generated sources.

## Example

The library's main purpose is to abstract away from rigid defaults, 

```kotlin
import com.quarkdown.automerge.annotations.Mergeable

@Mergeable
data class Preferences(
    val theme: String? = null, // If null, use system default
    val fontSize: Int? = null, // If null, use system default
    val autoSaveDelay: Int? = null, // If null, disable auto-save
)

object DefaultPreferencesFactory {
    fun mobile() = Preferences(theme = "light")
    fun desktop() = Preferences(fontSize = 16, autoSaveDelay = 30)
}

fun main() {
    val default = DefaultPreferencesFactory.desktop()
  
    // Assume user preferences are loaded from a config file.
    val user = Preferences(theme = "dark", autoSaveDelay = 10)

    // Merging user preferences with defaults. User values take precedence.
    val merged: Preferences = user.merge(default)
    println(merged) // Preferences(theme=dark, fontSize=16, autoSaveDelay=30)
```
