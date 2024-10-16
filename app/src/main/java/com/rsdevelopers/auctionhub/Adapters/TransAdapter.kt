package com.rsdevelopers.auctionhub.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rsdevelopers.auctionhub.Adapters.TransAdapter.TransViewHolder
import com.rsdevelopers.auctionhub.Models.Transactions
import com.rsdevelopers.auctionhub.R

class TransAdapter(var context: Context, var arrayList: ArrayList<Transactions>) :
    RecyclerView.Adapter<TransViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.wallet_items, parent, false)
        return TransViewHolder(v)
    }

    override fun onBindViewHolder(holder: TransViewHolder, position: Int) {
        val transactions = arrayList[position]
        val amount = "₹" + transactions.transAmount
        holder.trans_amount.text = amount
        holder.trans_time.text = transactions.transDate.toString()
        holder.trans_reason.text = transactions.transReason
        if (transactions.transType == "cr") {
            holder.trans_amount.setTextColor(holder.trans_amount.context.getColor(R.color.green))
        } else {
            holder.trans_amount.setTextColor(holder.trans_amount.context.getColor(R.color.red))
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class TransViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var trans_time: TextView
        var trans_amount: TextView
        var trans_reason: TextView

        init {
            trans_amount = itemView.findViewById(R.id.trans_amount)
            trans_time = itemView.findViewById(R.id.trans_time)
            trans_reason = itemView.findViewById(R.id.trans_reason)
        }
    }
}