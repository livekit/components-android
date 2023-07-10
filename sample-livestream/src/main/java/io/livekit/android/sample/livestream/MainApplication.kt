package io.livekit.android.sample.livestream

import android.app.Application
import com.github.ajalt.timberkt.Timber
import io.livekit.android.LiveKit
import io.livekit.android.util.LoggingLevel
import timber.log.Timber.DebugTree

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        LiveKit.loggingLevel = LoggingLevel.VERBOSE
        LiveKit.create(this).release()
    }
}