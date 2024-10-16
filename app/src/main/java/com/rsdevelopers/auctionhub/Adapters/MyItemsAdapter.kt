package com.rsdevelopers.auctionhub.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rsdevelopers.auctionhub.Models.AuctionItem
import com.rsdevelopers.auctionhub.Models.OnItemClickListener
import com.rsdevelopers.auctionhub.R

class MyItemsAdapter(private val mContext: Context, private val mDataList: List<AuctionItem>?) :
    RecyclerView.Adapter<MyItemsAdapter.ItemsViewHolder>() {

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mylist_items, parent, false)
        return ItemsViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        val items = mDataList!![position]
        holder.bind(mContext, items)
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    // Update this function to convert lambda to OnItemClickListener
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        mListener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                listener(position)
            }
        }
    }

    class ItemsViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var iImage: ImageView = itemView.findViewById(R.id.mylist_item_img)
        var iName: TextView = itemView.findViewById(R.id.mylist_item_name)
        var iStatus: TextView = itemView.findViewById(R.id.item_status_mylist)

        init {
            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition)
            }
        }

        fun bind(mContext: Context?, items: AuctionItem) {
            iName.text = items.itemName
            iStatus.text = items.itemStatus
            Glide.with(mContext!!).load(items.itemImage).into(iImage)
        }
    }
}
