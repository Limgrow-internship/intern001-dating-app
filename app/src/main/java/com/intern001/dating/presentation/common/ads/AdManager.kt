package com.intern001.dating.presentation.common.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

object AdManager {
    var nativeAdSmall: NativeAd? = null
    var nativeAdFull: NativeAd? = null

    fun preloadNativeAds(context: Context, onAllLoaded: (() -> Unit)? = null) {
        var loadedCount = 0

        fun done() {
            loadedCount++
            Log.d("AdManager", "Ads loaded: $loadedCount")
            if (loadedCount == 2) onAllLoaded?.invoke()
        }

        AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad: NativeAd ->
                Log.d("AdManager", "[SMALL] headline=${ad.headline}, media=${ad.mediaContent}, icon=${ad.icon}")
                nativeAdSmall?.destroy()
                nativeAdSmall = ad
                done()
            }
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
            .loadAd(AdRequest.Builder().build())

        AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad: NativeAd ->
                Log.d("AdManager", "[FULL] headline=${ad.headline}, media=${ad.mediaContent}, icon=${ad.icon}")
                nativeAdFull?.destroy()
                nativeAdFull = ad
                done()
            }
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    fun clear() {
        Log.d("AdManager", "Clearing ads!")
        nativeAdSmall?.destroy()
        nativeAdSmall = null
        nativeAdFull?.destroy()
        nativeAdFull = null
    }
}
