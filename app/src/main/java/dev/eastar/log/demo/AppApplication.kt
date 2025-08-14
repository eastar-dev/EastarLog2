package dev.eastar.log.demo

import android.app.Application
import android.content.Context
import android.log.Log

class AppApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.NOT_PREDICATE = {
            it.fileName in listOf(
                "TimberTree.kt",
                "Timber.kt",
            ) ||
                    it.methodName.startsWith("_DUMP")
        }
    }
}