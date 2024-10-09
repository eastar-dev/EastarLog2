package dev.eastar.log.demo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class BActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it: ActivityResult ->
        }

        setContentView(TextView(this).apply {
            text = "B"
            textSize = 100.sp
            setOnClickListener { launcher.launch(Intent(context, CActivity::class.java)) }
        })


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(RESULT_OK, intent.apply { putExtra("from", "BActivity") })
            }
        }

        // OnBackPressedDispatcher에 콜백 등록
        onBackPressedDispatcher.addCallback(this, callback)
    }

}
