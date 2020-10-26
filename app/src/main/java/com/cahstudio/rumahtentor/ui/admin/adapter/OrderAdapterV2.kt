package com.cahstudio.rumahtentor.ui.admin.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Student
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.ui.admin.OrderDetailAdminActivity
import com.cahstudio.rumahtentor.ui.admin.SeeScheduleActivity
import com.cahstudio.rumahtentor.ui.tentor.OrderDetailActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.item_order.view.*
import kotlinx.android.synthetic.main.item_order.view.itemorder_tvCourse
import kotlinx.android.synthetic.main.item_order.view.itemorder_tvCourseLevel
import kotlinx.android.synthetic.main.item_order.view.itemorder_tvOrder
import kotlinx.android.synthetic.main.item_order.view.itemorder_tvStatus
import kotlinx.android.synthetic.main.item_order_v2.view.*

class OrderAdapterV2(val context: Context, val orderList: List<Order>):
    RecyclerView.Adapter<OrderAdapterV2.ViewHolder>() {

    private lateinit var mRef: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderAdapterV2.ViewHolder {
        mRef = FirebaseDatabase.getInstance().reference
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_v2, parent, false))
    }

    override fun getItemCount(): Int = orderList.size

    override fun onBindViewHolder(holder: OrderAdapterV2.ViewHolder, position: Int) {
        val order = orderList[position]

        holder.tvLevel.text = order.level
        holder.tvCourse.text = order.course

        order.student_uid?.let { holder.getStudentByUid(it, mRef, context) }
        order.tentor_uid?.let { holder.getTentorByUid(it, mRef, context) }
        val arrayCourseId = order.course?.split(",")
        holder.sCourse = null
        arrayCourseId?.forEach {
            holder.getCourse(it, mRef, context)
        }

        if (order.status == "proses"){
            holder.tvStatus.text = "Status: Sedang proses"
        }else if (order.status == "reject"){
            holder.tvStatus.text = "Status: Ditolak"
        }else if (order.status == "ongoing"){
            holder.tvStatus.text = "Status: Sedang berjalan"
        }else if (order.status == "waiting schedule"){
            holder.tvStatus.text = "Status: Menunggu jadwal"
        }else if (order.status == "done"){
            holder.tvStatus.text = "Status: Selesai"
        }

        holder.itemView.setOnClickListener {
            var intent = Intent()
            if (order.status == "ongoing"){
                intent = Intent(context, SeeScheduleActivity::class.java)
            }else{
                Toast.makeText(context, "Jadwal belum dibuat", Toast.LENGTH_SHORT).show()
            }
            intent.putExtra("order_id", order.key)
            intent.putExtra("time", order.time)
            context.startActivity(intent)
        }

    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvLevel = view.itemorder_tvCourseLevel
        val tvCourse = view.itemorder_tvCourse
        val tvOrder = view.itemorder_tvOrder
        val tvTentor = view.itemorder_tvTentor
        val tvStatus = view.itemorder_tvStatus
        var sCourse: String? = null

        fun getStudentByUid(uid: String, reference: DatabaseReference, context: Context){
            reference.child("student").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val student = ds.getValue(Student::class.java) ?: return
                        tvOrder.text = "Pemesan: "+student.name
                    }
                }

            })
        }

        fun getTentorByUid(uid: String, reference: DatabaseReference, context: Context){
            reference.child("tentor").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val tentor = ds.getValue(Tentor::class.java) ?: return
                        tvTentor.text = "Tentor: "+tentor.name
                    }
                }

            })
        }

        fun getCourse(courseId: String?, reference: DatabaseReference, context: Context){
            reference.child("course").orderByChild("id").equalTo(courseId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val course = ds.getValue(Course::class.java) ?: return
                        if (sCourse == null){
                            sCourse = course.name
                        }else{
                            sCourse = "$sCourse,${course.name}"
                        }
                    }
                    tvCourse.text = sCourse
                }

            })
        }
    }
}