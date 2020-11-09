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

class ScheduleAdapter(val context: Context, val scheduleList: List<Schedule>, val confirm: (Schedule) -> Unit
                      , val role: String, val update: (Schedule,String) -> Unit, val reschedule: (Schedule) -> Unit):
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
        }else{
            holder.itemView.setOnClickListener {
                confirm(schedule)
            }
        }

        if (schedule.status == "ongoing"){
            holder.tvStatus.text = "Menunggu konfirmasi"
        }else if (schedule.status == "attend"){
            holder.tvStatus.text = "Hadir"
        }else if (schedule.status == "reschedule"){
            holder.tvStatus.text = "Reschedule"
        }

        if (schedule.tentor != null && schedule.student != null){
            if (schedule.tentor!! && schedule.student!!){
                update(schedule,"attend")
            }else{
                update(schedule,"reschedule")
            }
        }

        if (role == "admin"){
            holder.tvStatus.visibility = View.GONE
            holder.btnReschedule.visibility = View.VISIBLE
            holder.btnReschedule.setOnClickListener {
                reschedule(schedule)
            }
        }else if (role == "tentor"){
            if (schedule.tentor != null){
                if (schedule.tentor!!){
                    scheduleList[position].status = "attend"
                }else{
                    scheduleList[position].status = "reschedule"
                }
            }
        }else if (role == "student"){
            if (schedule.student != null){
                if (schedule.student!!){
                    scheduleList[position].status = "attend"
                }else{
                    scheduleList[position].status = "reschedule"
                }
            }
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvDate = view.itemschedule_tvDate
        val cvItem = view.itemschedule_cvItem
        val tvStatus = view.itemshcedule_tvStatus
        val btnReschedule = view.itemshcedule_tvReschedule
    }
}