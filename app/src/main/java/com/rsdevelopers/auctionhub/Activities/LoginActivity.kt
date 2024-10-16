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
import com.rsdevelopers.auctionhub.databinding.ActivityLoginBinding
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    var binding: ActivityLoginBinding? = null
    private var mAuth: FirebaseAuth? = null
    private var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        dialog = ProgressDialog(this@LoginActivity)
        dialog!!.setMessage("Logging in...")
        dialog!!.setCancelable(false)
        mAuth = FirebaseAuth.getInstance()
        binding!!.btnLogin.setOnClickListener { v: View? ->
            val email = binding!!.etEmailLogin.text.toString().trim { it <= ' ' }
            val pass = binding!!.etPassLogin.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                binding!!.etEmailLogin.error = "Required"
                binding!!.etEmailLogin.requestFocus()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding!!.etEmailLogin.error = "Invalid email"
                binding!!.etEmailLogin.requestFocus()
            } else if (TextUtils.isEmpty(pass)) {
                binding!!.etPassLogin.error = "Required"
                binding!!.etPassLogin.requestFocus()
            } else if (pass.length < 8 || !PASSWORD_PATTERN.matcher(pass).matches()) {
                binding!!.etPassLogin.error =
                    "Must be of minimum 8 Characters, 1 special character and no white spaces"
                binding!!.etPassLogin.requestFocus()
            } else {
                binding!!.etEmailLogin.error = null
                binding!!.etPassLogin.error = null
                dialog!!.show()
                mAuth!!.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                        if (task.isSuccessful) {
                            dialog!!.dismiss()
                            //                        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@LoginActivity,
                                "Invalid Credentials",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        binding!!.registerRedirect.setOnClickListener { v: View? ->
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
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