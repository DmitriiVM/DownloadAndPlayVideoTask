package com.example.downloadandplayvideotask

import android.content.Context
import android.net.Uri
import android.util.Log
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


    fun initializePlayer(context: Context, playerView: PlayerView, uri: Uri, playbackPosition : Long) {

        player = ExoPlayerFactory.newSimpleInstance(context)
        playerView.player = player
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

        player.apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            seekTo(playbackPosition)
            prepare(mediaSource, false, false)
        }
    }

    fun releasePlayer(playerView: PlayerView) : Long {
        val playbackPosition = player.currentPosition
        playerView.player = null
        player.release()
        return playbackPosition
    }

//    fun getPlayersParams() =  PlayerParams(player.playWhenReady, player.currentWindowIndex, player.currentPosition)
}