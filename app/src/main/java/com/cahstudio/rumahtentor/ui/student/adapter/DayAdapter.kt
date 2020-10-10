package com.cahstudio.rumahtentor.ui.student.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Day
import kotlinx.android.synthetic.main.item_day.view.*

class DayAdapter(val context: Context, val dayList: List<Day>, val add: (Day) -> Unit
                 , val remove: (Day) -> Unit): RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    var countDay = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_day, parent, false))
    }

    override fun getItemCount(): Int = dayList.size

    override fun onBindViewHolder(holder: DayAdapter.ViewHolder, position: Int) {
        val day = dayList[position]

        holder.cbDay.text = day.day
        holder.cbDay.setOnClickListener {
            if (holder.cbDay.isChecked){
                if (countDay == 4){
                    holder.cbDay.isChecked = false
                    Toast.makeText(context, "Jumlah maksimal hari yang dipilih adalah 4", Toast.LENGTH_SHORT).show()
                }else{
                    add(day)
                }
            }else{
                remove(day)
            }
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cbDay = view.itemday_cbDay
    }
}