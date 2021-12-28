package dev.eastar.log.demo

import android.app.Application
import android.content.Context
import android.log.Log
import android.log.logLifeCycle

class AppApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.logFilterClassNameRegex = "dev\\.eastar\\.log\\.demo\\.BActivity".toRegex()
        logLifeCycle()
    }
}