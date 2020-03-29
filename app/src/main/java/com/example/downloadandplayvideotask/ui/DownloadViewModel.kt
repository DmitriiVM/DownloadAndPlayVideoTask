package com.example.downloadandplayvideotask.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.downloadandplayvideotask.DownloadResult
import com.example.downloadandplayvideotask.MyVideoPlayer
import com.example.downloadandplayvideotask.PlayerParams
import com.example.downloadandplayvideotask.data.MyDownloadManager
import com.google.android.exoplayer2.ui.PlayerView
import java.net.URL

class DownloadViewModel : ViewModel() {


    private var params: PlayerParams? = null
    var isDownloading = false

    fun getDownloadLiveData() = MyDownloadManager.downloadLiveData

    fun download(url: URL, pathName: String, isAfterRestore: Boolean) {
        isDownloading = true
        MyDownloadManager.download(url, pathName, isAfterRestore)
    }

    fun pause() {
        isDownloading = false
        MyDownloadManager.pause()
    }

    fun clear(pathName: String) {
        isDownloading = false
        MyDownloadManager.clear(pathName)
    }

    override fun onCleared() {
        super.onCleared()
        MyDownloadManager.onDestroy()
    }




    fun onResume(url: URL, pathName: String, isAfterRestore: Boolean) {
        if (isDownloading) {
            MyDownloadManager.download(url, pathName, isAfterRestore)
        }
    }

    fun onPause(playerView: PlayerView) {
        if (getDownloadLiveData().value is DownloadResult.Success) {
            params = MyVideoPlayer.getPlayersParams()
            MyVideoPlayer.releasePlayer(playerView)
        } else if (isDownloading){
            MyDownloadManager.pause()
        }
    }

    fun startPlayer(context: Context, playerView: PlayerView, uri: Uri){
        isDownloading = false
        MyVideoPlayer.initializePlayer(context, playerView, uri, params ?: PlayerParams())
    }

    fun stopPlayer(playerView: PlayerView){
        params = null
        MyVideoPlayer.releasePlayer(playerView)
    }


}