package com.cahstudio.rumahtentor.ui.admin.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.ui.admin.TentorDetailActivity
import kotlinx.android.synthetic.main.item_user.view.*

class TentorAdapter(val context: Context, val tentorList: List<Tentor>):
    RecyclerView.Adapter<TentorAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TentorAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false))
    }

    override fun getItemCount(): Int = tentorList.size

    override fun onBindViewHolder(holder: TentorAdapter.ViewHolder, position: Int) {
        val tentor = tentorList[position]

        holder.tvStatus.visibility = View.VISIBLE
        holder.tvName.text = tentor.name

        if (tentor.account_status == "not confirmed"){
            holder.tvStatus.text = "Menunggu Konfirmasi"
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, TentorDetailActivity::class.java)
            intent.putExtra("uid", tentor.uid)
            context.startActivity(intent)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvName = view.itemmessage_tvName
        val tvStatus = view.itemmessage_tvStatus
    }
}