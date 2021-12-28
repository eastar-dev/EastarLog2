@file:Suppress("unused")

package android.log

import android.app.Activity
import android.app.Application
import android.log.Log.takeLastSafe
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun Application.logLifeCycle() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        logFragment(activity)
        val ext = if (activity.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
        activity.javaClass.simpleName.let { Log.pt(Log.ERROR, "onActivityCreated.($it.$ext:0)".takeLastSafe(), it) }
    }

    override fun onActivityDestroyed(activity: Activity) {
        val ext = if (activity.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
        activity.javaClass.simpleName.let { Log.pt(Log.WARN, "onActivityDestroyed.($it.$ext:0)".takeLastSafe(), it) }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
})

private fun logFragment(activity: Activity) {
    (activity as? AppCompatActivity)?.run {
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                    val ext = if (f.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
                    f.javaClass.simpleName.takeUnless {
                        "SupportRequestManagerFragment" == it
                    }?.let {
                        Log.pt(Log.ERROR, "onFragmentCreated.($it.$ext:0)".takeLastSafe(), it)
                    }
                }

                override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                    val ext = if (f.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
                    f.javaClass.simpleName.takeUnless {
                        "SupportRequestManagerFragment" == it
                    }?.let {
                        Log.pt(Log.WARN, "onFragmentDestroyed.($it.$ext:0)".takeLastSafe(), it)
                    }
                }
            }, true
        )
    }
}
