@file:Suppress("unused")

package android.log

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LifeCycleLog

fun Application.logActivity() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        logFragment(activity)
        val ext = if (activity?.javaClass?.getAnnotation(Metadata::class.java) == null) "java" else "kt"
        activity?.javaClass?.simpleName.let { Log.println(Log.ERROR, "onActivityCreated", "($it.$ext:0)", it) }
    }

    override fun onActivityDestroyed(activity: Activity?) {
        val ext = if (activity?.javaClass?.getAnnotation(Metadata::class.java) == null) "java" else "kt"
        activity?.javaClass?.simpleName.let { Log.println(Log.WARN, "onActivityDestroyed", "($it.$ext:0)", it) }
    }

    override fun onActivityStarted(activity: Activity?) {}
    override fun onActivityStopped(activity: Activity?) {}
    override fun onActivityPaused(activity: Activity?) {}
    override fun onActivityResumed(activity: Activity?) {}
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
})

private fun logFragment(activity: Activity?) {
    (activity as? AppCompatActivity)?.run {
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentCreated(fm: androidx.fragment.app.FragmentManager, f: androidx.fragment.app.Fragment, savedInstanceState: Bundle?) {
                val ext = if (f.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
                f.javaClass.simpleName.takeUnless { "SupportRequestManagerFragment" == it }?.let { Log.println(Log.ERROR, "onFragmentCreated", "($it.$ext:0)", it) }
            }

            override fun onFragmentDestroyed(fm: androidx.fragment.app.FragmentManager, f: androidx.fragment.app.Fragment) {
                val ext = if (f.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
                f.javaClass.simpleName.takeUnless { "SupportRequestManagerFragment" == it }?.let { Log.println(Log.WARN, "onFragmentDestroyed", "($it.$ext:0)", it) }
            }
        }, true)
    }
}

