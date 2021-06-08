package android.log

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class UncaughtExceptionHandler(context: Context, actor: ((Array<File>) -> Unit)? = null) {
    var path: String = "temp"
    var timeFormatter: String = "yyyyMMdd-HHmmss"
    var versionTag: String = "v"

    init {
        lastActivityWeakReference(context)
        uncaughtExceptionHandler {
            val uncaughtExceptionFilename = uncaughtExceptionFilename
            uncaughtScreen(context, uncaughtExceptionFilename)
            uncaughtStackTraceText(context, uncaughtExceptionFilename, it)
        }

        File(context.externalCacheDir, path).listFiles()?.let { actor?.invoke(it) }
    }


    fun clear(context: Context) {
        File(context.externalCacheDir, path).listFiles()?.forEach { file ->
            Log.e(file)
            file.deleteRecursively()
        }
    }


    private var mLastActivityWeakReference: WeakReference<Activity>? = null
    private fun lastActivityWeakReference(context: Context) {
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                mLastActivityWeakReference = WeakReference(activity)
            }
        })
    }

    private fun uncaughtExceptionHandler(block: (stackTraceText: String) -> Unit) {
        val def = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            block(throwable.stackTraceToString())
            def?.uncaughtException(thread, throwable)
        }
    }

    private val now get() = SimpleDateFormat(timeFormatter, Locale.getDefault()).format(Date(System.currentTimeMillis()))
    private val uncaughtExceptionFilename: String get() = mLastActivityWeakReference?.get()?.let { uncaughtExceptionFilename(it) } ?: now


    private fun uncaughtExceptionFilename(activity: Activity): String {
        var name = now + ":" + activity.javaClass.simpleName
        (activity.window.decorView as ViewGroup).findViewByChild(WebView::class.java)?.let {
            name += ":" + it.url?.substringAfterLast("/")?.substringBeforeLast(".")
            name += ":" + it.title
        }
        return name
    }

    private fun uncaughtStackTraceText(context: Context, filename: String, stackTraceText: String) {
        File(context.externalCacheDir, "$path/$filename.txt")
            .apply { parentFile?.mkdirs() }
            .writeText("$versionTag\n$filename\n$stackTraceText")
    }

    private fun uncaughtScreen(context: Context, filename: String) {
        mLastActivityWeakReference?.get()?.findViewById<View>(Window.ID_ANDROID_CONTENT)?.drawToBitmap(Bitmap.Config.RGB_565)
            ?.let { bitmap ->
                File(context.externalCacheDir, "$path/$filename.jpeg")
                    .apply { parentFile?.mkdirs() }
                    .outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                    }
            }
    }

    ////////////////////////////////////////////////////////////////////////////////
    @Suppress("UNCHECKED_CAST")
    private fun <T : View> ViewGroup.findViewByChild(clz: Class<T>): T? {
        val result = children.firstOrNull { clz.isInstance(it) }
            ?: children.filter { it is ViewGroup }
                .map { (it as ViewGroup).findViewByChild(clz) }
                .firstOrNull { clz.isInstance(it) }
        return result as? T
    }
}