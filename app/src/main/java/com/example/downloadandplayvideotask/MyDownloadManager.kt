package com.example.downloadandplayvideotask

import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MyDownloadManager(private val downloadManagerCallback: DownloadManagerCallback) {

    private var job: Job? = null
    private var url: URL? = null
    private var pathName: String? = null
    private lateinit var file: File
    private var fileFullLength = 0

    private var isCancelled = false
    private var isPaused = false
    private var isError = false

    fun clear() {
        isCancelled = true
        if (file.exists()) {
            file.delete()
        }
        downloadManagerCallback.onDownloadCleared()
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
        if (url != null && pathName != null) {
            download(url!!, pathName!!)
        }
    }

    fun download(url: URL, pathName: String) {
        this.url = url
        this.pathName = pathName

        job = CoroutineScope(Dispatchers.IO).launch {

            file = File(pathName)
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
                if (fileFullLength == 0) {
                    fileFullLength = fileLength
                }
                inputStream = BufferedInputStream(connection.inputStream)

                val data = ByteArray(4096)
                var downloadedFileLength = 0
                var numberOfBytes = 0

                var currentTime = System.currentTimeMillis()

                while (true) {
                    numberOfBytes = inputStream.read(data)
                    if (numberOfBytes == - 1) break
                    outputStream.write(data, 0, numberOfBytes)
                    downloadedFileLength += numberOfBytes

                    if (isCancelled) {
                        if (file.exists()) {
                            file.delete()
                        }
                        break
                    }

                    if (isPaused) {
                        break
                    }

                    if (fileLength > 0) {

                        withContext(Dispatchers.Main) {
                            if ((System.currentTimeMillis() - currentTime) > 200) {
                                downloadManagerCallback.onProgressUpdate(
                                    file.length().toInt(),
                                    fileFullLength
                                )
                                currentTime = System.currentTimeMillis()
                            }
                        }
                    }
                }


            } catch (e: Exception) {
                isError = true
                withContext(Dispatchers.Main) {
                    downloadManagerCallback.onError("Exception ${e}")
                }
            } finally {
                inputStream?.close()
                outputStream?.close()
                connection?.disconnect()
                withContext(Dispatchers.Main) {
                    if (!isPaused && !isCancelled && !isError) {
                        downloadManagerCallback.onDownloadFinished()
                    }
                }
                if (isCancelled || !isPaused) {
                    fileFullLength = 0
                }
                isCancelled = false
                isPaused = false
            }
        }
    }

    fun onDestroy() {
        job?.cancel()
    }
}


interface DownloadManagerCallback {

    fun onProgressUpdate(progress: Int, fileLength: Int)

    fun onDownloadFinished()

    fun onDownloadCleared()

    fun onError(message: String)
}
