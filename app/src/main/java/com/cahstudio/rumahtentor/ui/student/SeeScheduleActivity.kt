package com.cahstudio.rumahtentor.ui.student

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Schedule
import com.cahstudio.rumahtentor.ui.adapter.ScheduleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_see_schedule.*
import kotlinx.android.synthetic.main.activity_see_schedule_tentor.*
import kotlinx.android.synthetic.main.activity_see_schedule_tentor.schedule_recyclerview
import kotlinx.android.synthetic.main.dialog_give_rating.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class SeeScheduleActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: ScheduleAdapter
    private lateinit var dialogConfirm: AlertDialog.Builder
    private lateinit var ratingDialog: Dialog

    private var mOrderId: String? = null
    private var mSchedule: Schedule? = null
    private var mScheduleList = mutableListOf<Schedule>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_schedule)

        configureToolbar()
        initiliaze()
        initializeConfirmDialog()
        initializeRatingDialog()
        getSchedule()
        checkRating()
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
        mAdapter = ScheduleAdapter(this, mScheduleList, {schedule -> showConfirmDialog(schedule) }
            , "student",{schedule,status -> },{schedule ->  })
        mAdapter.time = time
        schedule_recyclerview.layoutManager = layoutManager
        schedule_recyclerview.adapter = mAdapter

        schedule_btnRating.setOnClickListener {
            ratingDialog.show()
        }
    }

    fun getSchedule(){
        mOrderId?.let { mRef.child("order").child(it).child("schedule").orderByChild("id").addValueEventListener(object :
            ValueEventListener {
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
            1)).toString()).child("student").setValue("attend").addOnCompleteListener {
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
            1)).toString()).child("student").setValue("reschedule").addOnCompleteListener {
            if (it.isSuccessful){
                dialog.dismiss()
                getSchedule()
            }else{
                dialog.dismiss()
                Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } }
    }

    fun checkRating(){
        mOrderId?.let { mRef.child("order").child(it).child("rating").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null){
                    schedule_btnRating.visibility = View.VISIBLE
                }
            }

        }) }
    }

    fun initializeRatingDialog(){
        ratingDialog = Dialog(this)
        ratingDialog.setContentView(R.layout.dialog_give_rating)
        ratingDialog.setCancelable(true)

        Objects.requireNonNull(ratingDialog.window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ratingDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        ratingDialog.dialoggive_btnSubmit.setOnClickListener {
            val rating = ratingDialog.dialoggive_ratingBar.rating
            giveRating(rating)
        }
    }

    fun giveRating(rating: Float){
        mOrderId?.let { mRef.child("order").child(it).child("rating").setValue(rating)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    schedule_btnRating.visibility = View.GONE
                    Toast.makeText(this, "Berhasil memberi rating", Toast.LENGTH_SHORT).show()
                    ratingDialog.dismiss()
                    checkRating()
                }else{
                    Toast.makeText(this, "Gagal memberi rating", Toast.LENGTH_SHORT).show()
                }
            }}
    }
}