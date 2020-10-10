package com.cahstudio.rumahtentor.ui.tentor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import kotlinx.android.synthetic.main.item_choose_course.view.*

class ChooseCourseAdapter(val context: Context, val courseList: List<Course>, val add: (Course) -> Unit
                          , val remove: (Course) -> Unit)
    : RecyclerView.Adapter<ChooseCourseAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChooseCourseAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_choose_course, parent, false))
    }

    override fun getItemCount(): Int = courseList.size

    override fun onBindViewHolder(holder: ChooseCourseAdapter.ViewHolder, position: Int) {
        val course = courseList[position]
        holder.cbCourse.text = course.name

        holder.cbCourse.setOnClickListener {
            if (holder.cbCourse.isChecked){
                add(course)
            }else{
                remove(course)
            }
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cbCourse = view.itemchoosecourse_cbCourse
    }
}