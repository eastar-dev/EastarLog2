@file:Suppress("unused", "FunctionName", "NonAsciiCharacters", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package easteregg

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.log.Log
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity

fun Application.easterEgg() = easterEgg(EasterEgg::class)

class EasterEgg(private val activity: Activity) {
    fun func1(activity: Context, context: Context) {
    }

    fun func2() {
    }

    fun AppCompatActivity.func3() {
    }

    fun Activity.allActivity(context: Context) {
        val items = packageManager
            .getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            .activities
            .map {
                it.name
            }.filter {
                it.startsWith(packageName)
            }.filterNot {
                it == javaClass.name
            }.map { clz ->
                mapOf(
                    "name" to clz.takeLastWhile { it != '.' },
                    "class" to clz
                )
            }

        val from = arrayOf("name", "class")
        val to = intArrayOf(android.R.id.text1, android.R.id.text2)

        val adapter = SimpleAdapter(context, items, android.R.layout.simple_list_item_2, from, to)

        AlertDialog.Builder(context)
            .setAdapter(adapter) { dialog, which ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    val item = (dialog as AlertDialog).listView.getItemAtPosition(which) as Map<String, String>
                    startActivity(Intent().setClassName(context, item["class"]!!))
                } catch (e: Exception) {
                    Log.printStackTrace(e)
                }
            }
            .show()
    }

    /** activity에 _test1이라는 함수를 실행해줍니다. */
    fun _test1() {
        kotlin.runCatching {
            activity.javaClass.getMethod("_test1").invoke(activity)
        }
    }

    fun crashButton() {
        throw RuntimeException("Test Crash") // Force a crash
    }

    fun _log_divider() = Log.e("=".repeat(100))

}
