package com.cahstudio.rumahtentor.ui.student

import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import com.cahstudio.rumahtentor.model.Day
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.ui.student.adapter.DayAdapter
import com.cahstudio.rumahtentor.ui.tentor.adapter.ChooseCourseAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class OrderActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mDayAdapter: DayAdapter
    private lateinit var mRef: DatabaseReference
    private lateinit var mUserFirebase: FirebaseUser
    private lateinit var sCourseAdapter: ArrayAdapter<String>
    private lateinit var sTentorAdapter: ArrayAdapter<String>
    private lateinit var mCourseAdapter: ChooseCourseAdapter

    private var mDayList = mutableListOf<Day>()
    private var mChoosedDayList = mutableListOf<Day>()
    private var mCourseList = mutableListOf<Course>()
    private var mChooseCourseList = mutableListOf<Course>()
    private var mTentorList = mutableListOf<Tentor>()
    private var sCourseList = mutableListOf<String>()
    private var sTentorList = mutableListOf<String>()

    private var level = ""
    private var tentorUid = ""
    private var keyOrder: String? = ""
    private var countDay = 0
    private var sCourse = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        configureToolbar()
        initialize()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Pesan Tentor"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mUserFirebase = FirebaseAuth.getInstance().currentUser!!

        order_rgLevel.setOnCheckedChangeListener { radioGroup, i ->
            when(i){
                R.id.order_rbSD ->{
                    level = "SD"
                }
                R.id.order_rbSMP ->{
                    level = "SMP"
                }
                R.id.order_rbSMA ->{
                    level = "SMA"
                }
            }
            getCourse()
        }

        order_etTime.setOnClickListener {
            showTimePicker()
        }

        order_sCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }

        }

        order_sTentor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getTentorUid()
            }

        }

        order_btnOrder.setOnClickListener {
            orderTentor()
        }

        val courseLayoutManager = GridLayoutManager(this,3)
        mCourseAdapter = ChooseCourseAdapter(this, mCourseList, {course -> addCourse(course) }
            , {course -> removeCourse(course) })
        order_rvCourse.layoutManager = courseLayoutManager
        order_rvCourse.adapter = mCourseAdapter

        val layoutManager = LinearLayoutManager(this)
        mDayList.add(Day("Senin"))
        mDayList.add(Day("Selasa"))
        mDayList.add(Day("Rabu"))
        mDayList.add(Day("Kamis"))
        mDayList.add(Day("Jum'at"))
        mDayList.add(Day("Sabtu"))
        mDayList.add(Day("Minggu"))
        mDayAdapter = DayAdapter(this, mDayList,{day -> addDay(day) },{day -> removeDay(day) })
        order_rvDay.layoutManager = layoutManager
        order_rvDay.adapter = mDayAdapter

        sCourseList.add("Pilih pelajaran")
        sCourseAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, sCourseList)
        order_sCourse.adapter = sCourseAdapter

        sTentorList.add("Pilih tentor")
        sTentorAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, sTentorList)
        order_sTentor.adapter = sTentorAdapter
    }

    fun showTimePicker(){
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val pickedDateTime = Calendar.getInstance()
            pickedDateTime.set(startYear, startMonth, startDay, hour, minute)
            val format = "kk:mm"
            val sdf = SimpleDateFormat(format, Locale("in", "ID"))
            order_etTime.setText(sdf.format(pickedDateTime.time))
        }, startHour, startMinute, true).show()
    }

    fun addDay(day: Day){
        if (countDay < 4){
            countDay++
            mDayAdapter.countDay = countDay
            mChoosedDayList.add(day)
        }
    }

    fun removeDay(day: Day){
        countDay--
        mDayAdapter.countDay = countDay
        mChoosedDayList.remove(day)
    }

    fun getCourse(){
        sCourseList.clear()
        sCourseList.add("Pilih pelajaran")
        sCourseAdapter.notifyDataSetChanged()
        mRef.child("course").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mCourseList.clear()
                for (ds in snapshot.children){
                    val course = ds.getValue(Course::class.java) ?: return

                    mCourseList.add(course)
                }

                if (mCourseList.isNotEmpty()){
                    mCourseList.forEach {
                        val course = it.name+"-"+it.id
                        if (it.level?.contains(level)!!){
                            sCourseList.add(course)
                        }
                    }
                    mCourseAdapter.notifyDataSetChanged()
                }
            }

        })
    }

    fun addCourse(course: Course){
        sCourse = ""
        mChooseCourseList.add(course)
        mChooseCourseList.forEach {
            sCourse += if (sCourse.isEmpty()){
                it.id.toString()
            }else{
                ",${it.id.toString()}"
            }
        }
        getTentor()
    }

    fun removeCourse(course: Course){
        sCourse = ""
        mChooseCourseList.remove(course)
        mChooseCourseList.forEach {
            sCourse += if (sCourse.isEmpty()){
                it.id.toString()
            }else{
                ",${it.id.toString()}"
            }
        }
        getTentor()
    }

    fun getTentor(){
        mTentorList.clear()
        sTentorList.clear()
        sTentorList.add("Pilih tentor")
        sTentorAdapter.notifyDataSetChanged()
        mRef.child("tentor").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    val tentor = ds.getValue(Tentor::class.java) ?: return

                    mTentorList.add(tentor)
                }

                if (mCourseList.isNotEmpty()){
                    mTentorList.forEach {
                        val tentor = it.name+"-"+it.email
                        if (sCourse.isNotEmpty()){
                            var isContains = false
                            mChooseCourseList.forEach { course ->
                                isContains = it.course?.contains(course.id.toString())!!
                            }

                            if (isContains){
                                sTentorList.add(tentor)
                            }
                        }
                    }
                    sTentorAdapter.notifyDataSetChanged()
                }
            }

        })
    }

    fun getTentorUid(){
        val tentor = order_sTentor.selectedItem.toString()
        if (tentor != "Pilih tentor"){
            val email = order_sTentor.selectedItem.toString().split("-")[1]
            mRef.child("tentor").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children){
                            tentorUid = ds.key.toString()
                        }
                    }

                })
        }
    }

    fun orderTentor(){
        val tentor = order_sTentor.selectedItem.toString()
        val time = order_etTime.text.toString()

        if (level.isEmpty() || sCourse.isBlank() || tentor == "Pilih tentor" || time.isEmpty()
            || mChoosedDayList.isEmpty()){
            Toast.makeText(this, "Lengkapi form pemesanan dengan benar", Toast.LENGTH_SHORT).show()
        }else{
            mRef.child("order").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var total = snapshot.childrenCount
                    keyOrder = mRef.child("order").push().key
                    var day = ""
                    var count = 0
                    mChoosedDayList.forEach {
                        if (count == 0){
                            day += it.day
                        }else{
                            day += ","+it.day
                        }
                        count++
                    }
                    val order = Order((total+1).toInt(), keyOrder, mUserFirebase.uid, tentorUid, level
                        , sCourse ,day ,time,"proses","","")

                    setOrderInStudent()
                    setOrderInTentor()

                    keyOrder?.let { mRef.child("order").child(it).setValue(order).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(applicationContext, "Berhasil melakukan pemesanan", Toast.LENGTH_SHORT).show()
                            finish()
                        }else{
                            Toast.makeText(applicationContext, "Gagal melakukan pemesanan, coba lagi", Toast.LENGTH_SHORT).show()
                        }
                    } }
                }

            })
        }
    }

    fun setOrderInStudent(){
        mRef.child("student").child(mUserFirebase.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var count = snapshot.child("order").childrenCount
                val no = count + 1
                mRef.child("student").child(mUserFirebase.uid).child("order")
                    .child(no.toString()).child("no").setValue(no)
                mRef.child("student").child(mUserFirebase.uid).child("order")
                    .child(no.toString()).child("key").setValue(keyOrder)
                mRef.child("student").child(mUserFirebase.uid).child("current_order")
                    .setValue(keyOrder)
                mRef.child("student").child(mUserFirebase.uid).child("status")
                    .setValue("waiting")
            }

        })
    }

    fun setOrderInTentor(){
        mRef.child("tentor").child(tentorUid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var count = snapshot.child("order").childrenCount
                val no = count + 1
                mRef.child("tentor").child(tentorUid).child("order")
                    .child(no.toString()).child("no").setValue(no)
                mRef.child("tentor").child(tentorUid).child("order")
                    .child(no.toString()).child("key").setValue(keyOrder)
            }

        })
    }
}