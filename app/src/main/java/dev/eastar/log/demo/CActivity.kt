package dev.eastar.log.demo

import android.app.Activity
import android.log.LogActivity
import android.os.Bundle
import android.widget.TextView

@Suppress("FunctionName", "unused", "NonAsciiCharacters")
class CActivity : LogActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "C"
            textSize = 100.sp
        })
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, intent.apply { putExtra("from", "CActivity") })
        super.onBackPressed()
    }
}
