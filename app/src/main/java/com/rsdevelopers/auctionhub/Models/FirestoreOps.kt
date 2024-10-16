package com.rsdevelopers.auctionhub.Models

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirestoreOps {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    fun unregisterListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    // Add user's balance data to Firestore
    fun addWallet(context: Context?, wallet_amount: String?) {
        db.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update("balance", wallet_amount)
            .addOnSuccessListener {
                // User's balance data added to Firestore
                Log.e("Add Wallet", "Amount added to wallet")
            }
            .addOnFailureListener { e: Exception ->
                // Handle error
                Toast.makeText(context, "Add Wallet error: " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    // Retrieve wallet data from Firestore using callback
    fun getWalletData(context: Context?, callback: (DocumentSnapshot) -> Unit) {
        listenerRegistration = db.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                value?.let { callback(it) }
            }
    }

    // Retrieve user data from Firestore using callback
    fun getUserData(context: Context?, callback: (DocumentSnapshot?) -> Unit) {
        db.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(task.result)
                } else {
                    Toast.makeText(
                        context,
                        "Error: " + task.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Add transaction details to Firestore
    fun addTrans(context: Context?, data: Transactions?) {
        db.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("Transactions")
            .add(data!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("Add Trans", "Transaction added successfully")
                } else {
                    Toast.makeText(context, "Failed to add transaction", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
