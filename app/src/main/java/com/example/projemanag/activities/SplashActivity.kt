package com.example.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager.LayoutParams.*
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )

        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        tv_app_name.typeface = typeface

        @Suppress("DEPRECATION")
        Handler().postDelayed({

            val currentUserID = FirestoreClass().getCurrentUserID()

            if (currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }
            else {
                startActivity(Intent(this, IntroActivity::class.java))
            }

            finish()

        }, 2500)
    }
}