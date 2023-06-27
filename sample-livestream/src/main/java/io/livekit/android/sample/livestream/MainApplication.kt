package io.livekit.android.sample.livestream

import android.app.Application
import com.github.ajalt.timberkt.Timber
import io.livekit.android.LiveKit
import timber.log.Timber.DebugTree

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //LiveKit.loggingLevel = LoggingLevel.VERBOSE
        LiveKit.create(this).release()
        Timber.plant(DebugTree())
    }
}