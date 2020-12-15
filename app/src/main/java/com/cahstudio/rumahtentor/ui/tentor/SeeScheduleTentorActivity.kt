package com.cahstudio.rumahtentor.ui.tentor

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Schedule
import com.cahstudio.rumahtentor.ui.adapter.ScheduleAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_see_schedule_tentor.*
import kotlinx.android.synthetic.main.toolbar.*

class SeeScheduleTentorActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: ScheduleAdapter
    private lateinit var dialogConfirm: AlertDialog.Builder

    private var mOrderId: String? = null
    private var mSchedule: Schedule? = null
    private var mScheduleList = mutableListOf<Schedule>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_schedule_tentor)

        configureToolbar()
        initiliaze()
        getSchedule()
        initializeConfirmDialog()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Jadwal"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initiliaze(){
        mRef = FirebaseDatabase.getInstance().reference
        mOrderId = intent.getStringExtra("order_id")
        val time = intent.getStringExtra("time")

        val layoutManager = LinearLayoutManager(this)
        mAdapter = ScheduleAdapter(this, mScheduleList,{schedule -> showConfirmDialog(schedule) }
            ,"tentor",{schedule ->  })
        mAdapter.time = time
        schedule_recyclerview.layoutManager = layoutManager
        schedule_recyclerview.adapter = mAdapter
    }

    fun getSchedule(){
        mOrderId?.let { mRef.child("order").child(it).child("schedule").orderByChild("id").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mScheduleList.clear()
                for (value in snapshot.children){
                    val schedule = value.getValue(Schedule::class.java) ?: return
                    mScheduleList.add(schedule)
                }
                mAdapter.notifyDataSetChanged()

            }

        }) }
    }

    fun initializeConfirmDialog(){
        dialogConfirm = AlertDialog.Builder(this)
        dialogConfirm.setTitle("Konfirmasi Kehadiran")

        dialogConfirm.setPositiveButton("Hadir") { dialog, which ->
            if (mSchedule != null){
                attended(dialog)
            }
        }

        dialogConfirm.setNegativeButton("Re-Schedule") { dialog, which ->
            if (mSchedule != null){
                reschedule(dialog)
            }
        }
    }

    fun showConfirmDialog(schedule: Schedule){
        dialogConfirm.setMessage("Les akan dilaksanakan pada ${schedule.date}")
        dialogConfirm.show()
        mSchedule = schedule
    }

    fun attended(dialog: DialogInterface){
        mOrderId?.let { mRef.child("order").child(it).child("schedule").child((mSchedule?.id?.minus(
            1)).toString()).child("tentor").setValue("attend").addOnCompleteListener {
            if (it.isSuccessful){
                dialog.dismiss()
                getSchedule()
            }else{
                dialog.dismiss()
                Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } }
    }

    fun reschedule(dialog: DialogInterface){
        mOrderId?.let { mRef.child("order").child(it).child("schedule").child((mSchedule?.id?.minus(
            1)).toString()).child("tentor").setValue("reschedule").addOnCompleteListener {
            if (it.isSuccessful){
                dialog.dismiss()
                getSchedule()
            }else{
                dialog.dismiss()
                Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } }
    }
}