package com.rsdevelopers.auctionhub.Activities

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rsdevelopers.auctionhub.Models.FirestoreOps
import com.rsdevelopers.auctionhub.Models.Users
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.ActivityUpdateProfileBinding

class UpdateProfileActivity : AppCompatActivity() {
    private var binding: ActivityUpdateProfileBinding? = null
    private var users: Users? = null
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var dialog: ProgressDialog? = null
    private var firestoreOps: FirestoreOps? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setSupportActionBar(binding!!.upPfToolbar)
        binding!!.upPfToolbar.setNavigationIcon(R.drawable.ic_back)
        binding!!.upPfToolbar.setNavigationOnClickListener { onBackPressed() }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        firestoreOps = FirestoreOps()
        dialog = ProgressDialog(this@UpdateProfileActivity)
        dialog!!.setMessage("Updating...")
        dialog!!.setCancelable(false)

        // Fetch and display user data
        firestoreOps!!.getUserData(applicationContext) { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                users = documentSnapshot.toObject(Users::class.java)
                users?.let {
                    binding!!.etUpName.setText(it.name)
                    binding!!.etUpEmail.setText(it.email)
                    binding!!.etUpMobile.setText(it.mobile)
                    Glide.with(this@UpdateProfileActivity)
                        .load(it.userImage)
                        .apply(RequestOptions().placeholder(R.drawable.profile).error(R.drawable.profile))
                        .into(binding!!.upProfileImage)
                }
            } else {
                Toast.makeText(this@UpdateProfileActivity, "No data found!", Toast.LENGTH_SHORT).show()
            }
        }

        // Update profile data
        binding!!.upBtn.setOnClickListener { updateProfile() }

        // Image picker
        binding!!.upProfileImage.setOnClickListener { imgContent.launch("image/*") }
    }

    private fun updateProfile() {
        val name = binding!!.etUpName.text.toString().trim()
        val email = binding!!.etUpEmail.text.toString().trim()
        val mobile = binding!!.etUpMobile.text.toString().trim()

        // Validate input fields
        when {
            name.isEmpty() -> binding!!.upNameLayout.error = "Please enter name"
            email.isEmpty() -> binding!!.upEmailLayout.error = "Please enter email"
            mobile.isEmpty() -> binding!!.upMobLayout.error = "Please enter number"
            else -> {
                binding!!.upNameLayout.error = null
                binding!!.upEmailLayout.error = null
                binding!!.upMobLayout.error = null

                // Show progress dialog
                dialog!!.show()

                // Create update map
                val map = mapOf("name" to name, "email" to email, "mobile" to mobile)

                // Update Firestore document
                db!!.collection("Users")
                    .document(auth!!.currentUser!!.uid)
                    .update(map)
                    .addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful) {
                            onBackPressed()
                            Toast.makeText(this@UpdateProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@UpdateProfileActivity, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    // Upload Image and update Firestore
    private val imgContent = registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
        result?.let {
            dialog!!.show()
            val ref = FirebaseStorage.getInstance().reference.child("users/${auth!!.currentUser!!.uid}")
            val uploadTask = ref.putFile(it)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    db!!.collection("Users")
                        .document(auth!!.currentUser!!.uid)
                        .update("userImage", downloadUri.toString())
                        .addOnCompleteListener { task1 ->
                            dialog!!.dismiss()
                            if (task1.isSuccessful) {
                                onBackPressed()
                                Toast.makeText(this@UpdateProfileActivity, "Image updated successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@UpdateProfileActivity, "Error: ${task1.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}
