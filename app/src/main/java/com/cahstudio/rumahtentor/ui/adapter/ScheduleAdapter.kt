package com.cahstudio.rumahtentor.ui.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Schedule
import kotlinx.android.synthetic.main.item_schedule.view.*

class ScheduleAdapter(val context: Context, val scheduleList: List<Schedule>):
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    var time: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false))
    }

    override fun getItemCount(): Int = scheduleList.size

    override fun onBindViewHolder(holder: ScheduleAdapter.ViewHolder, position: Int) {
        val schedule = scheduleList[position]

        if (time != null){
            holder.tvDate.text = "${schedule.date} $time WIB"
        }else{
            holder.tvDate.text = schedule.date
        }

        if (schedule.status != "ongoing"){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.cvItem.setCardBackgroundColor(context.resources.getColor(R.color.gray, null))
            }else{
                holder.cvItem.setCardBackgroundColor(context.resources.getColor(R.color.gray))
            }
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvDate = view.itemschedule_tvDate
        val cvItem = view.itemschedule_cvItem
    }
}