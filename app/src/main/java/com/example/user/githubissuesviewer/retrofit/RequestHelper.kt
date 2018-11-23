package com.example.user.githubissuesviewer.retrofit

import android.util.Log
import com.example.user.githubissuesviewer.Exceptions
import com.example.user.githubissuesviewer.Types
import com.example.user.githubissuesviewer.activity.MainActivity.Companion.MY_TAG
import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.model.Repo
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit

fun Observable<String>.findReposByName(gitAPI: GithubAPI): Observable<Pair<JSONObject, Object?>> {
    return this.flatMap { it ->
        gitAPI.findReposByName(it)
            .timeout(15, TimeUnit.SECONDS)
            .map { it -> it.items }
            .map {
                var message = JSONObject()
                if (it.isEmpty()) {
                    message.put("type", Types.EXCEPTION)
                    message.put("species", Exceptions.SEARCH_EXCEPTION)
                    Pair(message, null)
                } else {
                    message.put("type", Types.REPO)
                    Pair(message, it[0] as Object)
                }
            }
            .onErrorResumeNext(Function {
                var exceptionMessage = JSONObject()
                exceptionMessage.put("type", Types.EXCEPTION)
                exceptionMessage.put("species", Exceptions.NETWORK_EXCEPTION)
                Observable.just(Pair(exceptionMessage, it as Object))
            })
            .subscribeOn(Schedulers.io())
    }
}


fun Observable<Pair<JSONObject, Object?>>.getIssuesByRepo(gitAPI: GithubAPI): Observable<Pair<JSONObject, Object?>> {
    return this.flatMap {
        gitAPI.getIssues((it.second as Repo).owner.login, (it.second as Repo).name)
            .timeout(15, TimeUnit.SECONDS)
            .map {
                var message = JSONObject()
                message.put("type", Types.ISSUES)
                Pair(message, it as Object)
            }
            .onErrorResumeNext(Function {
                Log.d(MY_TAG, it.javaClass.name)
                var exceptionMessage = JSONObject()
                when (it.javaClass) {
                    IndexOutOfBoundsException::class.java -> {
                        exceptionMessage.put("type", Types.ISSUES)
                        Observable.just(Pair(exceptionMessage, emptyArray<Issue>() as Object))
                    }
                    else -> {
                        exceptionMessage.put("type", Types.EXCEPTION)
                        exceptionMessage.put("species", Exceptions.NETWORK_EXCEPTION)
                        Observable.just(Pair(exceptionMessage, it as Object))
                    }
                }
            })
            .subscribeOn(Schedulers.io())
    }

}
