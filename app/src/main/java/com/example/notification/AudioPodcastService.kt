package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.screens.AudioPodcast
import com.example.util.AudioPodcastManager
import kotlinx.coroutines.*

class AudioPodcastService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    companion object {
        const val CHANNEL_ID = "audio_podcast_playback_channel"
        const val NOTIFICATION_ID = 202610

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_SEEK = "com.example.action.SEEK"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_NARRATOR = "extra_narrator"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_CLASS = "extra_class"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_SEEK_POS = "extra_seek_pos"
    }

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Audio focus listener for handling phone calls or interruptions
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                resumePlayback()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        createNotificationChannel()
        setupManagerCallbacks()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
                val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: ""
                val narrator = intent.getStringExtra(EXTRA_NARRATOR) ?: ""
                val duration = intent.getStringExtra(EXTRA_DURATION) ?: ""
                val className = intent.getStringExtra(EXTRA_CLASS) ?: "Class 10"
                val audioUrl = intent.getStringExtra(EXTRA_URL) ?: ""

                val podcast = AudioPodcast(title, subject, duration, narrator, className, audioUrl)
                startNewPodcast(podcast)
            }
            ACTION_PLAY_PAUSE -> {
                togglePlayPause()
            }
            ACTION_STOP -> {
                stopPodcastService()
            }
            ACTION_SEEK -> {
                val pos = intent.getLongExtra(EXTRA_SEEK_POS, 0L)
                mediaPlayer?.seekTo(pos.toInt())
            }
        }
        return START_NOT_STICKY
    }

    private fun setupManagerCallbacks() {
        AudioPodcastManager.onPlayPauseAction = {
            togglePlayPause()
        }
        AudioPodcastManager.onStopAction = {
            stopPodcastService()
        }
        AudioPodcastManager.onSeekAction = { pos ->
            val seekIntent = Intent(this, AudioPodcastService::class.java).apply {
                action = ACTION_SEEK
                putExtra(EXTRA_SEEK_POS, pos)
            }
            startService(seekIntent)
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest?.let {
                audioManager?.requestAudioFocus(it) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } ?: false
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    private fun startNewPodcast(podcast: AudioPodcast) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener(this@AudioPodcastService)
                setOnCompletionListener(this@AudioPodcastService)
                setOnErrorListener(this@AudioPodcastService)
            }
        } else {
            mediaPlayer?.reset()
        }

        AudioPodcastManager.currentlyPlaying = podcast
        AudioPodcastManager.isPlaying = false
        AudioPodcastManager.currentProgress = 0f
        AudioPodcastManager.currentPositionMs = 0L
        AudioPodcastManager.durationMs = 0L

        showNotification(podcast, isPlaying = false)

        try {
            val url = if (podcast.audioUrl.isNotEmpty()) podcast.audioUrl else "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            stopPodcastService()
        }
    }

    private fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    private fun resumePlayback() {
        val player = mediaPlayer ?: return
        if (!player.isPlaying && requestAudioFocus()) {
            player.start()
            AudioPodcastManager.isPlaying = true
            AudioPodcastManager.currentlyPlaying?.let { showNotification(it, isPlaying = true) }
            startProgressTracker()
        }
    }

    private fun pausePlayback() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            AudioPodcastManager.isPlaying = false
            AudioPodcastManager.currentlyPlaying?.let { showNotification(it, isPlaying = false) }
            stopProgressTracker()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val pos = player.currentPosition.toLong()
                        val duration = player.duration.toLong()
                        if (duration > 0) {
                            AudioPodcastManager.currentPositionMs = pos
                            AudioPodcastManager.durationMs = duration
                            AudioPodcastManager.currentProgress = pos.toFloat() / duration.toFloat()
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun stopPodcastService() {
        stopProgressTracker()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        AudioPodcastManager.currentlyPlaying = null
        AudioPodcastManager.isPlaying = false
        AudioPodcastManager.currentProgress = 0f
        AudioPodcastManager.currentPositionMs = 0L
        AudioPodcastManager.durationMs = 0L

        abandonAudioFocus()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        val duration = mp?.duration?.toLong() ?: 0L
        AudioPodcastManager.durationMs = duration
        resumePlayback()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopPodcastService()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        stopPodcastService()
        return true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "অডিও পডকাস্ট প্লেয়ার (Audio Podcast Playback)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "পডকাস্ট লেকচার প্লেব্যাকের কন্ট্রোল নোটিফিকেশন"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(podcast: AudioPodcast, isPlaying: Boolean) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            301,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action intents for playback controls
        val playPauseIntent = Intent(this, AudioPodcastService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            302,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AudioPodcastService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            303,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_video_online) // Media styled standard system icon
            .setContentTitle(podcast.title)
            .setContentText("${podcast.subject} • ${podcast.narrator}")
            .setSubText(podcast.className)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(isPlaying)
            .setContentIntent(contentPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, builder.build())
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
