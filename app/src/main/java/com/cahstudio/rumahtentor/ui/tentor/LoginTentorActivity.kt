package com.cahstudio.rumahtentor.ui.tentor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.ui.admin.MainAdminActivity
import com.cahstudio.rumahtentor.ui.student.MainActivity
import com.cahstudio.rumahtentor.ui.student.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginTentorActivity : AppCompatActivity(), View.OnClickListener {
    private var mAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mPrefEditor: SharedPreferences.Editor
    private lateinit var mRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_tentor)

        initialize()
    }

    fun initialize(){
        mAuth = FirebaseAuth.getInstance()
        mRef = FirebaseDatabase.getInstance().reference
        mPrefEditor = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE).edit()

        login_btnLoginTentor.setOnClickListener(this)
        login_tvRegister.setOnClickListener(this)

        checkUserLoggedIn()
    }

    fun checkForm(){
        val email = login_etEmail.text.toString()
        val password = login_etPassword.text.toString()
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Lengkapi email atau password", Toast.LENGTH_SHORT).show()
        }else{
            login_btnLoginTentor.text = ""
            login_progressbar.visibility = View.VISIBLE
            login(email, password)
        }
    }

    fun login(email: String, password: String){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                mPrefEditor.putString("mode", "tentor")
                mPrefEditor.apply()
                checkUserLoggedIn()
            }else{
                login_btnLoginTentor.text = "Masuk"
                login_progressbar.visibility = View.GONE
                Toast.makeText(this, "Login gagal, email atau password salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkUserLoggedIn(){
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null){
                updateToken()
            }
        }
    }

    fun updateToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                mAuth.currentUser?.uid?.let {
                    mRef.child("token").child(it).setValue(instanceIdResult.token).addOnCompleteListener {
                        if (it.isSuccessful){
                            startActivity(Intent(applicationContext, MainTentorActivity::class.java))
                            finish()
                        }else{
                            Toast.makeText(this, "Gagal mengupdate token", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }.addOnFailureListener { e ->
                e.localizedMessage?.let { it1 ->
                    Toast.makeText(this, it1, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.login_tvRegister->{
                startActivity(Intent(this, RegisterTentorActivity::class.java))
                finish()
            }
            R.id.login_btnLoginTentor->{
                checkForm()
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
}