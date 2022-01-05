package android.log

import android.content.Intent

abstract class LogFragment : androidx.fragment.app.Fragment() {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogActivity.onActivityResultLog(javaClass, requestCode, resultCode, data)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        Log.pc(
            Log.ERROR,
            if (requestCode == -1) "startActivity" else "startActivityForResult",
            "▶▶",
            javaClass,
            intent.component?.shortClassName ?: intent.toUri(0),
            intent,
            "0x%08X".format(requestCode)
        )
        super.startActivityForResult(intent, requestCode)
    }
}
