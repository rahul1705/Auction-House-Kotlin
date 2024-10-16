package com.rsdevelopers.auctionhub.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.rsdevelopers.auctionhub.Activities.*
import com.rsdevelopers.auctionhub.Models.FirestoreOps
import com.rsdevelopers.auctionhub.Models.Users
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    var binding: FragmentSettingsBinding? = null
    private var users: Users? = null
    private var mAuth: FirebaseAuth? = null
    private var firestoreOps: FirestoreOps? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        firestoreOps = FirestoreOps()
        loadProfileData()

        // Setting up listeners for buttons in the Settings fragment
        binding!!.aboutBtn.setOnClickListener {
            val i = Intent(context, AdditionalInfo::class.java)
            i.putExtra("data", "about")
            startActivity(i)
        }
        binding!!.policyBtn.setOnClickListener {
            val i = Intent(context, AdditionalInfo::class.java)
            i.putExtra("data", "policy")
            startActivity(i)
        }
        binding!!.contactBtn.setOnClickListener {
            val i = Intent(context, AdditionalInfo::class.java)
            i.putExtra("data", "contact")
            startActivity(i)
        }
        binding!!.shareBtn.setOnClickListener {
            // Share app intent setup
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val subject = "Check out this auction app!"
            val message =
                "I've been using this auction app and it's great. You should check it out!"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(shareIntent, "Share using"))
        }
        binding!!.historyBtn.setOnClickListener {
            startActivity(
                Intent(context, WinActivity::class.java)
            )
        }
        binding!!.updatePTv.setOnClickListener {
            startActivity(
                Intent(context, UpdateProfileActivity::class.java)
            )
        }
        binding!!.walletBtn.setOnClickListener {
            startActivity(
                Intent(context, WalletActivity::class.java)
            )
        }
        binding!!.logoutBtn.setOnClickListener {
            mAuth!!.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return binding!!.root
    }

    // Load profile data from Firestore and display it
    private fun loadProfileData() {
        firestoreOps!!.getUserData(context) { documentSnapshot: DocumentSnapshot? ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                // User data found in Firestore
                users = documentSnapshot.toObject(Users::class.java)
                if (users != null) {
                    binding!!.dUserName.text = users!!.name
                    binding!!.dUserEmail.text = users!!.email
                    Glide.with(requireContext()).load(users!!.userImage)
                        .placeholder(R.drawable.profile).into(binding!!.profileImage)
                }
            } else {
                // Handle case when user data is not found
                binding!!.dUserName.setText(R.string.error_name)
                binding!!.dUserEmail.setText(R.string.error_email)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreOps!!.unregisterListener()
    }
}
