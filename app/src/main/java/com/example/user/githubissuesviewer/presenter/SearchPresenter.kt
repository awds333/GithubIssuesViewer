package com.example.user.githubissuesviewer.presenter

import android.util.Log
import com.example.user.githubissuesviewer.activity.MainActivity.Companion.MY_TAG
import com.example.user.githubissuesviewer.avatar.AvatarHelper
import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.model.Repo
import com.example.user.githubissuesviewer.retrofit.RequestHelper
import com.example.user.githubissuesviewer.view.SearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SearchPresenter(
    private val view: SearchView,
    private val requestHelper: RequestHelper,
    private val disposable: CompositeDisposable,
    private val avatarHelper: AvatarHelper
) {

    fun searchForRepository() {
        val name = view.getName()
        if (name.trim().isEmpty()) {
            view.showProgressBar(false)
            view.displayNameException()
            return
        }
        disposable.clear()
        view.showProgressBar(true)
        disposable.add(
            requestHelper.findReposByName(name.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    if (list.isNotEmpty()) {
                        view.displayRepoName(list[0].owner.login + "/" + list[0].name)
                        getIssuesByRepo(list[0])
                    } else {
                        view.displaySearchException()
                        view.showProgressBar(false)
                    }
                }
                    , { e ->
                        Log.d(MY_TAG, e.toString())
                        view.showProgressBar(false)
                        view.displayNetworkException()
                    })
        )
    }

    private fun getIssuesByRepo(repo: Repo) {
        disposable.clear()
        disposable.add(
            requestHelper.getIssuesByRepo(repo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    view.hideExceptionView()
                    view.showProgressBar(false)
                    view.setIssuesList(list)
                    view.cleanAvatars()
                    getImages(list)
                }
                    , { e ->
                        Log.d(MY_TAG, e.toString())
                        view.showProgressBar(false)
                        if (e.javaClass == IndexOutOfBoundsException::class.java) {
                            view.setIssuesList(emptyList())
                        } else
                            view.displayNetworkException()
                    })
        )
    }

    private fun getImages(issues: List<Issue>) {
        disposable.add(avatarHelper.getAvatars(issues)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view.setAvatar(it.second,it.first)
            }, { e ->
                Log.d(MY_TAG, e.toString())
            }))
    }
}