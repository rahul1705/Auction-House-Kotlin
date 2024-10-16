import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.rsdevelopers.auctionhub.Models.AuctionItem
import com.rsdevelopers.auctionhub.Models.OnItemClickListener
import com.rsdevelopers.auctionhub.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class BidAdapter(private val mContext: Context, private val mDataList: List<AuctionItem>?) :
    RecyclerView.Adapter<BidAdapter.MyViewHolder>() {

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bid_list_item, parent, false)
        return MyViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
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

    class MyViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var iName: TextView = itemView.findViewById(R.id.item_name_display)
        var iExpiry: TextView = itemView.findViewById(R.id.expire_time_rv)
        var iImage: ImageView = itemView.findViewById(R.id.item_image_rv)
        var mCountDownTimer: CountDownTimer? = null

        init {
            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition)
            }
        }

        fun bind(mContext: Context?, items: AuctionItem) {
            iName.text = items.itemName

            // Cancel any previous timer before starting a new one
            mCountDownTimer?.cancel()

            if (items.expiryDate != "Expired") {
                runTimer(items)
            }
            Glide.with(mContext!!).load(items.itemImage).into(iImage)
        }

        private fun runTimer(items: AuctionItem) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expiryDate: Date? = try {
                items.expiryDate?.let { format.parse(it) }
            } catch (e: ParseException) {
                e.printStackTrace()
                Toast.makeText(itemView.context, "No valid Date", Toast.LENGTH_SHORT).show()
                return
            }
            expiryDate?.let {
                val millisUntilFinished = it.time - System.currentTimeMillis()
                mCountDownTimer = object : CountDownTimer(millisUntilFinished, 1000) {
                    override fun onTick(millisUntilFinished1: Long) {
                        val days = millisUntilFinished1 / (1000 * 60 * 60 * 24)
                        val hours = millisUntilFinished1 / (1000 * 60 * 60) % 24
                        val minutes = millisUntilFinished1 / (1000 * 60) % 60
                        val seconds = millisUntilFinished1 / 1000 % 60
                        iExpiry.text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d:%02d:%02d",
                            days,
                            hours,
                            minutes,
                            seconds
                        )
                    }

                    override fun onFinish() {
                        items.expiryDate = "Expired"
                        if (items.buyerId != null) {
                            items.itemStatus = "Sold"
                        } else {
                            items.itemStatus = "Expired Unsold"
                        }
                        val db = FirebaseFirestore.getInstance()
                        items.itemId?.let {
                            db.collection("Items").document(it)
                                .update("expiryDate", "Expired")
                        }
                        if (items.buyerId != null) {
                            items.itemId?.let {
                                db.collection("Items").document(it)
                                    .update("itemStatus", "Sold")
                            }
                        } else {
                            items.itemId?.let {
                                db.collection("Items").document(it)
                                    .update("itemStatus", "Unsold")
                            }
                        }
                    }
                }.start()
            }
        }
    }
}
