package dev.eastar.log.demo

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class CActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "C"
            textSize = 100.sp
        })
    }

    override fun onBackPressed() {
        setResult(RESULT_OK, intent.apply { putExtra("from", "CActivity") })
        super.onBackPressed()
    }
}
