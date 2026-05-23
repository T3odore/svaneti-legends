package com.example.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050
    private val activeTracks = java.util.concurrent.ConcurrentHashMap.newKeySet<AudioTrack>()

    var isMuted: Boolean = false

    fun playJump() {
        if (isMuted) return
        scope.launch {
            try {
                // Pitch sweep up (frequency goes from 300Hz to 800Hz)
                val durationMs = 120
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 350 + progress * 550
                    val angle = 2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val sample = (sin(angle) * 12000).toInt().toShort()
                    buffer[i] = sample
                }

                playBuffer(buffer)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun playCollectCoin() {
        if (isMuted) return
        scope.launch {
            try {
                // Classic arpeggio (C6, E6 notes)
                val durationMs = 150
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = if (progress < 0.4) 1046.50 else 1318.51 // C6 then E6
                    val angle = 2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)
                    // Apply fade-out envelope
                    val envelope = 1.0 - progress
                    val sample = (sin(angle) * 10000 * envelope).toInt().toShort()
                    buffer[i] = sample
                }

                playBuffer(buffer)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun playCollectWine() {
        if (isMuted) return
        scope.launch {
            try {
                // Royal major triad chord C5, E5, G5 blended
                val durationMs = 200
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / SAMPLE_RATE
                    val sampleC = sin(2.0 * Math.PI * 523.25 * t)
                    val sampleE = sin(2.0 * Math.PI * 659.25 * t)
                    val sampleG = sin(2.0 * Math.PI * 783.99 * t)
                    val combined = (sampleC + sampleE + sampleG) / 3.0
                    val envelope = 1.0 - progress
                    buffer[i] = (combined * 12000 * envelope).toInt().toShort()
                }

                playBuffer(buffer)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun playHurt() {
        if (isMuted) return
        scope.launch {
            try {
                // Bass sweep down from 200Hz to 60Hz with low amplitude noise
                val durationMs = 250
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 200.0 - (progress * 130.0)
                    val angle = 2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val envelope = 1.0 - progress
                    // Square wave + low random scratch noise for crash effect
                    val square = if (sin(angle) > 0) 1 else -1
                    val noise = (Math.random() * 2.0 - 1.0) * 0.2
                    val valShort = ((square + noise) / 1.2 * 8000 * envelope).toInt().toShort()
                    buffer[i] = valShort
                }

                playBuffer(buffer)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun playUnlockFanfare() {
        if (isMuted) return
        scope.launch {
            try {
                // Royal fan fare with rising Georgian triad motives
                val durationMs = 450
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / SAMPLE_RATE
                    // 3 notes sequential pattern
                    val freq = when {
                        progress < 0.3 -> 392.00 // G4
                        progress < 0.6 -> 523.25 // C5
                        else -> 659.25 // E5
                    }
                    val angle = 2.0 * Math.PI * freq * t
                    val envelope = if (progress > 0.8) 1.0 - (progress - 0.8) / 0.2 else 1.0
                    buffer[i] = (sin(angle) * 12000 * envelope).toInt().toShort()
                }

                playBuffer(buffer)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun playBuffer(buffer: ShortArray) {
        if (activeTracks.size >= 4) {
            // Protect system from AudioFlinger native track exhaustion crashes under rapid collision updates
            return
        }
        var audioTrack: AudioTrack? = null
        try {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_GAME)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = android.media.AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                .build()

            audioTrack = android.media.AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(android.media.AudioTrack.MODE_STATIC)
                .build()

            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                activeTracks.add(audioTrack)
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                val finalTrack = audioTrack
                scope.launch {
                    val delayTime = (buffer.size.toFloat() / SAMPLE_RATE * 1000).toLong() + 100
                    kotlinx.coroutines.delay(delayTime)
                    cleanupTrack(finalTrack)
                }
            } else {
                audioTrack.release()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                audioTrack?.release()
            } catch (t: Throwable) {
                // Ignore
            }
        }
    }

    private fun cleanupTrack(track: AudioTrack) {
        try {
            activeTracks.remove(track)
            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.stop()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            try {
                track.release()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}
