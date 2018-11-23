package com.example.user.githubissuesviewer.service

import io.reactivex.Observable
import org.json.JSONObject

interface HttpHandler {
    fun getRepo(name: String): Observable<Pair<JSONObject, Object?>>
    fun getObservable(): Observable<Pair<JSONObject,Object?>>
    fun saveMessages()
    fun connect()
}