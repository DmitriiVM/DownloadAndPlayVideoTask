package com.example.downloadandplayvideotask.ui

import androidx.lifecycle.ViewModel
import com.example.downloadandplayvideotask.data.MyDownloadManager
import java.net.URL

class DownloadViewModel : ViewModel() {

    fun getDownloadLiveData() = MyDownloadManager.downloadLiveData

    fun download(url: URL, pathName: String, isAfterRestore: Boolean) {
        MyDownloadManager.download(url, pathName, isAfterRestore)
    }

    fun pause() {
        MyDownloadManager.pause()
    }

    fun clear(pathName: String) {
        MyDownloadManager.clear(pathName)
    }

    override fun onCleared() {
        super.onCleared()
        MyDownloadManager.onDestroy()
    }
}