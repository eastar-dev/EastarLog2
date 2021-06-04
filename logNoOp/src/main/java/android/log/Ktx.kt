package android.log


val String?.singleLog: String get() = ""
val Boolean?.IW: Int get() = if (this == true) android.util.Log.INFO else android.util.Log.WARN
val Boolean?.priority: Int get() = IW