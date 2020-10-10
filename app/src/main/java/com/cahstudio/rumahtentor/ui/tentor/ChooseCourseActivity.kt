package com.cahstudio.rumahtentor.ui.tentor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.GridLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import com.cahstudio.rumahtentor.ui.tentor.adapter.ChooseCourseAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_choose_course.*
import kotlinx.android.synthetic.main.item_choose_course.*
import kotlinx.android.synthetic.main.toolbar.*

class ChooseCourseActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: ChooseCourseAdapter
    private lateinit var mUserFirebase: FirebaseUser

    private var mCourseList = mutableListOf<Course>()
    private var mChooseCourseList = mutableListOf<Course>()
    private var mLevel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_course)

        configureToolbar()
        initialize()
        getCourse()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Pilih Mata Pelajaran"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mAdapter = ChooseCourseAdapter(this, mCourseList, {course -> addCourse(course) }
            , {course -> removeCourse(course) })
        mLevel = intent.getStringExtra("level")
        mUserFirebase = FirebaseAuth.getInstance().currentUser!!

        val layoutManager = GridLayoutManager(this,3)
        choosecourse_recyclerview.layoutManager = layoutManager
        choosecourse_recyclerview.adapter = mAdapter

        choosecourse_tvNext.setOnClickListener {
            saveCourse()
        }
    }

    fun getCourse(){
        mRef.child("course").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    val course = ds.getValue(Course::class.java) ?: return
                    var isSame = false
                    if (mLevel != null){
                        val arrayString = mLevel!!.split(",")
                        arrayString.forEach {
                            if (course?.level?.contains(it)!!){
                                isSame = true
                            }
                        }

                        if (isSame){
                            if (course != null){
                                mCourseList.add(course)
                            }
                        }
                    }
                }
                mAdapter.notifyDataSetChanged()
            }

        })
    }

    fun saveCourse(){
        var course = ""
        var i = 0
        mChooseCourseList.forEach {
            if (i == 0){
                course += it.id
            }else{
                course += ","+it.id
            }
            i++
        }

        mRef.child("tentor").child(mUserFirebase.uid).child("course").setValue(course).addOnCompleteListener {
            if (it.isSuccessful){
                startActivity(Intent(this, MainTentorActivity::class.java))
                finish()
            }else{
                Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addCourse(course: Course){
        mChooseCourseList.add(course)
    }

    fun removeCourse(course: Course){
        mChooseCourseList.remove(course)
    }
}