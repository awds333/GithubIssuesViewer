package com.example.user.githubissuesviewer.presenter

import android.graphics.Bitmap
import com.example.user.githubissuesviewer.Exceptions
import com.example.user.githubissuesviewer.Types
import com.example.user.githubissuesviewer.model.Issue
import com.example.user.githubissuesviewer.model.Repo
import com.example.user.githubissuesviewer.model.User
import com.example.user.githubissuesviewer.service.HttpHandler
import com.example.user.githubissuesviewer.view.SearchView
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class SearchPresenterUnitTest {

    @Before
    fun setMainThreadScheduler() {
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.single() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.newThread() }
    }

    @Test
    fun empty_name_test() {
        var handler = mock(HttpHandler::class.java)
        `when`(handler.getObservable()).thenReturn(Observable.empty())
        var view = mock(SearchView::class.java)
        `when`(view.getName()).thenReturn("   ")
        var presenter = SearchPresenter(view, handler, emptyList())
        presenter.searchForRepository()
        verify(handler).getObservable()
        verify(view).getName()
        verify(view).showProgressBar(false)
        verify(view).displayNameException()
    }

    @Test
    fun normal_name_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        `when`(view.getName()).thenReturn("name")
        `when`(handler.getObservable()).thenReturn(Observable.empty())
        `when`(handler.getRepo("name")).thenReturn(Observable.empty())
        var presenter = SearchPresenter(view, handler, emptyList())
        presenter.searchForRepository()
        verify(handler).getObservable()
        verify(view).getName()
        verify(view).showProgressBar(true)
        verify(handler).getRepo("name")
    }

    @Test
    fun success_request_repo_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        `when`(view.getName()).thenReturn("name")
        `when`(handler.getObservable()).thenReturn(Observable.empty())
        var message = JSONObject()
        message.put("type", Types.REPO)
        var repo = Repo()
        var user = User()
        user.login = "login"
        repo.name = "name"
        repo.owner = user
        `when`(handler.getRepo("name")).then {
            Observable.just(Pair(message, repo))
        }
        var presenter = SearchPresenter(view, handler, emptyList())
        presenter.searchForRepository()
        verify(handler).getObservable()
        verify(view).getName()
        verify(view).showProgressBar(true)
        verify(handler).getRepo("name")
        verify(handler).connect()
        verify(view).displayRepoName("login/name")
        verify(view).setIssuesList(emptyList())
    }

    @Test
    fun success_issues_request_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        `when`(view.getName()).thenReturn("name")
        `when`(handler.getObservable()).thenReturn(Observable.empty())
        var messageIssues = JSONObject()
        messageIssues.put("type", Types.ISSUES)
        var user = User()
        user.login = "login"
        var issue = Issue()
        issue.user = user
        `when`(handler.getRepo("name")).then {
            Observable.just(Pair(messageIssues, listOf(issue)))
        }
        var presenter = SearchPresenter(view, handler, emptyList())
        presenter.searchForRepository()
        verify(handler).getObservable()
        verify(view).getName()
        verify(view).showProgressBar(true)
        verify(handler).getRepo("name")
        verify(handler).connect()
        verify(view).showProgressBar(false)
        verify(view).hideExceptionView()
        verify(view).setIssuesList(listOf(issue))
    }

    @Test
    fun empty_search_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        `when`(view.getName()).thenReturn("name")
        `when`(handler.getObservable()).thenReturn(Observable.empty())
        var message = JSONObject()
        message.put("type", Types.EXCEPTION)
        message.put("species", Exceptions.SEARCH_EXCEPTION)
        `when`(handler.getRepo("name")).then {
            Observable.just(Pair(message, IOException()))
        }
        var presenter = SearchPresenter(view, handler, emptyList())
        presenter.searchForRepository()
        verify(handler).getObservable()
        verify(view).getName()
        verify(view).showProgressBar(true)
        verify(handler).getRepo("name")
        verify(handler).connect()
        verify(view).showProgressBar(false)
        verify(view).displaySearchException()
    }

    @Test
    fun network_exception_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        var message = JSONObject()
        message.put("type", Types.EXCEPTION)
        message.put("species", Exceptions.NETWORK_EXCEPTION)
        `when`(handler.getObservable()).then {
            Observable.just(Pair(message, IOException()))
        }
        var presenter = SearchPresenter(view, handler, emptyList())
        verify(handler).getObservable()
        verify(view).showProgressBar(false)
        verify(view).displayNetworkException()
    }

    @Test
    fun avatar_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        var message = JSONObject()
        message.put("type", Types.AVATAR)
        message.put("url", "url")
        var bmp = mock(Bitmap::class.java)
        `when`(handler.getObservable()).then {
            Observable.just(Pair(message, bmp))
        }
        var issue = Issue()
        var user = User()
        user.avatar_url = "url"
        issue.user = user
        var presenter = SearchPresenter(view, handler, listOf(issue))
        verify(handler).getObservable()
        verify(view).setAvatar(0, bmp)
    }

    @Test
    fun update_distinct_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        var message = JSONObject()
        message.put("type", Types.ISSUES)
        var issue = Issue()
        issue.title = "t1"
        var issue2 = Issue()
        issue2.title = "t2"
        `when`(handler.getObservable()).then {
            Observable.just(Pair(message, listOf(issue, issue2)))
        }
        var presenter = SearchPresenter(view, handler, listOf(issue, issue2))
        verify(handler).getObservable()
        verify(view, never()).setIssuesList(listOf(issue, issue2))
    }

    @Test
    fun update_test() {
        var handler = mock(HttpHandler::class.java)
        var view = mock(SearchView::class.java)
        var message = JSONObject()
        message.put("type", Types.ISSUES)
        var issue = Issue()
        issue.title = "t1"
        var issue2 = Issue()
        issue2.title = "t2"
        `when`(handler.getObservable()).then {
            Observable.just(Pair(message, listOf(issue2, issue)))
        }
        var presenter = SearchPresenter(view, handler, listOf(issue, issue2))
        verify(handler).getObservable()
        verify(view).showProgressBar(false)
        verify(view).setIssuesList(listOf(issue2, issue))
    }
}