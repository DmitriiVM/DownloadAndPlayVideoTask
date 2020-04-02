package com.example.downloadandplayvideotask.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

object MyDownloadManager {

    private var job: Job? = null
    private var isCancelled = AtomicBoolean(false)
    private var isPaused = AtomicBoolean(false)
    private const val REFRESH_DELAY = 200
    private const val TAG = "video_downloader_task"
    private val _downloadLiveData = MutableLiveData<DownloadResult>()
    val downloadLiveData: LiveData<DownloadResult>
        get() = _downloadLiveData

    fun clear(pathName: String) {
        isCancelled.set(true)
        val file = File(pathName)
        if (file.exists()) {
            file.delete()
        }
        _downloadLiveData.value =
            DownloadResult.Clear
    }

    fun pause() {
        isPaused.set(true)
    }

    fun download(url: URL, pathName: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            if (isFileAlreadyDownloaded(url, pathName)) {
                _downloadLiveData.postValue(
                    DownloadResult.Success(SuccessResult.ALREADY_DOWNLOADED)
                )
                return@launch
            }
            val file = File(pathName)
            var connection: HttpURLConnection? = null
            var outputStream: BufferedOutputStream? = null
            var inputStream: BufferedInputStream? = null

            try {
                connection = url.openConnection() as HttpURLConnection
                outputStream = if (file.exists()) {
                    connection.setRequestProperty("Range", "bytes=${file.length()}-")
                    BufferedOutputStream(FileOutputStream(file, true))
                } else {
                    isCancelled.set(false)
                    BufferedOutputStream(FileOutputStream(file))
                }
                connection.connect()
                inputStream = BufferedInputStream(connection.inputStream)
                val data = ByteArray(4096)
                var downloadedFileLength = 0
                var numberOfBytes: Int
                var currentTime = System.currentTimeMillis()

                var fullFileLength = getFullFileLength(url)

                while (true) {
                    if (isCancelled.get()) {
                        _downloadLiveData.postValue(DownloadResult.Clear)
                        break
                    }
                    if (isPaused.get()) {
                        _downloadLiveData.postValue(DownloadResult.Paused)
                        break
                    }
                    numberOfBytes = inputStream.read(data)
                    if (numberOfBytes == -1) {
                        _downloadLiveData.postValue(
                            DownloadResult.Success(SuccessResult.FINISHED)
                        )
                        break
                    }
                    outputStream.write(data, 0, numberOfBytes)
                    downloadedFileLength += numberOfBytes
                    if (System.currentTimeMillis() - currentTime > REFRESH_DELAY) {
                        _downloadLiveData.postValue(
                            DownloadResult.Progress(
                                file.length().toInt(),
                                fullFileLength
                            )
                        )
                        currentTime = System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "MyDownloadManager :  Exception ${e}")
                _downloadLiveData.postValue(
                    DownloadResult.Error
                )
            } finally {
                inputStream?.close()
                outputStream?.close()
                connection?.disconnect()
                isCancelled.set(false)
                isPaused.set(false)
            }
        }
    }

    private fun isFileAlreadyDownloaded(url: URL, pathName: String): Boolean {
        val connection = url.openConnection()
        val serverLength = connection.contentLength
        val localLength = File(pathName).length()
        if (File(pathName).exists()) {
            if (serverLength.toLong() == localLength) {
                return true
            }
        }
        return false
    }

    private fun getFullFileLength(url: URL) : Int{
        val connection = url.openConnection()
        return connection.contentLength
    }

    fun onDestroy() {
        job?.cancel()
    }
}
