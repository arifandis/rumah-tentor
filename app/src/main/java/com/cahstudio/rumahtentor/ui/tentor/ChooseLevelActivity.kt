package com.cahstudio.rumahtentor.ui.tentor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.cahstudio.rumahtentor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_choose_level.*
import kotlinx.android.synthetic.main.toolbar.*

class ChooseLevelActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mRef: DatabaseReference
    private var mUserFirebase: FirebaseUser? = null

    private var levelList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_level)

        configureToolbar()
        initialize()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Pilih Tingkatan"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mAuth = FirebaseAuth.getInstance()
        mUserFirebase = mAuth.currentUser
        mRef = FirebaseDatabase.getInstance().reference

        chooselevel_tvNext.setOnClickListener {
            addLevel()
        }

        chooselevel_cbSD.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked){
                levelList.add(chooselevel_cbSD.text.toString())
            }else{
                levelList.remove(chooselevel_cbSD.text.toString())
            }
        }

        chooselevel_cbSMP.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked){
                levelList.add(chooselevel_cbSMP.text.toString())
            }else{
                levelList.remove(chooselevel_cbSMP.text.toString())
            }
        }

        chooselevel_cbSMA.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked){
                levelList.add(chooselevel_cbSMA.text.toString())
            }else{
                levelList.remove(chooselevel_cbSMA.text.toString())
            }
        }
    }

    fun addLevel(){
        if (levelList.isEmpty()){
            Toast.makeText(this, "Pilih jenjang yang akan di ajar", Toast.LENGTH_SHORT).show()
        }else{
            var level = ""
            var i = 0
            levelList.forEach {
                if (i == 0){
                    level += it
                }else{
                    level += ","+it
                }
                i++
            }
            mUserFirebase?.uid?.let { mRef.child("tentor").child(it).child("level")
                .setValue(level).addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(this, ChooseCourseActivity::class.java)
                        intent.putExtra("level", level)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, "Internal server error", Toast.LENGTH_SHORT).show()
                    }
                } }
        }
    }
}