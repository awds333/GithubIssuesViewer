package com.example.user.githubissuesviewer.retrofit

import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.model.RepoList
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubAPI {
    @GET("repos/{user}/{repo}/issues?state=all")
    fun getIssues(@Path("user") user: String, @Path("repo") repo: String): Observable<List<Issue>>

    @GET("search/repositories")
    fun findReposByName(@Query("q") name:String): Observable<RepoList>
}