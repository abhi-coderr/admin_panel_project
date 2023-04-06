package com.example.adminapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.example.adminapp.R
import com.example.adminapp.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigationDelay()
        animationLogo()

    }

    private fun animationLogo() {
        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_anim)
        binding.logoMain.startAnimation(slideAnimation)
    }

    private fun navigationDelay() {
        Handler().postDelayed({
            // Intent is used to switch from one activity to another.
            val i = Intent(this, TestimonySelectorActivity::class.java)
            startActivity(i) // invoke the SecondActivity.
            finish() // the current activity will get finished.
        }, 3000)
    }

}