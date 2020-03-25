package com.example.downloadandplayvideotask

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var player : MyPlayer? = null
    private lateinit var uri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        buttonStart.setOnClickListener {
            if (buttonStart.text == "Start") {
                player = MyPlayer(this)
                uri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
                player?.initializePlayer(playerView, uri)
                buttonStart.text = "Release"
            } else {
                player?.releasePlayer()
                buttonStart.text = "Start"
            }

        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > Build.VERSION_CODES.N) {
            player?.initializePlayer(playerView, uri)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= Build.VERSION_CODES.N) {
            player?.initializePlayer(playerView, uri)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= Build.VERSION_CODES.N) {
            player?.releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > Build.VERSION_CODES.N) {
            player?.releasePlayer()
        }
    }
}
