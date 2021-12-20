package android.log

import org.junit.Test
import java.nio.charset.Charset

class TestLogTest {

    @Test
    fun getSingleLog() {
        println("가나다라마바사아자차".singleLog)
        println("가나다라마바사아자차".toByteArray().size)
        println("가나다라마바사아자차".toByteArray(Charsets.ISO_8859_1).size)
        println("가나다라마바사아자차".toByteArray(Charset.forName("euc-kr")).size)
        "가나다라마바사아자차".toByteArray(Charset.forName("euc-kr"))
    }
}