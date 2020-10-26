package com.cahstudio.rumahtentor.ui.tentor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.blanjaque.service.FCMClientHelper
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Message
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.model.request.Request
import com.cahstudio.rumahtentor.ui.adapter.MessageAdapter
import com.cahstudio.rumahtentor.ui.adapter.ScheduleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chat_admin.*
import kotlinx.android.synthetic.main.activity_create_schedule.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class ChatAdminActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: MessageAdapter

    private val compositeDisposable = CompositeDisposable()
    private var mUser: FirebaseUser? = null
    private var mTentor: Tentor? = null
    private var mMessageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_admin)

        configureToolbar()
        initialize()
        profile()
        getMessage()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Admin"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mUser = FirebaseAuth.getInstance().currentUser

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        mAdapter = MessageAdapter(this, mMessageList, mUser?.uid)
        chatadmin_recyclerview.layoutManager = layoutManager
        chatadmin_recyclerview.adapter = mAdapter

        chatadmin_btnSend.setOnClickListener {
            checkMessage()
        }
    }

    fun profile(){
        mUser?.uid?.let {
            mRef.child("tentor").child(it).addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    mTentor = snapshot.getValue(Tentor::class.java)
                }

            })
        }
    }

    fun getMessage(){
        mUser?.uid?.let {
                mRef.child("tentor").child(it).child("message")
                    .child("eKAwdUASOkfuJsxqnUJyH3patcX2").addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            mMessageList.clear()
                            for (value in p0.children){
                                if (value.child("id").getValue(Int::class.java) != null){
                                    var message = value.getValue(Message::class.java)
                                        ?: return

                                    if (message.id != null){
                                        mMessageList.add(message)
                                    }
                                }
                            }
                            mAdapter.notifyDataSetChanged()
                            onBottomScrolling()
                        }

                    })
        }
    }

    fun sendMessage(message: String){
        val tokenRef = FirebaseDatabase.getInstance().reference
        tokenRef.child("token").orderByKey().equalTo("eKAwdUASOkfuJsxqnUJyH3patcX2")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(applicationContext, p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    var data = Message((mMessageList.size+1).toLong(),mTentor?.name,mUser?.uid,"Admin"
                        ,"eKAwdUASOkfuJsxqnUJyH3patcX2","",message)

                    val token = p0.child("eKAwdUASOkfuJsxqnUJyH3patcX2").value.toString()
                    val request = Request(token, data)
                    if (token != null && token.isNotEmpty()){
                        pushNotif(request)
                    }else{
                        saveMessage(request)
                    }
                }

            })
    }

    fun pushNotif(request: Request) {
        val api = FCMClientHelper.getRetrofitBasic().pushNotification(request).doOnError {
            Log.d("error push", it.localizedMessage)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            if (it.success === 1) {
                saveMessage(request)
            } else {
                saveMessage(request)
            }
        }
        compositeDisposable.add(api)
    }

    fun checkMessage(){
        val message = chatadmin_msg.text.toString()
        if (message.isBlank()){
            chatadmin_msg.error = "Pesan belum di isi"
        }else{
            sendMessage(message)
        }
    }

    fun saveMessage(request: Request){
        mUser?.uid?.let {
            mRef.child("tentor").child(it).child("message").child(
                "eKAwdUASOkfuJsxqnUJyH3patcX2").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(applicationContext, p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    chatadmin_msg.setText("")

                    //to me
                    mRef.child("tentor").child(it).child("message").child("eKAwdUASOkfuJsxqnUJyH3patcX2")
                        .child("name").setValue("Admin")
                    mRef.child("tentor").child(it).child("message").child("eKAwdUASOkfuJsxqnUJyH3patcX2")
                        .child("uid").setValue("eKAwdUASOkfuJsxqnUJyH3patcX2")
                    mRef.child("tentor").child(it).child("message").child("eKAwdUASOkfuJsxqnUJyH3patcX2")
                        .child(request.data.id.toString()).setValue(request.data)

                    //to admin
                    mRef.child("admin").child("eKAwdUASOkfuJsxqnUJyH3patcX2").child("message").child(it)
                        .child("name").setValue(mTentor?.name)
                    mRef.child("admin").child("eKAwdUASOkfuJsxqnUJyH3patcX2").child("message").child(it)
                        .child("uid").setValue(it)
                    mRef.child("admin").child("eKAwdUASOkfuJsxqnUJyH3patcX2").child("message").child(it)
                        .child(request.data.id.toString()).setValue(request.data)
                }

            })
        }
    }

    fun onBottomScrolling(){
        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        val layoutManager = chatadmin_recyclerview.layoutManager as LinearLayoutManager
        if (mMessageList.isNotEmpty()){
            smoothScroller.targetPosition = mMessageList.size-1
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }
}