package com.cahstudio.rumahtentor.ui.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Tentor
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tentor_detail.*
import kotlinx.android.synthetic.main.toolbar.*

class TentorDetailActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentor_detail)

        configureToolbar()
        initialize()
        getTentor()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Detail Tentor"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mUid = intent.getStringExtra("uid")

        tentordetail_btnConfirm.setOnClickListener {
            confirmTentor()
        }
    }

    fun getTentor(){
        mRef.child("tentor").child(mUid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val tentor = snapshot.getValue(Tentor::class.java)?: return

                tentordetail_tvName.text = tentor.name
                tentordetail_tvEmail.text = tentor.email
                tentordetail_tvBankName.text = tentor.bank
                tentordetail_tvAccountName.text = tentor.bank_account_name
                tentordetail_tvAccountNumber.text = tentor.bank_no_rek

                Picasso.get().load(tentor.ktm).into(tentordetail_ivKtm)
            }

        })
    }

    fun confirmTentor(){
        tentordetail_progressbar.visibility = View.VISIBLE
        tentordetail_btnConfirm.text = ""
        mRef.child("tentor").child(mUid).child("account_status").setValue("confirmed")
            .addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Konfirmasi tentor berhasil", Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    tentordetail_progressbar.visibility = View.GONE
                    tentordetail_btnConfirm.text = "Konfirmasi"
                    Toast.makeText(this, "Konfirmasi tentor gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }
}