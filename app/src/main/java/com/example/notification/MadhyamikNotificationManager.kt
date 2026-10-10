package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object MadhyamikNotificationManager {

    const val CHANNEL_STUDY_REMINDERS = "study_reminders_channel"
    const val CHANNEL_MOCK_TESTS = "mock_tests_channel"
    const val CHANNEL_STREAK_SAVER = "streak_saver_channel"
    const val CHANNEL_BOARD_NOTICES = "board_notices_channel"

    const val EXTRA_NAVIGATE_TO = "extra_navigate_to"
    const val EXTRA_TARGET_SUBJECT = "extra_target_subject"
    const val EXTRA_TARGET_TEST_ID = "extra_target_test_id"
    const val EXTRA_ACTION_TYPE = "extra_action_type"

    const val DESTINATION_HOME = "home"
    const val DESTINATION_MOCK_TESTS = "mock_tests"
    const val DESTINATION_CHALLENGE = "challenge"
    const val DESTINATION_ASSESSMENT = "assessment"
    const val DESTINATION_PROFILE = "profile"

    private var channelsCreated = false

    /**
     * Initializes notification channels with appropriate audio, vibration, and priority levels.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // 1. Daily Study Reminders Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_STUDY_REMINDERS,
                "দৈনিক পড়াশোনা অনুস্মারক",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "মাধ্যমিক পরীক্ষার নিয়মিত প্রস্তুতি ও দৈনিক পড়ার তাগিদ"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
            }

            // 2. Mock Test & Exam Alerts Channel
            val mockTestChannel = NotificationChannel(
                CHANNEL_MOCK_TESTS,
                "মক টেস্ট ও পরীক্ষার সময়সূচী",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "রবিবাসরীয় পশ্চিমবঙ্গ মক টেস্ট ও অধ্যায়ভিত্তিক পরীক্ষার সতর্কতা"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
            }

            // 3. Streak Saver Channel
            val streakChannel = NotificationChannel(
                CHANNEL_STREAK_SAVER,
                "ধারাবাহিকতা রক্ষা সতর্কতা",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "দৈনিক পড়াশোনার ধারাবাহিকতা যাতে বজায় থাকে তার জন্য সান্ধ্যকালীন অ্যালার্ট"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
            }

            // 4. Board Notices & Tips Channel
            val noticesChannel = NotificationChannel(
                CHANNEL_BOARD_NOTICES,
                "পর্ষদ নোটিশ ও পরামর্শ",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "সিলেবাস আপডেট, পরীক্ষার টিপস ও ফর্মুলা রিভিশন"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(reminderChannel, mockTestChannel, streakChannel, noticesChannel)
            )
            channelsCreated = true
        }
    }

    /**
     * Display a Daily Study Reminder notification with direct quick-action to start studying.
     */
    fun showDailyStudyReminder(
        context: Context,
        title: String? = null,
        body: String? = null,
        subjectName: String? = null,
        isBengali: Boolean = true
    ) {
        ensureChannels(context)

        val notificationTitle = title ?: if (isBengali) {
            "📚 আজ পড়ার সময় হয়েছে! মাধ্যমিক ২০২৬"
        } else {
            "📚 Time to Study! Madhyamik 2026"
        }

        val notificationBody = body ?: if (isBengali) {
            if (subjectName != null) {
                "আজকের লক্ষ্য: $subjectName এর গুরুত্বপূর্ণ প্রশ্নগুলো দ্রুত ঝালাই করে নিন।"
            } else {
                "আজকের ১৫টি প্রশ্নের দৈনিক অনুশীলন শেষ করে আপনার স্ট্রিক এগিয়ে নিন!"
            }
        } else {
            if (subjectName != null) {
                "Today's Goal: Practice important questions from $subjectName."
            } else {
                "Complete your daily 15-question practice to protect your streak!"
            }
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, DESTINATION_CHALLENGE)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action button to start practice immediately
        val practiceIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, DESTINATION_CHALLENGE)
            putExtra(EXTRA_ACTION_TYPE, "start_daily_practice")
        }
        val practicePendingIntent = PendingIntent.getActivity(
            context,
            102,
            practiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionText = "পড়া শুরু করুন ▶"

        val builder = NotificationCompat.Builder(context, CHANNEL_STUDY_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(0, actionText, practicePendingIntent)

        notifySafely(context, 1001, builder.build())
    }

    /**
     * Display an Upcoming Mock Test Schedule notification.
     */
    fun showMockTestAlert(
        context: Context,
        title: String? = null,
        body: String? = null,
        testTitle: String? = null,
        testId: String? = null,
        startsInMinutes: Int? = null,
        isBengali: Boolean = true
    ) {
        ensureChannels(context)

        val notificationTitle = title ?: if (isBengali) {
            "🏆 আগামী মক টেস্ট শুরু হতে চলেছে!"
        } else {
            "🏆 Upcoming Mock Test Alert!"
        }

        val notificationBody = body ?: if (isBengali) {
            val testName = testTitle ?: "রবিবাসরীয় অল-বেঙ্গল মাধ্যমিক মেগা মক টেস্ট"
            if (startsInMinutes != null && startsInMinutes > 0) {
                "আর মাত্র $startsInMinutes মিনিট পর $testName শুরু হবে! আপনার প্রস্তুতি যাচাই করুন।"
            } else {
                "$testName এখন লাইভ! হাজারো সহপাঠীর সাথে প্রতিযোগিতা করে নিজের র‍্যাংক জানুন।"
            }
        } else {
            val testName = testTitle ?: "All-Bengal Madhyamik Mega Mock Test"
            if (startsInMinutes != null && startsInMinutes > 0) {
                "$testName starts in $startsInMinutes minutes! Check your exam readiness."
            } else {
                "$testName is now LIVE! Compete across Bengal and check your rank."
            }
        }

        val openMockHubIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, DESTINATION_MOCK_TESTS)
            testId?.let { putExtra(EXTRA_TARGET_TEST_ID, it) }
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            201,
            openMockHubIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionText = "মক টেস্ট দিন ✍️"

        val builder = NotificationCompat.Builder(context, CHANNEL_MOCK_TESTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(0, actionText, contentPendingIntent)

        notifySafely(context, 2001, builder.build())
    }

    /**
     * Display a Streak Saver Alert when the user hasn't studied today and the night is approaching.
     */
    fun showStreakSaverAlert(
        context: Context,
        streakDays: Int,
        isBengali: Boolean = true
    ) {
        ensureChannels(context)

        val notificationTitle = if (isBengali) {
            "🔥 আপনার $streakDays দিনের স্ট্রিক বিপদে!"
        } else {
            "🔥 Your $streakDays-Day Streak is at Risk!"
        }

        val notificationBody = if (isBengali) {
            "মধ্যরাতের আগে অন্তত ৫টি প্রশ্নের উত্তর দিয়ে আপনার $streakDays দিনের পড়াশোনার ধারাবাহিকতা বজায় রাখুন।"
        } else {
            "Answer at least 5 questions before midnight to protect your $streakDays-day study streak."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, DESTINATION_CHALLENGE)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            301,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_STREAK_SAVER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "স্ট্রিক বাঁচান 🔥", pendingIntent)

        notifySafely(context, 3001, builder.build())
    }

    /**
     * Display a generic incoming FCM notification with custom channel and payload routing.
     */
    fun showCustomFcmNotification(
        context: Context,
        title: String,
        body: String,
        channelId: String = CHANNEL_STUDY_REMINDERS,
        navigateTo: String? = null,
        targetId: String? = null,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        ensureChannels(context)

        val targetChannel = when (channelId) {
            CHANNEL_MOCK_TESTS -> CHANNEL_MOCK_TESTS
            CHANNEL_STREAK_SAVER -> CHANNEL_STREAK_SAVER
            CHANNEL_BOARD_NOTICES -> CHANNEL_BOARD_NOTICES
            else -> CHANNEL_STUDY_REMINDERS
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            navigateTo?.let { putExtra(EXTRA_NAVIGATE_TO, it) }
            targetId?.let { putExtra(EXTRA_TARGET_TEST_ID, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, targetChannel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notifySafely(context, notificationId, builder.build())
    }

    private fun ensureChannels(context: Context) {
        if (!channelsCreated) {
            createNotificationChannels(context)
        }
    }

    private fun notifySafely(context: Context, id: Int, notification: android.app.Notification) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(id, notification)
            }
        } catch (e: SecurityException) {
            // Permission not yet granted by user
        }
    }
}
