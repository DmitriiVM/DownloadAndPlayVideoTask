package com.example.downloadandplayvideotask

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

//    private lateinit var player : SimpleExoPlayer
//    private var playWhenPlayerReady = true
//    private var currentWindow = 0
//    private var playbackPosition = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonStart.setOnClickListener {

            val player = ExoPlayerFactory.newSimpleInstance(this)
            playerView.player = player

            val uri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
            val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

            player.playWhenReady = true
            player.prepare(mediaSource)
        }
    }

//    override fun onStart() {
//        super.onStart()
//        if (Util.SDK_INT > Build.VERSION_CODES.N){
//            initializePlayer()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (Util.SDK_INT <= Build.VERSION_CODES.N || player == null){
//            initializePlayer()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (Util.SDK_INT <= Build.VERSION_CODES.N) {
//            releasePlayer()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (Util.SDK_INT > Build.VERSION_CODES.N) {
//            releasePlayer()
//        }
//    }

//    private fun initializePlayer(){
//        player = ExoPlayerFactory.newSimpleInstance(this)
//        playerView.player = player
//
//        val uri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
//        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
//        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
//
////        player.apply {
////            repeatMode = Player.REPEAT_MODE_ALL
////            playWhenReady = playWhenPlayerReady
////            seekTo(currentWindow, playbackPosition)
////            prepare(mediaSource, false, false)
////        }
//
//        player.playWhenReady = true
//        player.prepare(mediaSource)

//    }

//    private fun releasePlayer(){
//        player?.let {
//            playbackPosition = it.currentPosition
//            currentWindow = it.currentWindowIndex
//            playWhenPlayerReady = it.playWhenReady
//            it.release()
//        }
//        player = null
//    }
}
