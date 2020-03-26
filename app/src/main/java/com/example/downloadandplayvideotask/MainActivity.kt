package com.example.downloadandplayvideotask

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity(), DownloadManagerCallback {

    private lateinit var uri: Uri
    private var appState = AppState.IDLE
    private var editTextURL: String? = null
    private lateinit var myDownloadManager: MyDownloadManager
    private var player: SimpleExoPlayer? = null
    private var playWhenPlayerReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var fullFileLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uri = Uri.parse(PATH_NAME)
        myDownloadManager = MyDownloadManager(this)

        savedInstanceState?.let {
            currentWindow = savedInstanceState.getInt(CURRENT_WINDOW)
            playbackPosition = savedInstanceState.getLong(PLAYBACK_POSITION)
            appState = savedInstanceState.getSerializable(APP_STATE) as AppState
            editTextURL = savedInstanceState.getString(EDIT_TEXT_URL)
            editTextUrl.setText(editTextURL)
            textViewResult.text = savedInstanceState.getString(MESSAGE_TEXT) ?: ""
            fullFileLength = savedInstanceState.getInt(FULL_FILE_LENGTH)
        }

        when (appState) {
            AppState.DOWNLOAD -> {
                setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
                myDownloadManager.download(URL(DEFAULT_URL), PATH_NAME)
            }
            AppState.PAUSE -> {
                setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
                buttonPause.text = getString(R.string.resume)
            }
            AppState.PLAY -> setButtonsEnabled(
                btnDownload = false,
                btnPaused = false,
                btnClear = true
            )
            else -> setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
        }

        buttonDownload.setOnClickListener {
            if (isWriteExternalStoragePermissionGranted(this)) {
                requestWriteExternalStoragePermission(this)
            } else {
                fullFileLength = 0
                setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
                startDownload(editTextUrl.text.toString())
            }
        }

        buttonPause.setOnClickListener {
            buttonDownload.isEnabled = false
            when (appState) {
                AppState.PAUSE -> {
                    startDownload(editTextURL)
                    buttonPause.text = getString(R.string.pause)
                }
                else -> {
                    appState = AppState.PAUSE
                    myDownloadManager.pause()
                    buttonPause.text = getString(R.string.resume)
                }
            }
        }

        buttonClear.setOnClickListener {
            if (appState == AppState.PLAY) {
                releasePlayer()
            }
            setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
            myDownloadManager.clear(PATH_NAME)
            appState = AppState.CLEARED
        }
    }

    private fun startDownload(url: String?) {
        appState = AppState.DOWNLOAD
        if (url.isNullOrBlank()) {
            myDownloadManager.download(URL(DEFAULT_URL), PATH_NAME)
        } else {
            myDownloadManager.download(URL(url), PATH_NAME)
        }
    }

    private fun setButtonsEnabled(btnDownload: Boolean, btnPaused: Boolean, btnClear: Boolean) {
        buttonDownload.isEnabled = btnDownload
        buttonPause.isEnabled = btnPaused
        buttonClear.isEnabled = btnClear
    }

    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView.player = player
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(this, userAgent)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

        player?.apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = playWhenPlayerReady
            seekTo(currentWindow, playbackPosition)
            prepare(mediaSource, false, false)
        }
    }

    private fun releasePlayer() {
        player?.apply {
            playbackPosition = currentPosition
            currentWindow = currentWindowIndex
            playWhenPlayerReady = playWhenReady
            playerView.player = null
            release()
        }
    }

    override fun onProgressUpdate(progress: Int, fileLength: Int) {
        if (fullFileLength == 0) {
            fullFileLength = fileLength.toInt()
        }
        textViewResult.text = getString(R.string.progress, progress / 1000, fullFileLength / 1000)
    }

    override fun onDownloadFinished() {
        setButtonsEnabled(btnDownload = false, btnPaused = false, btnClear = true)
        textViewResult.text = getString(R.string.result_success)

        appState = AppState.PLAY
        initializePlayer()
    }

    override fun onDownloadCleared() {
        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
        textViewResult.text = getString(R.string.result_cleared)
    }

    override fun onError(message: String) {
        if (appState != AppState.PLAY) {
            textViewResult.text = message
            setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
        }
    }

    override fun onStart() {
        super.onStart()
        if (appState == AppState.PLAY && !isNougatOrLower()) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (appState == AppState.PLAY && isNougatOrLower()) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (appState == AppState.PLAY && isNougatOrLower()) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (appState == AppState.PLAY && !isNougatOrLower()) releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        myDownloadManager.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        player?.let {
            outState.putInt(CURRENT_WINDOW, it.currentWindowIndex)
            outState.putLong(PLAYBACK_POSITION, it.currentPosition)
        }
        outState.putSerializable(APP_STATE, appState)
        outState.putString(EDIT_TEXT_URL, editTextURL)
        outState.putString(MESSAGE_TEXT, textViewResult.text.toString())
        outState.putInt(FULL_FILE_LENGTH, fullFileLength)
    }

    companion object {
        private const val PLAYBACK_POSITION = "playback_position"
        private const val CURRENT_WINDOW = "current_window"
        private const val APP_STATE = "app_state"
        private const val MESSAGE_TEXT = "message_text"
        private const val EDIT_TEXT_URL = "edit_text_url"
        private const val FULL_FILE_LENGTH = "full_file_length"

        private const val DEFAULT_URL =
//            "https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"     // 10 Mb
            "https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4"   // 30 Mb
        private const val PATH_NAME = "/storage/emulated/0/Download/exoplayervideo.mp4"
    }
}

enum class AppState {
    IDLE, DOWNLOAD, PAUSE, PLAY, CLEARED
}
