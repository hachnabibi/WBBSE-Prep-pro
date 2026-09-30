package com.example.ui.components

import android.app.Activity
import android.content.ContextWrapper
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.AdMobManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun AdMobNativeCard(
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var useNativeView by remember { mutableStateOf(false) }

    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    DisposableEffect(context) {
        val targetContext = activity ?: context
        try {
            val adLoader = AdLoader.Builder(targetContext, AdMobManager.NATIVE_ADVANCED_AD_UNIT)
                .forNativeAd { nativeAd ->
                    loadedNativeAd = nativeAd
                    useNativeView = true
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdMobNativeCard", "Native ad failed: ${error.message}, code: ${error.code}")
                        useNativeView = false
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Throwable) {
            Log.e("AdMobNativeCard", "AdLoader exception: ${e.message}")
            useNativeView = false
        }

        onDispose {
            loadedNativeAd?.destroy()
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (useNativeView && loadedNativeAd != null) {
                // Official Real Google AdMob NativeAdView
                AndroidView(
                    factory = { ctx ->
                        val nativeAdView = NativeAdView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                        val dp = ctx.resources.displayMetrics.density
                        val p = (12 * dp).toInt()

                        val cardContainer = LinearLayout(ctx).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(p, p, p, p)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        // Top row: Ad Badge + Advertiser
                        val topRow = LinearLayout(ctx).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                bottomMargin = (8 * dp).toInt()
                            }
                        }

                        val adBadge = TextView(ctx).apply {
                            text = "Test Ad"
                            setTextColor(android.graphics.Color.WHITE)
                            textSize = 10f
                            typeface = Typeface.DEFAULT_BOLD
                            val hp = (6 * dp).toInt()
                            val vp = (2 * dp).toInt()
                            setPadding(hp, vp, hp, vp)
                            background = GradientDrawable().apply {
                                setColor(android.graphics.Color.parseColor("#EA4335"))
                                cornerRadius = 4 * dp
                            }
                        }
                        topRow.addView(adBadge)

                        val advertiserView = TextView(ctx).apply {
                            setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                            textSize = 11f
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                                marginStart = (8 * dp).toInt()
                            }
                        }
                        topRow.addView(advertiserView)
                        nativeAdView.advertiserView = advertiserView
                        cardContainer.addView(topRow)

                        // Middle: Icon + Headline & Body
                        val middleRow = LinearLayout(ctx).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                bottomMargin = (8 * dp).toInt()
                            }
                        }

                        val iconView = ImageView(ctx).apply {
                            val s = (40 * dp).toInt()
                            layoutParams = LinearLayout.LayoutParams(s, s).apply {
                                marginEnd = (10 * dp).toInt()
                            }
                        }
                        middleRow.addView(iconView)
                        nativeAdView.iconView = iconView

                        val textCol = LinearLayout(ctx).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        }

                        val headlineView = TextView(ctx).apply {
                            setTextColor(android.graphics.Color.WHITE)
                            textSize = 14f
                            typeface = Typeface.DEFAULT_BOLD
                            maxLines = 2
                        }
                        textCol.addView(headlineView)
                        nativeAdView.headlineView = headlineView

                        val bodyView = TextView(ctx).apply {
                            setTextColor(android.graphics.Color.parseColor("#CBD5E1"))
                            textSize = 12f
                            maxLines = 2
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = (2 * dp).toInt()
                            }
                        }
                        textCol.addView(bodyView)
                        nativeAdView.bodyView = bodyView

                        middleRow.addView(textCol)
                        cardContainer.addView(middleRow)

                        val ctaBtn = android.widget.Button(ctx).apply {
                            setTextColor(android.graphics.Color.WHITE)
                            textSize = 13f
                            typeface = Typeface.DEFAULT_BOLD
                            background = GradientDrawable().apply {
                                setColor(android.graphics.Color.parseColor("#4F46E5"))
                                cornerRadius = 10 * dp
                            }
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (40 * dp).toInt()
                            )
                        }
                        cardContainer.addView(ctaBtn)
                        nativeAdView.callToActionView = ctaBtn

                        nativeAdView.addView(cardContainer)
                        nativeAdView
                    },
                    update = { view ->
                        val ad = loadedNativeAd
                        if (ad != null) {
                            (view.headlineView as? TextView)?.text = ad.headline
                            (view.bodyView as? TextView)?.text = ad.body
                            (view.callToActionView as? android.widget.Button)?.text = ad.callToAction ?: "Install"
                            if (ad.icon != null) {
                                view.iconView?.visibility = View.VISIBLE
                                (view.iconView as? ImageView)?.setImageDrawable(ad.icon?.drawable)
                            } else {
                                view.iconView?.visibility = View.GONE
                            }
                            if (ad.advertiser != null) {
                                view.advertiserView?.visibility = View.VISIBLE
                                (view.advertiserView as? TextView)?.text = ad.advertiser
                            } else {
                                view.advertiserView?.visibility = View.GONE
                            }
                            view.setNativeAd(ad)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Official Real Google AdMob Test Ad (Medium Rectangle 300x250)
                // Guaranteed 100% fill by Google with "Test Ad", Google colored logo, and creative
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.MEDIUM_RECTANGLE)
                            adUnitId = AdMobManager.BANNER_AD_UNIT
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(250.dp)
                )
            }
        }
    }
}
