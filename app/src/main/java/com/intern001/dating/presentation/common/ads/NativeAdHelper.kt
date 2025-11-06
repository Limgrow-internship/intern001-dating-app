package com.intern001.dating.presentation.common.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.intern001.dating.R

object NativeAdHelper {
    fun bindNativeAd(
        context: Context,
        adContainer: ViewGroup,
        nativeAd: NativeAd?,
        layoutId: Int,
        onBind: ((NativeAdView, NativeAd) -> Unit)? = null,
    ) {
        if (nativeAd == null) return
        val adView = LayoutInflater.from(context).inflate(layoutId, adContainer, false) as NativeAdView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_icon)

        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        if (mediaView != null) {
            adView.mediaView = mediaView
            adView.mediaView?.setMediaContent(nativeAd.mediaContent)
        }

        (adView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
        (adView.headlineView as? TextView)?.text = nativeAd.headline
        (adView.bodyView as? TextView)?.text = nativeAd.body
        (adView.callToActionView as? Button)?.text = nativeAd.callToAction
        adView.setNativeAd(nativeAd)
        adContainer.removeAllViews()
        adContainer.addView(adView)
        onBind?.invoke(adView, nativeAd)
    }
}
