package com.example.downloadandplayvideotask

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private var appState = AppState.IDLE
    private var editTextURL: String = DEFAULT_URL
    //    private lateinit var myDownloadManager: MyDownloadManager
//    private var player: SimpleExoPlayer? = null
////    private var playWhenPlayerReady = true
////    private var currentWindow = 0
////    private var playbackPosition = 0L
    private var player: MyVideoPlayer? = null
    private var params: PlayerParams? = null

//    private var fullFileLength = 0

    private lateinit var viewModel: DownloadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uri = Uri.parse(PATH_NAME)
        savedInstanceState?.let {
            //            currentWindow = savedInstanceState.getInt(CURRENT_WINDOW)
//            playbackPosition = savedInstanceState.getLong(PLAYBACK_POSITION)
            params = savedInstanceState.getParcelable(PLAYER_PARAMS)
            appState = savedInstanceState.getSerializable(APP_STATE) as AppState
            editTextURL = savedInstanceState.getString(EDIT_TEXT_URL) ?: DEFAULT_URL
            editTextUrl.setText(editTextURL)
            textViewResult.text = savedInstanceState.getString(MESSAGE_TEXT) ?: ""
//            fullFileLength = savedInstanceState.getInt(FULL_FILE_LENGTH)
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(DownloadViewModel::class.java)

        player = MyVideoPlayer(this)

        when (appState) {
            AppState.DOWNLOAD -> {
                setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
//                if (savedInstanceState == null) {
//                    viewModel.download(URL(editTextURL), PATH_NAME, false)
//                }
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

        viewModel.getDownloadLiveData().observe(this, Observer<DownloadResult> { result ->
            when (result) {
                is DownloadResult.Progress -> {
//                    if (fullFileLength == 0) {
//                        fullFileLength = fileLength.toInt()
//                    }
                    textViewResult.text = getString(
                        R.string.progress,
                        result.progress / 1000,
                        result.fileLength / 1000
                    )
                }
                is DownloadResult.Success -> {
                    setButtonsEnabled(btnDownload = false, btnPaused = false, btnClear = true)
                    textViewResult.text = getString(R.string.result_success)



//                    Log.d("mmm", "MainActivity :  onCreate --  ")

                    if (appState != AppState.PLAY) {
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        Log.d("mmm", "MainActivity :  onCreate --  $appState")

                        player?.initializePlayer(
                            playerView,
                            uri,
                            params ?: PlayerParams(),
                            true,
                            0,
                            0,
                            "rrrrr"
                        )
                    }
                    appState = AppState.PLAY
                }
                is DownloadResult.Clear -> {
                    setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
                    textViewResult.text = getString(R.string.result_cleared)
                }
                is DownloadResult.Error -> {
                    if (appState != AppState.PLAY) {
                        textViewResult.text = result.message
                        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
                    }
                }
            }
        })


//        myDownloadManager = MyDownloadManager(this)
        button.setOnClickListener {
            player?.initializePlayer(playerView, uri, params ?: PlayerParams(), true, 0, 0, "rrrrr")
        }





        buttonDownload.setOnClickListener {
            if (isWriteExternalStoragePermissionGranted(this)) {
                requestWriteExternalStoragePermission(this)
            } else {
//                fullFileLength = 0
                setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
                if (editTextUrl.text.isNotBlank()){
                    editTextURL = editTextUrl.text.toString()
                }
                startDownload(editTextURL)
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
//                    myDownloadManager.pause()
                    viewModel.pause()
                    buttonPause.text = getString(R.string.resume)
                }
            }
        }

        buttonClear.setOnClickListener {
            if (appState == AppState.PLAY) {
                player?.releasePlayer()
            }
            setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
//            myDownloadManager.clear(PATH_NAME)
            viewModel.clear(PATH_NAME)
            appState = AppState.CLEARED
        }
    }

    private fun startDownload(url: String?) {
        appState = AppState.DOWNLOAD
//        if (url.isNullOrBlank()) {
////            myDownloadManager.download(URL(DEFAULT_URL), PATH_NAME)
//            viewModel.download(URL(DEFAULT_URL), PATH_NAME)
//        } else {
//            myDownloadManager.download(URL(url), PATH_NAME)
            viewModel.download(URL(editTextURL), PATH_NAME, false)
//        }
    }

    private fun setButtonsEnabled(btnDownload: Boolean, btnPaused: Boolean, btnClear: Boolean) {
        buttonDownload.isEnabled = btnDownload
        buttonPause.isEnabled = btnPaused
        buttonClear.isEnabled = btnClear
    }


    override fun onStart() {
        super.onStart()
        if (!isNougatOrLower()) {
            when (appState) {
                AppState.DOWNLOAD -> {
//                    Log.d("mmm", "MainActivity :  onStart --  $editTextURL")
                    viewModel.download(URL(editTextURL), PATH_NAME, true)
                }
                AppState.PLAY -> {
                    player?.initializePlayer(
                        playerView,
                        uri,
                        params ?: PlayerParams(),
                        true,
                        0,
                        0,
                        "55"
                    )
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (isNougatOrLower())



            when (appState) {
                AppState.DOWNLOAD -> {
                    viewModel.download(URL(editTextURL), PATH_NAME, true)
                }
                AppState.PLAY -> {
                    player?.initializePlayer(
                        playerView,
                        uri,
                        params ?: PlayerParams(),
                        true,
                        0,
                        0,
                        "55"
                    )
                }
            }

    }

    override fun onPause() {
        super.onPause()
        if (isNougatOrLower()) {
            when (appState) {
                AppState.DOWNLOAD -> {
                    viewModel.pause()
                }
                AppState.PLAY -> {
                    params = player?.getPlayersParams()
                    player?.releasePlayer()
                }
            }

        }
    }

    override fun onStop() {
        super.onStop()
        if (!isNougatOrLower()) {

            when (appState) {
                AppState.DOWNLOAD -> {
//                    Log.d("mmm", "MainActivity :  onStop --  ")
                    viewModel.pause()
                }
                AppState.PLAY -> {
                    params = player?.getPlayersParams()
                    player?.releasePlayer()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        player?.releasePlayer()
//        myDownloadManager.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

//        player?.let {
//            outState.putInt(CURRENT_WINDOW, it.currentWindowIndex)
//            outState.putLong(PLAYBACK_POSITION, it.currentPosition)
//        }
//        playerView.player = null
        if (appState == AppState.PLAY) {
//            Log.d("mmm", "MainActivity :  onSaveInstanceState --  ")
            outState.putParcelable(PLAYER_PARAMS, player?.getPlayersParams())
        }
        outState.putSerializable(APP_STATE, appState)
        outState.putString(EDIT_TEXT_URL, editTextURL)
        outState.putString(MESSAGE_TEXT, textViewResult.text.toString())
//        outState.putInt(FULL_FILE_LENGTH, fullFileLength)
    }

    companion object {
        private const val PLAYBACK_POSITION = "playback_position"
        private const val CURRENT_WINDOW = "current_window"
        private const val PLAYER_PARAMS = "player_params"
        private const val APP_STATE = "app_state"
        private const val MESSAGE_TEXT = "message_text"
        private const val EDIT_TEXT_URL = "edit_text_url"
        private const val FULL_FILE_LENGTH = "full_file_length"

        private const val DEFAULT_URL =
//            "https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"     // 10 Mb
//                    "https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4"   // 30 Mb
//            "http://mirrors.standaloneinstaller.com/video-sample/star_trails.mp4"   // 20 Mb
            "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"   // 20 Mb
        private const val PATH_NAME = "/storage/emulated/0/Download/exoplayervideo.mp4"
    }
}

enum class AppState {
    IDLE, DOWNLOAD, PAUSE, PLAY, CLEARED
}
