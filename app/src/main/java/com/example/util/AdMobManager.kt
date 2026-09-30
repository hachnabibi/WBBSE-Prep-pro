package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"
    private var isInitialized = false

    // Official Google AdMob Test Unit IDs (100% safe for testing and Play Store compliance)
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val APP_OPEN_AD_UNIT = "ca-app-pub-3940256099942544/9257390701"
    const val NATIVE_ADVANCED_AD_UNIT = "ca-app-pub-3940256099942544/2247696110"
    const val BANNER_AD_UNIT = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_AD_UNIT = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_AD_UNIT = "ca-app-pub-3940256099942544/5224354917"
    const val REWARDED_INTERSTITIAL_AD_UNIT = "ca-app-pub-3940256099942544/5354046379"

    // Production AdMob Unit IDs (Saved for release builds)
    const val PROD_APP_ID = "ca-app-pub-2002844631471576~1374859376"
    const val PROD_APP_OPEN_AD_UNIT = "ca-app-pub-2002844631471576/1983399534"
    const val PROD_NATIVE_ADVANCED_AD_UNIT = "ca-app-pub-2002844631471576/4101376542"
    const val PROD_BANNER_AD_UNIT = "ca-app-pub-2002844631471576/6671531976"
    const val PROD_INTERSTITIAL_AD_UNIT = "ca-app-pub-2002844631471576/3781530868"
    const val PROD_REWARDED_AD_UNIT = "ca-app-pub-2002844631471576/5760780803"
    const val PROD_REWARDED_INTERSTITIAL_AD_UNIT = "ca-app-pub-2002844631471576/1733041538"

    private var appOpenAd: AppOpenAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null

    private var isLoadingAppOpen = false
    private var isLoadingInterstitial = false
    private var isLoadingRewarded = false
    private var isLoadingRewardedInterstitial = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                MobileAds.initialize(context) { initializationStatus ->
                    isInitialized = true
                    Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
                    loadAppOpenAd(context)
                    loadInterstitialAd(context)
                    loadRewardedAd(context)
                    loadRewardedInterstitialAd(context)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to initialize AdMob SDK: ${e.message}")
            }
        }
    }

    // --- App Open Ad ---
    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isLoadingAppOpen) return
        isLoadingAppOpen = true
        try {
            val adRequest = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                APP_OPEN_AD_UNIT,
                adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAppOpen = false
                        Log.d(TAG, "App Open Ad Loaded")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        appOpenAd = null
                        isLoadingAppOpen = false
                        Log.e(TAG, "App Open Ad Failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            isLoadingAppOpen = false
        }
    }

    fun hasAppOpenAd(): Boolean = appOpenAd != null

    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit = {}): Boolean {
        val ad = appOpenAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    loadAppOpenAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "App Open Ad Failed to show: ${adError.message}")
                    appOpenAd = null
                    loadAppOpenAd(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity)
            return true
        } else {
            onAdDismissed()
            loadAppOpenAd(activity)
            return false
        }
    }

    // --- Interstitial Ad ---
    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                INTERSTITIAL_AD_UNIT,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                        Log.d(TAG, "Interstitial Ad Loaded")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        Log.e(TAG, "Interstitial Ad Failed: ${error.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            isLoadingInterstitial = false
            interstitialAd = null
        }
    }

    fun hasInterstitialAd(): Boolean = interstitialAd != null

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit): Boolean {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial Ad Failed to show: ${error.message}")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity)
            return true
        } else {
            onAdDismissed()
            loadInterstitialAd(activity)
            return false
        }
    }

    // --- Rewarded Ad ---
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                REWARDED_AD_UNIT,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                        Log.d(TAG, "Rewarded Ad Loaded")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                        Log.e(TAG, "Rewarded Ad Failed: ${error.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            isLoadingRewarded = false
            rewardedAd = null
        }
    }

    fun hasRewardedAd(): Boolean = rewardedAd != null

    fun showRewardedAd(activity: Activity, onRewardEarned: (amount: Int, type: String) -> Unit, onAdDismissed: () -> Unit): Boolean {
        val ad = rewardedAd
        if (ad != null) {
            var rewardGranted = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    if (!rewardGranted) {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded Ad Failed to show: ${error.message}")
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                rewardGranted = true
                onRewardEarned(rewardItem.amount, rewardItem.type)
                onAdDismissed()
            }
            return true
        } else {
            loadRewardedAd(activity)
            return false
        }
    }

    // --- Rewarded Interstitial Ad ---
    fun loadRewardedInterstitialAd(context: Context) {
        if (rewardedInterstitialAd != null || isLoadingRewardedInterstitial) return
        isLoadingRewardedInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedInterstitialAd.load(
                context,
                REWARDED_INTERSTITIAL_AD_UNIT,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        rewardedInterstitialAd = ad
                        isLoadingRewardedInterstitial = false
                        Log.d(TAG, "Rewarded Interstitial Ad Loaded")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedInterstitialAd = null
                        isLoadingRewardedInterstitial = false
                        Log.e(TAG, "Rewarded Interstitial Ad Failed: ${error.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            isLoadingRewardedInterstitial = false
            rewardedInterstitialAd = null
        }
    }

    fun hasRewardedInterstitialAd(): Boolean = rewardedInterstitialAd != null

    fun showRewardedInterstitialAd(activity: Activity, onRewardEarned: (amount: Int, type: String) -> Unit, onAdDismissed: () -> Unit): Boolean {
        val ad = rewardedInterstitialAd
        if (ad != null) {
            var rewardGranted = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedInterstitialAd = null
                    loadRewardedInterstitialAd(activity)
                    if (!rewardGranted) {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded Interstitial Ad Failed to show: ${error.message}")
                    rewardedInterstitialAd = null
                    loadRewardedInterstitialAd(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                rewardGranted = true
                onRewardEarned(rewardItem.amount, rewardItem.type)
                onAdDismissed()
            }
            return true
        } else {
            loadRewardedInterstitialAd(activity)
            return false
        }
    }
}
