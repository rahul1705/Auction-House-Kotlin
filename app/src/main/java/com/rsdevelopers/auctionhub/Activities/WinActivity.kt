package com.rsdevelopers.auctionhub.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.rsdevelopers.auctionhub.Adapters.MyItemsAdapter
import com.rsdevelopers.auctionhub.Models.AuctionItem
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.ActivityWinHistoryBinding

class WinActivity : AppCompatActivity() {
    var binding: ActivityWinHistoryBinding? = null
    private var db: FirebaseFirestore? = null
    private var mAdapter: MyItemsAdapter? = null
    private var itemList: MutableList<AuctionItem>? = null
    private var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWinHistoryBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.winningsToolbar)
        binding!!.winningsToolbar.setNavigationIcon(R.drawable.ic_back)
        binding!!.winningsToolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        itemList = ArrayList()
        // Initialize RecyclerView
        binding!!.winningsRv.setHasFixedSize(true)
        binding!!.winningsRv.layoutManager = LinearLayoutManager(this@WinActivity)
        mAdapter = MyItemsAdapter(this@WinActivity, itemList)
        binding!!.winningsRv.adapter = mAdapter
        mAdapter!!.setOnItemClickListener { position: Int ->
            val items = (itemList as ArrayList<AuctionItem>).get(position)
            val intent = Intent(this@WinActivity, BidsInfoActivity::class.java)
            intent.putExtra("itemId", items.itemId)
            intent.putExtra("mData", "myWins")
            startActivity(intent)
        }
        try {
            myWins
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val myWins: Unit
        private get() {
            binding!!.bidProgress.visibility = View.VISIBLE
            db!!.collection("Items").whereEqualTo("expiryDate", "Expired").get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    binding!!.bidProgress.visibility = View.GONE
                    if (queryDocumentSnapshots.isEmpty) {
                        binding!!.noItemsBid.visibility = View.VISIBLE
                        binding!!.winningsRv.visibility = View.GONE
                    } else {
                        itemList!!.clear()
                        for (documentSnapshot in queryDocumentSnapshots) {
                            val items = documentSnapshot.toObject(AuctionItem::class.java)
                            binding!!.noItemsBid.visibility = View.GONE
                            binding!!.winningsRv.visibility = View.VISIBLE
                            items.itemId = documentSnapshot.id
                            if (items.buyerId == auth!!.currentUser!!.uid) {
                                itemList!!.add(items)
                                return@addOnSuccessListener
                            }
                            if (itemList!!.isEmpty()) {
                                binding!!.noItemsBid.visibility = View.VISIBLE
                                binding!!.winningsRv.visibility = View.GONE
                                return@addOnSuccessListener
                            }
                        }
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Log.d(
                        "Error getting documents: ",
                        e.localizedMessage
                    )
                }
        }
}