package com.rsdevelopers.auctionhub.Activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.ActivityAdditionalInfoBinding

class AdditionalInfo : AppCompatActivity() {
    var binding: ActivityAdditionalInfoBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdditionalInfoBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        binding!!.additionalToolbar.setNavigationIcon(R.drawable.ic_back)
        binding!!.additionalToolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        val data = intent.getStringExtra("data")
        when (data) {
            "about" -> {
                binding!!.additionalToolbar.title = "About Us"
                binding!!.webview.loadUrl("file:///android_res/raw/about_us.html")
            }
            "policy" -> {
                binding!!.additionalToolbar.title = "Privacy Policy"
                binding!!.webview.loadUrl("file:///android_res/raw/privacy_policy.html")
            }
            "contact" -> {
                binding!!.additionalToolbar.title = "Contact Us"
                binding!!.webview.visibility = View.GONE
                binding!!.contactLayout.visibility = View.VISIBLE
            }
        }
    }
}