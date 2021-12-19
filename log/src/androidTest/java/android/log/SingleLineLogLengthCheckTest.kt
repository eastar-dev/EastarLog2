package android.log

import org.junit.Test
import java.nio.charset.Charset

class SingleLineLogLengthCheckTest {

    @Test
    fun singleLineLogLengthCheck() {
        val tagCount = 50
        val logCount = 5000-tagCount
        android.util.Log.e((1..tagCount/10).joinToString("") { "%10d".format(it) }, (1..logCount/10).joinToString("") { "%10d".format(it) })
        android.util.Log.e("1234567890".repeat(tagCount/10), "1234567890".repeat(logCount/10))
        android.util.Log.e("가나다라마".repeat(tagCount/10), "가나다라마".repeat(logCount/10))
        android.util.Log.e("0".repeat(tagCount/10), "1234567890".repeat(logCount/10))

        android.util.Log.e("1",""+"0123456789".toByteArray(Charsets.UTF_8).size)
        android.util.Log.e("2",""+"가나다라마".toByteArray(Charsets.UTF_8).size)
    }
    @Test
    fun singleLineLogLengthCheck_when_tagLastWhiteSpace() {
        val tagCount = 50
        val logCount = 5000-tagCount
        android.util.Log.e((1..tagCount/10).joinToString("") { "%10d".format(it) }, (1..logCount/10).joinToString("") { "%10d".format(it) })
        android.util.Log.e("1234567890".repeat(tagCount/10), "1234567890".repeat(logCount/10))
        android.util.Log.e("가나다라  ".repeat(tagCount/10), "가나다라마".repeat(logCount/10))
        android.util.Log.e("123456789 ".repeat(tagCount/10), "1234567890".repeat(logCount/10))
        android.util.Log.e(" 234567890".repeat(tagCount/10), "1234567890".repeat(logCount/10))

        //tag trim right
    }
    @Test
    fun getMaxLogLength_when_tag1Byte() {
        val logCount = 5000
        android.util.Log.e("0", (1..logCount/10).joinToString("") { "%10d".format(it) })
        android.util.Log.e("0", "1234567890".repeat(logCount/10))
        //tag 1byte
        //log 4064byte
    }
    @Test
    fun getMaxLogLength_when_tag2byte() {
        val logCount = 5000
        android.util.Log.e("01", (1..logCount/10).joinToString("") { "%10d".format(it) })
        android.util.Log.e("01", "1234567890".repeat(logCount/10))

        //tag 2byte
        //log 4063byte
    }
    @Test
    fun getMaxTagLength_when_FatalNotException() {
        val logCount = 4040
        android.util.Log.e((1..logCount/10).joinToString("") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount/10), "0")

        //no Fatal but nothing display
    }
    @Test
    fun getMaxTagLength_when_Fatal() {
        val logCount = 4030
        android.util.Log.e((1..logCount/10).joinToString("") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount/10), "0")

        //no Fatal but nothing display
    }
    @Test
    fun getMaxTagLength_when_FatalWithLargeLog() {
        val logCount = 4030
        android.util.Log.e((1..logCount/10).joinToString("") { "%10d".format(it) }, "1234567890".repeat(logCount/10))
        android.util.Log.e("1234567890".repeat(logCount/10), "0")

        //no Fatal but nothing display
    }
    @Test
    fun getMaxTagLength_when_noFatalDisplay() {
        val logCount = 3230
        android.util.Log.e((1..logCount/10).joinToString("") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount/10), "0")

        //tag length 3230 byte no fatal output but display 87byte
    }

    @Test
    fun getMaxTagLength() {
        val logCount = 80
        android.util.Log.e((1..logCount/10).joinToString("","","....") { "%10d".format(it) }, "0")
        android.util.Log.e("1234567890".repeat(logCount/10)+"1234", "0")
        // tag max length 84 byte
    }
}
//로그의 길이는 a 버전 ios 기준 약 4000byte tag 길이와 연관이 있음
//E/         1         2         3         4         5:          1         2         3         4         5         6         7         8         9        10        11        12        13        14        15        16        17        18        19        20        21        22        23        24        25        26        27        28        29        30        31        32        33        34        35        36        37        38        39        40        41        42        43        44        45        46        47        48        49        50        51        52        53        54        55        56        57        58        59        60        61        62        63        64        65        66        67        68        69        70        71        72        73        74        75        76        77        78        79        80        81        82        83        84        85        86        87        88        89        90        91        92        93        94        95        96        97        98        99       100       101       102       103       104       105       106       107       108       109       110       111       112       113       114       115       116       117       118       119       120       121       122       123       124       125       126       127       128       129       130       131       132       133       134       135       136       137       138       139       140       141       142       143       144       145       146       147       148       149       150       151       152       153       154       155       156       157       158       159       160       161       162       163       164       165       166       167       168       169       170       171       172       173       174       175       176       177       178       179       180       181       182       183       184       185       186       187       188       189       190       191       192       193       194       195       196       197       198       199       200       201       202       203       204       205       206       207       208       209       210       211       212       213       214       215       216       217       218       219       220       221       222       223       224       225       226       227       228       229       230       231       232       233       234       235       236       237       238       239       240       241       242       243       244       245       246       247       248       249       250       251       252       253       254       255       256       257       258       259       260       261       262       263       264       265       266       267       268       269       270       271       272       273       274       275       276       277       278       279       280       281       282       283       284       285       286       287       288       289       290       291       292       293       294       295       296       297       298       299       300       301       302       303       304       305       306       307       308       309       310       311       312       313       314       315       316       317       318       319       320       321       322       323       324       325       326       327       328       329       330       331       332       333       334       335       336       337       338       339       340       341       342       343       344       345       346       347       348       349       350       351       352       353       354       355       356       357       358       359       360       361       362       363       364       365       366       367       368       369       370       371       372       373       374       375       376       377       378       379       380       381       382       383       384       385       386       387       388       389       390       391       392       393       394       395       396       397       398       399       400       401
//E/01234567890123456789012345678901234567890123456789: 0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234
//E/가나다라마가나다라마가나다라마가나다라마가나다라마: 가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마가나다라마
