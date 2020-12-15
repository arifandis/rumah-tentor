package com.cahstudio.rumahtentor.ui.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Student
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.activity_order_detail.detail_tvCourse
import kotlinx.android.synthetic.main.activity_order_detail.detail_tvDay
import kotlinx.android.synthetic.main.activity_order_detail.detail_tvLevel
import kotlinx.android.synthetic.main.activity_order_detail.detail_tvName
import kotlinx.android.synthetic.main.activity_order_detail.detail_tvStatus
import kotlinx.android.synthetic.main.activity_order_detail.detail_tvStatusLabel
import kotlinx.android.synthetic.main.activity_order_detail.detail_tvTime
import kotlinx.android.synthetic.main.activity_order_detail_admin.*
import kotlinx.android.synthetic.main.toolbar.*

class OrderDetailAdminActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mRef: DatabaseReference
    private lateinit var actionBar: ActionBar
    private lateinit var mFirebaseUser: FirebaseUser
    private var mOrder: Order? = null

    private var orderId = ""
    private var sCourse: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail_admin)

        configureToolbar()
        initialize()
        getDetail()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Rincian Pesanan"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        orderId = intent.getStringExtra("order_id")

        detail_btnCreateSchedule.setOnClickListener(this)
        detail_btnConfirmPayment.setOnClickListener(this)
    }

    fun getDetail(){
        mRef.child("order").orderByKey().equalTo(orderId).addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                sCourse = null
                for (ds in snapshot.children){
                    if (ds.key == orderId){
                        mOrder = ds.getValue(Order::class.java) ?: return
                    }
                }

                if (mOrder?.status == "waiting schedule"){
                    detail_btnCreateSchedule.visibility = View.VISIBLE
                    detail_btnConfirmPayment.visibility = View.GONE
                    detail_tvProofLabel.visibility = View.GONE
                    detail_ivProofPayment.visibility = View.GONE
                    detail_tvTypePayment.visibility = View.GONE
                    detail_tvTypePaymentLabel.visibility = View.GONE
                }else{
                    detail_btnCreateSchedule.visibility = View.GONE
                    detail_btnConfirmPayment.visibility = View.VISIBLE
                    detail_tvTypePayment.visibility = View.VISIBLE
                    detail_tvTypePaymentLabel.visibility = View.VISIBLE

                    detail_tvTypePayment.text = mOrder?.payment_type

                    if (mOrder?.payment_type == "transfer"){
                        detail_tvProofLabel.visibility = View.VISIBLE
                        detail_ivProofPayment.visibility = View.VISIBLE

                        Picasso.get().load(mOrder?.payment).into(detail_ivProofPayment)
                    }
                }

                detail_tvStatus.visibility = View.VISIBLE
                detail_tvStatusLabel.visibility = View.VISIBLE

                if (mOrder?.status == "proses"){
                    detail_tvStatus.text = "Sedang proses"
                }else if (mOrder?.status == "reject"){
                    detail_tvStatus.text = "Ditolak"
                }else if (mOrder?.status == "ongoing"){
                    detail_tvStatus.text = "Sedang berjalan"
                }else if (mOrder?.status == "waiting schedule"){
                    detail_tvStatus.text = "Menunggu jadwal"
                }else if (mOrder?.status == "waiting confirm payment"){
                    detail_tvStatus.text = "Menunggu konfirmasi pembayaran"
                }else if (mOrder?.status == "done"){
                    detail_tvStatus.text = "Selesai"
                }

                detail_tvLevel.text = mOrder?.level
                detail_tvDay.text = mOrder?.day
                detail_tvTime.text = mOrder?.time+" WIB"

                getStudentByKey(mOrder?.student_uid)
                val arrayCourseId = mOrder?.course?.split(",")
                arrayCourseId?.forEach {
                    getCourseById(it)
                }
            }

        })
    }

    fun getCourseById(id: String?){
        mRef.child("course").orderByChild("id").equalTo(id).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var course: Course? = null
                for (ds in snapshot.children){
                    if (ds.child("id").getValue(String::class.java) == id){
                        course = ds.getValue(Course::class.java) ?: return
                        if (sCourse == null){
                            sCourse = course.name
                        }else{
                            sCourse = "$sCourse,${course.name}"
                        }
                    }
                }

                detail_tvCourse.text = sCourse

            }

        })
    }

    fun getStudentByKey(key: String?){
        mRef.child("student").orderByKey().equalTo(key).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var student: Student? = null
                for (ds in snapshot.children){
                    if (ds.key == key){
                        student = ds.getValue(Student::class.java) ?: return
                    }
                }

                detail_tvName.text = student?.name

            }

        })
    }

    fun confirmPayment(){
        mRef.child("order").child(orderId).child("status").setValue("waiting schedule")
            .addOnCompleteListener {
                if (it.isSuccessful){
                    mRef.child("tentor").child(mOrder?.tentor_uid.toString()).child("status")
                        .setValue("waiting schedule")
                    mRef.child("student").child(mOrder?.student_uid.toString()).child("status")
                        .setValue("waiting schedule")

                    Toast.makeText(applicationContext, "Konfirmasi pembayaran berhasil", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.detail_btnCreateSchedule -> {
                val intent = Intent(this, CreateScheduleActivity::class.java)
                intent.putExtra("order", mOrder)
                startActivity(intent)
            }
            R.id.detail_btnConfirmPayment -> {
                confirmPayment()
            }
        }
    }
}