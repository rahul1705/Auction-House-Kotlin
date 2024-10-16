package com.rsdevelopers.auctionhub.Activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.rsdevelopers.auctionhub.Adapters.TransAdapter
import com.rsdevelopers.auctionhub.Models.FirestoreOps
import com.rsdevelopers.auctionhub.Models.Transactions
import com.rsdevelopers.auctionhub.Models.Users
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.ActivityWalletBinding
import org.json.JSONObject
import java.util.*

class WalletActivity : AppCompatActivity(), PaymentResultListener {
    var binding: ActivityWalletBinding? = null
    private var arrayList: ArrayList<Transactions>? = null
    private var adapter: TransAdapter? = null
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var amount = 0.0
    private var users: Users? = null
    private var listenerRegistration: ListenerRegistration? = null
    private var firestoreOps: FirestoreOps? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        Checkout.preload(this@WalletActivity)
        firestoreOps = FirestoreOps()
        setSupportActionBar(binding!!.walletToolbar)
        binding!!.walletToolbar.setNavigationIcon(R.drawable.ic_back)
        binding!!.walletToolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding!!.walletRv.setHasFixedSize(true)
        binding!!.walletRv.layoutManager = LinearLayoutManager(this@WalletActivity)
        arrayList = ArrayList()
        adapter = TransAdapter(this@WalletActivity, arrayList!!)
        binding!!.walletRv.adapter = adapter
        cardDetails
        transHistory
        binding!!.addBalBtn.setOnClickListener { v: View? -> addWalletAmount() }
    }

    private val cardDetails: Unit
        get() {
            firestoreOps!!.getWalletData(this@WalletActivity) { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    users = documentSnapshot.toObject(Users::class.java)
                    if (users != null) {
                        val bal = "₹ " + users!!.balance
                        binding!!.userName.text = users!!.name
                        binding!!.availBal.text = bal
                    }
                }
            }
        }

    private fun addWalletAmount() {
        val builder = AlertDialog.Builder(this@WalletActivity)
        val inflater = layoutInflater
        val v = inflater.inflate(R.layout.amount_add_alert, null)
        val layout_amount = v.findViewById<TextInputLayout>(R.id.amount_add_layout)
        val et_amount_add = v.findViewById<TextInputEditText>(R.id.et_add_amount)

//        Upload code here...
        builder.setPositiveButton(R.string.add) { dialog: DialogInterface?, which: Int ->
            val amount_str = et_amount_add.text.toString().trim { it <= ' ' }
            amount = try {
                amount_str.toDouble() * 100
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                return@setPositiveButton
            }
            if (amount_str.isEmpty()) {
                layout_amount.error = "Enter amount in ₹"
            } else {
                startPayment()
            }
        }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.setView(v).setCancelable(false).show()
    }

    fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("your razorpay key here...")

        //        checkout.setImage(R.drawable.logo);
        try {
            val options = JSONObject()
            options.put("name", users!!.name)
            options.put("description", "Add Money to Wallet")
            options.put("send_sms_hash", true)
            options.put("allow_rotation", true)
            options.put("currency", "INR")
            options.put("amount", amount)
            options.put("prefill.email", users!!.email)
            options.put("prefill.contact", users!!.mobile)
            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)
            checkout.open(this@WalletActivity, options)
        } catch (e: Exception) {
            Toast.makeText(
                this@WalletActivity,
                "Error in starting Razorpay Checkout " + e.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val transHistory: Unit
        @SuppressLint("NotifyDataSetChanged")
        get() {
            listenerRegistration = db!!.collection("Users").document(auth!!.currentUser!!.uid)
                .collection("Transactions")
                .orderBy("transDate", Query.Direction.DESCENDING)
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.e("Firestore error: ", error.message!!)
                        return@addSnapshotListener
                    }
                    assert(value != null)
                    if (value!!.isEmpty) {
                        binding!!.walletRv.visibility = View.GONE
                        binding!!.noTrans.visibility = View.VISIBLE
                    } else {
                        binding!!.walletRv.visibility = View.VISIBLE
                        binding!!.noTrans.visibility = View.GONE
                        arrayList!!.clear()
                        for (document in value.documents) {
                            val transactions = document.toObject(Transactions::class.java)
                            if (transactions != null) {
                                try {
                                    transactions.transId = document.id
                                    arrayList!!.add(transactions)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@WalletActivity,
                                        e.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@WalletActivity,
                                    "No Data found!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }
        }

    override fun onPaymentSuccess(s: String) {
        val am = (amount / 100 + users!!.balance.toFloat()).toString()
        firestoreOps!!.addWallet(applicationContext, am)
        val transactions = Transactions(Date(), amount / 100, "cr", "Added to Wallet", "")
        firestoreOps!!.addTrans(applicationContext, transactions)
        Toast.makeText(this, "Success payment", Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentError(i: Int, s: String) {
        Toast.makeText(this, "Fail to make the payment!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (listenerRegistration != null) {
            listenerRegistration!!.remove()
        }
        firestoreOps!!.unregisterListener()
    }
}