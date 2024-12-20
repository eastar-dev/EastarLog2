import android.log.Log
import dev.eastar.log.demo.MainActivity.Companion.getMaxLogLength_when_tag1Byte
import org.junit.Test

class EastarLog2TestApp {


    @Test
    fun getMaxLogLength_when_tag1Byte_JUnit() {
        getMaxLogLength_when_tag1Byte()
    }

    @Test
    fun tagTest() {
        testMethod()
        한글함수()
        이것은_한글_함수_테스트_입니다()
        이것은_한글함수_테스트_입니다()
        length1()
        length12()
        length123()
        length1234()
        length12345()
        length123456()
        length1234567()
        length12345678()
        length123456789()
        length1234567890()
    }

    private fun testMethod() {
        Log.e("tag 부분에는 'class::method' 형식으로 표시됩니다.")
    }

    @Suppress("TestFunctionName", "NonAsciiCharacters")
    fun 한글함수() {
        Log.e("tag 부분에 한글이 있어도 길이를 잘 정렬해서 출력합니다.")
    }

    @Suppress("NonAsciiCharacters", "TestFunctionName")
    fun 이것은_한글_함수_테스트_입니다() {
        Log.w("한글이 짤리면 한글 중간을 인식해서 '.'로 변경합니다.")
    }

    @Suppress("NonAsciiCharacters", "TestFunctionName")
    fun 이것은_한글함수_테스트_입니다() {
        Log.w("함수명 '이것은_한글_함수_테스트_입니다' 길어서 앞부분이 짤립니다.")
    }

    fun length1() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length12() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length123() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length1234() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length12345() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length123456() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length1234567() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length12345678() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length123456789() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    fun length1234567890() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")

    @Test
    fun getLogTest() {
        val text1 = "가1나23다라456마바아"
        Log.e(text1)
        val text2 = "가1나23다라456마바아".repeat(300)
        Log.w(text2)
    }
}