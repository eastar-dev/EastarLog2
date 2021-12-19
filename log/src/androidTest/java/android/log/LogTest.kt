package android.log

import org.junit.Test
import kotlin.experimental.and
import kotlin.experimental.inv


class SafeCutTest {
    @Test
    fun safeSplitTest() {
        repeat(10) {
            val tokens = "가1나23다라456마바아자".repeat(100).safeSplit(it + 3)
            android.util.Log.e("_", tokens.toString())
        }
    }
}

//return value count
private fun String.safeSplit(lengthByte: Int): List<String> {
    require(lengthByte >= 3) { "min split length getter then 3" }
    val textByteArray = toByteArray()
    if (textByteArray.size <= lengthByte)
        return listOf(this)

    val tokens = mutableListOf<String>()
    //0xc0 : 1100 0000
    //0x80 : 1000 0000
    var startOffset = 0
    while (startOffset + lengthByte < textByteArray.size) {
        val position = startOffset + lengthByte - 1
        val token = if (textByteArray[position] and 0x80.toByte() == 0x00.toByte()) {
            String(textByteArray, startOffset, lengthByte)
        } else {
            var backByte = 0
            while (textByteArray[position - backByte] and 0xc0.toByte() == 0x80.toByte()) backByte++
            val charBytePositionLength = backByte + 1
            val charByteLength = textByteArray[position - backByte].inv().countLeadingZeroBits()

            if (charBytePositionLength == charByteLength) {
                String(textByteArray, startOffset, lengthByte)
            } else {
                String(textByteArray, startOffset, lengthByte - charBytePositionLength)
            }
        }
        tokens += token
        startOffset += token.toByteArray().size
    }
    tokens += String(textByteArray, startOffset, textByteArray.size - startOffset)
    return tokens
}