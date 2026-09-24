package com.yourteam.shareplate.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourteam.shareplate.R
import com.yourteam.shareplate.data.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(private var items: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotifTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotifTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message
        val date = item.timestamp?.toDate()
        holder.tvTime.text = if (date != null) SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(date) else "Just now"
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<NotificationItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
