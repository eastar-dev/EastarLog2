package android.log

import org.junit.Test


class SplitSafeTest {
    //3000byte 3~30byte split
    //repeat 100000,   19s without log
    //repeat 100000,   26s with log
    //repeat 1000  , 697ms with log
    //repeat 100   , 193ms with log
    @Test
    fun splitSafeTest() {
        val text = "가1나23다라456마바아자".repeat(100)
        android.util.Log.e("_", "" + text.toByteArray().size)

        repeat(100_000) {
            val tokens = text.splitSafe(it % 30 + 3)
            android.util.Log.e("_", tokens.toString())
        }
    }
}
