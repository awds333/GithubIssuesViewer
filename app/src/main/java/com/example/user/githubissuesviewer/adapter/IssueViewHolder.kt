package com.example.user.githubissuesviewer.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.issue_item.view.*

class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val txt_login = itemView.login
    val txt_number = itemView.number
    val img_avatar =  itemView.avatar
    val txt_title = itemView.title
    val txt_comments_count = itemView.comments_count
}