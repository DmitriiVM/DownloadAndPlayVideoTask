package com.example.downloadandplayvideotask.ui

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.downloadandplayvideotask.*
import com.example.util.SharedPreferenceHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: DownloadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(DownloadViewModel::class.java)


        setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
        buttonDownload.setOnClickListener {
            if (isWriteExternalStoragePermissionGranted(this)) {
                requestWriteExternalStoragePermission(this)
            } else {
                startDownload()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.getDownloadLiveData().value !is DownloadResult.Success) {
            viewModel.onResume(false)
        }
        subscribeObserver()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause(playerView)
    }

    private fun startDownload() {
        setUrl()
        setButtonsEnabled(btnDownload = false, btnPaused = true, btnClear = true)
        viewModel.download(false)
    }

    private fun setUrl(){
        if (editTextUrl.text.isNotBlank()) {
            val stringUrl = editTextUrl.text.toString().trim()
            if (validateUrl(stringUrl)) {
                SharedPreferenceHelper.putStringUrl(this, stringUrl)
            } else {
                Toast.makeText(this, "Url is not valid", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            SharedPreferenceHelper.putStringUrl(this, DEFAULT_URL)
        }
    }


    private fun setButtonsEnabled(btnDownload: Boolean, btnPaused: Boolean, btnClear: Boolean) {
        buttonDownload.isEnabled = btnDownload
        buttonPause.isEnabled = btnPaused
        buttonClear.isEnabled = btnClear
    }


    private fun subscribeObserver() {

        viewModel.getDownloadLiveData().observe(this, Observer<DownloadResult> { result ->
            when (result) {
                is DownloadResult.Progress -> {
                    setButtonsEnabled(false, true, true)
                    textViewResult.text = getString(
                        R.string.progress, result.progress / 1000, result.fileLength / 1000
                    )
                    buttonPause.setOnClickListener {
                        viewModel.pause()
                    }
                    buttonClear.setOnClickListener {
                        viewModel.clear()
                    }
                }

                is DownloadResult.Paused -> {
                    setButtonsEnabled(false, true, true)
                    buttonPause.text = getString(R.string.resume)

                    buttonPause.setOnClickListener {
                        buttonPause.text = getString(R.string.pause)
                        viewModel.download(false)
                    }
                    buttonClear.setOnClickListener {
                        viewModel.clear()
                    }
                }

                is DownloadResult.Clear -> {
                    setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
                    buttonPause.text = getString(R.string.pause)
                    textViewResult.text = getString(R.string.result_cleared)
                    buttonDownload.setOnClickListener {
                        startDownload()
                    }
                }


                is DownloadResult.Success -> {
                    setButtonsEnabled(btnDownload = false, btnPaused = false, btnClear = true)
                    textViewResult.text = getString(R.string.result_success)

                    viewModel.startPlayer(this, playerView)

                    buttonClear.setOnClickListener {
                        viewModel.stopPlayer(playerView)
                        viewModel.clear()
                    }
                }


                is DownloadResult.Error -> {
                    setButtonsEnabled(btnDownload = true, btnPaused = false, btnClear = false)
                    textViewResult.text = result.message
                    buttonDownload.setOnClickListener {
                        startDownload()
                    }
                }
            }
        })
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                setButtonsEnabled(false, false, false)
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.isDownloading || viewModel.getDownloadLiveData().value is DownloadResult.Paused) {
            //dialog
            viewModel.clear()
            viewModel.isDownloading = false
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val DEFAULT_URL =
            "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
    }
}
