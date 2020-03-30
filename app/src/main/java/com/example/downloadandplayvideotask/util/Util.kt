package com.example.downloadandplayvideotask.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.downloadandplayvideotask.R
import com.google.android.exoplayer2.util.Util
import java.util.regex.Pattern

fun isNougatOrLower() = Util.SDK_INT <= Build.VERSION_CODES.N

fun isWriteExternalStoragePermissionGranted(context: Context) =
    (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)

fun requestWriteExternalStoragePermission(activity: Activity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        WRITE_EXTERNAL_STORAGE
    )
}

internal fun Context.showDialog(function: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.dialog_title))
        .setMessage(getString(R.string.dialog_message))
        .setNegativeButton(getString(R.string.dialog_negative_button), null)
        .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
            function()
        }
        .show()
}

const val WRITE_EXTERNAL_STORAGE = 555

const val URL_PATTERN = """\b(https?://)(www.)?(([\w]+)[-.]?(\w)+)+[.][a-zA-z]+[/](([\w${'$'}-.+!*'()]+)([/])([\w${'$'}-.+!*'()]+))+[.](mp4|m4a|fmp4|webm|matroska)${'$'}"""

fun validateUrl(url : String) = Pattern.compile(URL_PATTERN).matcher(url).matches()
