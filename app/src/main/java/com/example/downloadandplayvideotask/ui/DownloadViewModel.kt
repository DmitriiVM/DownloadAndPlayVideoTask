package com.example.downloadandplayvideotask.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.downloadandplayvideotask.DownloadResult
import com.example.downloadandplayvideotask.MyVideoPlayer
import com.example.downloadandplayvideotask.data.MyDownloadManager
import com.example.util.SharedPreferenceHelper
import com.google.android.exoplayer2.ui.PlayerView
import java.net.URL

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    var isDownloading = false
    private val uri = Uri.parse(PATH_NAME)
    var textViewMessage = ""

    fun getDownloadLiveData() = MyDownloadManager.downloadLiveData

    fun download(isAfterRestore: Boolean) {
        isDownloading = true
        MyDownloadManager.download(getUrl(), PATH_NAME, isAfterRestore)
    }

    private fun getUrl() : URL {
        val stringUrl = SharedPreferenceHelper.getStringUrl(getApplication()) ?: MainActivity.DEFAULT_URL
        return URL(stringUrl)
    }

    fun pause() {
        isDownloading = false
        MyDownloadManager.pause()
    }

    fun clear() {
        isDownloading = false
        MyDownloadManager.clear(PATH_NAME)
    }

    override fun onCleared() {
        super.onCleared()
        MyDownloadManager.onDestroy()
    }

    fun onResume(isAfterRestore: Boolean) {
        if (isDownloading) {
            MyDownloadManager.download(getUrl(), PATH_NAME, isAfterRestore)
        }
    }

    fun onPause(playerView: PlayerView) {
        if (getDownloadLiveData().value is DownloadResult.Success) {
            val playbackPosition = MyVideoPlayer.releasePlayer(playerView)
            SharedPreferenceHelper.putPlaybackPosition(getApplication(), playbackPosition)
        } else if (isDownloading){
            MyDownloadManager.pause()
        }
    }

    fun startPlayer(context: Context, playerView: PlayerView){
        isDownloading = false
        val playbackPosition = SharedPreferenceHelper.getPlaybackPosition(getApplication())
        MyVideoPlayer.initializePlayer(context, playerView, uri, playbackPosition)
    }

    fun stopPlayer(playerView: PlayerView){
        SharedPreferenceHelper.putPlaybackPosition(getApplication(), START_POSITION)
        MyVideoPlayer.releasePlayer(playerView)
    }

    companion object {
        private const val PATH_NAME = "/storage/emulated/0/Download/exoplayervideo.mp4"
        private const val START_POSITION = 0L
    }
}