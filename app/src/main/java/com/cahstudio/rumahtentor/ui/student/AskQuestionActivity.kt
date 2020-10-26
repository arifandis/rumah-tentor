package com.cahstudio.rumahtentor.ui.student

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
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Student
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.model.request.Request
import com.cahstudio.rumahtentor.ui.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ask_question.*
import kotlinx.android.synthetic.main.toolbar.*

class AskQuestionActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: MessageAdapter

    private val compositeDisposable = CompositeDisposable()
    private var mUser: FirebaseUser? = null
    private var mStudent: Student? = null
    private var mTentor: Tentor? = null
    private var mOrderId: String? = null
    private var mOrder: Order? = null
    private var mMessageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_question)

        configureToolbar()
        initialize()
        profile()
        getCurrentOrder()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mUser = FirebaseAuth.getInstance().currentUser
        mOrderId = intent.getStringExtra("current_order")

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        mAdapter = MessageAdapter(this, mMessageList, mUser?.uid)
        ask_recyclerview.layoutManager = layoutManager
        ask_recyclerview.adapter = mAdapter

        ask_btnSend.setOnClickListener {
            checkMessage()
        }
    }

    fun getCurrentOrder(){
        mOrderId?.let { mRef.child("order").child(it).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mOrder = snapshot.getValue(Order::class.java)
                getTentor()
            }

        }) }
    }

    fun getTentor(){
        mOrder?.tentor_uid?.let { mRef.child("tentor").child(it).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mTentor = snapshot.getValue(Tentor::class.java)
                toolbar_tvTitle.text = mTentor?.name
                getMessage()
            }

        }) }
    }

    fun profile(){
        mUser?.uid?.let {
            mRef.child("student").child(it).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    mStudent = snapshot.getValue(Student::class.java)
                }

            })
        }
    }

    fun checkMessage(){
        val message = ask_msg.text.toString()
        if (message.isBlank()){
            ask_msg.error = "Pesan belum di isi"
        }else{
            sendMessage(message)
        }
    }

    fun sendMessage(message: String){
        val tokenRef = FirebaseDatabase.getInstance().reference
        tokenRef.child("token").orderByKey().equalTo(mTentor?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(applicationContext, p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    var data = Message((mMessageList.size+1).toLong(),mStudent?.name,mUser?.uid,mTentor?.name
                        ,mTentor?.uid,"",message)

                    val token = p0.child(mTentor?.uid.toString()).value.toString()
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

    fun saveMessage(request: Request){
        mUser?.uid?.let {
            mRef.child("tentor").child(it).child("message").child(
                mOrder?.tentor_uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(applicationContext, p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    ask_msg.setText("")

                    //to me
                    mRef.child("student").child(it).child("message").child(mTentor?.uid.toString())
                        .child("name").setValue(mTentor?.name)
                    mRef.child("student").child(it).child("message").child(mTentor?.uid.toString())
                        .child("uid").setValue(mTentor?.uid.toString())
                    mRef.child("student").child(it).child("message").child(mTentor?.uid.toString())
                        .child(request.data.id.toString()).setValue(request.data)

                    //to tentor
                    mRef.child("tentor").child(mTentor?.uid.toString()).child("message").child(it)
                        .child("name").setValue(mStudent?.name)
                    mRef.child("tentor").child(mTentor?.uid.toString()).child("message").child(it)
                        .child("uid").setValue(it)
                    mRef.child("tentor").child(mTentor?.uid.toString()).child("message").child(it)
                        .child(request.data.id.toString()).setValue(request.data)
                }

            })
        }
    }

    fun getMessage(){
        mUser?.uid?.let {
            mRef.child("student").child(it).child("message")
                .child(mTentor?.uid.toString()).addValueEventListener(object : ValueEventListener {
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

    fun onBottomScrolling(){
        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        val layoutManager = ask_recyclerview.layoutManager as LinearLayoutManager
        if (mMessageList.isNotEmpty()){
            smoothScroller.targetPosition = mMessageList.size-1
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }
}