## What different EastarLog2?

android.log.Log is more smart, powerfull then android.util.Log
Android developers often need to logcat.
So I made it.

### log pretty output at JSON, XML another .... ?

android.log.Log help you're project tracking find bug.

### log quick setting you're project

just replace all

    ```kotlin
        import android.util.Log -> import android.log.Log
    ```

just it!

### [log show or hide](/log/src/main/java/android/log/Log.kt#L87) _option_

    ```kotlin
        @JvmField
        var LOG = true
    ```

### [log prefix](/log/src/main/java/android/log/Log.kt#L105) _option_

    ```kotlin
        @JvmField
        var PREFIX = "``"
    ```

### [log skip](/log/src/main/java/android/log/Log.kt#L119-L123) _option_

    ```kotlin
        @JvmField
        var NOT_REGEX: Regex = "".toRegex()
    
        @JvmField
        var NOT_PREDICATE: (StackTraceElement) -> Boolean = { false }
    ```

## What's new?

1. add [LogLifeCycle.kt](log/src/main/java/android/log/LogLifeCycle.kt)
2. supported Android Studio 3.5.x ~ Android Studio Ladybug | 2024.2.1 Patch 3

## How...

### Publishing Central Portal

https://central.sonatype.com/artifact/dev.eastar/eastar-log

#### [Add the dependency](/app/build.gradle.kts#L99-L102)

```kotlin
dependencies {
    debugImplementation(libs.eastar.log)
    releaseImplementation(libs.eastar.log.no.op)
}
```

## License

 ```code
Copyright 2017 eastar Jeong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
