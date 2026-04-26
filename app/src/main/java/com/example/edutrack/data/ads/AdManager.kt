package com.example.edutrack.data.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private val initialized = AtomicBoolean(false)

    fun initialize(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            MobileAds.initialize(context.applicationContext)
        }
    }
}
