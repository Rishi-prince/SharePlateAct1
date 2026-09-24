package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import com.yourteam.shareplate.data.UserSessionManager
import com.yourteam.shareplate.data.model.Listing
import java.text.SimpleDateFormat
import java.util.Locale

class ListingDetailsFragment : Fragment(R.layout.fragment_listing_details) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBack = view.findViewById<TextView>(R.id.tvBack)
        tvBack.setOnClickListener { findNavController().popBackStack() }

        val listingId = arguments?.getString("listingId")
        if (listingId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid listing", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val tvFoodType = view.findViewById<TextView>(R.id.tvDetailFoodType)
        val tvStatus = view.findViewById<TextView>(R.id.tvDetailStatus)
        val tvQuantity = view.findViewById<TextView>(R.id.tvDetailQuantity)
        val tvPickupPoint = view.findViewById<TextView>(R.id.tvDetailPickupPoint)
        val tvPickupWindow = view.findViewById<TextView>(R.id.tvDetailPickupWindow)
        val btnReserve = view.findViewById<MaterialButton>(R.id.btnReserve)

        btnReserve.visibility = View.GONE

        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        db.collection("listings").document(listingId)
            .get()
            .addOnSuccessListener { doc ->
                val listing = doc.toObject(Listing::class.java)?.copy(listingId = doc.id)
                if (listing == null) {
                    Toast.makeText(requireContext(), "Listing not found", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return@addOnSuccessListener
                }

                tvFoodType.text = listing.foodType
                tvStatus.text = "Status: ${listing.status.uppercase()}"
                tvQuantity.text = "Remaining Portions: ${listing.remainingQuantity} / ${listing.quantity}"
                tvPickupPoint.text = "Pickup Point: ${listing.pickupPoint}"

                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val startStr = listing.pickupWindowStart?.toDate()?.let { sdf.format(it) } ?: "--"
                val endStr = listing.pickupWindowEnd?.toDate()?.let { sdf.format(it) } ?: "--"
                tvPickupWindow.text = "Pickup Window: $startStr - $endStr"

                UserSessionManager.fetchUserRole { role ->
                    if (role == "student" && currentUser != null) {
                        db.collection("reservations")
                            .whereEqualTo("studentId", currentUser.uid)
                            .whereEqualTo("listingId", listingId)
                            .whereEqualTo("status", "reserved")
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val existingResId = querySnapshot.documents[0].id
                                    btnReserve.visibility = View.VISIBLE
                                    btnReserve.isEnabled = true
                                    btnReserve.text = "View Pickup Pass"
                                    btnReserve.setBackgroundColor(android.graphics.Color.parseColor("#17324D"))
                                    btnReserve.setOnClickListener {
                                        val bundle = Bundle().apply { putString("reservationId", existingResId) }
                                        findNavController().navigate(R.id.action_listingDetailsFragment_to_pickupPassFragment, bundle)
                                    }
                                } else if (listing.remainingQuantity > 0 && listing.status == "active") {
                                    btnReserve.visibility = View.VISIBLE
                                    btnReserve.isEnabled = true
                                    btnReserve.text = "Reserve Portion"
                                    btnReserve.setBackgroundColor(android.graphics.Color.parseColor("#16A34A"))
                                    btnReserve.setOnClickListener {
                                        val bundle = Bundle().apply { putString("listingId", listingId) }
                                        findNavController().navigate(R.id.action_listingDetailsFragment_to_reservationConfirmationFragment, bundle)
                                    }
                                } else {
                                    btnReserve.visibility = View.VISIBLE
                                    btnReserve.isEnabled = false
                                    btnReserve.text = "Sold Out / Unavailable"
                                }
                            }
                    } else {
                        btnReserve.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load listing: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
