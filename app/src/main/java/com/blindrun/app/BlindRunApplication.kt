package com.blindrun.app

import android.app.Application
import com.amap.api.maps.MapsInitializer
import com.amap.api.location.AMapLocationClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BlindRunApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            MapsInitializer.updatePrivacyShow(this, true, true)
            MapsInitializer.updatePrivacyAgree(this, true)
            MapsInitializer.setApiKey(BuildConfig.AMAP_KEY)
            AMapLocationClient.setApiKey(BuildConfig.AMAP_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}