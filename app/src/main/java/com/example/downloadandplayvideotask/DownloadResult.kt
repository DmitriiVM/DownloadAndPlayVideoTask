package com.example.downloadandplayvideotask

sealed class DownloadResult {

    data class Progress(val progress: Int, val fileLength: Int) : DownloadResult()
    object Error : DownloadResult()
    data class Success(val message: SuccessResult) : DownloadResult()
    object Clear : DownloadResult()
    object Paused : DownloadResult()
}

enum class SuccessResult {
    FINISHED, ALREADY_DOWNLOADED
}