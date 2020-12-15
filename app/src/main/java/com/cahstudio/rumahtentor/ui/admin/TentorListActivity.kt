package com.cahstudio.rumahtentor.ui.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.model.User
import com.cahstudio.rumahtentor.ui.adapter.UserAdapter
import com.cahstudio.rumahtentor.ui.admin.adapter.TentorAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_tentor_list.*
import kotlinx.android.synthetic.main.toolbar.*

class TentorListActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: TentorAdapter

    private var mTentorList = mutableListOf<Tentor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentor_list)

        configureToolbar()
        initialize()
        getTentorList()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Daftar Tentor"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference

        val layoutManager = LinearLayoutManager(this)
        mAdapter = TentorAdapter(this, mTentorList)
        tentor_recyclerview.layoutManager = layoutManager
        tentor_recyclerview.adapter = mAdapter
    }

    fun getTentorList(){
        mRef.child("tentor").orderByChild("account_status").equalTo("not confirmed")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    mTentorList.clear()
                    snapshot.children.forEach {
                        val tentor = it.getValue(Tentor::class.java)?: return
                        mTentorList.add(tentor)
                    }
                    mAdapter.notifyDataSetChanged()
                }

            })
    }

    fun gotoTentorDetail(user: Tentor){
        val intent = Intent(this, TentorDetailActivity::class.java)
        intent.putExtra("uid", user.uid)
        startActivity(intent)
    }
}