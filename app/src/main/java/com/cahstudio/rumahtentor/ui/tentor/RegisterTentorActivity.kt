package com.cahstudio.rumahtentor.ui.tentor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Student
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.ui.student.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.register_btnRegis
import kotlinx.android.synthetic.main.activity_register.register_etConfirmPassword
import kotlinx.android.synthetic.main.activity_register.register_etEmail
import kotlinx.android.synthetic.main.activity_register.register_etName
import kotlinx.android.synthetic.main.activity_register.register_etPassword
import kotlinx.android.synthetic.main.activity_register.register_progressbar
import kotlinx.android.synthetic.main.activity_register_tentor.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource

class RegisterTentorActivity : AppCompatActivity(), View.OnClickListener {
    private var mAuth = FirebaseAuth.getInstance()
    private var mRef = FirebaseDatabase.getInstance().reference
    private var mStorageRef = FirebaseStorage.getInstance().reference
    private var mUri: Uri? = null

    private lateinit var easyImage: EasyImage
    private lateinit var mPrefEditor: SharedPreferences.Editor
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_tentor)

        initialize()
    }

    fun initialize(){
        mAuth = FirebaseAuth.getInstance()
        mPrefEditor = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE).edit()
        easyImage = EasyImage.Builder(this).setCopyImagesToPublicGalleryFolder(false)
            .allowMultiple(false).build()

        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CAMERA
            , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            , 1)

        register_btnRegis.setOnClickListener(this)
        btnUploadKtm.setOnClickListener(this)

        checkUserLoggedIn()
    }

    fun checkForm(){
        val name = register_etName.text.toString()
        val email = register_etEmail.text.toString()
        val bank = register_etBank.text.toString()
        val noRek = register_etNoRek.text.toString()
        val accountName = register_etAccountName.text.toString()
        val password = register_etPassword.text.toString()
        val confirmPassword = register_etConfirmPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
            bank.isEmpty() || noRek.isEmpty() || accountName.isEmpty()){
            Toast.makeText(this, "Isi data dengan benar", Toast.LENGTH_SHORT).show()
        }else if (password != confirmPassword){
            Toast.makeText(this, "Konfirmasi password salah", Toast.LENGTH_SHORT).show()
        }else{
            register_progressbar.visibility = View.VISIBLE
            register_btnRegis.text = ""
            register(name, email, password, bank, noRek, accountName)
        }
    }

    fun register(name: String, email: String, password: String, bank: String, noRek: String, accountName: String){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
            if (it.isSuccessful){
                val authUser = it.result?.user
                val tentor = Tentor("",email,"",name,authUser?.uid,bank,noRek,accountName,"","not teaching","")
                authUser?.let { it1 -> uploadKtm(it1, tentor) }
            }else{
                register_progressbar.visibility = View.GONE
                register_btnRegis.text = "Daftar"
                Toast.makeText(this, "Pendaftaran gagal, coba lagi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun uploadKtm(auth: FirebaseUser, tentor: Tentor){
        val storageRef = mStorageRef.child("tentor").child(auth.uid).child(mUri?.lastPathSegment+"")
        val uploadTask = mUri?.let { storageRef.putFile(it) }
        uploadTask?.addOnSuccessListener {
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                storageRef.downloadUrl
            }.addOnCompleteListener {
                if (it.isSuccessful){
                    val uri = it.result
                    tentor.ktm = uri.toString()
                    mRef.child("tentor").child(auth.uid).setValue(tentor)
                    mPrefEditor.putString("mode", "tentor")
                    mPrefEditor.apply()
                    Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()

                    checkUserLoggedIn()
                }else{
                    Toast.makeText(applicationContext, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }?.addOnFailureListener {
            Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun checkUserLoggedIn(){
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null){
                startActivity(Intent(this, ChooseLevelActivity::class.java))
                finish()
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.register_btnRegis->{
                checkForm()
            }
            R.id.btnUploadKtm -> {
                easyImage.openChooser(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback(){
            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                mUri = Uri.fromFile(imageFiles[0].file)
                try {
                    tvNameKTM.visibility = View.VISIBLE
                    tvNameKTM.text = imageFiles[0].file.name

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