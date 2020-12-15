package com.cahstudio.rumahtentor.ui.student

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Course
import com.cahstudio.rumahtentor.model.Order
import com.cahstudio.rumahtentor.model.Tentor
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.toolbar.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.io.File
import java.text.NumberFormat
import java.util.*


class PaymentActivity : AppCompatActivity(), View.OnClickListener{
    private lateinit var mRef: DatabaseReference
    private lateinit var mStorageRef: StorageReference
    private lateinit var easyImage: EasyImage
    private lateinit var mUri: Uri
    private lateinit var actionBar: ActionBar

    private var mFirebaseUser: FirebaseUser? = null
    private var mFile: File? = null
    private var mCurrentOrder = ""
    private var isTransfer = false
    private var mCourse = Course()
    private var mCourseList = mutableListOf<Course>()
    private var mOrder = Order()
    private var mTentor = Tentor()
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        configureToolbar()
        initialize()
        getOrder()
    }

    private fun configureToolbar(){
        if (toolbar_toolbar != null) setSupportActionBar(toolbar_toolbar)
        if (supportActionBar != null) {
            actionBar = supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(false)
            toolbar_tvTitle.textSize = 18f
            toolbar_tvTitle.text = "Pembayaran"
            toolbar_btnBack.visibility = View.VISIBLE

            toolbar_btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initialize(){
        mRef = FirebaseDatabase.getInstance().reference
        mStorageRef = FirebaseStorage.getInstance().reference
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        easyImage = EasyImage.Builder(this).setCopyImagesToPublicGalleryFolder(false)
            .allowMultiple(false).build()

        mCurrentOrder = intent.getStringExtra("current_order")

        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CAMERA
            , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            , 1)

        payment_btnUpload.setOnClickListener(this)
        payment_btnConfirm.setOnClickListener(this)
        payment_radiogroup.setOnCheckedChangeListener { radioGroup, i ->
            checkMethodPayment()
        }
    }

    fun checkMethodPayment(){
        if (payment_rbtnOffline.isChecked){
            payment_layoutProof.visibility = View.GONE
            isTransfer = false
        }else if (payment_rbtnTransfer.isChecked){
            payment_layoutProof.visibility = View.VISIBLE
            isTransfer = true
        }
    }

    fun getOrder(){
        mRef.child("order").child(mCurrentOrder).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java) ?: return
                mOrder = order
                getTentor(order)
            }

        })
    }

    fun getTentor(order: Order){
        mRef.child("tentor").orderByChild("uid").equalTo(order.tentor_uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var tentor = Tentor()
                for (ds in snapshot.children){
                    tentor = ds.getValue(Tentor::class.java) ?: return
                }
                val arrayCourseId = order.course?.split(",")
                arrayCourseId?.forEach {
                    getCourse(it,tentor)
                }

            }

        })
    }

    fun getCourse(crouseId: String, tentor: Tentor){
        mRef.child("course").orderByChild("id").equalTo(crouseId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mCourseList.clear()
                var course = Course()
                for (ds in snapshot.children){
                    course = ds.getValue(Course::class.java) ?: return
                }
                mCourseList.add(course)
                val subtotal = course.price * 14
                total += subtotal

                val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                payment_tvTranfer.text = "Transfer ke nomor rekening BANK ${tentor.bank?.toUpperCase()} " +
                        "${tentor.bank_no_rek} a/n ${tentor.bank_account_name?.toUpperCase()} dengan total pembayaran " +
                        "${formatRupiah.format(total)}"
            }

        })
    }

    fun confirmPayement(){
        payment_progressbar.visibility = View.VISIBLE
        payment_btnConfirm.text = ""
        if (isTransfer) {
            val storageRef = mStorageRef.child("order").child(mCurrentOrder).child(mUri.lastPathSegment+"")
            val uploadTask = storageRef.putFile(mUri)
            uploadTask.addOnSuccessListener {
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    storageRef.downloadUrl
                }.addOnCompleteListener {
                    if (it.isSuccessful){
                        val uri = it.result
                        Toast.makeText(applicationContext, "Berhasil di konfirmasi", Toast.LENGTH_SHORT).show()
                        Log.d("uri", uri.toString())
                        mRef.child("order").child(mCurrentOrder).child("payment").setValue(uri.toString())
                        mRef.child("order").child(mCurrentOrder).child("payment_type").setValue("transfer")
                        mRef.child("order").child(mCurrentOrder).child("status").setValue("waiting confirm payment")
                        mOrder.student_uid?.let { it1 -> mRef.child("student").child(it1).child("status").setValue("waiting confirm payment") }
                        mOrder.tentor_uid?.let { it1 -> mRef.child("tentor").child(it1).child("status").setValue("waiting confirm payment") }
                        payment_progressbar.visibility = View.GONE
                        payment_btnConfirm.text = "Konfirmasi"
                        finish()
                    }else{
                        payment_progressbar.visibility = View.GONE
                        payment_btnConfirm.text = "Konfirmasi"
                        Toast.makeText(applicationContext, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }else{
            mRef.child("order").child(mCurrentOrder).child("payment").setValue("")
            mRef.child("order").child(mCurrentOrder).child("payment_type").setValue("offline")
            mRef.child("order").child(mCurrentOrder).child("status").setValue("waiting confirm payment")
            mOrder.student_uid?.let { it1 -> mRef.child("student").child(it1).child("status").setValue("waiting confirm payment") }
            mOrder.tentor_uid?.let { it1 -> mRef.child("tentor").child(it1).child("status").setValue("waiting confirm payment") }
            payment_progressbar.visibility = View.GONE
            payment_btnConfirm.text = "Konfirmasi"
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.payment_btnConfirm -> {
                confirmPayement()
            }
            R.id.payment_btnUpload -> {
                easyImage.openChooser(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback(){
            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                mUri = Uri.fromFile(imageFiles[0].file)
                try {
//                    val bitmap = MediaStore.Images.Media.getBitmap(
//                        contentResolver,
//                        Uri.fromFile(imageFiles[0].file))
                    payment_tvProof.text = imageFiles[0].file.name
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1 -> {
                if (!grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}