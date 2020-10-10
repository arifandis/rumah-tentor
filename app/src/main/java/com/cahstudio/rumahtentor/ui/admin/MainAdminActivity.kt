package com.cahstudio.rumahtentor.ui.admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.ui.student.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main_admin.*

class MainAdminActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mRef: DatabaseReference
    private lateinit var mPref: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_admin)

        initialize()
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mPref = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE).edit()

        main_cvOrderList.setOnClickListener(this)
        main_cvSeeSchedule.setOnClickListener(this)
        main_cvChatTentor.setOnClickListener(this)
        main_ivLogout.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.main_cvOrderList -> {
                startActivity(Intent(this, OrderListActivity::class.java))
            }
            R.id.main_cvSeeSchedule -> {

            }
            R.id.main_cvChatTentor -> {

            }
            R.id.main_ivLogout -> {
                mPref.clear()
                mPref.apply()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}