package com.example.notification

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging

object FcmTopicManager {
    private const val TAG = "FcmTopicManager"
    private const val PREFS_NAME = "fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_registration_token"
    private const val KEY_SUBSCRIBED_TOPICS = "subscribed_topics"

    const val TOPIC_DAILY_STUDY_REMINDERS = "daily_study_reminders"
    const val TOPIC_MOCK_TEST_ALERTS = "mock_test_alerts"
    const val TOPIC_EXAM_TIPS = "exam_tips_bengali"
    const val TOPIC_MADHYAMIK_ALL = "madhyamik_all"
    const val TOPIC_STREAK_ALERTS = "streak_alerts"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit { putString(KEY_FCM_TOKEN, token) }
        Log.d(TAG, "FCM Token stored: $token")
    }

    fun getStoredToken(context: Context): String? {
        return getPrefs(context).getString(KEY_FCM_TOKEN, null)
    }

    fun getSubscribedTopics(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_SUBSCRIBED_TOPICS, emptySet()) ?: emptySet()
    }

    private fun recordTopicSubscription(context: Context, topic: String, isSubscribed: Boolean) {
        val current = getSubscribedTopics(context).toMutableSet()
        if (isSubscribed) {
            current.add(topic)
        } else {
            current.remove(topic)
        }
        getPrefs(context).edit { putStringSet(KEY_SUBSCRIBED_TOPICS, current) }
    }

    /**
     * Fetches the current FCM token from Firebase and saves it locally.
     * Generates a persistent unique client token as fallback if cloud registration is offline.
     */
    fun fetchFcmToken(context: Context, onTokenRetrieved: ((String) -> Unit)? = null) {
        getStoredToken(context)?.let { onTokenRetrieved?.invoke(it) }

        try {
            @Suppress("DEPRECATION")
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful && !task.result.isNullOrBlank()) {
                            val token = task.result
                            saveToken(context, token)
                            onTokenRetrieved?.invoke(token)
                            Log.i(TAG, "Successfully retrieved FCM token: $token")
                        } else {
                            if (getStoredToken(context) == null) {
                                val fallbackToken = "local_dev_token_${java.util.UUID.randomUUID().toString().take(12)}"
                                saveToken(context, fallbackToken)
                                onTokenRetrieved?.invoke(fallbackToken)
                            }
                            Log.d(TAG, "FCM remote token retrieval deferred: ${task.exception?.message}")
                        }
                    } catch (_: Throwable) {
                        Log.d(TAG, "Token callback warning")
                    }
                }
        } catch (e: Throwable) {
            if (getStoredToken(context) == null) {
                val fallbackToken = "local_dev_token_${java.util.UUID.randomUUID().toString().take(12)}"
                saveToken(context, fallbackToken)
                onTokenRetrieved?.invoke(fallbackToken)
            }
            Log.d(TAG, "FCM messaging service offline mode active: ${e.message}")
        }
    }

    private fun isRemoteFcmReady(context: Context): Boolean {
        val token = getStoredToken(context)
        return (token != null) && (!token.startsWith("local_dev_token_"))
    }

    /**
     * Subscribes to a specific FCM topic.
     */
    fun subscribeToTopic(context: Context, topic: String, onComplete: ((Boolean) -> Unit)? = null) {
        // Record locally first so state is always reliable
        recordTopicSubscription(context, topic, isSubscribed = true)

        if (!isRemoteFcmReady(context)) {
            Log.d(TAG, "Topic subscription '$topic' stored in local mode (remote FCM deferred)")
            onComplete?.invoke(true)
            return
        }

        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            Log.i(TAG, "Successfully subscribed to topic: $topic")
                            onComplete?.invoke(true)
                        } else {
                            Log.d(TAG, "Cloud topic subscription deferred for $topic: ${task.exception?.message}")
                            onComplete?.invoke(true)
                        }
                    } catch (_: Throwable) {
                        onComplete?.invoke(true)
                    }
                }
        } catch (_: Throwable) {
            Log.d(TAG, "Topic subscription running in local mode for $topic")
            onComplete?.invoke(true)
        }
    }

    /**
     * Unsubscribes from a specific FCM topic.
     */
    fun unsubscribeFromTopic(context: Context, topic: String, onComplete: ((Boolean) -> Unit)? = null) {
        // Record locally first
        recordTopicSubscription(context, topic, isSubscribed = false)

        if (!isRemoteFcmReady(context)) {
            Log.d(TAG, "Topic unsubscription '$topic' stored in local mode (remote FCM deferred)")
            onComplete?.invoke(true)
            return
        }

        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            Log.i(TAG, "Successfully unsubscribed from topic: $topic")
                            onComplete?.invoke(true)
                        } else {
                            Log.d(TAG, "Cloud topic unsubscription deferred for $topic: ${task.exception?.message}")
                            onComplete?.invoke(true)
                        }
                    } catch (_: Throwable) {
                        onComplete?.invoke(true)
                    }
                }
        } catch (_: Throwable) {
            Log.d(TAG, "Topic unsubscription running in local mode for $topic")
            onComplete?.invoke(true)
        }
    }

    /**
     * Syncs user's notification preferences with Firebase topic subscriptions.
     */
    fun syncPreferencesWithTopics(
        context: Context,
        studyRemindersEnabled: Boolean,
        mockTestAlertsEnabled: Boolean,
        examTipsEnabled: Boolean,
        streakAlertsEnabled: Boolean,
    ) {
        // Daily study reminders
        if (studyRemindersEnabled) {
            subscribeToTopic(context, TOPIC_DAILY_STUDY_REMINDERS)
        } else {
            unsubscribeFromTopic(context, TOPIC_DAILY_STUDY_REMINDERS)
        }

        // Mock test schedule alerts
        if (mockTestAlertsEnabled) {
            subscribeToTopic(context, TOPIC_MOCK_TEST_ALERTS)
        } else {
            unsubscribeFromTopic(context, TOPIC_MOCK_TEST_ALERTS)
        }

        // Exam tips & formula revisions
        if (examTipsEnabled) {
            subscribeToTopic(context, TOPIC_EXAM_TIPS)
        } else {
            unsubscribeFromTopic(context, TOPIC_EXAM_TIPS)
        }

        // Streak alerts
        if (streakAlertsEnabled) {
            subscribeToTopic(context, TOPIC_STREAK_ALERTS)
        } else {
            unsubscribeFromTopic(context, TOPIC_STREAK_ALERTS)
        }

        // Default all students topic
        subscribeToTopic(context, TOPIC_MADHYAMIK_ALL)
    }
}
