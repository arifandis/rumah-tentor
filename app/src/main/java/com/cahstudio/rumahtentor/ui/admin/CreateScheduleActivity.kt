package com.cahstudio.rumahtentor.ui.admin

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Schedule
import com.cahstudio.rumahtentor.ui.adapter.ScheduleAdapter
import com.cahstudio.rumahtentor.utils.NotificationReceiver
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_schedule.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class CreateScheduleActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, View.OnClickListener {
    private var calendar = Calendar.getInstance()
    private lateinit var actionBar: ActionBar
    private lateinit var mAdapter: ScheduleAdapter
    private lateinit var mRef: DatabaseReference

    private var mScheduleList = mutableListOf<Schedule>()
    private var mOrder: Order? = null
    private var id: Long = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_schedule)

        configureToolbar()
        initialize()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Buat Jadwal"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mOrder = intent.getParcelableExtra("order")

        val layoutManager = LinearLayoutManager(this)
        mAdapter = ScheduleAdapter(this, mScheduleList,{},"admin",{schedule,status ->},{})
        createschedule_recyclerview.layoutManager = layoutManager
        createschedule_recyclerview.adapter = mAdapter

        createschedule_btnAddDay.setOnClickListener(this)
        createschedule_btnSave.setOnClickListener(this)
    }

    private fun showDatePicker(){
        DatePickerDialog(this, this, calendar
            .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun saveSchedule(){
        mOrder?.key?.let { key ->
            mRef.child("order").child(key).child("schedule").setValue(mScheduleList).addOnCompleteListener {
                if (it.isSuccessful){
                    mRef.child("order").child(key).child("status").setValue("ongoing")
                    mRef.child("student").child(mOrder?.student_uid!!).child("status").setValue("studying")
                    mRef.child("tentor").child(mOrder?.tentor_uid!!).child("status").setValue("teaching")

                    var i = 0
                    mScheduleList.forEach {
                        if (it.status == "ongoing"){
                            setReminder(it, i)
                        }
                        i++
                    }
                    finish()
                }else{
                    Toast.makeText(this, "Gagal menyimpan jadwal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun setReminder(schedule: Schedule, i: Int){
        val calendarReminder = Calendar.getInstance()
        val datetime = schedule.date+" "+mOrder?.time
        val format = "yyyy-MM-dd HH:mm"
        val sdf = SimpleDateFormat(format, Locale("in", "ID"))
        val date = sdf.parse(datetime)
        calendarReminder.time = date
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        manager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarReminder.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val format = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(format, Locale("in", "ID"))
        calendar.set(Calendar.YEAR, p1)
        calendar.set(Calendar.MONTH, p2)
        calendar.set(Calendar.DAY_OF_MONTH, p3)
        val schedule = Schedule(id,sdf.format(calendar.time),"ongoing", null, null)
        mScheduleList.add(schedule)
        mAdapter.notifyDataSetChanged()
        id++
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.createschedule_btnAddDay -> {
                if (id <= 14){
                    showDatePicker()
                }else{
                    Toast.makeText(this, "Total jadwal adalah 14 hari", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.createschedule_btnSave -> {
                if (id > 14){
                    saveSchedule()
                }else{
                    Toast.makeText(this, "Total jadwal adalah 14 hari", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}