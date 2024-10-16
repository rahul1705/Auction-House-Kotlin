package com.rsdevelopers.auctionhub.Fragments

import BidAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.rsdevelopers.auctionhub.Activities.BidsInfoActivity
import com.rsdevelopers.auctionhub.Models.AuctionItem
import com.rsdevelopers.auctionhub.databinding.FragmentBidBinding
import java.util.*

class BidFragment : Fragment() {
    var binding: FragmentBidBinding? = null
    private var db: FirebaseFirestore? = null
    private var mAdapter: BidAdapter? = null
    private var itemList: MutableList<AuctionItem>? = null
    private var auth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBidBinding.inflate(inflater, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        itemList = ArrayList()
        // Initialize RecyclerView
//        binding.bidRecycler.setHasFixedSize(true);
        binding!!.bidRecycler.layoutManager = LinearLayoutManager(activity)
        mAdapter = BidAdapter(requireActivity(), itemList)
        binding!!.bidRecycler.adapter = mAdapter
        mAdapter!!.setOnItemClickListener { position: Int ->
            val items = (itemList as ArrayList<AuctionItem>).get(position)
            val intent = Intent(activity, BidsInfoActivity::class.java)
            intent.putExtra("itemId", items.itemId)
            intent.putExtra("mData", "allBids")
            startActivity(intent)
        }
        try {
            auctionData
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding!!.root
    }

    @get:SuppressLint("NotifyDataSetChanged")
    private val auctionData: Unit
        private get() {
            binding!!.bidProgress.visibility = View.VISIBLE
            db!!.collection("Items").whereNotEqualTo("expiryDate", "Expired")
                .orderBy("expiryDate", Query.Direction.ASCENDING).get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    binding!!.bidProgress.visibility = View.GONE
                    if (queryDocumentSnapshots.isEmpty) {
                        binding!!.bidRecycler.visibility = View.GONE
                        binding!!.noItemsBid.visibility = View.VISIBLE
                    } else {
                        itemList!!.clear()
                        for (documentSnapshot in queryDocumentSnapshots) {
                            val items = documentSnapshot.toObject(AuctionItem::class.java)
                            if (items.sellerId != Objects.requireNonNull(
                                    auth!!.currentUser
                                )?.uid
                            ) {
                                itemList!!.add(items)
                                binding!!.noItemsBid.visibility = View.GONE
                                binding!!.bidRecycler.visibility = View.VISIBLE
                            }
                            if (itemList!!.isEmpty()) {
                                binding!!.bidRecycler.visibility = View.GONE
                                binding!!.noItemsBid.visibility = View.VISIBLE
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