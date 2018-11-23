package com.example.user.githubissuesviewer.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.user.githubissuesviewer.R
import com.example.user.githubissuesviewer.avatar.LruBitmapCache
import com.example.user.githubissuesviewer.model.Issue

class IssueAdapter(internal var context: Context, internal var issueList: List<Issue>) :
    RecyclerView.Adapter<IssueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewTipe: Int): IssueViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.issue_item, parent, false)
        return IssueViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return issueList.size
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.txt_login.text = issueList[position].user.login
        holder.txt_comments_count.text = issueList[position].comments.toString()
        holder.txt_number.text = issueList[position].number.toString()
        holder.txt_title.text = issueList[position].title
        holder.txt_state.visibility = if (issueList[position].state.equals("closed")) View.VISIBLE else View.INVISIBLE
        if (LruBitmapCache.hasBitmap(issueList[position].user.avatar_url))
            holder.img_avatar.setImageBitmap(LruBitmapCache.getBitmap(issueList[position].user.avatar_url))
        else
            holder.img_avatar.setImageResource(android.R.drawable.ic_menu_crop)
    }


}