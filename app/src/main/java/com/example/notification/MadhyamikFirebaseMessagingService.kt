package com.example.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MadhyamikFirebaseMessagingService : FirebaseMessagingService() {

    override fun onRegistered(token: String) {
        super.onRegistered(token)
        Log.d(TAG, "Refreshed FCM token (via onRegistered): $token")
        FcmTopicManager.saveToken(applicationContext, token)

        // Subscribe new token to default topics
        FcmTopicManager.subscribeToTopic(applicationContext, FcmTopicManager.TOPIC_MADHYAMIK_ALL)
        FcmTopicManager.subscribeToTopic(applicationContext, FcmTopicManager.TOPIC_DAILY_STUDY_REMINDERS)
        FcmTopicManager.subscribeToTopic(applicationContext, FcmTopicManager.TOPIC_MOCK_TEST_ALERTS)
    }

    @Deprecated("Use onRegistered instead", ReplaceWith("onRegistered(token)"))
    override fun onNewToken(token: String) {
        // Fallback for older versions if needed, though super.onNewToken is empty.
        // We handle everything in onRegistered now.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        // Extract title and body from data payload or notification payload
        val title = data["title"] ?: notification?.title
        val body = data["body"] ?: notification?.body
        val type = data["type"] ?: data["category"] ?: "study_reminder"
        val targetScreen = data["target_screen"] ?: data["screen"] ?: MadhyamikNotificationManager.DESTINATION_HOME
        val targetId = data["target_id"] ?: data["test_id"]
        val subjectName = data["subject_name"] ?: data["subject"]

        if (title != null || body != null) {
            when (type.lowercase()) {
                "study_reminder", "reminder", "daily_study" -> {
                    MadhyamikNotificationManager.showDailyStudyReminder(
                        context = applicationContext,
                        title = title,
                        body = body,
                        subjectName = subjectName
                    )
                }
                "mock_test", "mock_test_alert", "exam_alert" -> {
                    val startsIn = data["starts_in_minutes"]?.toIntOrNull()
                    MadhyamikNotificationManager.showMockTestAlert(
                        context = applicationContext,
                        title = title,
                        body = body,
                        testTitle = data["test_title"],
                        testId = targetId,
                        startsInMinutes = startsIn
                    )
                }
                "streak_saver", "streak_alert" -> {
                    val streakDays = data["streak_days"]?.toIntOrNull() ?: 1
                    MadhyamikNotificationManager.showStreakSaverAlert(
                        context = applicationContext,
                        streakDays = streakDays
                    )
                }
                else -> {
                    val channel = data["channel"] ?: MadhyamikNotificationManager.CHANNEL_STUDY_REMINDERS
                    MadhyamikNotificationManager.showCustomFcmNotification(
                        context = applicationContext,
                        title = title ?: "WBBSE মাধ্যমিক প্রস্তুতি",
                        body = body ?: "আপনার নতুন পাঠ আপডেট এসেছে!",
                        channelId = channel,
                        navigateTo = targetScreen,
                        targetId = targetId
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "MadhyamikFCMService"
    }
}
