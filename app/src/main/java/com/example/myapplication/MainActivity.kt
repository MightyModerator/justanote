// Defines a package for the code which is a way to organize and group together related pieces of code.
package com.example.myapplication

// Import statements to include Android specific functionalities.
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast


// Declaration of MainActivity class which inherits from AppCompatActivity.
// AppCompatActivity is a base class for activities that use the support library action bar features.
class MainActivity : AppCompatActivity() {
    // Override the onCreate method which is called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the user interface layout for this Activity.
        // The layout file is defined in the project res/layout/activity_main.xml.
        setContentView(R.layout.activity_main)

        // Find the Button view with the ID 'btnLogin' and assign it to the variable btnLogin.
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        // Set an onClickListener on the button to define what should happen when the button is clicked.
        btnLogin.setOnClickListener {
            // Display a Toast message. Toasts provide simple feedback about an operation in a small popup.
            Toast.makeText(this, getString(R.string.signed_in), Toast.LENGTH_LONG).show()

            // Find the ImageView with the ID 'ivIcon' and assign it to the variable ivIcon.
            val ivIcon = findViewById<ImageView>(R.id.ivIcon)
            // Load the rotation animation defined in res/anim/rotate.xml and assign it to the variable rotation.
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            // Start the animation on the ivIcon ImageView.
            ivIcon.startAnimation(rotation)

            // Create an Intent to start ListActivity, which is another activity in our application.
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }
    }
}