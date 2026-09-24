package com.yourteam.shareplate.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.yourteam.shareplate.R
import com.yourteam.shareplate.data.model.Listing

class PickupPassFragment : Fragment(R.layout.fragment_pickup_pass) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reservationId = arguments?.getString("reservationId")
        if (reservationId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid pass", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val ivQRCode = view.findViewById<ImageView>(R.id.ivQRCode)
        val tvFoodType = view.findViewById<TextView>(R.id.tvPassFoodType)
        val tvPickupPoint = view.findViewById<TextView>(R.id.tvPassPickupPoint)
        val tvReservationId = view.findViewById<TextView>(R.id.tvPassReservationId)
        val btnBackToHome = view.findViewById<MaterialButton>(R.id.btnBackToHome)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelReservation)

        btnCancel.visibility = View.GONE
        tvReservationId.text = "Reservation ID: $reservationId"

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(reservationId, BarcodeFormat.QR_CODE, 400, 400)
            ivQRCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("reservations").document(reservationId)
            .get()
            .addOnSuccessListener { resDoc ->
                val status = resDoc.getString("status") ?: "reserved"
                if (status == "reserved") {
                    btnCancel.visibility = View.VISIBLE
                    btnCancel.setOnClickListener {
                        val listingId = resDoc.getString("listingId")
                        if (!listingId.isNullOrEmpty()) {
                            db.runTransaction { transaction ->
                                val listingRef = db.collection("listings").document(listingId)
                                val listingDoc = transaction.get(listingRef)
                                val remaining = listingDoc.getLong("remainingQuantity") ?: 0
                                
                                transaction.update(listingRef, "remainingQuantity", remaining + 1)
                                transaction.update(listingRef, "status", "active")
                                transaction.update(db.collection("reservations").document(reservationId), "status", "cancelled")
                                null
                            }.addOnSuccessListener {
                                Toast.makeText(requireContext(), "Reservation cancelled. Food returned to feed.", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack(R.id.homeFeedFragment, false)
                            }.addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to cancel: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val listingId = resDoc.getString("listingId")
                if (!listingId.isNullOrEmpty()) {
                    db.collection("listings").document(listingId)
                        .get()
                        .addOnSuccessListener { listingDoc ->
                            val listing = listingDoc.toObject(Listing::class.java)
                            if (listing != null) {
                                tvFoodType.text = listing.foodType
                                tvPickupPoint.text = "Pickup Point: ${listing.pickupPoint}"
                            }
                        }
                }
            }

        btnBackToHome.setOnClickListener {
            findNavController().navigate(R.id.action_pickupPassFragment_to_homeFeedFragment)
        }
    }
}
