package io.livekit.android.sample.livestream

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.github.ajalt.timberkt.Timber
import io.livekit.android.LiveKit
import io.livekit.android.util.LoggingLevel
import timber.log.Timber.DebugTree

class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        LiveKit.loggingLevel = LoggingLevel.VERBOSE
        LiveKit.create(this).release()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}