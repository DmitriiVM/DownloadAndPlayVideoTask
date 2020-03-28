package com.example.downloadandplayvideotask

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object MyDownloadManager {

    private var job: Job? = null
    private var isCancelled = false
    private var isPaused = false
    private var fileFullSize = 0

    private val _downloadLiveData = MutableLiveData<DownloadResult>()
    val downloadLiveData: MutableLiveData<DownloadResult>
        get() = _downloadLiveData

    fun clear(pathName: String) {
        fileFullSize = 0
        isCancelled = true
        val file = File(pathName)
        if (file.exists()) {
            file.delete()
        }
        _downloadLiveData.value = DownloadResult.Clear
    }

    fun pause() {
        isPaused = true
    }

    fun download(url: URL, pathName: String, isAfterRestore: Boolean) {
        if (isAfterRestore)  isPaused = false

        job = CoroutineScope(Dispatchers.IO).launch {

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
                    isCancelled = false
                    BufferedOutputStream(FileOutputStream(file))
                }

                connection.connect()
                val fileLength = connection.contentLength
                if (fileFullSize == 0) fileFullSize = fileLength

                inputStream = BufferedInputStream(connection.inputStream)

                val data = ByteArray(4096)
                var downloadedFileLength = 0
                var numberOfBytes: Int

                var currentTime = System.currentTimeMillis()

                while (true) {

                    if (isCancelled || isPaused) break

                    numberOfBytes = inputStream.read(data)
                    if (numberOfBytes == -1) {
                        _downloadLiveData.postValue(DownloadResult.Success)
                        break
                    }

                    outputStream.write(data, 0, numberOfBytes)
                    downloadedFileLength += numberOfBytes

                    if ((System.currentTimeMillis() - currentTime) > 200) {
                        _downloadLiveData.postValue(
                            DownloadResult.Progress(file.length().toInt(), fileFullSize)
                        )
                        currentTime = System.currentTimeMillis()
                    }
                }

            } catch (e: Exception) {
                _downloadLiveData.postValue(DownloadResult.Error("Exception ${e}"))
            } finally {
                inputStream?.close()
                outputStream?.close()
                connection?.disconnect()
                isCancelled = false
                isPaused = false
            }
        }
    }

    fun onDestroy() {
        job?.cancel()
    }
}