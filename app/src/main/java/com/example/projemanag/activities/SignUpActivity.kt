package com.example.projemanag.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setUpActionBar()

        btn_sign_up.setOnClickListener {
            registerUser()
        }

    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_color_24)
        }

        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("please enter a name", true)
                false
            }

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
        val name = et_name.text.toString().trim { it <= ' ' }
        val email = et_email.text.toString().trim { it <= ' ' }
        val password = et_password.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)){

            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    }
                    else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this, "you have successfully registered", Toast.LENGTH_LONG).show()

        hideProgressDialog()

        FirebaseAuth.getInstance().signOut()
        finish()
    }
}