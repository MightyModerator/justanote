package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            Toast.makeText(this, getString(R.string.signed_in), Toast.LENGTH_LONG).show()

            val ivIcon = findViewById<ImageView>(R.id.ivIcon)
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            ivIcon.startAnimation(rotation)

            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }
    }
}