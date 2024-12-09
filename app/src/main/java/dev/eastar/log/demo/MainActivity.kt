@file:Suppress("NonAsciiCharacters", "FunctionName", "unused")

package dev.eastar.log.demo

import android.content.Intent
import android.log.Log
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    // Define ActivityResultLauncher to start BActivity and handle the result
    private val startBActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val fromActivity = result.data?.getStringExtra("from") ?: "Unknown"
                Log.i("ActivityResult", "Returned from $fromActivity")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            TextView(context).apply {
                text = "1"
                textSize = 100.sp
                setOnClickListener {
                    // Use the launcher to start BActivity instead of startActivityForResult
                    startBActivityLauncher.launch(Intent(context, BActivity::class.java))
                }
            }.also { addView(it) }
            Button(context).apply {
                text = "Log Test"
                setOnClickListener { logTest() }
            }.also { addView(it) }
            Button(context).apply {
                text = "Legacy log test"
                setOnClickListener { Logger.e("%s %d", "test", 200) }
            }.also { addView(it) }
            Button(context).apply {
                text = "wtf log test"
                setOnClickListener { android.util.Log.wtf("tag", "msg") }
            }.also { addView(it) }
            Button(context).apply {
                text = "Migration log"
                setOnClickListener { LoggerMigration.e("%s %d", "test", 200) }
            }.also { addView(it) }
            Button(context).apply {
                text = "Migration log with classname filter"
                setOnClickListener {
                    //way 1
                    Log.NOT_REGEX = "${LoggerMigration::class.java.name}".toRegex()
                    //way 2
                    Log.NOT_PREDICATE = {
                        // Custom logging filter
                        false
                    }
                    LoggerMigration.e("%s %d", "test", 200)
                }
            }.also { addView(it) }
        })

        logTest()
    }

    private fun logTest() {
        getMaxLogLength_when_tag1Byte()

        testMethod()
        한글함수()
        이것은_한글_함수_테스트_입니다()
        이것은_링크가_걸리는_로그_입니다()
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

        Throwable().printStackTrace()

        로그함수에_리턴값은_무엇을_반환하나()
    }

    private fun testMethod() {
        Log.e("tag 부분에는 log 호출 부분이 표시됩니다.")
    }

    private fun 한글함수() {
        Log.e("한글 함수가 있어도 길이를 잘 정렬해서 출력합니다.")
    }

    private fun 이것은_한글_함수_테스트_입니다() {
        Log.w("한글이 짤리면 한글 중간을 인식해서 '.'로 변경합니다.")
    }

    private fun 이것은_링크가_걸리는_로그_입니다() {
        android.util.Log.e("", """at dev.eastar.log.demo.MainActivity.logTest(MainActivity.kt:85)""")
        android.util.Log.e("", """at dev.eastar.log.demo.MainActivity.logTest CActivity.kt:85  MainActivity.kt:85 """)
        android.util.Log.e("", "(MainActivity:85)")
        android.util.Log.e("", "(dev.eastar.log.demo.MainActivity.kt:85)")
        android.util.Log.e("", """at (MainActivity.kt:85)""")
        android.util.Log.e("", "(MainActivity.kt:85)")
        android.util.Log.e("(MainActivity.kt:85)", "")
        android.util.Log.e("", "MainActivity.kt:85")
        android.util.Log.e("", "MainActivity.kt:85")
        android.util.Log.e("MainActivity.kt:-1", "")
        android.util.Log.e("(MainActivity.kt:0)", "")
        android.util.Log.e("BActivity.java:0", "")
    }

    private fun 로그함수에_리턴값은_무엇을_반환하나() {
        android.util.Log.e("", "" + android.util.Log.v("log level", "Log.v"))
        android.util.Log.e("", "" + android.util.Log.d("log level", "Log.d"))
        android.util.Log.e("", "" + android.util.Log.i("log level", "Log.i"))
        android.util.Log.e("", "" + android.util.Log.w("log level", "Log.w"))
        android.util.Log.e("", "" + android.util.Log.e("log level", "Log.e"))
    }

    private fun length1() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length12() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length123() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length1234() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length12345() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length123456() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length1234567() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length12345678() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length123456789() = Log.i("길이가 길이가 달라도 정렬을 합니다.")
    private fun length1234567890() = Log.i("길이가 길이가 달라도 정렬을 합니다.")

    companion object {
        fun getMaxLogLength_when_tag1Byte() {
            android.util.Log.e("", (1..500).joinToString("") { "%10d".format(it) })
            android.util.Log.e("", "1234567890".repeat(500))
            android.util.Log.e("1", "1234567890".repeat(500))
            android.util.Log.e("12", "1234567890".repeat(500))
            android.util.Log.e("123", "1234567890".repeat(500))
            android.util.Log.e("가", "1234567890".repeat(500))

            android.util.Log.e("", "가나다라마".repeat(500))

            android.util.Log.e(83.lorem, "1234567890".repeat(500))
            android.util.Log.e(84.lorem, "1234567890".repeat(500))
//            android.util.Log.e(85.lorem, "1234567890".repeat(500))
//            android.util.Log.e(447.lorem, (1..500).joinToString("") { "%10d".format(it) })
//            android.util.Log.e(447.lorem, "1234567890".repeat(500))
//            android.util.Log.e(448.lorem, "1234567890".repeat(500))
//            android.util.Log.e(449.lorem, "1234567890".repeat(500))
//            android.util.Log.e(450.lorem, "1234567890".repeat(500))
//            android.util.Log.e(451.lorem, "1234567890".repeat(500))
//            android.util.Log.e(1000.lorem, "1234567890".repeat(500))
//            android.util.Log.e(10000.lorem, "1234567890".repeat(500))
//            android.util.Log.e(100000.lorem, "1234567890".repeat(500))
            //tag 0~120
            //log 4064
        }
    }
}

private val Int.lorem: String
    get() =
        ("" + this +
                """
    
    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
    Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
    Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
    Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
""".trimIndent().replace("\n", " ")).take(this)
