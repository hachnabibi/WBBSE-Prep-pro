package com.example

import android.app.Application
import android.util.Log
import com.example.notification.MadhyamikNotificationManager
import com.google.firebase.FirebaseApp

class DeshExamApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("DeshExamApplication", "Application onCreate started")
        
        // Initialize Firebase safely
        try {
            var app = FirebaseApp.initializeApp(this)
            if (app == null && FirebaseApp.getApps(this).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:950229620449:android:com.wbbseprep.app")
                    .setApiKey("AIzaSyFallbackDummyKeyForLocalPreview0000")
                    .setProjectId("ais-asia-southeast1-dd4cee0a05")
                    .setGcmSenderId("950229620449")
                    .build()
                app = FirebaseApp.initializeApp(this, options)
                Log.i("DeshExamApplication", "FirebaseApp initialized with local fallback options.")
            }
            if (app != null) {
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().isAutoInitEnabled = false
                } catch (e: Throwable) {
                    Log.e("DeshExamApplication", "FCM auto-init disable failed: ${e.message}")
                }
                try {
                    val isFallback = (app.options.applicationId == "1:123456789012:android:a1b2c3d4e5f6g7h8")
                    if (isFallback) {
                        com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
                        Log.i("DeshExamApplication", "Crashlytics collection disabled for dummy fallback options.")
                    }
                } catch (e: Throwable) {
                    Log.e("DeshExamApplication", "Crashlytics config failed: ${e.message}")
                }
                Log.i("DeshExamApplication", "FirebaseApp successfully initialized.")
            } else {
                Log.e("DeshExamApplication", "FirebaseApp.initializeApp returned null.")
            }
        } catch (e: Throwable) {
            Log.e("DeshExamApplication", "CRITICAL: FirebaseApp initialization failed: ${e.message}", e)
        }

        // Initialize notification channels safely
        try {
            MadhyamikNotificationManager.createNotificationChannels(this)
            Log.i("DeshExamApplication", "Notification channels initialized.")
        } catch (e: Throwable) {
            Log.e("DeshExamApplication", "Notification channels setup failed: ${e.message}", e)
        }

        // Initialize Google Mobile Ads SDK safely and lazily
        try {
            com.example.util.AdMobManager.initialize(this)
            Log.i("DeshExamApplication", "AdMob initialized.")
        } catch (e: Throwable) {
            Log.e("DeshExamApplication", "AdMob initialization failed: ${e.message}", e)
        }
        
        Log.i("DeshExamApplication", "Application onCreate completed")
    }
}
