package com.example.user.githubissuesviewer.retrofit

import android.util.Log
import com.example.user.githubissuesviewer.activity.MainActivity.Companion.MY_TAG
import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.model.Repo
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class RequestHelper {
    var gitApi: GithubAPI = RetrofitClient.instance.create(GithubAPI::class.java)

    fun findReposByName(name: String): Observable<List<Repo>> {
        return gitApi.findReposByName(name)
            .timeout(15, TimeUnit.SECONDS)
            .map { it.items }
    }

    fun getIssuesByRepo(repo: Repo): Observable<List<Issue>> {
        return Observable.interval(0,10, TimeUnit.SECONDS)
            .flatMap { gitApi.getIssues(repo.owner.login, repo.name) }
            .timeout(15, TimeUnit.SECONDS)
            .doOnNext { Log .d(MY_TAG,"next") }
            .distinctUntilChanged { t1, t2 ->
                if (t1.size != t2.size)
                    false
                for (i in 0 until t1.size)
                    if (t1[i] != t2[i])
                        false
                true
            }
    }
}