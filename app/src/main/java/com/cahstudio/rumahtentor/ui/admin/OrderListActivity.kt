package com.cahstudio.rumahtentor.ui.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.ui.tentor.adapter.OrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_list_order.*
import kotlinx.android.synthetic.main.activity_order_list.*
import kotlinx.android.synthetic.main.toolbar.*

class OrderListActivity : AppCompatActivity() {
    private lateinit var actionBar: ActionBar
    private lateinit var mRef: DatabaseReference
    private lateinit var mAdapter: OrderAdapter

    private var mUser: FirebaseUser? = null
    private var mOrderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)

        configureToolbar()
        initialize()
        getOrderList()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Daftar Pesanan"
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
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        mAdapter = OrderAdapter(this, mOrderList, {}
            , {})
        orderlist_recyclerview.layoutManager = layoutManager
        orderlist_recyclerview.adapter = mAdapter
    }

    fun getOrderList(){
        orderlist_progressbbar.visibility = View.VISIBLE
        orderlist_recyclerview.visibility = View.GONE
        mRef.child("order").orderByChild("no").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mOrderList.clear()
                for (ds in snapshot.children){
                    val order = ds.getValue(Order::class.java) ?: return
                    if (order.status == "waiting schedule"){
                        mOrderList.add(order)
                    }
                }

                mAdapter.notifyDataSetChanged()
                orderlist_progressbbar.visibility = View.GONE
                orderlist_recyclerview.visibility = View.VISIBLE
            }

        })
    }
}