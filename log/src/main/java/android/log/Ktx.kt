package android.log


val String?.singleLog: String get() = this?.take(3600)?.trim()?.replace("\r", "")?.replace('\n', '↵') ?: ""
val Boolean?.IW: Int get() = if (this == true) android.util.Log.INFO else android.util.Log.WARN
val Boolean?.priority: Int get() = IW