package io.livekit.android.sample.livestream

import android.app.Application
import com.github.ajalt.timberkt.Timber
import timber.log.Timber.DebugTree

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //LiveKit.loggingLevel = LoggingLevel.VERBOSE
        Timber.plant(DebugTree())
    }
}