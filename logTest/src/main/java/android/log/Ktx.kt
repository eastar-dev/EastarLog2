package android.log


val String?.singleLog: String get() = this?.take(3500)?.trim()?.replace("\r", "")?.replace('\n', '↵') ?: ""