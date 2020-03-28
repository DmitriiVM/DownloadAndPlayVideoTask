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

class MyVideoPlayer(private val context: Context) {


    private lateinit var player: SimpleExoPlayer
    private lateinit var view: PlayerView
    //    private var playWhenPlayerReady = true
//    private var currentWindow = 0
//    private var playbackPosition = 0L
//    private var params = PlayerParams()


    fun initializePlayer(playerView: PlayerView, uri: Uri, params: PlayerParams, playWhenPlayerReady : Boolean, currentWindow : Int, playbackPosition : Long, text : String) {
        Log.d("mmm", "MyVideoPlayer : $text initializePlayer ${params.playbackPosition} -- ${playbackPosition} ")

        player = ExoPlayerFactory.newSimpleInstance(context)
        playerView.player = player
        view = playerView
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

    fun releasePlayer() {
//        player.apply {
//            params.playbackPosition = currentPosition
//            params.currentWindow = currentWindowIndex
//            params.playWhenPlayerReady = playWhenReady
//
//
//        }
//        Log.d("mmm", "MyVideoPlayer :  releasePlayer --  ")
        view.player = null
        player.release()

    }

    fun getPlayersParams() : PlayerParams{
        return if (::player.isInitialized){
            PlayerParams(player.playWhenReady, player.currentWindowIndex, player.currentPosition)
        } else {
            PlayerParams()
        }
    }

//    fun setParams(params : PlayerParams){
//        this.params = params
//    }

//    fun setInitialProperties(currentWindow : Int, playbackPosition : Long){
//        this.currentWindow = currentWindow
//        this.playbackPosition = playbackPosition
//    }

}