package com.lyfeforcelabs.kollaborate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class AuthenticationActivity : AppCompatActivity() {

    // Initalise the firebase auth object within the class.
    var firebaseAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        val sharedPref = getSharedPreferences("login", 0)

        // Get a new instance of the firebase auth object.
        firebaseAuth = FirebaseAuth.getInstance()

    }
}
