package com.cahstudio.rumahtentor.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.User
import kotlinx.android.synthetic.main.item_user.view.*

class UserAdapter(val context: Context, val userList: List<User>, val listener: (User) -> Unit)
    : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false))
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val message = userList[position]

        holder.tvName.text = message.name
        holder.itemView.setOnClickListener {
            listener(message)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvName = view.itemmessage_tvName
    }
}