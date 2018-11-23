package com.example.user.githubissuesviewer.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.example.user.githubissuesviewer.R
import com.example.user.githubissuesviewer.adapter.IssueAdapter
import com.example.user.githubissuesviewer.adapter.IssueViewHolder
import com.example.user.githubissuesviewer.avatar.LruBitmapCache
import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.presenter.SearchPresenter
import com.example.user.githubissuesviewer.service.RequestService
import com.example.user.githubissuesviewer.view.SearchView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SearchView, View.OnClickListener, ServiceConnection {

    private lateinit var presenter: SearchPresenter
    var issueList: List<Issue> = emptyList()
    private var terminalClosing = true
    private var buttonEnabled = true
    private val handler = Handler()

    override fun onServiceDisconnected(name: ComponentName?) {
        if (!isFinishing) {
            searchButton.isEnabled = false
            bindService(Intent(this, RequestService::class.java), this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        presenter = SearchPresenter(this, (service as RequestService.MyBinder).getHttpHandler(), issueList)
        searchButton.isEnabled = true
        Log.d(MY_TAG, "service binded")
    }

    companion object {
        const val MY_TAG = "my_tag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        bindService(Intent(this, RequestService::class.java), this, Context.BIND_AUTO_CREATE)
    }

    private fun initViews() {
        searchButton.setOnClickListener(this)
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        name.setText(savedInstanceState!!.getString("name"))
        supportActionBar!!.title = savedInstanceState!!.getString("title")
        progressBar.visibility = savedInstanceState!!.getInt("progress_visible")
        exceptionView.text = savedInstanceState!!.getString("exception_text")
        exceptionView.visibility = savedInstanceState!!.getInt("exception_visible")
        issueList =
                Gson().fromJson(savedInstanceState!!.getString("issues"), object : TypeToken<List<Issue>>() {}.type)
        recycler.adapter = IssueAdapter(this, issueList)
        recycler.scrollToPosition(savedInstanceState!!.getInt("position"))
    }

    override fun onDestroy() {
        presenter.finish()
        if (terminalClosing)
            stopService(Intent(this, RequestService::class.java))
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
        var position = 0
        if (issueList != emptyList<Issue>() && issues != emptyList<Issue>())
            position = (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        issueList = issues
        recycler.adapter = IssueAdapter(this, issues)
        recycler.scrollToPosition(position)
    }

    override fun onClick(v: View?) {
        if (buttonEnabled) {
            buttonEnabled = false
            presenter.searchForRepository()
            handler.postDelayed({ buttonEnabled = true }, 1500)
        }

    }

    override fun hideExceptionView() {
        exceptionView.visibility = View.INVISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        terminalClosing = false
        outState!!.putString("name", name.text.toString())
        outState!!.putString("title", supportActionBar!!.title.toString())
        outState!!.putInt("progress_visible", progressBar.visibility)
        outState!!.putInt("exception_visible", exceptionView.visibility)
        outState!!.putString("exception_text", exceptionView.text.toString())
        outState!!.putString("issues", Gson().toJson(issueList).toString())
        outState!!.putInt("position", (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
    }

    override fun setAvatar(position: Int, avatar: Bitmap) {
        if (recycler.findViewHolderForAdapterPosition(position) != null)
            (recycler.findViewHolderForAdapterPosition(position) as IssueViewHolder)
                .img_avatar.setImageBitmap(avatar)
    }

    override fun cleanAvatars(key: String) {
        LruBitmapCache.clean(key)
    }
}
