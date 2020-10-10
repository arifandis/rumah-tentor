package com.cahstudio.rumahtentor.ui.tentor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.ui.student.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.main_ivLogout
import kotlinx.android.synthetic.main.activity_main_tentor.*

class MainTentorActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mPref: SharedPreferences.Editor
    private var mFirebaseUser: FirebaseUser? = null
    private lateinit var mRef: DatabaseReference
    private lateinit var mTentor: Tentor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tentor)

        initialize()
    }

    fun initialize(){
        mPref = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE).edit()
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        mRef = FirebaseDatabase.getInstance().reference

        main_ivLogout.setOnClickListener(this)
        main_cvOrderList.setOnClickListener(this)

        getTentorDetail()
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.main_ivLogout -> {
                mPref.clear()
                mPref.apply()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            R.id.main_cvOrderList -> {
                startActivity(Intent(this, ListOrderActivity::class.java))
            }
            R.id.main_cvSeeSchedule -> {
                if (mTentor?.current_order != null && mTentor?.current_order!!.isNotEmpty()){
                    if (mTentor!!.status == "waiting payment"){
                        Toast.makeText(this, "Menunggu proses pembayaran dari siswa."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mTentor!!.status == "waiting schedule"){
                        Toast.makeText(this, "Menunggu jadwal dari Admin."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mTentor!!.status == "teaching"){
                        startActivity(Intent(this, SeeScheduleTentorActivity::class.java))
                    }
                }else{
                    Toast.makeText(this, "Anda belum menerima pesanan."
                        , Toast.LENGTH_SHORT).show()
                }
            }
            R.id.main_cvChatAdmin -> {
                startActivity(Intent(this, ChatAdminActivity::class.java))
            }
            R.id.main_cvAnswer -> {
                if (mTentor!!.status == "waiting payment"){
                    Toast.makeText(this, "Menunggu proses pembayaran dari siswa."
                        , Toast.LENGTH_SHORT).show()
                }else if (mTentor!!.status == "waiting schedule"){
                    Toast.makeText(this, "Menunggu jadwal dari Admin."
                        , Toast.LENGTH_SHORT).show()
                }else if (mTentor!!.status == "teaching"){
                    startActivity(Intent(this, AnswerQuestionActivity::class.java))
                }
            }
        }
    }

    fun getTentorDetail(){
        mFirebaseUser?.uid?.let {
            mRef.child("tentor").child(it).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    mTentor = snapshot.getValue(Tentor::class.java) ?: return
                }

            })
        }
    }
}