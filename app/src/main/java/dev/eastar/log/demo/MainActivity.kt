package dev.eastar.log.demo

import android.content.Intent
import android.log.Log
import android.log.LogActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : LogActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            TextView(context).apply {
                text = "1"
                textSize = 100.sp
                setOnClickListener { startActivityForResult(Intent(context, BActivity::class.java), 1) }
            }.also { addView(it) }
            Button(context).apply {
                text = "Legacy log test"
                setOnClickListener { Logger.e("%s %d", "test", 200) }
            }.also { addView(it) }
            Button(context).apply {
                text = "Migration log"
                setOnClickListener { LoggerMigration.e("%s %d", "test", 200) }
            }.also { addView(it) }
            Button(context).apply {
                text = "Migration log with classname filter"
                setOnClickListener {
                    //way 1
                    Log.logFilterClassNameRegex = "${LoggerMigration::class.java.name}".toRegex()
                    //way 2
                    Log.logFilterPredicate = {
//                        check
//                        android.util.Log.e("~", it.className)
//                        android.util.Log.e("~", it.fileName)
//                        android.util.Log.e("~", it.methodName)
                        false
                    }
                    LoggerMigration.e("%s %d", "test", 200)
                }
            }.also { addView(it) }
        })

        logTest()

        getMaxLogLength_when_tag1Byte()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun logTest() {
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

    private fun 한글함수() {
        Log.e("tag 부분에 한글이 있어도 길이를 잘 정렬해서 출력합니다.")
    }

    private fun 이것은_한글_함수_테스트_입니다() {
        Log.w("한글이 짤리면 한글 중간을 인식해서 '.'로 변경합니다.")
    }

    private fun 이것은_한글함수_테스트_입니다() {
        Log.w("함수명 '이것은_한글_함수_테스트_입니다' 길어서 앞부분이 짤립니다.")
    }

    private fun length1() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length12() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length123() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length1234() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length12345() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length123456() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length1234567() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length12345678() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length123456789() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")
    private fun length1234567890() = Log.i("길이가 길어질수록 앞부분이 짤립니다.")

    companion object {

        fun getMaxLogLength_when_tag1Byte() {
            val logCount = 5000
            android.util.Log.e("~", (1..logCount / 10).joinToString("") { "%10d".format(it) })
            android.util.Log.e("~", "1234567890".repeat(logCount / 10))
            android.util.Log.e("~~", "1234567890".repeat(logCount / 10))
            //tag 1
            //log 4064
        }
    }

}
