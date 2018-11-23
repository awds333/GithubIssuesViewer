package com.example.user.githubissuesviewer.avatar

import android.graphics.Bitmap
import com.example.user.githubissuesviewer.Types
import com.example.user.githubissuesviewer.model.Issue
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit

fun Observable<Pair<JSONObject, Object?>>.getAvatars(): Observable<Pair<JSONObject, Object?>> {
    return this.flatMap({
        Observable.fromArray(it.second as List<Issue>)
            .flatMapIterable { it -> it }
            .map { it.user.avatar_url }
            .distinct()
            .filter { !LruBitmapCache.hasBitmap(it) }
            .flatMap({
                Observable.zip(
                    ImageLoader.getImage(it),
                    Observable.just(it),
                    BiFunction { bitmap: Bitmap, url: String -> Pair(url, bitmap) })
                    .timeout(15, TimeUnit.SECONDS)
                    .map {
                        var message = JSONObject()
                        message.put("type", Types.AVATAR)
                        message.put("url", it.first)
                        Pair(message, it.second as Object)
                    }
                    .onErrorResumeNext(Observable.empty())
                    .subscribeOn(Schedulers.io())
            }, 5)
            .doOnNext {
                LruBitmapCache.putBitmap(it.first.getString("url"), it.second as Bitmap)
            }
    }, 1)
}
