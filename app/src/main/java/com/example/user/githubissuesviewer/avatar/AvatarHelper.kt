package com.example.user.githubissuesviewer.avatar

import android.graphics.Bitmap
import com.example.user.githubissuesviewer.model.Issue
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class AvatarHelper {
    fun getAvatars(issues: List<Issue>): Observable<Pair<Bitmap, String>> {
        return Observable.interval(0, 10, TimeUnit.SECONDS)
            .flatMap {
                Observable.fromArray(issues)
                    .flatMapIterable { it -> it }
                    .map { it.user.avatar_url }
                    .distinct()
            }
            .filter { !LruBitmapCache.hasBitmap(it) }
            .flatMap({
                Observable.zip(
                    ImageLoader.getImage(it),
                    Observable.just(it),
                    BiFunction { bitmape: Bitmap, position: String -> Pair(bitmape, position) })
            }
                , 5)
            .doOnNext { LruBitmapCache.putBitmap(it.second, it.first) }
    }
}