package android.log

import androidx.annotation.ContentView

abstract class LogFragment : androidx.fragment.app.Fragment {
    constructor() : super()

    @ContentView
    constructor(contentLayoutId: Int) : super(contentLayoutId)
}