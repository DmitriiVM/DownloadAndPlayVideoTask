package com.example.downloadandplayvideotask

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MyPlayer(private val context: Context) {

    private lateinit var player : SimpleExoPlayer
    private lateinit var view: PlayerView
    private var playWhenPlayerReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    fun initializePlayer(playerView: PlayerView, uri: Uri) {
        player = ExoPlayerFactory.newSimpleInstance(context)
        playerView.player = player
        view = playerView

        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

        player.apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = playWhenPlayerReady
            seekTo(currentWindow, playbackPosition)
            prepare(mediaSource, false, false)
        }
    }

    fun releasePlayer() {
        player.apply {
            playbackPosition = currentPosition
            currentWindow = currentWindowIndex
            playWhenPlayerReady = playWhenReady
            view.player = null
            release()
        }
    }
}