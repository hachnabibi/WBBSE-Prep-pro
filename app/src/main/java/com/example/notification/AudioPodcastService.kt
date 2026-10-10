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
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.screens.AudioPodcast
import com.example.util.AudioPodcastManager
import kotlinx.coroutines.*
import java.util.Locale

class AudioPodcastService : Service(),
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener {

    companion object {
        const val CHANNEL_ID = "audio_podcast_playback_channel"
        const val NOTIFICATION_ID = 202610

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_SEEK = "com.example.action.SEEK"
        const val ACTION_SPEED = "com.example.action.SPEED"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_NARRATOR = "extra_narrator"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_CLASS = "extra_class"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_BOARD_ID = "extra_board_id"
        const val EXTRA_SEEK_POS = "extra_seek_pos"
        const val EXTRA_SPEED = "extra_speed"

        private const val UTTERANCE_ID = "podcast_narration_utterance"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var isUsingTts = false
    private var pendingPodcastToNarrate: AudioPodcast? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Audio focus listener
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
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        try {
            textToSpeech = TextToSpeech(applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val langResult = textToSpeech?.setLanguage(Locale("bn", "IN"))
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.language = Locale.getDefault()
                    }
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            AudioPodcastManager.isPlaying = true
                            AudioPodcastManager.isBuffering = false
                            AudioPodcastManager.statusMessage = null
                            AudioPodcastManager.currentlyPlaying?.let { showNotification(it, isPlaying = true) }
                        }

                        override fun onDone(utteranceId: String?) {
                            if (isUsingTts) {
                                AudioPodcastManager.currentProgress = 1f
                                stopPodcastService()
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            if (isUsingTts) {
                                AudioPodcastManager.isBuffering = false
                            }
                        }
                    })
                    isTtsReady = true
                    pendingPodcastToNarrate?.let { pending ->
                        pendingPodcastToNarrate = null
                        startTtsNarration(pending)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                val boardId = intent.getStringExtra(EXTRA_BOARD_ID) ?: "WBBSE"

                val podcast = AudioPodcast(
                    title = title,
                    subject = subject,
                    duration = duration,
                    narrator = narrator,
                    className = className,
                    audioUrl = audioUrl,
                    boardId = boardId
                )
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
                seekToPosition(pos)
            }
            ACTION_SPEED -> {
                val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
                setPlaybackSpeed(speed)
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
            seekToPosition(pos)
        }
        AudioPodcastManager.onSpeedChangeAction = { speed ->
            setPlaybackSpeed(speed)
        }
    }

    private fun parseDurationToMs(durStr: String): Long {
        val clean = durStr.trim()
        val bengaliToAscii = clean.map {
            when (it) {
                '০' -> '0'; '১' -> '1'; '২' -> '2'; '৩' -> '3'; '৪' -> '4'
                '৫' -> '5'; '৬' -> '6'; '৭' -> '7'; '৮' -> '8'; '৯' -> '9'
                else -> it
            }
        }.joinToString("")
        val digits = Regex("\\d+").find(bengaliToAscii)?.value?.toIntOrNull() ?: 5
        return digits * 60 * 1000L
    }

    private fun ensureAudioVolume() {
        try {
            audioManager?.let { am ->
                val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (curVol <= 1 && maxVol > 0) {
                    val target = (maxVol * 0.85f).toInt().coerceAtLeast(1)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSubjectAssetAudio(podcast: AudioPodcast): String {
        val sub = podcast.subject.lowercase()
        return when {
            sub.contains("ইতিহাস") || sub.contains("history") -> "audio/lecture_history.mp3"
            sub.contains("বিজ্ঞান") || sub.contains("science") || sub.contains("তড়িৎ") || sub.contains("পদার্থ") || sub.contains("রসায়ন") || sub.contains("জীবন") -> "audio/lecture_science.mp3"
            sub.contains("ভূগোল") || sub.contains("geography") || sub.contains("পরিবেশ") -> "audio/lecture_geography.mp3"
            sub.contains("গণিত") || sub.contains("math") -> "audio/lecture_math.mp3"
            sub.contains("বাংলা") || sub.contains("bengali") || sub.contains("কোনি") || sub.contains("সাহিত্য") -> "audio/lecture_bengali.mp3"
            else -> "audio/lecture_general.mp3"
        }
    }

    private fun prepareMediaPlayer(): MediaPlayer {
        val player = mediaPlayer ?: MediaPlayer().also { mediaPlayer = it }
        player.reset()
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        player.setVolume(1.0f, 1.0f)
        player.setOnPreparedListener(this@AudioPodcastService)
        player.setOnCompletionListener(this@AudioPodcastService)
        player.setOnErrorListener(this@AudioPodcastService)
        player.setOnBufferingUpdateListener(this@AudioPodcastService)
        return player
    }

    private fun playFromBundledAsset(podcast: AudioPodcast) {
        try {
            isUsingTts = false
            AudioPodcastManager.isBuffering = false
            AudioPodcastManager.statusMessage = null
            val player = prepareMediaPlayer()
            val assetPath = getSubjectAssetAudio(podcast)
            val afd = applicationContext.assets.openFd(assetPath)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            player.isLooping = true
            player.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback to TTS narration if asset opening fails
            isUsingTts = true
            startTtsNarration(podcast)
        }
    }

    private fun startNewPodcast(podcast: AudioPodcast) {
        // Reset states
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        AudioPodcastManager.currentlyPlaying = podcast
        AudioPodcastManager.isPlaying = false
        AudioPodcastManager.isBuffering = true
        AudioPodcastManager.statusMessage = "অডিও লোড হচ্ছে..."
        AudioPodcastManager.currentProgress = 0f
        AudioPodcastManager.currentPositionMs = 0L

        val estimatedDuration = parseDurationToMs(podcast.duration)
        AudioPodcastManager.durationMs = estimatedDuration

        showNotification(podcast, isPlaying = false)
        ensureAudioVolume()

        // Determine playback source: remote url or bundled asset lecture
        val validUrl = podcast.audioUrl.trim()
        val isHttpUrl = validUrl.startsWith("http://", ignoreCase = true) || validUrl.startsWith("https://", ignoreCase = true)

        if (isHttpUrl && !validUrl.contains("soundhelix.com", ignoreCase = true)) {
            isUsingTts = false
            playViaMediaPlayer(validUrl, podcast)
        } else {
            // Guaranteed instant playback from local bundled MP3 asset
            playFromBundledAsset(podcast)
        }
    }

    private fun playViaMediaPlayer(url: String, podcast: AudioPodcast) {
        try {
            val player = prepareMediaPlayer()
            player.setDataSource(url)
            player.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to bundled asset
            playFromBundledAsset(podcast)
        }
    }

    private fun startTtsNarration(podcast: AudioPodcast) {
        isUsingTts = true
        if (!isTtsReady) {
            pendingPodcastToNarrate = podcast
            AudioPodcastManager.isBuffering = true
            AudioPodcastManager.statusMessage = "অডিও প্রস্তুত করা হচ্ছে..."
            return
        }

        AudioPodcastManager.isBuffering = false
        AudioPodcastManager.statusMessage = null

        val script = buildLectureScript(podcast)
        val speed = AudioPodcastManager.playbackSpeed
        try {
            textToSpeech?.setSpeechRate(speed)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech?.speak(script, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
            } else {
                @Suppress("DEPRECATION")
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = UTTERANCE_ID
                textToSpeech?.speak(script, TextToSpeech.QUEUE_FLUSH, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        AudioPodcastManager.isPlaying = true
        showNotification(podcast, isPlaying = true)
        startProgressTracker()
    }

    private fun buildLectureScript(podcast: AudioPodcast): String {
        val cleanSubject = podcast.subject.replace(Regex("\\s*\\([A-Za-z\\s&]+\\)\\s*"), "").trim()
        val narratorIntro = if (podcast.narrator.isNotBlank()) "উপস্থাপনায় আছেন ${podcast.narrator}।" else ""
        return "${podcast.title}। বিষয়: ${cleanSubject}। ${narratorIntro} এই অডিও পডকাস্ট ক্লাসে গুরুত্বপূর্ণ তথ্য ও সারসংক্ষেপ ব্যাখ্যা করা হচ্ছে। মনোযোগ দিয়ে শুনুন এবং পরীক্ষার প্রস্তুতি দৃঢ় করুন।"
    }

    private fun togglePlayPause() {
        if (AudioPodcastManager.isPlaying) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    private fun resumePlayback() {
        try {
            requestAudioFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ensureAudioVolume()

        if (isUsingTts) {
            val podcast = AudioPodcastManager.currentlyPlaying ?: return
            val script = buildLectureScript(podcast)
            try {
                textToSpeech?.setSpeechRate(AudioPodcastManager.playbackSpeed)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech?.speak(script, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
                } else {
                    @Suppress("DEPRECATION")
                    textToSpeech?.speak(script, TextToSpeech.QUEUE_FLUSH, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            AudioPodcastManager.isPlaying = true
            AudioPodcastManager.currentlyPlaying?.let { showNotification(it, isPlaying = true) }
            startProgressTracker()
        } else {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.setVolume(1.0f, 1.0f)
                    player.start()
                    AudioPodcastManager.isPlaying = true
                    AudioPodcastManager.currentlyPlaying?.let { showNotification(it, isPlaying = true) }
                    startProgressTracker()
                }
            }
        }
    }

    private fun pausePlayback() {
        if (isUsingTts) {
            textToSpeech?.stop()
            AudioPodcastManager.isPlaying = false
            AudioPodcastManager.currentlyPlaying?.let { showNotification(it, isPlaying = false) }
            stopProgressTracker()
        } else {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    AudioPodcastManager.isPlaying = false
                    AudioPodcastManager.currentlyPlaying?.let { showNotification(it, isPlaying = false) }
                    stopProgressTracker()
                }
            }
        }
    }

    private fun seekToPosition(pos: Long) {
        val bounded = pos.coerceIn(0L, AudioPodcastManager.durationMs.coerceAtLeast(0L))
        AudioPodcastManager.currentPositionMs = bounded
        if (AudioPodcastManager.durationMs > 0) {
            AudioPodcastManager.currentProgress = bounded.toFloat() / AudioPodcastManager.durationMs.toFloat()
        }
        if (!isUsingTts && mediaPlayer != null) {
            try {
                mediaPlayer?.seekTo(bounded.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setPlaybackSpeed(speed: Float) {
        AudioPodcastManager.playbackSpeed = speed
        try {
            if (isUsingTts) {
                textToSpeech?.setSpeechRate(speed)
            } else {
                mediaPlayer?.let { player ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val params = player.playbackParams
                        params.speed = speed
                        player.playbackParams = params
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive) {
                if (isUsingTts) {
                    if (AudioPodcastManager.isPlaying) {
                        val step = (500L * AudioPodcastManager.playbackSpeed).toLong()
                        val nextPos = (AudioPodcastManager.currentPositionMs + step).coerceAtMost(AudioPodcastManager.durationMs)
                        AudioPodcastManager.currentPositionMs = nextPos
                        if (AudioPodcastManager.durationMs > 0) {
                            AudioPodcastManager.currentProgress = nextPos.toFloat() / AudioPodcastManager.durationMs.toFloat()
                        }
                    }
                } else {
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
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null

        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        AudioPodcastManager.currentlyPlaying = null
        AudioPodcastManager.isPlaying = false
        AudioPodcastManager.isBuffering = false
        AudioPodcastManager.statusMessage = null
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
        if (duration > 0 && AudioPodcastManager.durationMs <= 0L) {
            AudioPodcastManager.durationMs = duration
        }
        AudioPodcastManager.isBuffering = false
        AudioPodcastManager.statusMessage = null
        ensureAudioVolume()
        mp?.setVolume(1.0f, 1.0f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && AudioPodcastManager.playbackSpeed != 1.0f) {
            try {
                val params = mp?.playbackParams
                params?.speed = AudioPodcastManager.playbackSpeed
                if (params != null) {
                    mp?.playbackParams = params
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        resumePlayback()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        // Buffering progress available if needed
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopPodcastService()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        // Fallback to bundled asset rather than crashing or silent TTS
        AudioPodcastManager.currentlyPlaying?.let { podcast ->
            playFromBundledAsset(podcast)
        } ?: run {
            stopPodcastService()
        }
        return true
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "অডিও পডকাস্ট প্লেয়ার",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "পডকাস্ট অডিও লেকচার প্লেব্যাকের কন্ট্রোল নোটিফিকেশন"
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
        val playPauseTitle = if (isPlaying) "বিরতি" else "চালান"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentTitle(podcast.title)
            .setContentText("${podcast.subject} • ${podcast.narrator}")
            .setSubText(podcast.className)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(isPlaying)
            .setContentIntent(contentPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "বন্ধ করুন", stopPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, builder.build())
        }
    }

    override fun onDestroy() {
        stopProgressTracker()
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null

        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        textToSpeech = null

        AudioPodcastManager.onPlayPauseAction = null
        AudioPodcastManager.onStopAction = null
        AudioPodcastManager.onSeekAction = null
        AudioPodcastManager.onSpeedChangeAction = null

        serviceScope.cancel()
        super.onDestroy()
    }
}
