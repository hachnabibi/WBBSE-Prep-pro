package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.screens.AudioPodcast

object AudioPodcastManager {
    var currentlyPlaying by mutableStateOf<AudioPodcast?>(null)
    var isPlaying by mutableStateOf(false)
    var currentProgress by mutableStateOf(0f)
    var currentPositionMs by mutableStateOf(0L)
    var durationMs by mutableStateOf(0L)

    // Callback actions to control the service from UI
    var onPlayPauseAction: (() -> Unit)? = null
    var onStopAction: (() -> Unit)? = null
    var onSeekAction: ((Long) -> Unit)? = null
}
