package com.example.downloadandplayvideotask

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.util.Util

fun isNougatOrLower() = Util.SDK_INT <= Build.VERSION_CODES.N

fun isWriteExternalStoragePermissionGranted(context: Context) =
    (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)

fun requestWriteExternalStoragePermission(activity: Activity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        555
    )
}