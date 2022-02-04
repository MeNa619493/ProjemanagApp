package com.example.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.et_email
import kotlinx.android.synthetic.main.activity_sign_in.et_password

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setUpActionBar()

        btn_sign_in.setOnClickListener {
            registerUser()
        }
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_color_24)
        }

        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("please enter a email", true)
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("please enter a password", true)
                false
            }

            else -> {
                true
            }
        }
    }

    private fun registerUser(){
        val email = et_email.text.toString().trim { it <= ' ' }
        val password = et_password.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)){

            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful){
                        FirestoreClass().loadUserData(this)
                    }
                    else {
                        Toast.makeText(this,"Authentication failed",Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun signInSuccess(user: User){

        hideProgressDialog()

        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}