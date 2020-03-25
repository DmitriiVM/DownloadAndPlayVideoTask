package com.example.downloadandplayvideotask

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.net.URL


class MainActivity : AppCompatActivity(), DownloadManagerCallback {

    private var player: MyPlayer? = null
    private lateinit var uri: Uri
    private var url =
        URL("https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4")
    private var isPaused = false

    private lateinit var myDownloadManager: MyDownloadManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uri = Uri.parse("content://downloads/all_downloads/89")

        myDownloadManager = MyDownloadManager(this)

        buttonClear.isEnabled = false
        buttonPause.isEnabled = false

        buttonDownload.setOnClickListener {
            if (isWriteExternalStoragePermissionGranted(this)) {
                requestWriteExternalStoragePermission(this)
            } else {
                buttonPause.isEnabled = true
                buttonClear.isEnabled = true
                buttonDownload.isEnabled = false
                if (editTextUrl.text.isNotBlank()){
                    url = URL(editTextUrl.text.toString())
                }
                myDownloadManager.download(url)
            }
        }

        buttonPause.setOnClickListener {
            buttonDownload.isEnabled = false
            if (isPaused) {
                isPaused = false
                myDownloadManager.resume()
                buttonPause.text = "Pause"
            } else {
                isPaused = true
                myDownloadManager.pause()
                buttonPause.text = "Resume"
            }
        }

        buttonClear.setOnClickListener {
            buttonClear.isEnabled = false
            buttonPause.isEnabled = false
            buttonDownload.isEnabled = true
            myDownloadManager.clear()
        }

        buttonTemp.setOnClickListener {
            val file = File("/storage/emulated/0/Download/ttt.mp4")
            if (file.exists()) {
                Log.d("mmm", "MainActivity :  onCreate --  ${file.length()}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isNougatOrLower()) player?.initializePlayer(playerView, uri)
    }

    override fun onResume() {
        super.onResume()
        if (isNougatOrLower()) player?.initializePlayer(playerView, uri)
    }

    override fun onPause() {
        super.onPause()
        if (isNougatOrLower()) player?.releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (!isNougatOrLower()) player?.releasePlayer()
    }

    override fun onProgressUpdate(progress: Int, fileLenth: Int) {
        textViewResult.text = "Downloading: ${progress/1000}kb / ${fileLenth/1000}kb"
    }

    override fun onDownloadFinished(message: String) {
        textViewResult.text = message
        buttonPause.isEnabled = false
        buttonClear.isEnabled = true
        buttonDownload.isEnabled = true
        startPlayer()
    }

    private fun startPlayer() {
//        player = MyPlayer(this)
//        uri = Uri.fromFile(File("/storage/emulated/0/Download/ttt.mp4"))
//        player?.initializePlayer(playerView, uri)
    }

    override fun onError(message: String) {
        textViewResult.text = "Success"
        buttonPause.isEnabled = false
        buttonClear.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()

        myDownloadManager.onDestroy()
    }
}
