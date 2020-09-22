package com.cahstudio.rumahtentor.ui.tentor.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Student
import com.cahstudio.rumahtentor.ui.admin.OrderDetailAdminActivity
import com.cahstudio.rumahtentor.ui.tentor.OrderDetailActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.item_order.view.*

class OrderAdapter(val context: Context, val orderList: List<Order>, val accepct: (Order) -> Unit
                   , val reject: (Order) -> Unit):
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private lateinit var mRef: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderAdapter.ViewHolder {
        mRef = FirebaseDatabase.getInstance().reference
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order, parent, false))
    }

    override fun getItemCount(): Int = orderList.size

    override fun onBindViewHolder(holder: OrderAdapter.ViewHolder, position: Int) {
        val order = orderList[position]

        holder.tvLevel.text = order.level
        holder.tvCourse.text = order.course

        order.student_uid?.let { holder.getStudentByUid(it, mRef, context) }
        holder.getCourse(order.course, mRef, context)

        if (order.status != "proses"){
            holder.btnAccept.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
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
            if (order.status == "waiting schedule"){
                intent = Intent(context, OrderDetailAdminActivity::class.java)
            }else{
                intent = Intent(context, OrderDetailActivity::class.java)
            }
            intent.putExtra("order_id", order.key)
            context.startActivity(intent)
        }

        holder.btnAccept.setOnClickListener {
            accepct(order)
        }

        holder.btnReject.setOnClickListener {
            reject(order)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvLevel = view.itemorder_tvCourseLevel
        val tvCourse = view.itemorder_tvCourse
        val tvOrder = view.itemorder_tvOrder
        val tvStatus = view.itemorder_tvStatus
        val btnAccept = view.itemorder_btnAccept
        val btnReject = view.itemorder_btnReject

        fun getStudentByUid(uid: String, reference: DatabaseReference, context: Context){
            reference.child("student").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(object : ValueEventListener{
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

        fun getCourse(courseId: String?, reference: DatabaseReference, context: Context){
            reference.child("course").orderByChild("id").equalTo(courseId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val course = ds.getValue(Course::class.java) ?: return
                        tvCourse.text = course.name
                    }
                }

            })
        }
    }
}