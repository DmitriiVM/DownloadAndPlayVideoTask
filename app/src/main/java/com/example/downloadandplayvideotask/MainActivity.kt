package com.example.downloadandplayvideotask

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity(), DownloadManagerCallback {

    private var player: MyPlayer? = null
    private lateinit var uri: Uri
    private var isPaused = false

    private lateinit var myDownloadManager: MyDownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uri = Uri.fromFile(File(PATH_NAME))

        myDownloadManager = MyDownloadManager(this)

        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)

        buttonDownload.setOnClickListener {
            if (isWriteExternalStoragePermissionGranted(this)) {
                requestWriteExternalStoragePermission(this)
            } else {
                setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
                val url = if (editTextUrl.text.isNotBlank()) {
                    URL(editTextUrl.text.toString())
                } else {
                    URL(DEFAULT_URL)
                }
                myDownloadManager.download(url, PATH_NAME)
            }
        }

        buttonPause.setOnClickListener {
            buttonDownload.isEnabled = false
            if (isPaused) {
                isPaused = false
                myDownloadManager.resume()
                buttonPause.text = getString(R.string.pause)
            } else {
                isPaused = true
                myDownloadManager.pause()
                buttonPause.text = getString(R.string.resume)
            }
        }

        buttonClear.setOnClickListener {
            setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
            myDownloadManager.clear()
            isPaused = false
        }

        buttonTemp.setOnClickListener {
            uri = Uri.parse("https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4")
            startPlayer()
        }
    }

    private fun setButtonsEnabled(btnDownload: Boolean, btnPaused: Boolean, btnClear: Boolean) {
        buttonDownload.isEnabled = btnDownload
        buttonPause.isEnabled = btnPaused
        buttonClear.isEnabled = btnClear
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

    override fun onProgressUpdate(progress: Int, fileLength: Int) {
        textViewResult.text = getString(R.string.progress, progress / 1000, fileLength / 1000)
    }

    override fun onDownloadFinished() {
        setButtonsEnabled(btnDownload = false, btnPaused = false, btnClear = true)
        textViewResult.text = getString(R.string.result_success)


//        uri = Uri.parse("content://downloads/all_downloads/89")
//        startPlayer()
    }

    override fun onDownloadCleared() {
        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = true)
        textViewResult.text = getString(R.string.result_cleared)
    }

    private fun startPlayer() {
        player = MyPlayer(this)
        player?.initializePlayer(playerView, uri)
    }

    override fun onError(message: String) {
        textViewResult.text = message
        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        myDownloadManager.onDestroy()
    }

    companion object {
        private const val DEFAULT_URL =
            "https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"
        private const val PATH_NAME = "/storage/emulated/0/Download/exoplayervideo.mp4"
    }
}
