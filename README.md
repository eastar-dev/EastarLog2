[![Release](https://jitpack.io/v/djrain/EastarLog.svg)](https://jitpack.io/#djrain/EastarLog)

## What different EastarLog2?

android.log.Log is more smart, powerfull then android.util.Log
Android developers often need to logcat.
So I made it.

### log pretty output at JSON, XML another .... ?
android.log.Log help you'r project tracking find bug.

### log quick setting you'r project
just replace all 'import android.util.Log;' -> 'import android.log.Log;' just it!

### log show or hide?
```java
	public static boolean LOG = BuildConfig.DEBUG;
```
https://github.com/djrain/log/blob/master/log/src/main/java/android/log/Log.java#L73



## What's new?
add LActivity, LFragment
add LifeCycleLog.kt
supported android studio 3.5.x ~ 4.0.x



## How...

### Gradle with jitpack

#### Add it in your root build.gradle at the end of repositories:
```javascript
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
#### Add the dependency
```javascript
dependencies {
        implementation 'com.github.djrain:EastarLog:4.0.0'
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
