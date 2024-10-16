package com.rsdevelopers.auctionhub.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.rsdevelopers.auctionhub.Activities.BidsInfoActivity
import com.rsdevelopers.auctionhub.Adapters.MyItemsAdapter
import com.rsdevelopers.auctionhub.Models.AuctionItem
import com.rsdevelopers.auctionhub.databinding.FragmentMyItemsBinding

class MyItemsFragment : Fragment() {
    var binding: FragmentMyItemsBinding? = null
    private var db: FirebaseFirestore? = null
    private var mAdapter: MyItemsAdapter? = null
    private var itemList: MutableList<AuctionItem>? = null
    private var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyItemsBinding.inflate(inflater, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        itemList = ArrayList()
        // Initialize RecyclerView
//        binding.myItemsRv.setHasFixedSize(true);
        binding!!.myItemsRv.layoutManager = LinearLayoutManager(activity)
        mAdapter = MyItemsAdapter(requireActivity(), itemList)
        binding!!.myItemsRv.adapter = mAdapter
        mAdapter!!.setOnItemClickListener { position: Int ->
            val items = (itemList as ArrayList<AuctionItem>).get(position)
            val intent = Intent(activity, BidsInfoActivity::class.java)
            intent.putExtra("itemId", items.itemId)
            intent.putExtra("mData", "myItems")
            startActivity(intent)
        }
        try {
            myItems
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding!!.root
    }

    private val myItems: Unit
        private get() {
            binding!!.bidProgress.visibility = View.VISIBLE
            db!!.collection("Items").whereEqualTo("sellerId", auth!!.currentUser!!.uid).get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    binding!!.bidProgress.visibility = View.GONE
                    if (queryDocumentSnapshots.isEmpty) {
                        binding!!.noItemsBid.visibility = View.VISIBLE
                        binding!!.myItemsRv.visibility = View.GONE
                    } else {
                        itemList!!.clear()
                        for (documentSnapshot in queryDocumentSnapshots) {
                            val items = documentSnapshot.toObject(AuctionItem::class.java)
                            binding!!.noItemsBid.visibility = View.GONE
                            binding!!.myItemsRv.visibility = View.VISIBLE
                            items.itemId = documentSnapshot.id
                            itemList!!.add(items)
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