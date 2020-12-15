package com.cahstudio.rumahtentor.ui.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.User
import com.cahstudio.rumahtentor.ui.adapter.UserAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat_tentor.*
import kotlinx.android.synthetic.main.toolbar.*

class ChatTentorActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: UserAdapter

    private var mMessageList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_tentor)

        configureToolbar()
        initialize()
        getMessageList()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Pesan"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference

        val layoutManager = LinearLayoutManager(this)
        mAdapter = UserAdapter(this, mMessageList, { userMessage -> gotoChat(userMessage.uid) })
        chattentor_recyclerview.layoutManager = layoutManager
        chattentor_recyclerview.adapter = mAdapter
    }

    fun getMessageList(){
        mRef.child("admin").child("eKAwdUASOkfuJsxqnUJyH3patcX2").child("message").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (value in snapshot.children){
                    val user = value.getValue(User::class.java) ?: return
                    mMessageList.add(user)
                }
                mAdapter.notifyDataSetChanged()
            }

        })
    }

    fun gotoChat(tentorUid: String?){
        val intent = Intent(this, OnChatActivity::class.java)
        intent.putExtra("tentor_uid", tentorUid)
        startActivity(intent)
    }

}