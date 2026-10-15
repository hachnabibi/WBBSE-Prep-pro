package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

/**
 * SoundEffectsManager for playing audio feedback (correct answer, wrong answer) during quizzes and assessments.
 * Uses mp3 files from the app's assets folder (audio/correct-answer.mp3, audio/wrong-answer.mp3).
 */
object SoundEffectsManager {
    private const val TAG = "SoundEffectsManager"

    fun playCorrectSound(context: Context) {
        playSoundFromAssets(context.applicationContext, "audio/correct-answer.mp3")
    }

    fun playWrongSound(context: Context) {
        playSoundFromAssets(context.applicationContext, "audio/wrong-answer.mp3")
    }

    private fun playSoundFromAssets(context: Context, assetPath: String) {
        try {
            val afd = context.assets.openFd(assetPath)
            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setOnCompletionListener { mp ->
                    try {
                        mp.release()
                    } catch (_: Throwable) {}
                }
                setOnErrorListener { mp, _, _ ->
                    try {
                        mp.release()
                    } catch (_: Throwable) {}
                    true
                }
                start()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error playing sound effect '$assetPath': ${e.message}")
        }
    }
}
