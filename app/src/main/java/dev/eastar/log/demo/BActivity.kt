package dev.eastar.log.demo

import android.app.Activity
import android.content.Intent
import android.log.LogActivity
import android.os.Bundle
import android.widget.TextView

@Suppress("FunctionName", "unused", "NonAsciiCharacters")
class BActivity : LogActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "B"
            textSize = 100.sp
            setOnClickListener { startActivityForResult(Intent(context, CActivity::class.java),2) }
        })
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, intent.apply { putExtra("from", "BActivity") })
        super.onBackPressed()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
