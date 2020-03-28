package com.example.downloadandplayvideotask

sealed class DownloadResult {

    data class Progress(val progress: Int, val fileLength: Int) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
    object Success : DownloadResult()
    object Clear : DownloadResult()
}