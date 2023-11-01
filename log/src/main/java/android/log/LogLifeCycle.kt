@file:Suppress("unused")

package android.log

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

typealias LogLifeCycle = Unit

fun Application.logLifeCycle() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity as? FragmentActivity)?.logFragment()
        val (locator, log) = activity.toLog()
        Log.pml(Log.LIFECYCLE_CREATE, "onActivityCreated", locator, "▶$log", "${activity.intent} ${activity.intent.extras.toStringEx()}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        val (locator, log) = activity.toLog()
        Log.pml(Log.LIFECYCLE_DESTROYED, "onActivityDestroyed", locator, "◀$log")
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
})

fun FragmentActivity.logFragment() {
    supportFragmentManager.registerFragmentLifecycleCallbacks(
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                if ("SupportRequestManagerFragment" == f.javaClass.simpleName) return
                val (locator, log) = f.toLog()
                Log.pml(Log.LIFECYCLE_CREATE, "onFragmentCreated", locator, "▷$log", f.arguments.toStringEx())
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentDestroyed(fm, f)
                if ("SupportRequestManagerFragment" == f.javaClass.simpleName) return
                val (locator, log) = f.toLog()
                Log.pml(Log.LIFECYCLE_DESTROYED, "onFragmentDestroyed", locator, "◁$log")
            }
        }, true
    )
}

fun Application.logActivity() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val (locator, log) = activity.toLog()
        Log.pml(Log.LIFECYCLE_CREATE, "onActivityCreated", locator, "▶$log", "${activity.intent} ${activity.intent.extras.toStringEx()}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        val (locator, log) = activity.toLog()
        Log.pml(Log.LIFECYCLE_DESTROYED, "onActivityDestroyed", locator, "◀$log")
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
})


private fun Any.toLog(): Pair<String, String> {
    val simpleName = javaClass.simpleName
    val hashCode = hashCode().toString(16)
    val ext = if (javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
    val locator = "($simpleName.$ext:0)"
    val log = "$simpleName{$hashCode}"
    return locator to log
}


private fun Bundle?.toStringEx(): String {
    val extras = this ?: return ""
    return extras.keySet().joinToString { key ->
        val value = extras.get(key)
        //val entryType = when (value) {
        //    is String -> "S"
        //    is Boolean -> "B"
        //    is Byte -> "b"
        //    is Char -> "c"
        //    is Double -> "d"
        //    is Float -> "f"
        //    is Int -> "i"
        //    is Long -> "l"
        //    is Short -> "s"
        //    else -> value?.javaClass?.simpleName
        //}

        val entryType = value?.javaClass?.simpleName.toString()

        val entryValue: String = when (value) {
            is String -> Uri.encode(value.toString())
            is Boolean -> Uri.encode(value.toString())
            is Byte -> Uri.encode(value.toString())
            is Char -> Uri.encode(value.toString())
            is Double -> Uri.encode(value.toString())
            is Float -> Uri.encode(value.toString())
            is Int -> Uri.encode(value.toString())
            is Long -> Uri.encode(value.toString())
            is Short -> Uri.encode(value.toString())
            else -> if (value?.javaClass?.isArray == true) {
                if (value is Array<*>)
                    value.contentToString()
                else
                    value.toString()
                //when (value) {
                //    is ByteArray -> value.contentToString()
                //    is CharArray -> value.contentToString()
                //    is FloatArray -> value.contentToString()
                //    is ShortArray -> value.contentToString()
                //    is BooleanArray -> value.contentToString()
                //    is DoubleArray -> value.contentToString()
                //    is IntArray -> value.contentToString()
                //    is LongArray -> value.contentToString()
                //    is Array<*> -> value.contentToString()
                //    else -> value.toString()
                //}
            } else {
                value.toString()
            }
        }
        "${entryType}.${Uri.encode(key)}=$entryValue;"
    }
}