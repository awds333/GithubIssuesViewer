package com.example.user.githubissuesviewer.avatar

import android.graphics.Bitmap
import android.support.v4.util.LruCache

object LruBitmapCache {
    private lateinit var mCache: LruCache<String, Bitmap>

    init {
        var size = (Runtime.getRuntime().freeMemory()).toInt()
        mCache = LruCache(size / 4)
    }

    fun getBitmap(key: String): Bitmap {

        return mCache.get(key)!!
    }

    @Synchronized
    fun putBitmap(key: String, value: Bitmap) {
        if (!hasBitmap(key))
            mCache.put(key, value)
    }

    @Synchronized
    fun hasBitmap(key: String): Boolean {
        return mCache.get(key) != null
    }

    fun clean(key: String) {
        mCache.remove(key)
        mCache.evictAll()
    }
}