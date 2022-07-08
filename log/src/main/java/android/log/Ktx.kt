package android.log

import java.nio.charset.Charset
import kotlin.experimental.and
import kotlin.experimental.inv

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

//etc
private val String.width get() = toByteArray(Charset.forName("euc-kr")).size
fun String.takePadStartSafeWidth(length: Int = Log.TAG_WIDTH): String {
    var text = takeLast(length)
    while (text.width != length) {
        text = if (text.width > length)
            text.drop(1)
        else
            text.padStart(length - text.width + text.length, '.')
    }
    return text
}

fun String.takePadEndSafeWidth(length: Int = Log.TAG_WIDTH): String {
    var text = take(length)
    while (text.width != length) {
        text = if (text.width > length)
            text.dropLast(1)
        else
            text.padEnd(length - text.width + text.length, '.')
    }
    return text
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

fun ByteArray.takeSafe(lengthByte: Int, startOffset: Int): String {
    if (size <= startOffset)
        return ""

    //앞에서 문자중간을 건너뜀
    var offset = startOffset
    while (size > offset && get(offset) and 0b1100_0000.toByte() == 0b1000_0000.toByte())
        offset++

    //문자열 길이가 짧은경우 끝까지
    if (size <= offset + lengthByte)
        return String(this, offset, size - offset)

    //char 중간이 아니면 거기까지
    if (get(offset + lengthByte) and 0b1100_0000.toByte() != 0b1000_0000.toByte())
        return String(this, offset, lengthByte)

    //char 중간이거나 끝이면 앞으로 땡김
    var position = offset + lengthByte
    while (get(--position) and 0b1100_0000.toByte() == 0b1000_0000.toByte()) Unit

    val charByteMoveCount = offset + lengthByte - position
    val charByteLength = get(position).inv().countLeadingZeroBits()

    return if (charByteLength == charByteMoveCount)
    //char 끝이면 거기까지
        String(this, offset, lengthByte)
    else
    //char 중간이면 뒤에버림
        String(this, offset, position - offset)
}

fun String.takeSafe(lengthByte: Int, startOffset: Int = 0) = toByteArray().takeSafe(lengthByte, startOffset)