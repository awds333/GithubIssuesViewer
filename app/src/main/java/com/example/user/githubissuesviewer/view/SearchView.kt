package com.example.user.githubissuesviewer.view

import android.graphics.Bitmap
import com.example.user.githubissuesviewer.model.Issue

interface SearchView {
    fun getName(): String
    fun displayNameException()
    fun displaySearchException()
    fun displayNetworkException()
    fun showProgressBar(active: Boolean)
    fun displayRepoName(name: String)
    fun setIssuesList(issues : List<Issue>)
    fun hideExceptionView()
    fun setAvatar(position:Int,avatar:Bitmap)
    fun cleanAvatars()
}