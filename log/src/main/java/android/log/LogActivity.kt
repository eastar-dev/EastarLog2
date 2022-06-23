package android.log

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executor

abstract class LogActivity : AppCompatActivity {

    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun startService(service: Intent): ComponentName? {
        Log.pc(Log.START, "startService", "▶▶", javaClass, service.component?.shortClassName ?: service.toUri(0), service)
        return super.startService(service)
    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        Log.pc(Log.START, "bindService", "▶▶", javaClass, service?.component?.shortClassName ?: service?.toUri(0), service)
        return super.bindService(service, conn, flags)
    }

    override fun bindService(service: Intent, flags: Int, executor: Executor, conn: ServiceConnection): Boolean {
        Log.pc(Log.START, "bindService", "▶▶", javaClass, service.component?.shortClassName ?: service.toUri(0), service)
        return super.bindService(service, flags, executor, conn)
    }

    override fun unbindService(conn: ServiceConnection) {
        Log.pc(Log.START, "unbindService", "▶▶", javaClass, conn)
        super.unbindService(conn)
    }

    override fun sendBroadcast(intent: Intent) {
        Log.pc(Log.START, "sendBroadcast", "▶▶", javaClass, intent.component?.shortClassName ?: intent.toUri(0), intent)
        super.sendBroadcast(intent)
    }

    override fun startActivities(intents: Array<out Intent>?) {
        Log.pc(Log.START, "startActivities", "▶▶", javaClass, intents?.forEach { it.component?.shortClassName ?: it.toUri(0) }, intents)
        super.startActivities(intents)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        Log.pc(Log.START, if (requestCode == -1) "startActivity" else "startActivityForResult", "▶▶", javaClass, intent.component?.shortClassName ?: intent.toUri(0), intent, "0x%08X".format(requestCode))
        super.startActivityForResult(intent, requestCode, options)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode shr 16 == 0)
            onActivityResultLog(javaClass, requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        internal fun onActivityResultLog(clz: Class<*>, requestCode: Int, resultCode: Int, data: Intent?) {
            val level = if (resultCode == Activity.RESULT_OK) Log.INFO else Log.WARN
            Log.pm(level, "onActivityResult", "◀◀",
                clz,
                "requestCode=0x%08x".format(requestCode),
                when (resultCode) {
                    Activity.RESULT_OK -> "Activity.RESULT_OK"
                    Activity.RESULT_CANCELED -> "Activity.RESULT_CANCELED"
                    else -> ""
                })
            if (data != null && data.extras != null)
                Log.p(level, data.extras)

        }
    }
}