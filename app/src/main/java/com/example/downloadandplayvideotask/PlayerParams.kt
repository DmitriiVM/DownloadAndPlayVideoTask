package com.example.downloadandplayvideotask

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayerParams(
    var playWhenPlayerReady: Boolean = true,
    var currentWindow: Int = 0,
    var playbackPosition: Long = 0L
) : Parcelable