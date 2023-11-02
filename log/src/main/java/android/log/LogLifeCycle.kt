@file:Suppress("unused")

package android.log

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

typealias LogLifeCycle = Unit

fun Application.logLifeCycle() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(caller: Activity, savedInstanceState: Bundle?) {
        (caller as? FragmentActivity)?.logFragment()
        val locator = caller.getLocator()
        val log = "${caller.javaClass.simpleName}{${caller.hashCode().toString(16)}}"
        Log.pml(Log.LIFECYCLE_CREATE, "onActivityCreated", locator, "▶$log", Log._DUMP(caller.intent).replace("\n", ","))
    }

    override fun onActivityDestroyed(caller: Activity) {
        val locator = caller.getLocator()
        val log = "${caller.javaClass.simpleName}{${caller.hashCode().toString(16)}}"
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
            override fun onFragmentCreated(fm: FragmentManager, caller: Fragment, savedInstanceState: Bundle?) {
                if ("SupportRequestManagerFragment" == caller.javaClass.simpleName) return
                val locator = caller.getLocator()
                val log = "${caller.javaClass.simpleName}{${caller.hashCode().toString(16)}}"
                Log.pml(Log.LIFECYCLE_CREATE, "onFragmentCreated", locator, "▷$log", Log._DUMP(caller.arguments).replace("\n", ","))
            }

            override fun onFragmentDestroyed(fm: FragmentManager, caller: Fragment) {
                super.onFragmentDestroyed(fm, caller)
                if ("SupportRequestManagerFragment" == caller.javaClass.simpleName) return
                val locator = caller.getLocator()
                val log = "${caller.javaClass.simpleName}{${caller.hashCode().toString(16)}}"
                Log.pml(Log.LIFECYCLE_DESTROYED, "onFragmentDestroyed", locator, "◁$log")
            }
        }, true
    )
}

fun Application.logActivity() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(caller: Activity, savedInstanceState: Bundle?) {
        val locator = caller.getLocator()
        val log = "${caller.javaClass.simpleName}{${caller.hashCode().toString(16)}}"
        Log.pml(Log.LIFECYCLE_CREATE, "onActivityCreated", locator, "▶$log", Log._DUMP(caller.intent).replace("\n", ","))
    }

    override fun onActivityDestroyed(caller: Activity) {
        val locator = caller.getLocator()
        val log = "${caller.javaClass.simpleName}{${caller.hashCode().toString(16)}}"
        Log.pml(Log.LIFECYCLE_DESTROYED, "onActivityDestroyed", locator, "◀$log")
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
})

private fun Any.getLocator(): String {
    val ext = if (javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
    return "(${javaClass.simpleName}.$ext:0)"
}
