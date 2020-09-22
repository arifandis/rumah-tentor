package com.cahstudio.rumahtentor.ui.student

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.ui.admin.LoginAdminActivity
import com.cahstudio.rumahtentor.ui.admin.MainAdminActivity
import com.cahstudio.rumahtentor.ui.tentor.LoginTentorActivity
import com.cahstudio.rumahtentor.ui.tentor.MainTentorActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var mAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mPref: SharedPreferences
    private lateinit var mPrefEditor: SharedPreferences.Editor
    private lateinit var mRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initialize()
    }

    fun initialize(){
        mAuth = FirebaseAuth.getInstance()
        mRef = FirebaseDatabase.getInstance().reference
        mPref = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE)
        mPrefEditor = getSharedPreferences("rumah_tentor", Context.MODE_PRIVATE).edit()

        login_btnLoginUser.setOnClickListener(this)
        login_btnLoginAdmin.setOnClickListener(this)
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
            login_btnLoginUser.text = ""
            login_progressbar.visibility = View.VISIBLE
            login(email, password)
        }
    }

    fun login(email: String, password: String){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                mPrefEditor.putString("mode", "student")
                mPrefEditor.apply()
                checkUserLoggedIn()
                startActivity(intent)
                finish()
            }else{
                login_btnLoginUser.text = "Masuk"
                login_progressbar.visibility = View.GONE
                Toast.makeText(this, "Login gagal, email atau password salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkUserLoggedIn(){
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null){
                val mode = mPref.getString("mode", "")
                if (mode?.isNotEmpty()!!){
                    var intent = Intent()
                    if (mode == "student"){
                        intent = Intent(this, MainActivity::class.java)
                    }else if (mode == "tentor"){
                        intent = Intent(this, MainTentorActivity::class.java)
                    }else if (mode == "admin"){
                        intent = Intent(this, MainAdminActivity::class.java)
                    }
                    updateToken(intent)
                }
            }
        }
    }

    fun updateToken(intent: Intent){
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                mAuth.currentUser?.uid?.let {
                    mRef.child("token").child(it).setValue(instanceIdResult.token).addOnCompleteListener {
                        if (it.isSuccessful){
                            startActivity(intent)
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
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
            R.id.login_btnLoginUser->{
                checkForm()
            }
            R.id.login_btnLoginTentor->{
                startActivity(Intent(this, LoginTentorActivity::class.java))
                finish()
            }
            R.id.login_btnLoginAdmin->{
                startActivity(Intent(this, LoginAdminActivity::class.java))
                finish()
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