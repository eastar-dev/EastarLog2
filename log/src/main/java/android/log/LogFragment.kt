package android.log

import android.content.Intent
import androidx.annotation.ContentView

abstract class LogFragment : androidx.fragment.app.Fragment {
    constructor() : super()

    @ContentView
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogActivity.onActivityResultLog(javaClass, requestCode, resultCode, data)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        Log.pc(Log.START, if (requestCode == -1) "startActivity" else "startActivityForResult", "▶▶", "requestCode=$requestCode", javaClass, intent.toUri(0))
        super.startActivityForResult(intent, requestCode)
    }
}
