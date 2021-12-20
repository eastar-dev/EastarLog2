package android.log

import java.nio.charset.Charset
import kotlin.experimental.and
import kotlin.experimental.inv

val String?.singleLog: String
    get() = this?.toByteArray()
        ?.take(3500)
        ?.dropLastWhile { it and 0xc0.toByte() == 0x80.toByte() }
        ?.dropLast(1)
        ?.toByteArray()
        ?.let { String(it) }
        ?.replace("\r", "")
        ?.replace('\n', '↵')
        ?: ""

val Boolean?.IW: Int get() = if (this == true) android.util.Log.INFO else android.util.Log.WARN
val Boolean?.priority: Int get() = IW

val String.width get() = toByteArray(Charset.forName("euc-kr")).size

fun String.takeSafe(lengthByte: Int, startOffset: Int = 0): String {
    require(lengthByte >= 3) { "min split length getter then 3" }
    val textByteArray = toByteArray()
    if (textByteArray.size <= lengthByte)
        return this

    return textByteArray.takeSafe(lengthByte, startOffset)
}

fun String.splitSafe(lengthByte: Int): List<String> {
    require(lengthByte >= 3) { "min split length getter then 3" }
    val textByteArray = toByteArray()
    if (textByteArray.size <= lengthByte)
        return listOf(this)

    val tokens = mutableListOf<String>()
    var startOffset = 0
    while (startOffset + lengthByte < textByteArray.size) {
        val token = textByteArray.takeSafe(lengthByte, startOffset)
        tokens += token
        startOffset += token.toByteArray().size
    }
    tokens += String(textByteArray, startOffset, textByteArray.size - startOffset)
    return tokens
}

private fun ByteArray.takeSafe(lengthByte: Int, startOffset: Int): String {
    //0xc0 : 1100 0000
    //0x80 : 1000 0000
    val textByteArray: ByteArray = this
    val position = startOffset + lengthByte - 1
    return if (textByteArray[position] and 0x80.toByte() == 0x00.toByte()) {
        String(textByteArray, startOffset, lengthByte)
    } else {
        var offset = 0
        while (textByteArray[position - offset] and 0xc0.toByte() == 0x80.toByte()) offset++
        val charByteLengthCurrentPosition = offset + 1
        val charByteLength = textByteArray[position - offset].inv().countLeadingZeroBits()

        if (charByteLengthCurrentPosition == charByteLength) {
            String(textByteArray, startOffset, lengthByte)
        } else {
            String(textByteArray, startOffset, lengthByte - charByteLengthCurrentPosition)
        }
    }
}
