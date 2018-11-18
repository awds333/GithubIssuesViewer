package com.example.user.githubissuesviewer.avatar

import android.graphics.Bitmap
import android.support.v4.util.LruCache
import android.util.Log
import com.example.user.githubissuesviewer.activity.MainActivity.Companion.MY_TAG

object LruBitmapCache {
    private lateinit var mCache: LruCache<String, Bitmap>

    init {
        var size = (Runtime.getRuntime().freeMemory()).toInt()
        mCache = LruCache(size/4)
    }

    fun getBitmap(key: String): Bitmap {

        return mCache.get(key)!!
    }

    fun putBitmap(key: String, value: Bitmap) {
        if (!hasBitmap(key))
            mCache.put(key, value)
        Log.d(MY_TAG, hasBitmap(key).toString())
    }

    fun hasBitmap(key: String): Boolean {
        return mCache.get(key) != null
    }

    fun clean(){
        mCache.evictAll()
    }
}