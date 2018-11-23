package com.example.user.githubissuesviewer.presenter

import android.graphics.Bitmap
import android.util.Log
import com.example.user.githubissuesviewer.Exceptions
import com.example.user.githubissuesviewer.Types
import com.example.user.githubissuesviewer.activity.MainActivity
import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.model.Repo
import com.example.user.githubissuesviewer.service.HttpHandler
import com.example.user.githubissuesviewer.view.SearchView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.json.JSONObject

class SearchPresenter(
    private val view: SearchView,
    private val handler: HttpHandler,
    private var issues: List<Issue>
) {
    private var disposable: Disposable = subscribe(handler.getObservable())

    fun searchForRepository() {
        val name = view.getName()
        if (name.trim().isEmpty()) {
            view.showProgressBar(false)
            view.displayNameException()
            return
        }
        if (!disposable.isDisposed)
            disposable.dispose()
        view.showProgressBar(true)
        disposable = subscribe(handler.getRepo(name.trim()))
        handler.connect()
    }

    private inline fun subscribe(observable: Observable<Pair<JSONObject, Object?>>): Disposable {
        return observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it.first.getInt("type")) {
                    Types.REPO -> {
                        val repo = (it.second as Repo)
                        view.displayRepoName(repo.owner.login + "/" + repo.name)
                        view.setIssuesList(emptyList())
                        for (issue in issues)
                            view.cleanAvatars(issue.user.avatar_url)
                    }
                    Types.ISSUES -> {
                        view.showProgressBar(false)
                        view.hideExceptionView()
                        var list = it.second as List<Issue>
                        if (!equals(list,issues)) {
                            view.setIssuesList(it.second as List<Issue>)
                            issues = it.second as List<Issue>
                        }
                    }
                    Types.AVATAR -> {
                        for (issue in issues)
                            if (issue.user.avatar_url.equals(it.first.getString("url")))
                                view.setAvatar(issues.indexOf(issue), it.second as Bitmap)
                    }
                    Types.EXCEPTION -> {
                        when (it.first.getInt("species")) {
                            Exceptions.NETWORK_EXCEPTION -> {
                                view.showProgressBar(false)
                                view.displayNetworkException()
                            }
                            Exceptions.SEARCH_EXCEPTION -> {
                                view.showProgressBar(false)
                                view.displaySearchException()
                            }
                        }
                        Log.d(MainActivity.MY_TAG, "exception " + it.first.get("species"))
                    }
                }
            }, {
                Log.d(MainActivity.MY_TAG, "main failed!")
            }, {
                Log.d(MainActivity.MY_TAG, "main completed!")
            })
    }

    fun equals(a: List<Issue>, b: List<Issue>): Boolean {
        if(a.size!=b.size)
            return false
        for(i in 0 until a.size) {
            if (a[i].title != b[i].title)
                return false
            else if (a[i].comments != b[i].comments)
                return false
            else if (a[i].number != b[i].number)
                return false
            else if (a[i].state != b[i].state)
                return false
            else if (a[i].user.login != b[i].user.login)
                return false
            else if (a[i].user.avatar_url != b[i].user.avatar_url)
                return false
        }
        return true
    }

    fun finish() {
        handler.saveMessages()
        if (!disposable.isDisposed)
            disposable.dispose()
    }
}