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
            val response = okClient.newCall(Request.Builder().url(url).build()).execute()
            if (!it.isDisposed)
                if (response.isSuccessful) {
                    it.onNext(BitmapFactory.decodeStream(response.body()!!.byteStream()))
                    it.onComplete()
                } else {
                    it.onError(IOException("RequestFailed"))
                }
        }
    }
}