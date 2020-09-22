package com.cahstudio.rumahtentor.ui.student

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.utils.Utils
import com.cahstudio.rumahtentor.model.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mPref: SharedPreferences.Editor
    private var mFirebaseUser: FirebaseUser? = null
    private var mStudent: Student? = null
    private lateinit var mRef: DatabaseReference
    private var loadingDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialize()
    }

    fun initialize(){
        mPref = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE).edit()
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        mRef = FirebaseDatabase.getInstance().reference
        loadingDialog = Utils.setupProgressDialog(this)

        main_ivLogout.setOnClickListener(this)
        main_cvOrderTentor.setOnClickListener(this)
        main_cvSeeSchedule.setOnClickListener(this)
        main_cvPayment.setOnClickListener(this)

        getStudentDetail()
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
            R.id.main_cvOrderTentor -> {
                if (mStudent?.current_order != null && mStudent?.current_order!!.isNotEmpty()){
                    if (mStudent!!.status == "waiting"){
                        Toast.makeText(this, "Anda sudah melakukan pemesanan. Silahkan tunggu konfirmasi dari tentor."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mStudent!!.status == "payment"){
                        Toast.makeText(this, "Anda sudah melakukan pemesanan. Silahkan lakukan pembayan."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mStudent!!.status == "study"){
                        Toast.makeText(this, "Anda sudah melakukan pemesanan. Silahkan lanjutkan belajar Anda."
                            , Toast.LENGTH_SHORT).show()
                    } else if (mStudent!!.status == "waiting schedule"){
                        Toast.makeText(this, "Tunggu jadwal dari Admin."
                            , Toast.LENGTH_SHORT).show()
                    }
                }else{
                    startActivity(Intent(this, OrderActivity::class.java))
                }
            }
            R.id.main_cvSeeSchedule -> {
                if (mStudent?.current_order != null && mStudent?.current_order!!.isNotEmpty()){
                    if (mStudent!!.status == "waiting"){
                        Toast.makeText(this, "Anda sudah melakukan pemesanan. Silahkan tunggu konfirmasi dari tentor."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mStudent!!.status == "payment"){
                        Toast.makeText(this, "Anda sudah melakukan pemesanan. Silahkan lakukan pembayan."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mStudent!!.status == "study"){
                        startActivity(Intent(this, SeeScheduleActivity::class.java))
                    } else if (mStudent!!.status == "waiting schedule"){
                        Toast.makeText(this, "Tunggu jadwal dari Admin."
                            , Toast.LENGTH_SHORT).show()
                    } else{
                        startActivity(Intent(this, SeeScheduleActivity::class.java))
                    }
                }else{
                    Toast.makeText(this, "Anda belum melakukan pemesanan."
                        , Toast.LENGTH_SHORT).show()
                }
            }
            R.id.main_cvPayment -> {
                if (mStudent?.current_order != null && mStudent?.current_order!!.isNotEmpty()){
                    if (mStudent!!.status == "waiting"){
                        Toast.makeText(this, "Anda sudah melakukan pemesanan. Silahkan tunggu konfirmasi dari tentor."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mStudent!!.status == "payment"){
                        val intent = Intent(this, PaymentActivity::class.java)
                        intent.putExtra("current_order", mStudent!!.current_order)
                        startActivity(intent)
                    }else if (mStudent!!.status == "study"){
                        Toast.makeText(this, "Anda sudah melakukan pemesanan. Silahkan lanjutkan belajar Anda."
                            , Toast.LENGTH_SHORT).show()
                    }else if (mStudent!!.status == "waiting schedule"){
                        Toast.makeText(this, "Anda sudah melakukan pembayaran."
                            , Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, "Anda belum melakukan pemesanan."
                        , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getStudentDetail(){
        loadingDialog?.show()
        mFirebaseUser?.uid?.let {
            mRef.child("student").child(it).addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    loadingDialog?.dismiss()
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    loadingDialog?.dismiss()
                    mStudent = Student(it
                        , snapshot.child("name").getValue(String::class.java)
                        , snapshot.child("email").getValue(String::class.java)
                        , snapshot.child("status").getValue(String::class.java)
                        , snapshot.child("current_order").getValue(String::class.java))
                }

            })
        }
    }
}