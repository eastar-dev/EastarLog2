package android.log

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.annotation.ContentView
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

abstract class LogActivity : ComponentActivity {

    constructor() : super()

    @ContentView
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun recreate() {
        Log.pc(Log.START, "recreate", "▶▶", javaClass, intent.toUri(0))
        super.recreate()
    }

    override fun startService(service: Intent): ComponentName? {
        Log.pc(Log.START, "startService", "▶▶", javaClass, service.toUri(0))
        return super.startService(service)
    }

    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        Log.pc(Log.START, "bindService", "▶▶", javaClass, service?.toUri(0))
        return super.bindService(service, conn, flags)
    }

    override fun bindService(service: Intent, flags: Int, executor: Executor, conn: ServiceConnection): Boolean {
        Log.pc(Log.START, "bindService", "▶▶", javaClass, service.toUri(0))
        return super.bindService(service, flags, executor, conn)
    }

    override fun unbindService(conn: ServiceConnection) {
        Log.pc(Log.START, "unbindService", "▶▶", javaClass)
        super.unbindService(conn)
    }

    override fun sendBroadcast(intent: Intent) {
        Log.pc(Log.START, "sendBroadcast", "▶▶", javaClass, intent.toUri(0))
        super.sendBroadcast(intent)
    }

    override fun startActivities(intents: Array<out Intent>?) {
        Log.pc(Log.START, "startActivities", "▶▶", javaClass, intents?.joinToString { it.toUri(0) })
        super.startActivities(intents)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        Log.pc(Log.START, if (requestCode == -1) "startActivity" else "startActivityForResult", "▶▶", "requestCode=$requestCode", javaClass, intent.toUri(0))
        super.startActivityForResult(intent, requestCode, options)
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode shr 16 == 0)
            onActivityResultLog(javaClass, requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.pc(Log.START, "▶▶", javaClass, newConfig)
        super.onConfigurationChanged(newConfig)
    }

    companion object {
        internal fun onActivityResultLog(clz: Class<*>, requestCode: Int, resultCode: Int, data: Intent?) {
            val level = if (resultCode == Activity.RESULT_OK) Log.INFO else Log.WARN
            Log.pm(
                level, "onActivityResult", "◀◀",
                clz,
                "requestCode=$requestCode",
                when (resultCode) {
                    RESULT_OK -> "Activity.RESULT_OK"
                    RESULT_CANCELED -> "Activity.RESULT_CANCELED"
                    else -> ""
                },
                data?.toString(),
                data?.extras?.let { b ->
                    b.keySet().sorted()
                        .joinToString(",") { k ->
                            val v = b.get(k)
                            if (v?.javaClass?.isArray == true)
                                "$k:${v.javaClass.simpleName}=${(v as Array<*>).contentToString()}"
                            else
                                "$k:${v?.javaClass?.simpleName}=$v"
                        }
                }
            )
        }
    }
}
