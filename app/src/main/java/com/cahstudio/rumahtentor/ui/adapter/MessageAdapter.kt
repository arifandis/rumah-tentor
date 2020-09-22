package com.cahstudio.rumahtentor.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Message
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_on_chat.view.*

class MessageAdapter(val context: Context, val messageList: List<Message>, val userId: String?):
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_on_chat, parent, false))
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        val message = messageList[position]

        if (message.from_name == userId){
            holder.llFrom.visibility = View.VISIBLE
            holder.llTo.visibility = View.GONE
            holder.ivToImage.visibility = View.GONE

            holder.tvFromMessage.text = message.message
            if (message.image != null && message.image.isNotBlank()){
                holder.ivFromImage.visibility = View.VISIBLE
                Picasso.get().load(message.image).placeholder(R.color.gray).into(holder.ivFromImage)
            }
        }else{
            holder.llFrom.visibility = View.GONE
            holder.llTo.visibility = View.VISIBLE
            holder.ivFromImage.visibility = View.GONE

            holder.tvToMessage.text = message.message
            if (message.image != null && message.image.isNotBlank()){
                holder.ivToImage.visibility = View.VISIBLE
                Picasso.get().load(message.image).placeholder(R.color.gray).into(holder.ivToImage)
            }
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val llFrom = view.itemonchat_llMessageFrom
        val llTo = view.itemonchat_llMessageTo
        val tvFromMessage = view.itemonchat_tvMessageFrom
        val tvToMessage = view.itemonchat_tvMessageTo
        val ivFromImage = view.itemonchat_ivImageFrom
        val ivToImage = view.itemonchat_ivImageTo
    }
}