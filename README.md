# kotlin-automerge

AutoMerge is a tiny, compile-time, reflectionless Kotlin library that makes it possible to merge instances of the same data class,
in an immutable and user-friendly way.

The library was developed out of necessity for the [Quarkdown typesetting system](https://github.com/iamgio/quarkdown):
it's currently available only for Kotlin/JVM, though Kotlin Multiplatform would be easy to support.  
Contributions towards multiplatform support are welcome.

## Installation

```kotlin
plugins {
    id("com.quarkdown.automerge") version "1.1.0"
}

repositories {
    mavenCentral()
}
```

Also make sure Maven Central is enabled as a plugin repository in your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

## Deep-copying data classes

Annotating a data class with `@NestedData` will provide a `deepCopy` function, allowing waterfall copying of nested data classes.

The real power of this function is the flattening of nested properties:

```kotlin
@NestedData
data class Config(
    val app: AppConfig,
    val notifications: NotificationConfig,
)

data class AppConfig(
    val theme: String,
)

data class NotificationConfig(
    val email: EmailNotificationConfig,
    val push: PushNotificationConfig,
)

data class EmailNotificationConfig(
    val enabled: Boolean,
    val frequency: String,
)
```

Without the library, generating a copy with a modified nested property is a verbose operation:

```kotlin
val newConfig: Config = config.copy(
    app = config.app.copy(theme = "dark"),
    notifications = config.notifications.copy(
        email = config.notifications.email.copy(enabled = false)
    )
)
```

With `deepCopy`, it becomes much more concise:

```kotlin
val newConfig: Config = config.deepCopy(
    app_theme = "dark",
    notifications_email_enabled = false,
)
```

## Merging data classes

Annotating a data class with `@Mergeable` will provide a `merge` function.

```kotlin
@Mergeable
data class MyClass(
    val a: String,
    val b: Int? = null,
    val c: Boolean? = null,
)

val first = MyClass(a = "X", b = 42)
val second = MyClass(a = "Y", b = 7, c = true)

val merged: MyClass = first.merge(second) // MyClass(a=X, b=42, c=true)
```

### Real-world example

The library's main purpose is to abstract away from rigid defaults, making it possible to create flexible configurations.

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
    val preferences: Preferences = user.merge(default)
    println(preferences) // Preferences(theme=dark, fontSize=16, autoSaveDelay=10)
}
```

## Troubleshooting

Make sure that annotation processing is enabled in your IDE to ensure the generated functions can be resolved. 
Alternatively, build the project right after marking a class with an annotation.
