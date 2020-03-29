package com.example.util

import android.content.Context
import com.example.downloadandplayvideotask.ui.MainActivity

object SharedPreferenceHelper {

    private const val STRING_URL = "string_url"
    private const val PLAYBACK_POSITION = "playback_position"
    private const val SHARED_PREF = "shared_preference"

    fun putStringUrl(context: Context, stringUrl : String){
        context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(STRING_URL, stringUrl)
            .apply()
    }

    fun getStringUrl(context: Context) : String?  =
        context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            .getString(STRING_URL, MainActivity.DEFAULT_URL)


    fun putPlaybackPosition(context: Context, playbackPosition : Long){
        context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            .edit()
            .putLong(PLAYBACK_POSITION, playbackPosition)
            .apply()
    }

    fun getPlaybackPosition(context: Context) =
        context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            .getLong(PLAYBACK_POSITION, 0)
}