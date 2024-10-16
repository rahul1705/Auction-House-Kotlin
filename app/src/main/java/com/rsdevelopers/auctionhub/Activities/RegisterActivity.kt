package com.rsdevelopers.auctionhub.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rsdevelopers.auctionhub.Models.Users
import com.rsdevelopers.auctionhub.databinding.ActivityRegisterBinding
import java.util.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    var binding: ActivityRegisterBinding? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var userId: String? = null
    private var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dialog = ProgressDialog(this@RegisterActivity)
        dialog!!.setMessage("Creating Account...")
        dialog!!.setCancelable(false)
        binding!!.btnSp.setOnClickListener { v: View? ->
            val name = binding!!.etName.text.toString().trim { it <= ' ' }
            val mobile = binding!!.etMobile.text.toString().trim { it <= ' ' }
            val email = binding!!.etEmailSp.text.toString().trim { it <= ' ' }
            val pass = binding!!.etPassSp.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(name)) {
                binding!!.etName.error = "Required"
                binding!!.etName.requestFocus()
            } else if (TextUtils.isEmpty(mobile)) {
                binding!!.etMobile.error = "Required"
                binding!!.etMobile.requestFocus()
            } else if (mobile.length != 10) {
                binding!!.etMobile.error = "Invalid Number"
                binding!!.etMobile.requestFocus()
            } else if (TextUtils.isEmpty(email)) {
                binding!!.etEmailSp.error = "Required"
                binding!!.etEmailSp.requestFocus()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding!!.etEmailSp.error = "Invalid email"
                binding!!.etEmailSp.requestFocus()
            } else if (TextUtils.isEmpty(pass)) {
                binding!!.etPassSp.error = "Required"
                binding!!.etPassSp.requestFocus()
            } else if (pass.length < 8 || !PASSWORD_PATTERN.matcher(pass).matches()) {
                binding!!.etPassSp.error =
                    "Must be of minimum 8 Characters, 1 special character and no white spaces"
                binding!!.etPassSp.requestFocus()
            } else {
                binding!!.etName.error = null
                binding!!.etMobile.error = null
                binding!!.etEmailSp.error = null
                binding!!.etPassSp.error = null
                dialog!!.show()
                mAuth!!.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task: Task<AuthResult?> ->
                        if (task.isSuccessful) {
                            userId = mAuth!!.currentUser!!.uid
                            val users = Users(name, mobile, email, pass, Date())
                            db!!.collection("Users").document(userId!!).set(users)
                                .addOnCompleteListener { task1: Task<Void?> ->
                                    dialog!!.dismiss()
                                    if (task1.isSuccessful) {
                                        startActivity(
                                            Intent(
                                                this@RegisterActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            task1.exception!!.localizedMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            // If sign in fails, display a message to the user.
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@RegisterActivity,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        binding!!.loginRedirect.setOnClickListener { v: View? ->
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        private val PASSWORD_PATTERN = Pattern.compile(
            "^" + "(?=.*[@#$%^&+=])" +  // at least 1 special character
                    "(?=\\S+$)" +  // no white spaces
                    ".{4,}" +  // at least 4 characters
                    "$"
        )
    }
}