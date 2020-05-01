package com.lyfeforcelabs.kollaborate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_authentication.*

class AuthenticationActivity : AppCompatActivity() {

    // Initalise the firebase auth object within the class.
    var firebaseAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        val sharedPref = getSharedPreferences("login", 0)

        // Get a new instance of the firebase auth object.
        firebaseAuth = FirebaseAuth.getInstance()

        if(sharedPref.getBoolean("logged",false)) {
            val email = sharedPref.getString("EmailField","")!!
            val password = sharedPref.getString("PasswordField","")!!
            userLogIn(email, password, true)
        }

        // Collect the data from the email and password fields to complete the login.
        loginButton.setOnClickListener {
            val email = EmailField.text.toString()
            val password = PasswordField.text.toString()

            // If it meets the parameters of login function it moves the data from the fields
            // and puts them in the function.
            if (userLogIn(email, password, false)) {
                sharedPref.edit().putBoolean("logged", true).apply()
                sharedPref.edit().putString("EmailField", email).apply()
                sharedPref.edit().putString("PasswordField", password).apply()
            }

        }

        // Activate the user registration process when they tap on the register button.
        RegisterButton.setOnClickListener {
            createNewUser()
        }

    }

    // Create a new user account.
    private fun createNewUser() {
        val email = EmailField!!.text.toString()
        val password = PasswordField!!.text.toString()

        // Prints a message to the user that they need to enter their email.
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter your email.", Toast.LENGTH_SHORT).show()
            return
        }

        // Prints a message message to the user that they need to enter a password.
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the user account once that both fields have been completed.
        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Everything has worked as expected, send the user verification
                    // to the user.
                    Log.d ("Positive Action", "userCreated:Success")
                    val user = firebaseAuth!!.currentUser
                    sendUserEmailVerification()
                } else {
                    // Something has gone wrong, user account was not created.
                    // This is the message that is shown to the user.
                    Log.w ("Negative Action", "userCreated:Failed")
                    Toast.makeText(baseContext, "User account creation has failed",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Log the user in with an email and password.
    private fun userLogIn(email: String, password: String, logged: Boolean): Boolean {
        Log.d("User sign in has worked", "sign in: $email")

        // Form verification test
        if (!userFormValidatation(logged)) {
            return false
        }

        // Firebase signs the user in with their email and password.
        firebaseAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // If all parameters have been met it will sign the user in
                // and move to the next screen.
                val user = firebaseAuth!!.currentUser

            } else {
                // In the event something goes wrong, this message is displayed to the user.
                Log.w("Sign in fail", "signInWithEmail:Failed", task.exception)
                Toast.makeText(baseContext, "User authentication failed", Toast.LENGTH_SHORT)
                    .show()
                }
            }
        return true
    }

    // Validate the user form to ensure that all fields are open.
    private fun userFormValidatation(logged: Boolean): Boolean {
        var valid = true
        val user = firebaseAuth!!.currentUser

        // Verifies that the user field has been completed,
        // if not it fails and returns a message to the user.
        var email = EmailField.text.toString()
        if (TextUtils.isEmpty(email) && !logged) {
            Toast.makeText(baseContext, "Email is required", Toast.LENGTH_SHORT).show()
            valid = false
        }

        // Verifies that the password has been completed,
        // if no it fails and returns a message to the user.
        var password = PasswordField.text.toString()
        if(TextUtils.isEmpty(password) && !logged) {
            Toast.makeText(baseContext, "Password is required", Toast.LENGTH_SHORT).show()
            valid = false
        }

        // Firebase enforces sign in policy with an email.
        firebaseAuth!!.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.result!!.signInMethods!!.size == 0) {
                    // Print a message to the user that they need to register
                    // to use the app.
                    Toast.makeText(baseContext, "You need to register in order to use the app.", Toast.LENGTH_SHORT)
                        .show()
                    valid = false
                }

                else if (!(user!!.isEmailVerified)) {
                    // Print a message to the user that their account needs to be verified
                    // before they can sign into the app.
                    Toast.makeText(baseContext, "Your account needs to be verified before you can continue",
                    Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener {i ->
                // Prints a message to the user that they need to register
                // to use the app.
                Toast.makeText(baseContext, "You need to register in order to use the app.", Toast.LENGTH_SHORT)
                    .show()
            }

        return valid
    }

    private fun sendUserEmailVerification() {
        // Send the user the email verification so they can use their account.
        val user = firebaseAuth!!.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    // The verification email has been successfully sent to
                    // to the specified email address.
                    Toast.makeText(baseContext,
                    "Verification sent to ${user.email}", Toast.LENGTH_SHORT).show()
                } else {
                    // The verification email didn't send and it has been logged in
                    // the system.
                    Log.e("Something broke", "The email didn't send", task.exception)
                    Toast.makeText(baseContext, "Sorry the verification email didn't send",
                    Toast.LENGTH_SHORT).show()
                }
            }
    }
}
