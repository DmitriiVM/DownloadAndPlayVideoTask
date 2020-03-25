package com.example.downloadandplayvideotask

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection


class MainActivity : AppCompatActivity() {

    private var player: MyPlayer? = null
    private lateinit var uri: Uri


    private var isPaused = false
    private var isCleared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uri = Uri.parse("content://downloads/all_downloads/89")

        buttonClear.isEnabled = false
        buttonPause.isEnabled = false

        buttonClear.setOnClickListener {
//            val file = File("/storage/emulated/0/Download/ttt.mp4")
//
//            if (file.exists()) {
//                val downloadedLength = file.length()
//                Log.d("mmm", "MainActivity :  onCreate --  $downloadedLength")
//            }
            buttonClear.isEnabled = false
            buttonPause.isEnabled = false
            buttonDownload.isEnabled = true
        }


        buttonPause.setOnClickListener {
            if (isPaused) {
                isPaused = false
                buttonPause.text = "Pause"
                buttonDownload.isEnabled = true
            } else {
                isPaused = true
                buttonPause.text = "Resume"
                buttonDownload.isEnabled = false
            }

        }

        buttonDownload.setOnClickListener {
            buttonPause.isEnabled = true
            buttonClear.isEnabled = true

            if (isWriteExternalStoragePermissionGranted()) {
                requestWriteExternalStoragePermission()
            } else {

//                if (buttonStart.text == "Start") {
//                    player = MyPlayer(this@MainActivity)
//                    uri =
////                        Uri.fromFile(File("/storage/emulated/0/Download/ttt.mp4"))
//                        Uri.parse("content://downloads/all_downloads/89")
//
//                    player?.initializePlayer(playerView, uri)
//                    buttonStart.text = "Release"
//                } else {
//                    player?.releasePlayer()
//                    buttonStart.text = "Start"
//                }


                CoroutineScope(Dispatchers.IO).launch {

                    val url =
                        URL("https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4")
//                    var connection: HttpURLConnection? = null

                    var inputStream: BufferedInputStream? = null
                    val outputStream: BufferedOutputStream?

                    val file = File("/storage/emulated/0/Download/ttt.mp4")




                    var connection = url.openConnection() as HttpURLConnection
                    if (file.exists()) {
                        val downloadedLength = file.length()
                        Log.d("mmm", "MainActivity :  onCreate -downloadedLength-  $downloadedLength")
//                        connection.setRequestProperty("Range", "bytes=$downloadedLength-")
                        outputStream = BufferedOutputStream(FileOutputStream(file, true))
                    } else {
                        outputStream = BufferedOutputStream(FileOutputStream(file))
                    }



                    try {


                        connection.connect()
                        val fileSize = connection.contentLength
                        inputStream = BufferedInputStream(connection.inputStream)


                        val data = ByteArray(4096)
                        var downloadedLength = 0
                        var count = 0

                        var currentTime = System.currentTimeMillis()

                        while (count != -1) {
                            count = inputStream.read(data )
                            outputStream.write(data, 0, count)
                            downloadedLength += count

                            if (isPaused){
                                break
                            }

                            if (fileSize > 0) {

                                withContext(Dispatchers.Main) {
                                    if ((System.currentTimeMillis() - currentTime) > 500) {
                                        Log.d("mmm", "MainActivity :  onCreate --  $downloadedLength")
                                        textViewResult.text = "Downloading: ${downloadedLength}kb / ${fileSize}mb"
                                        currentTime = System.currentTimeMillis()
                                    }
                                }
                            }


                        }


                    } catch (e: Exception) {
//                        Log.d("mmm", "Exception ${e}")
                    } finally {
//                        Log.d("mmm", "MainActivity :  onCreate --  uuuuu")
                        inputStream?.close()
                        outputStream.close()
                        connection?.disconnect()


                    }


                }


            }


        }
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            555
        )
    }

    private fun isWriteExternalStoragePermissionGranted() =
        (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > Build.VERSION_CODES.N) {
            player?.initializePlayer(playerView, uri)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= Build.VERSION_CODES.N) {
            player?.initializePlayer(playerView, uri)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= Build.VERSION_CODES.N) {
            player?.releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > Build.VERSION_CODES.N) {
            player?.releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
