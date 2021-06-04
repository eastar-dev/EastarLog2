package android.log


val String?.singleLog: String get() = this?.take(3600)?.trim()?.replace("\r", "")?.replace('\n', '↵') ?: ""