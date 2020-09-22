package com.cahstudio.rumahtentor.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.UserMessage
import kotlinx.android.synthetic.main.item_message.view.*

class UserMessageAdapter(val context: Context, val userMessageList: List<UserMessage>, val listener: (UserMessage) -> Unit)
    : RecyclerView.Adapter<UserMessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserMessageAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false))
    }

    override fun getItemCount(): Int = userMessageList.size

    override fun onBindViewHolder(holder: UserMessageAdapter.ViewHolder, position: Int) {
        val message = userMessageList[position]

        holder.tvName.text = message.name
        holder.itemView.setOnClickListener {
            listener(message)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvName = view.itemmessage_tvName
    }
}