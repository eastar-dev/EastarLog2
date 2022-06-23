package android.log

import androidx.annotation.ContentView
import androidx.appcompat.app.AppCompatActivity

abstract class LogActivity : AppCompatActivity {

    constructor() : super()

    @ContentView
    constructor(contentLayoutId: Int) : super(contentLayoutId)
}
