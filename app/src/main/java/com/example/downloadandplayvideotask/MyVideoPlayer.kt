package com.example.downloadandplayvideotask

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

object MyVideoPlayer {


    private lateinit var player: SimpleExoPlayer


    fun initializePlayer(context: Context, playerView: PlayerView, uri: Uri, params: PlayerParams) {

        player = ExoPlayerFactory.newSimpleInstance(context)
        playerView.player = player
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

        player.apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = params.playWhenPlayerReady
            seekTo(params.currentWindow, params.playbackPosition)
            prepare(mediaSource, false, false)
        }
    }

    fun releasePlayer(playerView: PlayerView) {
        playerView.player = null
        player.release()
    }

    fun getPlayersParams() =  PlayerParams(player.playWhenReady, player.currentWindowIndex, player.currentPosition)
}