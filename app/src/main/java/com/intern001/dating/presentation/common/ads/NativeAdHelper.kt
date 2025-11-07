package com.intern001.dating.presentation.common.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.intern001.dating.databinding.LayoutNativeAdFullBinding
import com.intern001.dating.databinding.LayoutNativeAdSmallBinding

object NativeAdHelper {

    fun bindNativeAdSmall(
        context: Context,
        adContainer: ViewGroup,
        nativeAd: NativeAd?,
        onBind: ((NativeAdView, NativeAd) -> Unit)? = null,
    ) {
        if (nativeAd == null) return
        val binding = LayoutNativeAdSmallBinding.inflate(LayoutInflater.from(context), adContainer, false)
        val adView = binding.root

        adView.headlineView = binding.adHeadline
        adView.bodyView = binding.adBody
        adView.callToActionView = binding.adCallToAction
        adView.iconView = binding.adIcon

        (adView.iconView as? android.widget.ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
        (adView.headlineView as? android.widget.TextView)?.text = nativeAd.headline
        (adView.bodyView as? android.widget.TextView)?.text = nativeAd.body
        (adView.callToActionView as? android.widget.Button)?.text = nativeAd.callToAction
        adView.setNativeAd(nativeAd)
        adContainer.removeAllViews()
        adContainer.addView(adView)
        onBind?.invoke(adView, nativeAd)
    }

    fun bindNativeAdFull(
        context: Context,
        adContainer: ViewGroup,
        nativeAd: NativeAd?,
        onBind: ((NativeAdView, NativeAd) -> Unit)? = null,
    ) {
        if (nativeAd == null) return
        val binding = LayoutNativeAdFullBinding.inflate(LayoutInflater.from(context), adContainer, false)
        val adView = binding.root

        adView.headlineView = binding.adHeadline
        adView.bodyView = binding.adBody
        adView.callToActionView = binding.adCallToAction
        adView.iconView = binding.adIcon
        adView.mediaView = binding.adMedia

        (adView.iconView as? android.widget.ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
        (adView.headlineView as? android.widget.TextView)?.text = nativeAd.headline
        (adView.bodyView as? android.widget.TextView)?.text = nativeAd.body
        (adView.callToActionView as? android.widget.Button)?.text = nativeAd.callToAction
        // Media
        binding.adMedia.setMediaContent(nativeAd.mediaContent)
        adView.setNativeAd(nativeAd)
        adContainer.removeAllViews()
        adContainer.addView(adView)
        onBind?.invoke(adView, nativeAd)
    }
}
