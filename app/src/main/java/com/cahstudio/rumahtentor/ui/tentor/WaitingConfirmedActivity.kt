package com.cahstudio.rumahtentor.ui.tentor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.ui.student.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_waiting_confirmed.*

class WaitingConfirmedActivity : AppCompatActivity() {
    private lateinit var mRef: DatabaseReference
    private lateinit var mPref: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_confirmed)

        initialize()
        getTentor()
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mPref = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE).edit()

        btnLogout.setOnClickListener {
            logout()
        }
    }

    fun getTentor(){
        val uid = intent.getStringExtra("uid")
        mRef.child("tentor").child(uid).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val tentor = snapshot.getValue(Tentor::class.java) ?: return
                if (tentor.account_status == "confirmed"){
                    startActivity(Intent(applicationContext, ChooseLevelActivity::class.java))
                    finish()
                }
            }

        })
    }

    fun logout(){
        mPref.clear()
        mPref.apply()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}