package com.example.downloadandplayvideotask.ui

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.downloadandplayvideotask.*
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var uri: Uri
    private var appState = AppState.IDLE
    private var editTextURL: String = DEFAULT_URL
    private var player: MyVideoPlayer? = null
    private var params: PlayerParams? = null

    private lateinit var viewModel: DownloadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uri = Uri.parse(PATH_NAME)
        savedInstanceState?.let {
            params = savedInstanceState.getParcelable(PLAYER_PARAMS)
            appState = savedInstanceState.getSerializable(APP_STATE) as AppState
            editTextURL = savedInstanceState.getString(EDIT_TEXT_URL) ?: DEFAULT_URL
            editTextUrl.setText(editTextURL)
            textViewResult.text = savedInstanceState.getString(MESSAGE_TEXT) ?: ""
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(DownloadViewModel::class.java)

        player = MyVideoPlayer(this)
        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
        subscribeObserver()
        setOnClickListeners()
    }

    private fun subscribeObserver() {
        viewModel.getDownloadLiveData().observe(this, Observer<DownloadResult> { result ->
            when (result) {
                is DownloadResult.Progress -> {
                    setButtonsEnabled(false, true, true)
                    textViewResult.text = getString(
                        R.string.progress, result.progress / 1000, result.fileLength / 1000
                    )
                }
                is DownloadResult.Success -> {
                    setButtonsEnabled(btnDownload = false, btnPaused = false, btnClear = true)
                    textViewResult.text = getString(R.string.result_success)

                    if (appState != AppState.PLAY) {
                        player?.initializePlayer(playerView, uri, params ?: PlayerParams())
                        appState = AppState.PLAY
                    }
                }
                is DownloadResult.Clear -> {
                    setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
                    textViewResult.text = getString(R.string.result_cleared)
                }
                is DownloadResult.Error -> {
                    if (appState != AppState.PLAY) {
                        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
                        textViewResult.text = result.message
                    }
                }
            }
        })
    }

    private fun setOnClickListeners() {
        buttonDownload.setOnClickListener {
            if (isWriteExternalStoragePermissionGranted(this)) {
                requestWriteExternalStoragePermission(this)
            } else {
                startDownload()
            }
        }

        buttonPause.setOnClickListener {
            buttonDownload.isEnabled = false
            when (appState) {
                AppState.PAUSE -> {
                    appState = AppState.DOWNLOAD
                    viewModel.download(URL(editTextURL),
                        PATH_NAME, false)
                    buttonPause.text = getString(R.string.pause)
                }
                else -> {
                    appState = AppState.PAUSE
                    viewModel.pause()
                    buttonPause.text = getString(R.string.resume)
                }
            }
        }

        buttonClear.setOnClickListener {
            if (appState == AppState.PLAY) player?.releasePlayer()
            viewModel.clear(PATH_NAME)
            appState = AppState.CLEARED
        }
    }

    private fun startDownload() {
        setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
        if (editTextUrl.text.isNotBlank()) {
            editTextURL = editTextUrl.text.toString()
        }
        appState = AppState.DOWNLOAD
        viewModel.download(
            URL(editTextURL),
            PATH_NAME, false
        )
    }

    private fun setButtonsEnabled(btnDownload: Boolean, btnPaused: Boolean, btnClear: Boolean) {
        buttonDownload.isEnabled = btnDownload
        buttonPause.isEnabled = btnPaused
        buttonClear.isEnabled = btnClear
    }

    override fun onStart() {
        super.onStart()
        if (!isNougatOrLower()) handleInitializeFunction()
    }

    override fun onResume() {
        super.onResume()
        if (isNougatOrLower()) handleInitializeFunction()
    }

    private fun handleInitializeFunction() {
        when (appState) {
            AppState.DOWNLOAD -> {
                viewModel.download(URL(editTextURL),
                    PATH_NAME, true)
            }
            AppState.PLAY -> {
                player?.initializePlayer(playerView, uri, params ?: PlayerParams())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isNougatOrLower()) handleStopFunction()
    }

    override fun onStop() {
        super.onStop()
        if (!isNougatOrLower()) handleStopFunction()
    }

    private fun handleStopFunction() {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == WRITE_EXTERNAL_STORAGE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                setButtonsEnabled(false, false, false)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (appState == AppState.PLAY) {
            outState.putParcelable(PLAYER_PARAMS, player?.getPlayersParams())
        }
        outState.putSerializable(APP_STATE, appState)
        outState.putString(EDIT_TEXT_URL, editTextURL)
        outState.putString(MESSAGE_TEXT, textViewResult.text.toString())
    }

    companion object {
        private const val PLAYER_PARAMS = "player_params"
        private const val APP_STATE = "app_state"
        private const val MESSAGE_TEXT = "message_text"
        private const val EDIT_TEXT_URL = "edit_text_url"

        private const val DEFAULT_URL =
            "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        private const val PATH_NAME = "/storage/emulated/0/Download/exoplayervideo.mp4"
    }
}

enum class AppState {
    IDLE, DOWNLOAD, PAUSE, PLAY, CLEARED
}
