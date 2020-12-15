package com.cahstudio.rumahtentor.ui.student.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Tentor
import kotlinx.android.synthetic.main.item_tentor_with_rating.view.*

class TentorWithRatingAdapter(val context: Context, val tentorList: List<Tentor>, val select: (Tentor) -> Unit):
    RecyclerView.Adapter<TentorWithRatingAdapter.ViewHolder>() {

    var rowIndex = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TentorWithRatingAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tentor_with_rating, parent, false))
    }

    override fun getItemCount(): Int = tentorList.size

    override fun onBindViewHolder(holder: TentorWithRatingAdapter.ViewHolder, position: Int) {
        val tentor = tentorList[position]

        holder.tvName.text = tentor.name
        holder.tvRating.text = tentor.rating.toString()

        if (rowIndex == position){
            holder.ivCheck.visibility = View.VISIBLE
        }else{
            holder.ivCheck.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            select(tentor)
            rowIndex = position
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvName = view.itemtentor_tvName
        val ivCheck = view.itemtentor_ivCheck
        val tvRating = view.itemtentor_tvRating
    }
}