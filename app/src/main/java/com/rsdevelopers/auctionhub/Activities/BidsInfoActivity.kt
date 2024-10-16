package com.rsdevelopers.auctionhub.Activities

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.rsdevelopers.auctionhub.Models.AuctionItem
import com.rsdevelopers.auctionhub.Models.FirestoreOps
import com.rsdevelopers.auctionhub.Models.Transactions
import com.rsdevelopers.auctionhub.Models.Users
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.ActivityBidsInfoBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class BidsInfoActivity : AppCompatActivity() {
    var binding: ActivityBidsInfoBinding? = null
    private var items: AuctionItem? = null
    private var db: FirebaseFirestore? = null
    private var mCountDownTimer: CountDownTimer? = null
    private var listenerRegistration: ListenerRegistration? = null
    private var firestoreOps: FirestoreOps? = null
    private var users: Users? = null
    private var userBalance = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBidsInfoBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        val itemId = intent.getStringExtra("itemId")
        val mData = intent.getStringExtra("mData")
        db = FirebaseFirestore.getInstance()
        firestoreOps = FirestoreOps()
        setSupportActionBar(binding!!.infoToolbar)
        binding!!.infoToolbar.setNavigationIcon(R.drawable.ic_back)
        binding!!.infoToolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        getDetailsFromDB(itemId, mData)
        binding!!.btnBid.setOnClickListener { v: View? -> updateCurrentBid(itemId) }
        if (mData != "allBids") {
            binding!!.bottomLayout.visibility = View.GONE
        }
    }

    private fun updateCurrentBid(itemId: String?) {
        val bidAmount = binding!!.bidAmount.text.toString().trim { it <= ' ' }
        val currentBid: Double
        currentBid = try {
            bidAmount.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(
                this@BidsInfoActivity,
                "Please enter a valid bid amount",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        firestoreOps!!.getWalletData(applicationContext) { documentSnapshot: DocumentSnapshot ->
            if (documentSnapshot.exists()) {
                users = documentSnapshot.toObject(Users::class.java)
                assert(users != null)
                userBalance = users!!.balance.toDouble()
            }
        }
        if (currentBid <= items!!.currentBid) {
            Toast.makeText(
                this,
                "Amount should be more than current bid amount!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (userBalance < currentBid) {
            Toast.makeText(this, "Not enough amount in your wallet!", Toast.LENGTH_SHORT).show()
        } else {
            val updateBid: MutableMap<String, Any?> = HashMap()
            updateBid["currentBid"] = currentBid
            updateBid["itemId"] = itemId
            updateBid["buyerId"] = FirebaseAuth.getInstance().currentUser!!.uid
            db!!.collection("Items").document(itemId!!).update(updateBid)
                .addOnCompleteListener { task: Task<Void?> ->
                    binding!!.bidAmount.setText("")
                    if (task.isSuccessful) {
                        firestoreOps!!.addWallet(
                            applicationContext,
                            (userBalance - currentBid).toString()
                        )
                        val transactions =
                            Transactions(Date(), currentBid, "dr", "Bid on item", itemId)
                        firestoreOps!!.addTrans(applicationContext, transactions)
                        Toast.makeText(
                            this@BidsInfoActivity,
                            "Bid Placed Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@BidsInfoActivity,
                            "Failed to place bid!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun getDetailsFromDB(itemId: String?, mData: String?) {
        listenerRegistration = db!!.collection("Items").document(itemId!!)
            .addSnapshotListener { value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Toast.makeText(this@BidsInfoActivity, error.message, Toast.LENGTH_SHORT).show()
                }
                if (value != null && value.exists()) {
                    items = value.toObject(AuctionItem::class.java)
                    if (items != null) {
                        Glide.with(this@BidsInfoActivity).load(items!!.itemImage)
                            .into(binding!!.itemImageD)
                        binding!!.itemNameD.text = items!!.itemName
                        binding!!.itemDescD.text = items!!.itemDesc
                        val amount = "₹ " + items!!.currentBid
                        binding!!.itemMinD.text = amount
                        binding!!.infoToolbar.title = items!!.itemName
                        when (mData) {
                            "myItems", "allBids" -> if (items!!.expiryDate == "Expired") {
                                binding!!.expireTimer.text = items!!.expiryDate
                            } else {
                                runTimer(itemId)
                            }
                            "myWins" -> {
                                val win = "Congratulations! You are the winner \uD83C\uDF89"
                                binding!!.expireTimer.text = win
                                binding!!.expireTimer.setTextColor(getColor(R.color.gold))
                                binding!!.textView15.text = getString(R.string.purchased_amount)
                            }
                        }
                    }
                }
            }
    }

    private fun runTimer(itemId: String?) {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var expiryDate: Date? = null
        expiryDate = try {
            format.parse(items!!.expiryDate)
        } catch (e: ParseException) {
            e.printStackTrace()
            return
        }
        if (expiryDate != null) {
            val millisUntilFinished = expiryDate.time - System.currentTimeMillis()
            mCountDownTimer = object : CountDownTimer(millisUntilFinished, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // Update UI with the remaining time
                    val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                    val hours = millisUntilFinished / (1000 * 60 * 60) % 24
                    val minutes = millisUntilFinished / (1000 * 60) % 60
                    val seconds = millisUntilFinished / 1000 % 60
                    val timeLeft = String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d:%02d",
                        days,
                        hours,
                        minutes,
                        seconds
                    )
                    val info = "Expired in: $timeLeft"
                    binding!!.expireTimer.text = info
                }

                override fun onFinish() {
                    // Handle the expiry of the auction item
                    db!!.collection("Items").document(itemId!!).update("expiryDate", "Expired")
                    if (items!!.buyerId != null) {
                        db!!.collection("Items").document(itemId).update("itemStatus", "Sold")
                    } else {
                        db!!.collection("Items").document(itemId).update("itemStatus", "Unsold")
                    }
                }
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // cancel the countdown timer to avoid memory leaks
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
        if (listenerRegistration != null) {
            listenerRegistration!!.remove()
        }
    }
}