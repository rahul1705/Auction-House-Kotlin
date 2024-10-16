package com.rsdevelopers.auctionhub.Activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
import com.rsdevelopers.auctionhub.Fragments.BidFragment
import com.rsdevelopers.auctionhub.Fragments.MyItemsFragment
import com.rsdevelopers.auctionhub.Fragments.SellFragment
import com.rsdevelopers.auctionhub.Fragments.SettingsFragment
import com.rsdevelopers.auctionhub.Models.FirestoreOps
import com.rsdevelopers.auctionhub.Models.Users
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null
    private var users: Users? = null
    private var firestoreOps: FirestoreOps? = null
    @SuppressLint("NonConstantResourceId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        replaceFragment(BidFragment())
        firestoreOps = FirestoreOps()
        try {
            loadWalletData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding!!.bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bid -> replaceFragment(BidFragment())
                R.id.sell -> replaceFragment(SellFragment())
                R.id.myItems -> replaceFragment(MyItemsFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.commit()
    }

    private fun loadWalletData() {
        firestoreOps!!.getWalletData(applicationContext) { documentSnapshot: DocumentSnapshot ->
            if (documentSnapshot.exists()) {
                // User data found in Firestore
                users = documentSnapshot.toObject(Users::class.java)
                assert(users != null)
                val userBalance = "\uD83D\uDC5B " + users!!.balance
                binding!!.topAppBar.menu.findItem(R.id.bal_amount).title = userBalance
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreOps!!.unregisterListener()
    }
}