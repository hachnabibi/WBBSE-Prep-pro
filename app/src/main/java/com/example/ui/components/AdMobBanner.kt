package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.AdMobManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT,
    isBengali: Boolean = true
) {
    var hasLoadFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (hasLoadFailed) {
            AdBannerCard(
                isBengali = isBengali,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                hasLoadFailed = true
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}

