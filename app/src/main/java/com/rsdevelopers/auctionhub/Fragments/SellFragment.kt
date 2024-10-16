package com.rsdevelopers.auctionhub.Fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.rsdevelopers.auctionhub.Models.AuctionItem
import com.rsdevelopers.auctionhub.R
import com.rsdevelopers.auctionhub.databinding.FragmentSellBinding
import java.text.SimpleDateFormat
import java.util.*

class SellFragment : Fragment() {
    var binding: FragmentSellBinding? = null
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var dialog: ProgressDialog? = null
    private var imageUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSellBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(context)
        dialog!!.setMessage("Adding Details...")
        dialog!!.setCancelable(false)

        // Set an OnClickListener on the EditText to show the date and time picker
        binding!!.etExpire.setOnClickListener { showDateTimePicker() }
        binding!!.ivUploadPic.setOnClickListener { v: View? -> imgContent.launch("image/*") }
        binding!!.tvUploadPic.setOnClickListener { v: View? -> imgContent.launch("image/*") }
        binding!!.btnSell.setOnClickListener { v: View? -> uploadData() }
        return binding!!.root
    }

    private fun uploadData() {
        val itemName = binding!!.etItemName.text.toString().trim { it <= ' ' }
        val expDate = binding!!.etExpire.text.toString().trim { it <= ' ' }
        val minAmount = binding!!.etMinAmount.text.toString().trim { it <= ' ' }
        val itemDesc = binding!!.etDesc.text.toString().trim { it <= ' ' }
        if (itemName.isEmpty()) {
            binding!!.itemNameLayout.error = "Please enter name"
        } else if (expDate.isEmpty()) {
            binding!!.expireLayout.error = "Select expiry date and time"
        } else if (minAmount.isEmpty()) {
            binding!!.minAmountLayout.error = "Please enter minimum bid amount"
        } else if (itemDesc.isEmpty()) {
            binding!!.descLayout.error = "Please enter item description"
        } else if (imageUri == null) {
            Toast.makeText(context, "Please select item image", Toast.LENGTH_SHORT).show()
        } else {
            dialog!!.show()
            val currentBid: Double
            currentBid = try {
                minAmount.toDouble()
            } catch (e: NumberFormatException) {
                // Handle the exception here, e.g. display an error message to the user
                Toast.makeText(context, "Please enter a valid bid amount", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            val sellerId = auth!!.currentUser!!.uid
            val ref = FirebaseStorage.getInstance().reference.child(
                "items/" + UUID.randomUUID().toString()
            )
            val uploadTask = ref.putFile(imageUri!!)
            uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                ref.downloadUrl
            }
                .addOnCompleteListener { task: Task<Uri> ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val docRef = db!!.collection("Items").document()
                        val item = AuctionItem(
                            docRef.id,
                            itemName,
                            downloadUri.toString(),
                            itemDesc,
                            expDate,
                            currentBid,
                            sellerId,
                            Date(),
                            "Bid Running"
                        )
                        docRef.set(item).addOnCompleteListener { task1: Task<Void?> ->
                            dialog!!.dismiss()
                            binding!!.etItemName.setText("")
                            binding!!.etExpire.setText("")
                            binding!!.etMinAmount.setText("")
                            binding!!.etDesc.setText("")
                            binding!!.ivUploadPic.setImageResource(R.drawable.ic_upload)
                            if (task1.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Item added successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Item failed to upload", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        // Create a DatePickerDialog to allow the seller to select the expiry date
        val datePickerDialog = DatePickerDialog(
            context!!,
            { datePicker: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Update the calendar with the selected date
                calendar[Calendar.YEAR] = year
                calendar[Calendar.MONTH] = monthOfYear
                calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

                // Show a TimePickerDialog to allow the seller to select the expiry time
                val timePickerDialog = TimePickerDialog(
                    context,
                    { timePicker: TimePicker?, hourOfDay: Int, minute: Int ->
                        // Update the calendar with the selected time
                        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
                        calendar[Calendar.MINUTE] = minute
                        calendar[Calendar.SECOND] = 0

//                show it in textbox and set value in a Date object.
                        val dateFormat =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        //                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat., Locale.getDefault());
                        val expiryDate = dateFormat.format(calendar.time)
                        binding!!.etExpire.setText(expiryDate)
                    },
                    calendar[Calendar.HOUR_OF_DAY],
                    calendar[Calendar.MINUTE],
                    true
                )
                timePickerDialog.show()
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        calendar.add(Calendar.DATE, 1)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    var imgContent = registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { result ->
        if (result != null) {
            binding!!.ivUploadPic.setImageURI(result)
            imageUri = result
        }
    }
}