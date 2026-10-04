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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AdMobManager {
    private const val TAG = "AdMobManager"
    private var isInitialized = false
    private var appContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
        appContext = context.applicationContext
        if (!isInitialized) {
            try {
                MobileAds.initialize(context.applicationContext) { initializationStatus ->
                    isInitialized = true
                    Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
                    loadAppOpenAd()
                    loadInterstitialAd()
                    loadRewardedAd()
                    loadRewardedInterstitialAd()
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to initialize AdMob SDK: ${e.message}")
            }
        }
    }

    // --- App Open Ad ---
    fun loadAppOpenAd(context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext ?: return
        if (appContext == null) appContext = ctx
        if (appOpenAd != null || isLoadingAppOpen) return
        isLoadingAppOpen = true
        try {
            val adRequest = AdRequest.Builder().build()
            AppOpenAd.load(
                ctx,
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
                        Log.w(TAG, "App Open Ad Failed to load: ${loadAdError.message}. Retrying in 10s...")
                        scope.launch {
                            delay(10000L)
                            if (appOpenAd == null) loadAppOpenAd()
                        }
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
            appOpenAd = null
            scope.launch {
                delay(2000L)
                loadAppOpenAd()
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadAppOpenAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "App Open Ad Failed to show: ${adError.message}")
                    loadAppOpenAd()
                    onAdDismissed()
                }
            }
            ad.show(activity)
            return true
        } else {
            loadAppOpenAd(activity)
            onAdDismissed()
            return false
        }
    }

    // --- Interstitial Ad ---
    fun loadInterstitialAd(context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext ?: return
        if (appContext == null) appContext = ctx
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                ctx,
                INTERSTITIAL_AD_UNIT,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                        Log.d(TAG, "Interstitial Ad Loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        Log.w(TAG, "Interstitial Ad Failed: ${error.message} (code: ${error.code}). Retrying in 5s...")
                        scope.launch {
                            delay(5000L)
                            if (interstitialAd == null) loadInterstitialAd()
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            isLoadingInterstitial = false
            interstitialAd = null
        }
    }

    fun hasInterstitialAd(): Boolean = interstitialAd != null

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit = {}): Boolean {
        val ad = interstitialAd
        if (ad != null) {
            interstitialAd = null // Consumed immediately to prevent duplicate display
            scope.launch {
                delay(1000L)
                loadInterstitialAd()
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadInterstitialAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial Ad Failed to show: ${error.message}")
                    loadInterstitialAd()
                    onAdDismissed()
                }
            }
            ad.show(activity)
            return true
        } else {
            loadInterstitialAd(activity)
            onAdDismissed()
            return false
        }
    }

    // --- Rewarded Ad ---
    fun loadRewardedAd(context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext ?: return
        if (appContext == null) appContext = ctx
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                ctx,
                REWARDED_AD_UNIT,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                        Log.d(TAG, "Rewarded Ad Loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                        Log.w(TAG, "Rewarded Ad Failed: ${error.message} (code: ${error.code}). Retrying in 5s...")
                        scope.launch {
                            delay(5000L)
                            if (rewardedAd == null) loadRewardedAd()
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            isLoadingRewarded = false
            rewardedAd = null
        }
    }

    fun hasRewardedAd(): Boolean = rewardedAd != null

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onAdDismissed: () -> Unit
    ): Boolean {
        val ad = rewardedAd
        if (ad != null) {
            rewardedAd = null // Consumed immediately
            scope.launch {
                delay(1000L)
                loadRewardedAd()
            }
            var rewardGranted = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadRewardedAd()
                    if (!rewardGranted) {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded Ad Failed to show: ${error.message}")
                    loadRewardedAd()
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
    fun loadRewardedInterstitialAd(context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext ?: return
        if (appContext == null) appContext = ctx
        if (rewardedInterstitialAd != null || isLoadingRewardedInterstitial) return
        isLoadingRewardedInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedInterstitialAd.load(
                ctx,
                REWARDED_INTERSTITIAL_AD_UNIT,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        rewardedInterstitialAd = ad
                        isLoadingRewardedInterstitial = false
                        Log.d(TAG, "Rewarded Interstitial Ad Loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedInterstitialAd = null
                        isLoadingRewardedInterstitial = false
                        Log.w(TAG, "Rewarded Interstitial Ad Failed: ${error.message} (code: ${error.code}). Retrying in 5s...")
                        scope.launch {
                            delay(5000L)
                            if (rewardedInterstitialAd == null) loadRewardedInterstitialAd()
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            isLoadingRewardedInterstitial = false
            rewardedInterstitialAd = null
        }
    }

    fun hasRewardedInterstitialAd(): Boolean = rewardedInterstitialAd != null

    fun showRewardedInterstitialAd(
        activity: Activity,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onAdDismissed: () -> Unit
    ): Boolean {
        val ad = rewardedInterstitialAd
        if (ad != null) {
            rewardedInterstitialAd = null // Consumed immediately
            scope.launch {
                delay(1000L)
                loadRewardedInterstitialAd()
            }
            var rewardGranted = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadRewardedInterstitialAd()
                    if (!rewardGranted) {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded Interstitial Ad Failed to show: ${error.message}")
                    loadRewardedInterstitialAd()
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

    /**
     * Tries to show any available rewarded ad format (Rewarded Interstitial first, then Rewarded Ad).
     * If neither is ready, triggers background preloading for both and returns false.
     */
    fun showAnyRewardedAd(
        activity: Activity,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onAdDismissed: () -> Unit
    ): Boolean {
        if (hasRewardedInterstitialAd()) {
            return showRewardedInterstitialAd(activity, onRewardEarned, onAdDismissed)
        }
        if (hasRewardedAd()) {
            return showRewardedAd(activity, onRewardEarned, onAdDismissed)
        }
        loadRewardedAd(activity)
        loadRewardedInterstitialAd(activity)
        return false
    }
}
