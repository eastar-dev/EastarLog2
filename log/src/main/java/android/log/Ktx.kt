package android.log

import kotlin.experimental.and

val String?.singleLog: String
    get() = this?.toByteArray()
        ?.take(3500)
        ?.dropLastWhile { it and 0xc0.toByte() != 0x80.toByte() }
        ?.toByteArray()
        ?.let { String(it) }
        ?.replace("\r", "")
        ?.replace('\n', '↵')
        ?: ""

val Boolean?.IW: Int get() = if (this == true) android.util.Log.INFO else android.util.Log.WARN
val Boolean?.priority: Int get() = IW

