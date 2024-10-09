package dev.eastar.log.demo

import android.app.Application
import android.content.Context
import android.log.Log
import android.log.logLifeCycle
import easteregg.easterEgg

class AppApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.NOT_REGEX = "dev.eastar.log.demo.BActivity".toRegex()
    }
}