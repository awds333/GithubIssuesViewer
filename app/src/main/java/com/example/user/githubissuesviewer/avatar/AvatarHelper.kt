package com.example.user.githubissuesviewer.avatar

import android.graphics.Bitmap
import com.example.user.githubissuesviewer.model.Issue
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class AvatarHelper {
    fun getAvatars(issues: List<Issue>): Observable<Pair<Bitmap, Int>> {
        return Observable.zip(
            Observable.fromArray(issues).flatMapIterable { it -> it }.distinct(),
            Observable.range(0, issues.size),
            BiFunction { issue: Issue, position: Int -> Pair<String, Int>(issue.user.avatar_url, position) })
            .filter { !LruBitmapCache.hasBitmap(it.first) }
            .flatMap({
                Observable.zip(
                    ImageLoader.getImage(it.first),
                    Observable.just(it),
                    BiFunction { bitmape: Bitmap, position: Pair<String,Int> -> Pair(bitmape, position) })
            }
                , 5)
            .doOnNext { LruBitmapCache.putBitmap(it.second.first,it.first) }
            .retry(5)
            .map { Pair(it.first,it.second.second) }
    }
}