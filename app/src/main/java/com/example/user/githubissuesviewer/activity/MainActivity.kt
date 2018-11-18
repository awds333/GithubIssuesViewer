package com.example.user.githubissuesviewer.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.example.user.githubissuesviewer.R
import com.example.user.githubissuesviewer.adapter.IssueAdapter
import com.example.user.githubissuesviewer.adapter.IssueViewHolder
import com.example.user.githubissuesviewer.avatar.AvatarHelper
import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.presenter.SearchPresenter
import com.example.user.githubissuesviewer.retrofit.RequestHelper
import com.example.user.githubissuesviewer.view.SearchView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SearchView, View.OnClickListener {

    companion object {
        val MY_TAG = "my_tag"
    }

    private lateinit var disposable: CompositeDisposable
    private lateinit var presenter: SearchPresenter
    var issueList: List<Issue> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        disposable = CompositeDisposable()
        presenter = SearchPresenter(this, RequestHelper(), disposable, AvatarHelper())
        initViews()
    }

    private fun initViews() {
        searchButton.setOnClickListener(this)
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            issueList  = Gson().fromJson(savedInstanceState!!.getString("issues"),object :TypeToken<List<Issue>>(){}.type)
            recycler.adapter = IssueAdapter(this,issueList)
            recycler.scrollToPosition(savedInstanceState!!.getInt("position"))
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    override fun displayNameException() {
        exceptionView.text = getText(R.string.name_exception)
        exceptionView.visibility = View.VISIBLE
    }

    override fun displaySearchException() {
        exceptionView.text = getText(R.string.search_exception)
        exceptionView.visibility = View.VISIBLE
    }

    override fun displayNetworkException() {
        exceptionView.text = getText(R.string.net_exception)
        exceptionView.visibility = View.VISIBLE
    }

    override fun getName(): String {
        return name.text.toString()
    }

    override fun showProgressBar(active: Boolean) {
        progressBar.visibility = if (active) {
            exceptionView.visibility = View.INVISIBLE
            View.VISIBLE
        } else View.INVISIBLE
    }

    override fun displayRepoName(name: String) {
        supportActionBar!!.title = name
    }

    override fun setIssuesList(issues: List<Issue>) {
        issueList = issues
        recycler.adapter = IssueAdapter(this, issues)
    }

    override fun onClick(v: View?) {
        presenter.searchForRepository()
    }

    override fun hideExceptionView() {
        exceptionView.visibility = View.INVISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString("issues", Gson().toJson(issueList).toString())
        outState!!.putInt("position", (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
    }

    override fun setAvatar(position: Int, avatar: Bitmap) {
        if(recycler.findViewHolderForAdapterPosition(position)!=null)
            (recycler.findViewHolderForAdapterPosition(position) as IssueViewHolder)
                .img_avatar.setImageBitmap(avatar)
    }

    override fun cleanAvatars() {
        android.R.drawable.ic_menu_crop
    }
}
