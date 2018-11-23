package com.example.user.githubissuesviewer.avatar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object ImageLoader {
    private var okClient: OkHttpClient = OkHttpClient()

    fun getImage(url: String): Observable<Bitmap> {
        return Observable.create {
            try {
                val response = okClient.newCall(Request.Builder().url(url).build()).execute()
                if (!it.isDisposed) {
                    if (!response.isRedirect) {
                        var bitmap: Bitmap? = BitmapFactory.decodeStream(response.body()!!.byteStream())
                        if (bitmap != null)
                            it.onNext(bitmap)
                    }
                    it.onComplete()
                }
            } catch (e: IOException) {
                it.onComplete()
            }
        }
    }
}