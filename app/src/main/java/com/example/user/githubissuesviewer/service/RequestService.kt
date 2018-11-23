package com.example.user.githubissuesviewer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.user.githubissuesviewer.Types
import com.example.user.githubissuesviewer.activity.MainActivity.Companion.MY_TAG
import com.example.user.githubissuesviewer.avatar.getAvatars
import com.example.user.githubissuesviewer.retrofit.GithubAPI
import com.example.user.githubissuesviewer.retrofit.RetrofitClient
import com.example.user.githubissuesviewer.retrofit.findReposByName
import com.example.user.githubissuesviewer.retrofit.getIssuesByRepo
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.observables.ConnectableObservable
import io.reactivex.subjects.ReplaySubject
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RequestService : Service(), HttpHandler {

    private var mainOutputObservable: Observable<Pair<JSONObject, Object?>> = Observable.empty()
    private lateinit var repoObservable: ConnectableObservable<Pair<JSONObject, Object?>>
    private lateinit var issuesObservable: ConnectableObservable<Pair<JSONObject, Object?>>
    private lateinit var nameObservable: ConnectableObservable<String>
    private lateinit var avatarObservable: ConnectableObservable<Pair<JSONObject, Object?>>

    private var disposable = CompositeDisposable()

    private var bungSubject: ReplaySubject<Pair<JSONObject, Object?>> = ReplaySubject.create()

    private val gitAPI = RetrofitClient.instance.create(GithubAPI::class.java)

    override fun onCreate() {
        super.onCreate()
        Log.d(MY_TAG, "service create")
    }

    override fun onDestroy() {
        dispose()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder(this)
    }


    override fun getRepo(name: String): Observable<Pair<JSONObject, Object?>> {
        dispose()
        nameObservable = Observable.just(name).publish()
        repoObservable = nameObservable.findReposByName(gitAPI).publish()
        issuesObservable = Observable.combineLatest(
            repoObservable.filter { it.first.getInt("type") == Types.REPO },
            Observable.interval(0, 10, TimeUnit.SECONDS),
            BiFunction<Pair<JSONObject, Object?>, Long, Pair<JSONObject, Object?>> { repo, _ -> repo })
            .getIssuesByRepo(gitAPI).publish()
        avatarObservable = issuesObservable.filter { it.first.getInt("type") == Types.ISSUES }
            .getAvatars().publish()
        mainOutputObservable = Observable.merge(repoObservable, issuesObservable, avatarObservable)
        return mainOutputObservable
    }

    override fun getObservable(): Observable<Pair<JSONObject, Object?>> {
        return bungSubject
    }

    override fun connect() {
        disposable.add(avatarObservable.connect())
        disposable.add(issuesObservable.connect())
        disposable.add(repoObservable.connect())
        disposable.add(nameObservable.connect())
    }

    override fun saveMessages() {
        bungSubject.onComplete()
        bungSubject = ReplaySubject.create()
        mainOutputObservable.subscribe(bungSubject)
    }

    class MyBinder(val handler: HttpHandler) : Binder() {
        fun getHttpHandler(): HttpHandler {
            return handler
        }
    }

    fun dispose() {
        disposable.clear()
        bungSubject.onComplete()
    }
}
