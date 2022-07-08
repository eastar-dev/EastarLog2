import android.log.Log.splitSafe
import android.log.Log.takePadStartSafeWidth
import android.log.Log.takeSafe
import org.junit.Ignore
import org.junit.Test
import java.nio.charset.Charset

class AndroidLogcatTest {

    //로그의 길이는 a 버전 ios 기준 약 4000byte tag 길이와 연관이 있음
    //E/0:          1         2         3         4         5         6         7         8         9        10        11        12        13        14        15        16        17        18        19        20        21        22        23        24        25        26        27        28        29        30        31        32        33        34        35        36        37        38        39        40        41        42        43        44        45        46        47        48        49        50        51        52        53        54        55        56        57        58        59        60        61        62        63        64        65        66        67        68        69        70        71        72        73        74        75        76        77        78        79        80        81        82        83        84        85        86        87        88        89        90        91        92        93        94        95        96        97        98        99       100       101       102       103       104       105       106       107       108       109       110       111       112       113       114       115       116       117       118       119       120       121       122       123       124       125       126       127       128       129       130       131       132       133       134       135       136       137       138       139       140       141       142       143       144       145       146       147       148       149       150       151       152       153       154       155       156       157       158       159       160       161       162       163       164       165       166       167       168       169       170       171       172       173       174       175       176       177       178       179       180       181       182       183       184       185       186       187       188       189       190       191       192       193       194       195       196       197       198       199       200       201       202       203       204       205       206       207       208       209       210       211       212       213       214       215       216       217       218       219       220       221       222       223       224       225       226       227       228       229       230       231       232       233       234       235       236       237       238       239       240       241       242       243       244       245       246       247       248       249       250       251       252       253       254       255       256       257       258       259       260       261       262       263       264       265       266       267       268       269       270       271       272       273       274       275       276       277       278       279       280       281       282       283       284       285       286       287       288       289       290       291       292       293       294       295       296       297       298       299       300       301       302       303       304       305       306       307       308       309       310       311       312       313       314       315       316       317       318       319       320       321       322       323       324       325       326       327       328       329       330       331       332       333       334       335       336       337       338       339       340       341       342       343       344       345       346       347       348       349       350       351       352       353       354       355       356       357       358       359       360       361       362       363       364       365       366       367       368       369       370       371       372       373       374       375       376       377       378       379       380       381       382       383       384       385       386       387       388       389       390       391       392       393       394       395       396       397       398       399       400       401       402       403       404       405       406
    //E/0: 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234
    //E/01:          1         2         3         4         5         6         7         8         9        10        11        12        13        14        15        16        17        18        19        20        21        22        23        24        25        26        27        28        29        30        31        32        33        34        35        36        37        38        39        40        41        42        43        44        45        46        47        48        49        50        51        52        53        54        55        56        57        58        59        60        61        62        63        64        65        66        67        68        69        70        71        72        73        74        75        76        77        78        79        80        81        82        83        84        85        86        87        88        89        90        91        92        93        94        95        96        97        98        99       100       101       102       103       104       105       106       107       108       109       110       111       112       113       114       115       116       117       118       119       120       121       122       123       124       125       126       127       128       129       130       131       132       133       134       135       136       137       138       139       140       141       142       143       144       145       146       147       148       149       150       151       152       153       154       155       156       157       158       159       160       161       162       163       164       165       166       167       168       169       170       171       172       173       174       175       176       177       178       179       180       181       182       183       184       185       186       187       188       189       190       191       192       193       194       195       196       197       198       199       200       201       202       203       204       205       206       207       208       209       210       211       212       213       214       215       216       217       218       219       220       221       222       223       224       225       226       227       228       229       230       231       232       233       234       235       236       237       238       239       240       241       242       243       244       245       246       247       248       249       250       251       252       253       254       255       256       257       258       259       260       261       262       263       264       265       266       267       268       269       270       271       272       273       274       275       276       277       278       279       280       281       282       283       284       285       286       287       288       289       290       291       292       293       294       295       296       297       298       299       300       301       302       303       304       305       306       307       308       309       310       311       312       313       314       315       316       317       318       319       320       321       322       323       324       325       326       327       328       329       330       331       332       333       334       335       336       337       338       339       340       341       342       343       344       345       346       347       348       349       350       351       352       353       354       355       356       357       358       359       360       361       362       363       364       365       366       367       368       369       370       371       372       373       374       375       376       377       378       379       380       381       382       383       384       385       386       387       388       389       390       391       392       393       394       395       396       397       398       399       400       401       402       403       404       405       406
    //E/01: 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123
    @Test
    fun getMaxLogLength_when_tag1Byte() {
        val logCount = 5000
        android.util.Log.e("~", (1..logCount / 10).joinToString("") { "%10d".format(it) })
        android.util.Log.e("~", "1234567890".repeat(logCount / 10))
        android.util.Log.e("~~", "1234567890".repeat(logCount / 10))
        //tag 1
        //log 4064
    }

    @Test
    fun getMaxLogLength_when_korean() {
        val logCount = 5000
        android.util.Log.e("~", (1..logCount / 10).joinToString("") { "%10d".format(it) })
        android.util.Log.e("~", "1234567890".repeat(logCount / 10))
        android.util.Log.e("~", "가나다라마".repeat(logCount / 10))
    }


    @Test
    fun getMaxTagLength() {
        val logCount = 80
        android.util.Log.e((1..logCount / 10).joinToString("", "", "") { "%10d".format(it) } + "....", "~")
        android.util.Log.e("1234567890".repeat(logCount / 10) + "1234", "~")

        // tag max length 84 byte
    }


    @Test
    fun singleLineLogLengthCheck_when_tagTrimRight() {
        android.util.Log.e("12345678~ ", "12345678~ ")
        android.util.Log.e(" ~34567890", " ~34567890")
        //tag trim right
    }

    @Test
    fun getMaxTagLength_when_formatBroken() {
        val logCount = 4030
        android.util.Log.e((1..logCount / 10).joinToString("") { "%10d".format(it) }, "~")
        android.util.Log.e("1234567890".repeat(logCount / 10), "~")
        //로그가 package naem filter에 안걸린다.
        //걸릴때도 있다.
        //포멧은 항상 깨진다.
        //logcat 상에 no filter 기준으로 이전에 나온 로그에 영향을 받는다.
    }

    @Ignore
    @Test
    fun getMaxTagLength_when_Fatal() {
        val logCount = 4040
        android.util.Log.e((1..logCount / 10).joinToString("") { "%10d".format(it) }, "~")
        android.util.Log.e("1234567890".repeat(logCount / 10), "~")
        Thread.sleep(10 * 1000L)

        //Fatal 4040~
        //A/libc: Fatal signal 6 (SIGABRT), code -1 (SI_QUEUE) in tid 15022 (roidJUnitRunner), pid 14990 (eastar.log.test)
    }

    @Test
    fun getBestTagLength() {
        val tag = "1234567890".repeat(10)
        repeat(90) {
            android.util.Log.e(tag.take(it), "~$it")
        }

        // tag max length 84 byte
    }

    @Test
    fun getBestTagLengthKorean() {
        val tag = "가나다라마".repeat(10)
        repeat(35) {
            android.util.Log.e(tag.take(it), "~$it")
        }

        // 한글28글자 = 같은넓이의 영문56글자
    }

    //https://namu.wiki/w/UTF-8
    @Test
    fun count_of_first_bit() {
        android.util.Log.e(
            "~",
            0b10000000.toUByte()
                .run { toString(2) + " : " + inv().countLeadingZeroBits() + " 0b10000000.toUByte().inv().countLeadingZeroBits()" })
        android.util.Log.e(
            "~",
            0b11001011.toUByte()
                .run { toString(2) + " : " + inv().countLeadingZeroBits() + " 0b11001011.toUByte().inv().countLeadingZeroBits()" })
        android.util.Log.e(
            "~",
            0b11101011.toUByte()
                .run { toString(2) + " : " + inv().countLeadingZeroBits() + " 0b11101011.toUByte().inv().countLeadingZeroBits()" })
    }

    @Test
    fun tagWidthCheck() {
        android.util.Log.e((1..8).joinToString("") { "%10d".format(it) } + "....", "~")
        android.util.Log.e("1234567890".repeat(8) + "1234", "~")
        android.util.Log.e("1234567890", "~length : " + "1234567890".length)
        android.util.Log.e("가나다라마", "~length : " + "가나다라마".length)

        android.util.Log.e("1234567890", "~utf8  byte : " + "1234567890".toByteArray().size)
        android.util.Log.e("가나다라마", "~utf8  byte : " + "가나다라마".toByteArray().size)
        android.util.Log.e("가나다라마", "~euckr byte : " + "가나다라마".toByteArray(Charset.forName("euc-kr")).size)
        android.util.Log.e("01나다라마", "~euckr byte : " + "가나다라마".toByteArray(Charset.forName("euc-kr")).size)
        android.util.Log.e("가23다라마", "~euckr byte : " + "가나다라마".toByteArray(Charset.forName("euc-kr")).size)
        android.util.Log.e("가23다라45", "~euckr byte : " + "가나다라마".toByteArray(Charset.forName("euc-kr")).size)
    }

    //tag 길이 정렬
    @Test
    fun tagTakeLast50Byte() {
        android.util.Log.e((1..8).joinToString("") { "%10d".format(it) } + "....", "~")
        android.util.Log.e("1234567890".repeat(8) + "1234", "~")
        repeat(3) {
            val tag = "가1나23다라456마바사789".repeat(it + 1)
            android.util.Log.e(tag.takePadStartSafeWidth(50), "~")
        }
    }

    //tag 길이 정렬
    @Test
    fun tagTakeLastSafe() {
        android.util.Log.e("~".padStart(5), (1..8).joinToString("") { "%10d".format(it) })
        android.util.Log.e("~".padStart(5), "1234567890".repeat(8))
        repeat(10) {
            val tag = "가1나23다라456마바사789"
            android.util.Log.e("~$it".padStart(5), tag.takePadStartSafeWidth(it))
        }
        repeat(10) {
            val tag = "가1나23다라456마바사789"
            android.util.Log.e("~$it".padStart(5), tag.takePadStartSafeWidth(it + 15))
        }
    }


    @Test
    fun splitSafeTest() {
        //0xc0 : 1100 0000
        //0xc1 : 1100 0001
        val text = "가1나23다라456마바아자".repeat(5)
        android.util.Log.e("~", "" + text.toByteArray().size)

        repeat(10) {
            val tokens = text.splitSafe(it + 3)
            android.util.Log.i("~", tokens.toString())
        }
    }


    @Test
    fun takeSafe_last_save_cut_Test() {
        //0xc0 : 1100 0000
        //0xc1 : 1100 0001
        android.util.Log.e("~", (1..8).joinToString("") { "%10d".format(it) })
        android.util.Log.e("~", "1234567890".repeat(8))

        val text = "가1나23다라456마바아자"
        android.util.Log.i("~", "$text : ${text.toByteArray().size}byte")

        repeat(30) {
            val tokens = text.takeSafe(it + 10)
            android.util.Log.i("~", "$tokens~${it + 10}")
        }
    }

    @Test
    fun takeSafe_first_save_cut_Test() {
        //0xc0 : 1100 0000
        //0xc1 : 1100 0001
        android.util.Log.e("~", (1..8).joinToString("") { "%10d".format(it) })
        android.util.Log.e("~", "1234567890".repeat(8))

        val text = "가1나23다라456마바아자"
        android.util.Log.i("~", "$text : ${text.toByteArray().size}byte")

        repeat(35) {
            val tokens = text.takeSafe(10, it)
            android.util.Log.i("~", "$tokens~${tokens.toByteArray().size}")
        }
    }

    //3000byte 3~30byte split
    //repeat 100000,   19s without log
    //repeat  10000,    2s without log
    //repeat   1000, 300ms without log
    //repeat    100, 165ms without log
    //repeat 100000,   26s with log
    //repeat  10000,    4s with log
    //repeat   1000, 697ms with log
    //repeat    100, 193ms with log
    @Ignore
    @Test
    fun splitSafeSpeedTest() {
        val text = "가1나23다라456마바아자".repeat(100)
        android.util.Log.e("~", "" + text.toByteArray().size)

        repeat(10000) {
            val tokens = text.splitSafe(it % 30 + 3)
            android.util.Log.i("~", tokens.toString())
        }
    }

}
