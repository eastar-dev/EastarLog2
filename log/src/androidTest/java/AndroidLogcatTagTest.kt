import org.junit.Ignore
import org.junit.Test
import java.nio.charset.Charset

class AndroidLogcatTagTest {

    @Test
    fun logWidthCheck() {
        android.util.Log.e((1..8).joinToString("") { "%10d".format(it) } + "....", "0")
        android.util.Log.e("1234567890".repeat(8) + "1234", "0")
        android.util.Log.e("1234567890", "length : "+"1234567890".length)
        android.util.Log.e("1234567890", "utf8  byte : "+"1234567890".toByteArray().size)

        android.util.Log.e("가나다라마", "length : "+"가나다라마".length)
        android.util.Log.e("가나다라마", "utf8  byte : "+"가나다라마".toByteArray().size)
        android.util.Log.e("가나다라마", "euckr byte : "+"가나다라마".toByteArray(Charset.forName("euc-kr")).size)
        android.util.Log.e("01나다라마", "euckr byte : "+"가나다라마".toByteArray(Charset.forName("euc-kr")).size)
        android.util.Log.e("가23다라마", "euckr byte : "+"가나다라마".toByteArray(Charset.forName("euc-kr")).size)
        android.util.Log.e("가23다라45", "euckr byte : "+"가나다라마".toByteArray(Charset.forName("euc-kr")).size)
    }

    //https://namu.wiki/w/UTF-8
    @Test
    fun safeCut_when_korean() {
        //0xc0 : 1100 0000
        //0xc1 : 1100 0001
        //0x80 : 1000 0000
        android.util.Log.e("0xc0 1100 0000", ""+ 0xC0.toByte().countLeadingZeroBits())
        android.util.Log.e("0xc1 1100 0001", ""+ 0xC1.toByte().countLeadingZeroBits())
        android.util.Log.e("0xc0 1100 0000", ""+ 0xC0.toByte().countTrailingZeroBits())
        android.util.Log.e("0xc1 1100 0001", ""+ 0xC1.toByte().countTrailingZeroBits())
        android.util.Log.e("0xc0 1100 0000", ""+ 0xC0.toByte().countOneBits())
        android.util.Log.e("0xc1 1100 0001", ""+ 0xC1.toByte().countOneBits())

        android.util.Log.e("--------------", " ")

        android.util.Log.e("0xc0 1100 0000", ""+ 0xC0.inv().toByte().countLeadingZeroBits())
        android.util.Log.e("0xc1 1100 0001", ""+ 0xC1.inv().toByte().countLeadingZeroBits())
        android.util.Log.e("0xc0 1100 0000", ""+ 0xC0.inv().toByte().countTrailingZeroBits())
        android.util.Log.e("0xc1 1100 0001", ""+ 0xC1.inv().toByte().countTrailingZeroBits())
        android.util.Log.e("0xc0 1100 0000", ""+ 0xC0.inv().toByte().countOneBits())
        android.util.Log.e("0xc1 1100 0001", ""+ 0xC1.inv().toByte().countOneBits())

        android.util.Log.e("0x80", ""+ 0x80.toByte().countLeadingZeroBits())
        android.util.Log.e("0x80", ""+ 0x80.toByte().countTrailingZeroBits())
        android.util.Log.e("0x80", ""+ 0x80.toByte().countOneBits())
        android.util.Log.e("0x80", ""+ 0x80.toByte().countLeadingZeroBits())
        android.util.Log.e("0x80", ""+ 0x80.toByte().countTrailingZeroBits())
        android.util.Log.e("0x80", ""+ 0x80.toByte().countOneBits())

        //android.util.Log.e("0xc0 1100 0000", ""+ 0xC0.toByte().one

    }

    @Test
    fun getMaxLogLength_when_tag2byte() {
        val logCount = 5000
        android.util.Log.e("01", (1..logCount / 10).joinToString("") { "%10d".format(it) })
        android.util.Log.e("01", "1234567890".repeat(logCount / 10))

        //tag 2byte
        //log 4063byte
    }

    @Ignore
    @Test
    fun getMaxTagLength_when_Fatal() {
        val logCount = 4040
        android.util.Log.e((1..logCount / 10).joinToString("") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount / 10), "0")

        //Fatal
        //A/libc: Fatal signal 6 (SIGABRT), code -1 (SI_QUEUE) in tid 15022 (roidJUnitRunner), pid 14990 (eastar.log.test)
    }

    @Test
    fun getMaxTagLength_when_Nothing_Display() {
        val logCount = 4030
        android.util.Log.e((1..logCount / 10).joinToString("") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount / 10), "0")

        //no Fatal but nothing display
        //나올때도 있다
    }

    @Test
    fun getMaxTagLength_when_borken_format() {
        val logCount = 4030
        android.util.Log.e((1..logCount / 10).joinToString("") { "%10d".format(it) }, "1234567890".repeat(logCount / 10))
        android.util.Log.e("1234567890".repeat(logCount / 10), "0")

        //borken format
    }

    @Test
    fun getMaxTagLength_when_borken_format2() {
        val logCount = 3230
        android.util.Log.e((1..logCount / 10).joinToString("") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount / 10), "0")

        //tag length 3230 byte no fatal output but display 87byte
    }

    @Test
    fun getMaxTagLength() {
        val logCount = 80
        android.util.Log.e((1..logCount / 10).joinToString("", "", "") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount / 10) + "1234", "0")

        // tag max length 84 byte
    }
}
