package com.example.util

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.notification.AudioPodcastService
import com.example.ui.screens.AudioPodcast

object AudioPodcastManager {
    var currentlyPlaying by mutableStateOf<AudioPodcast?>(null)
    var isPlaying by mutableStateOf(false)
    var isBuffering by mutableStateOf(false)
    var currentProgress by mutableStateOf(0f)
    var currentPositionMs by mutableStateOf(0L)
    var durationMs by mutableStateOf(0L)
    var playbackSpeed by mutableStateOf(1.0f)
    var statusMessage by mutableStateOf<String?>(null)

    // Direct callback actions to control the service from UI
    var onPlayPauseAction: (() -> Unit)? = null
    var onStopAction: (() -> Unit)? = null
    var onSeekAction: ((Long) -> Unit)? = null
    var onSpeedChangeAction: ((Float) -> Unit)? = null

    fun togglePlayPause(context: Context? = null) {
        if (onPlayPauseAction != null) {
            onPlayPauseAction?.invoke()
        } else if (context != null) {
            val intent = Intent(context, AudioPodcastService::class.java).apply {
                action = AudioPodcastService.ACTION_PLAY_PAUSE
            }
            context.startService(intent)
        }
    }

    fun stop(context: Context? = null) {
        if (onStopAction != null) {
            onStopAction?.invoke()
        } else if (context != null) {
            val intent = Intent(context, AudioPodcastService::class.java).apply {
                action = AudioPodcastService.ACTION_STOP
            }
            context.startService(intent)
        } else {
            currentlyPlaying = null
            isPlaying = false
            isBuffering = false
            currentProgress = 0f
            currentPositionMs = 0L
            durationMs = 0L
        }
    }

    fun seekTo(positionMs: Long, context: Context? = null) {
        val bounded = positionMs.coerceIn(0L, durationMs.coerceAtLeast(0L))
        currentPositionMs = bounded
        if (durationMs > 0) {
            currentProgress = bounded.toFloat() / durationMs.toFloat()
        }
        if (onSeekAction != null) {
            onSeekAction?.invoke(bounded)
        } else if (context != null) {
            val intent = Intent(context, AudioPodcastService::class.java).apply {
                action = AudioPodcastService.ACTION_SEEK
                putExtra(AudioPodcastService.EXTRA_SEEK_POS, bounded)
            }
            context.startService(intent)
        }
    }

    fun seekRelative(deltaMs: Long, context: Context? = null) {
        val target = (currentPositionMs + deltaMs).coerceIn(0L, durationMs.coerceAtLeast(0L))
        seekTo(target, context)
    }

    fun setSpeed(speed: Float, context: Context? = null) {
        playbackSpeed = speed
        if (onSpeedChangeAction != null) {
            onSpeedChangeAction?.invoke(speed)
        } else if (context != null) {
            val intent = Intent(context, AudioPodcastService::class.java).apply {
                action = AudioPodcastService.ACTION_SPEED
                putExtra(AudioPodcastService.EXTRA_SPEED, speed)
            }
            context.startService(intent)
        }
    }
}
