package com.tristamisland.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import java.io.IOException
import kotlin.math.abs

/**
 * Looping music for one ambience at a time. The activity reports the foreground.
 * The screen reports the track for the current region. The same track keeps its
 * place across locations and actions. A different track crossfades. The rules
 * engine does not call this.
 */
internal class BackgroundMusic(context: Context) {
    private val appContext: Context = context.applicationContext
    private val audioManager: AudioManager =
        requireNotNull(appContext.getSystemService(AudioManager::class.java))
    private var lead: Channel? = null
    private var retire: Channel? = null
    private var fade: ValueAnimator? = null
    private var inForeground: Boolean = false
    private var desiredRes: Int? = null
    private var desiredVolume: Float = 0f
    private var label: String = ""

    private val focusRequest: AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(MUSIC_ATTRIBUTES)
            .setOnAudioFocusChangeListener(::onFocusChange)
            .build()

    fun onForeground(visible: Boolean) {
        if (inForeground == visible) return
        inForeground = visible
        if (visible) apply() else park()
    }

    fun render(res: Int?, volume: Float, label: String) {
        desiredRes = res
        desiredVolume = volume
        this.label = label
        if (inForeground) apply() else park()
    }

    private fun apply() {
        if (!inForeground) return
        val res = desiredRes
        if (res == null) {
            silence()
            return
        }
        when (audioManager.requestAudioFocus(focusRequest)) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> present(res)
            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> Unit
            else -> Unit
        }
    }

    private fun present(res: Int) {
        val active = lead
        if (active?.res == res && retire == null) {
            resumeOrAdjust(active)
            return
        }
        if (active?.res == res) {
            startCrossfade()
            return
        }
        if (retire?.res == res) {
            cancelFade()
            val returning = retire
            retire = lead
            lead = returning
            if (retire != null) startCrossfade() else returning?.let(::resumeOrAdjust)
            return
        }
        val incoming = openChannel(res) ?: return
        if (active == null) {
            lead = incoming
            fadeIn(incoming)
            return
        }
        cancelFade()
        retire?.let(::release)
        retire = active
        lead = incoming
        startCrossfade()
    }

    private fun resumeOrAdjust(channel: Channel) {
        if (!isPlaying(channel)) {
            if (channel.volume <= SILENT) {
                setVolume(channel, 0f)
                if (!start(channel)) return
                ramp(channel, desiredVolume, CROSSFADE_MS)
            } else {
                setVolume(channel, desiredVolume)
                start(channel)
            }
            return
        }
        if (abs(channel.volume - desiredVolume) > VOLUME_STEP) {
            ramp(channel, desiredVolume, VOLUME_MS)
        } else {
            setVolume(channel, desiredVolume)
        }
    }

    private fun fadeIn(channel: Channel) {
        setVolume(channel, 0f)
        if (!start(channel)) return
        ramp(channel, desiredVolume, CROSSFADE_MS)
    }

    private fun silence() {
        cancelFade()
        retire?.let {
            release(it)
            retire = null
        }
        val active = lead ?: return
        if (!isPlaying(active) && active.volume <= SILENT) {
            audioManager.abandonAudioFocusRequest(focusRequest)
            return
        }
        ramp(active, 0f, CROSSFADE_MS) {
            pause(active)
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    private fun startCrossfade() {
        val from = retire ?: return
        val to = lead ?: return
        cancelFade()
        setVolume(to, 0f)
        if (!start(to)) return
        val origin = from.volume
        val animator = ValueAnimator.ofFloat(0f, 1f)
        fade = animator
        animator.duration = CROSSFADE_MS
        animator.addUpdateListener {
            if (fade != animator) return@addUpdateListener
            val t = it.animatedValue as Float
            setVolume(from, origin * (1f - t))
            setVolume(to, desiredVolume * t)
        }
        animator.addListener(endListener(animator) {
            release(from)
            if (retire == from) retire = null
            setVolume(to, desiredVolume)
        })
        animator.start()
    }

    private fun ramp(channel: Channel, target: Float, duration: Long, onEnd: () -> Unit = {}) {
        cancelFade()
        val from = channel.volume
        if (duration <= 0L || abs(from - target) <= VOLUME_STEP) {
            setVolume(channel, target)
            onEnd()
            return
        }
        val animator = ValueAnimator.ofFloat(from, target)
        fade = animator
        animator.duration = duration
        animator.addUpdateListener {
            if (fade != animator) return@addUpdateListener
            setVolume(channel, it.animatedValue as Float)
        }
        animator.addListener(endListener(animator, onEnd))
        animator.start()
    }

    private fun endListener(animator: Animator, onEnd: () -> Unit): AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            private var cancelled: Boolean = false

            override fun onAnimationCancel(animation: Animator) {
                cancelled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (cancelled || fade != animator) return
                fade = null
                onEnd()
            }
        }

    private fun park() {
        cancelFade()
        val keep = when (val res = desiredRes) {
            null -> lead ?: retire
            else -> listOfNotNull(lead, retire).firstOrNull { it.res == res }
        }
        listOfNotNull(lead, retire).filter { it != keep }.forEach(::release)
        lead = keep
        retire = null
        keep?.let {
            pause(it)
            if (desiredRes != null) setVolume(it, desiredVolume)
        }
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    private fun openChannel(res: Int): Channel? {
        val created = MediaPlayer()
        try {
            created.setAudioAttributes(MUSIC_ATTRIBUTES)
            appContext.resources.openRawResourceFd(res).use { descriptor ->
                created.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length,
                )
            }
            created.isLooping = true
            created.prepare()
            return Channel(created, res)
        } catch (error: IOException) {
            Log.e(TAG, "Background music could not be prepared.", error)
        } catch (error: IllegalArgumentException) {
            Log.e(TAG, "Background music could not be prepared.", error)
        } catch (error: IllegalStateException) {
            Log.e(TAG, "Background music could not be prepared.", error)
        }
        created.release()
        return null
    }

    private fun start(channel: Channel): Boolean {
        return try {
            if (!channel.player.isPlaying) {
                channel.player.start()
                Log.i(TAG, "Playing $label")
            }
            true
        } catch (error: IllegalStateException) {
            Log.e(TAG, "Background music could not start.", error)
            if (lead == channel) lead = null
            if (retire == channel) retire = null
            release(channel)
            false
        }
    }

    private fun pause(channel: Channel) {
        try {
            if (channel.player.isPlaying) channel.player.pause()
        } catch (error: IllegalStateException) {
            Log.e(TAG, "Background music could not pause.", error)
            if (lead == channel) lead = null
            if (retire == channel) retire = null
            release(channel)
        }
    }

    private fun isPlaying(channel: Channel): Boolean = try {
        channel.player.isPlaying
    } catch (error: IllegalStateException) {
        false
    }

    private fun setVolume(channel: Channel, volume: Float) {
        channel.volume = volume
        try {
            channel.player.setVolume(volume, volume)
        } catch (error: IllegalStateException) {
            Log.e(TAG, "Background music could not set its volume.", error)
        }
    }

    private fun release(channel: Channel) {
        channel.player.release()
    }

    private fun cancelFade() {
        fade?.cancel()
        fade = null
    }

    private fun onFocusChange(change: Int) {
        try {
            when (change) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (inForeground) {
                        desiredRes?.let(::present) ?: silence()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    listOfNotNull(lead, retire).forEach(::pause)
                    audioManager.abandonAudioFocusRequest(focusRequest)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    listOfNotNull(lead, retire).forEach(::pause)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    cancelFade()
                    listOfNotNull(lead, retire).forEach { setVolume(it, it.volume * DUCK) }
                }
            }
        } catch (error: IllegalStateException) {
            Log.e(TAG, "Background music lost its player during a focus change.", error)
            cancelFade()
            lead?.let(::release)
            retire?.let(::release)
            lead = null
            retire = null
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    private class Channel(
        val player: MediaPlayer,
        val res: Int,
        var volume: Float = 0f,
    )

    private companion object {
        const val TAG: String = "BackgroundMusic"
        const val CROSSFADE_MS: Long = 1_800L
        const val VOLUME_MS: Long = 400L
        const val VOLUME_STEP: Float = 0.01f
        const val SILENT: Float = 0.01f
        const val DUCK: Float = 0.25f
        val MUSIC_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }
}
