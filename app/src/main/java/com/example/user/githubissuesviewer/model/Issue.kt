package com.example.user.githubissuesviewer.model

class Issue {
    var user: User = User()
    var title: String = "1"
    var comments: Int = 0
    var state: String = "open"
    var number: Int = 0
}