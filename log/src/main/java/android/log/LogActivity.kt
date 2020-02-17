package android.log

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class LogActivity : AppCompatActivity() {
    override fun startService(service: Intent?): ComponentName? {
        Log.pc(Log.ERROR, "startService", "▶▶", javaClass, service?.component?.shortClassName ?: service?.toUri(0), service)
        return super.startService(service)
    }

    override fun sendBroadcast(intent: Intent?) {
        Log.pc(Log.ERROR, "sendBroadcast", "▶▶", javaClass, intent?.component?.shortClassName ?: intent?.toUri(0), intent)
        super.sendBroadcast(intent)
    }

    override fun startActivities(intents: Array<out Intent>?) {
        Log.pc(Log.ERROR, "startActivities", "▶▶", javaClass, intents?.forEach { it.component?.shortClassName ?: it.toUri(0) }, intents)
        super.startActivities(intents)
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        Log.pc(Log.ERROR, if (requestCode == -1) "startActivity" else "startActivityForResult", "▶▶", javaClass, intent?.component?.shortClassName ?: intent?.toUri(0), intent, "0x%08X".format(requestCode))
        super.startActivityForResult(intent, requestCode, options)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode shr 16 == 0)
            onActivityResultLog(javaClass, requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        internal fun onActivityResultLog(clz: Class<*>, requestCode: Int, resultCode: Int, data: Intent?) {
            val level = if (resultCode == Activity.RESULT_OK) Log.INFO else Log.ERROR
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