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
                      , val role: String, val reschedule: (Schedule) -> Unit):
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    var time: String? = null
    var mScheduleList = mutableListOf<Schedule>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapter.ViewHolder {
        if (mScheduleList.isEmpty()){
            mScheduleList.addAll(scheduleList)
        }
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

        checkStatus(schedule.status, holder, schedule)

        if (role == "admin"){
            if (schedule.status == "reschedule"){
                holder.btnReschedule.visibility = View.VISIBLE
                holder.btnReschedule.setOnClickListener {
                    reschedule(schedule)
                }
            }
        }else if (role == "tentor"){
//            if (schedule.tentor != null){
//                if (schedule.tentor!! == "attend"){
//                    scheduleList[position].status = "attend"
//                    checkStatus(scheduleList[position].status, holder, schedule)
//                }else{
//                    scheduleList[position].status = "reschedule"
//                    checkStatus(scheduleList[position].status, holder, schedule)
//                }
//            }
        }else if (role == "student"){
            if (schedule.student != null){
                if (schedule.student!! == "attend"){
                    scheduleList[position].status = "attend"
                    checkStatus(scheduleList[position].status, holder, schedule)
                }else{
                    scheduleList[position].status = "reschedule"
                    checkStatus(scheduleList[position].status, holder, schedule)
                }
            }
        }
    }

    fun checkStatus(status: String?, holder: ViewHolder, schedule: Schedule){

        if (role != "admin"){
            if (status != "ongoing"){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.cvItem.setCardBackgroundColor(context.resources.getColor(R.color.gray, null))
                }else{
                    holder.cvItem.setCardBackgroundColor(context.resources.getColor(R.color.gray))
                }
                holder.itemView.isEnabled = false
            }else{
                holder.itemView.setOnClickListener {
                    confirm(schedule)
                }
            }

            if (status == "ongoing"){
                holder.tvStatus.text = "Menunggu konfirmasi"
            }else if (status == "attend"){
                holder.tvStatus.text = "Hadir"
            }else if (status == "reschedule"){
                holder.tvStatus.text = "Reschedule"
            }
        }else{
            holder.tvStatus.visibility = View.GONE
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvDate = view.itemschedule_tvDate
        val cvItem = view.itemschedule_cvItem
        val tvStatus = view.itemshcedule_tvStatus
        val btnReschedule = view.itemshcedule_tvReschedule
    }
}