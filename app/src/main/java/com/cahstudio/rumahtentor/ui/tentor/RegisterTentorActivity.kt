package com.cahstudio.rumahtentor.ui.tentor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.model.Student
import com.cahstudio.rumahtentor.model.Tentor
import com.cahstudio.rumahtentor.ui.student.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.register_btnRegis
import kotlinx.android.synthetic.main.activity_register.register_etConfirmPassword
import kotlinx.android.synthetic.main.activity_register.register_etEmail
import kotlinx.android.synthetic.main.activity_register.register_etName
import kotlinx.android.synthetic.main.activity_register.register_etPassword
import kotlinx.android.synthetic.main.activity_register.register_progressbar
import kotlinx.android.synthetic.main.activity_register_tentor.*

class RegisterTentorActivity : AppCompatActivity(), View.OnClickListener {
    private var mAuth = FirebaseAuth.getInstance()
    private var mRef = FirebaseDatabase.getInstance().reference

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

        register_btnRegis.setOnClickListener(this)

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
                val tentor = Tentor("",email,"",name,authUser?.uid,bank,noRek,accountName,"","not teaching")

                authUser?.uid?.let { it1 -> mRef.child("tentor").child(it1).setValue(tentor) }
                mPrefEditor.putString("mode", "tentor")
                mPrefEditor.apply()
                Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()

                checkUserLoggedIn()
            }else{
                register_progressbar.visibility = View.GONE
                register_btnRegis.text = "Daftar"
                Toast.makeText(this, "Pendaftaran gagal, coba lagi", Toast.LENGTH_SHORT).show()
            }
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