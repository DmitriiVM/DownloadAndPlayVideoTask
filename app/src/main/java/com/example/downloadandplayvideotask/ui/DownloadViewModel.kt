package com.example.downloadandplayvideotask.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.downloadandplayvideotask.data.MyDownloadManager
import java.net.URL

class DownloadViewModel : ViewModel() {

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

    fun onPause() {
        if (isDownloading){
            MyDownloadManager.pause()
        }
    }


}