package com.cahstudio.rumahtentor.ui.tentor

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Student
import com.cahstudio.rumahtentor.model.Tentor
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.toolbar.*

class OrderDetailActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mRef: DatabaseReference
    private lateinit var actionBar: ActionBar
    private lateinit var mFirebaseUser: FirebaseUser
    private lateinit var mPref: SharedPreferences
    private var mOrder: Order? = null

    private var orderId = ""
    private var sCourse: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

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
        mPref = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE)
        orderId = intent.getStringExtra("order_id")
        val mode = mPref.getString("mode","")

        if (mode == "tentor"){
            detail_tvTentorLabel.visibility = View.GONE
            detail_tvTentor.visibility = View.GONE
            detail_btnAccept.visibility = View.GONE
            detail_btnReject.visibility = View.GONE
        }else if (mode == "admin"){
            detail_btnAccept.visibility = View.VISIBLE
            detail_btnReject.visibility = View.VISIBLE
            detail_btnAccept.setOnClickListener(this)
            detail_btnReject.setOnClickListener(this)
        }

    }

    fun getDetail(){
        mRef.child("order").orderByKey().equalTo(orderId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    if (ds.key == orderId){
                        mOrder = ds.getValue(Order::class.java) ?: return
                    }
                }

                if (mOrder?.status == "proses"){
                    detail_btnAccept.visibility = View.VISIBLE
                    detail_btnReject.visibility = View.VISIBLE
                }else{
                    detail_btnAccept.visibility = View.GONE
                    detail_btnReject.visibility = View.GONE
                }

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
                getTentorByKey(mOrder?.tentor_uid)
                val arrayCourseId = mOrder?.course?.split(",")
                arrayCourseId?.forEach {
                    getCourseById(it)
                }
            }

        })
    }

    fun getCourseById(id: String?){
        mRef.child("course").orderByChild("id").equalTo(id).addListenerForSingleValueEvent(object : ValueEventListener{
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
        mRef.child("student").orderByKey().equalTo(key).addListenerForSingleValueEvent(object : ValueEventListener{
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

    fun getTentorByKey(key: String?){
        mRef.child("tentor").orderByKey().equalTo(key).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var tentor: Tentor? = null
                for (ds in snapshot.children){
                    if (ds.key == key){
                        tentor = ds.getValue(Tentor::class.java) ?: return
                    }
                }

                detail_tvTentor.text = tentor?.name

            }

        })
    }



    fun acceptOrder(){
        mRef.child("order").child(orderId).child("status").setValue("ongoing")
        mOrder?.student_uid?.let { mRef.child("student").child(it).child("status")
            .setValue("payment") }
        mOrder?.tentor_uid?.let { mRef.child("tentor").child(it).child("status")
            .setValue("waiting payment") }
        mOrder?.tentor_uid?.let { mRef.child("tentor").child(it).child("current_order")
            .setValue(mOrder?.key).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(applicationContext, "Pesanan diterima", Toast.LENGTH_SHORT).show()
                finish()
            }else{
                Toast.makeText(applicationContext, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } }
    }

    fun rejectOrder(){
        mRef.child("order").child(orderId).child("status").setValue("reject")
        mOrder?.student_uid?.let { mRef.child("student").child(it).child("status")
            .setValue("not studying") }
        mOrder?.student_uid?.let { mRef.child("student").child(it).child("current_order")
            .setValue("").addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(applicationContext, "Pesanan ditolak", Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    Toast.makeText(applicationContext, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            } }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.detail_btnAccept -> {
                acceptOrder()
            }
            R.id.detail_btnReject -> {
                rejectOrder()
            }
        }
    }
}