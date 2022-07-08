@file:Suppress("unused")

package android.log

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun Application.logLifeCycle() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        logFragment(activity)
        val ext = if (activity.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
        activity.javaClass.simpleName.let { Log.pl(Log.LIFECYCLE_CREATE, "onActivityCreated", "($it.$ext:0)", it) }
    }

    override fun onActivityDestroyed(activity: Activity) {
        val ext = if (activity.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
        activity.javaClass.simpleName.let { Log.pl(Log.LIFECYCLE_DESTROYED, "onActivityDestroyed", "($it.$ext:0)", it) }
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
                        Log.pl(Log.LIFECYCLE_CREATE, "onFragmentCreated", "($it.$ext:0)", it)
                    }
                }

                override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                    val ext = if (f.javaClass.getAnnotation(Metadata::class.java) == null) "java" else "kt"
                    f.javaClass.simpleName.takeUnless {
                        "SupportRequestManagerFragment" == it
                    }?.let {
                        Log.pl(Log.LIFECYCLE_DESTROYED, "onFragmentDestroyed", "($it.$ext:0)", it)
                    }
                }
            }, true
        )
    }
}
