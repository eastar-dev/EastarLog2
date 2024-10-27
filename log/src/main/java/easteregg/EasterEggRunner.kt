package easteregg

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import java.lang.reflect.Method
import kotlin.reflect.KClass

fun Application.easterEgg(vararg easterEggs: KClass<*>) = registerActivityLifecycleCallbacks(
    EasterEggRunner(*easterEggs.map { it.java.name }.toTypedArray())
)

class EasterEggRunner(
    private vararg val easterEggClzName: String
) : Application.ActivityLifecycleCallbacks {
    private var systemWindowInsetTop: Int = 0
    override fun onActivityStarted(activity: Activity) {
        val parent = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        if (parent.findViewWithTag<View>(EASTER_EGG_VIEW_TAG) != null)
            return
        val packageManager = activity.packageManager
        val packageName = activity.packageName
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        val versionCode = runCatching { PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0)) }.getOrDefault(-1L)

        ViewCompat.setOnApplyWindowInsetsListener(parent) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            //Log.i("EasterEggRunner", insets.top)
            systemWindowInsetTop = insets.top
            windowInsets
        }

        val easterEggButton = TextView(activity).apply {
            doOnPreDraw {
                val locationInWindow = IntArray(2)
                getLocationInWindow(locationInWindow)
                if (locationInWindow[1] == 0) y = systemWindowInsetTop.toFloat()
            }

            @SuppressLint("SetTextI18n")
            text = "$versionName::$versionCode"
            tag = EASTER_EGG_VIEW_TAG
            setTextColor(0xffff0000.toInt())
            textSize = 9f // sp
            setBackgroundColor(0x0000ff00)
        }.also {
            parent.addView(it, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val classMethods = easterEggClzName.flatMap {
            getMethods(it)
        }.sortedBy { it[KEY_METHOD_NAME] as String }

        val adapter = SimpleAdapter(
            activity,
            classMethods,
            android.R.layout.simple_list_item_1,
            arrayOf(KEY_METHOD_NAME),
            intArrayOf(android.R.id.text1)
        )

        easterEggButton.setOnClickListener {
            AlertDialog.Builder(activity)
                .setAdapter(adapter) { dialog, which ->
                    val classMethod = (dialog as AlertDialog).listView.getItemAtPosition(which) as Map<*, *>
                    invokeMethod(
                        classMethod[KEY_CLASS] as Class<*>,
                        classMethod[KEY_METHOD] as Method,
                        activity
                    )
                }
                .show()
        }

        easterEggButton.setOnLongClickListener {
            val classMethod = classMethods.firstOrNull() ?: return@setOnLongClickListener false
            invokeMethod(
                classMethod[KEY_CLASS] as Class<*>,
                classMethod[KEY_METHOD] as Method,
                activity
            )
            true
        }
    }

    private fun getMethods(clz: String) = runCatching {
        Class.forName(clz).run {
            methods.filter { it.declaringClass == this }
                //.filter { it.returnType == Void.TYPE }
                .filterNot { it.name.contains("$") }
                .map {
                    mapOf(
                        KEY_CLASS to this,
                        KEY_METHOD to it,
                        KEY_CLASS_NAME to simpleName,
                        KEY_METHOD_NAME to it.name
                    )
                }
                .toList()
        }
    }.getOrDefault(emptyList())

    private fun invokeMethod(clazz: Class<*>, method: Method, activity: Activity) {
        val constructor = clazz.getConstructor(Activity::class.java)
        val receiver = constructor.newInstance(activity)
        method
            .parameterTypes
            .map { parameterType ->
                parameterType.runCatching {
                    cast(activity)
                }.onFailure {
                    val stack = Exception().stackTrace[0]
                    val locator = String.format("(%s:%d)", stack.fileName, stack.lineNumber)
                    android.util.Log.w("EasterEgg", "``$locator :: !${activity::class.java.name}를 ${parameterType.name}으로 변환할 수 없습니다.")
                }.getOrThrow()
            }
            .also {
                if (it.isEmpty())
                    method.invoke(receiver)
                else
                    method.invoke(receiver, *it.toTypedArray())
            }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private const val EASTER_EGG_VIEW_TAG = "show_me_the_money"
        private const val KEY_CLASS = "class"
        private const val KEY_METHOD = "method"
        private const val KEY_CLASS_NAME = "className"
        private const val KEY_METHOD_NAME = "methodName"
    }
}
