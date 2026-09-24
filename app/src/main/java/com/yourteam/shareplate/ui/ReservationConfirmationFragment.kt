package com.yourteam.shareplate.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import java.util.Date
import java.util.UUID

class ReservationConfirmationFragment : Fragment(R.layout.fragment_reservation_confirmation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listingId = arguments?.getString("listingId")
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (listingId.isNullOrEmpty() || currentUser == null) {
            Toast.makeText(requireContext(), "Invalid reservation request", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val listingRef = db.collection("listings").document(listingId)
        val reservationId = UUID.randomUUID().toString()
        val reservationRef = db.collection("reservations").document(reservationId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(listingRef)
            val remaining = snapshot.getLong("remainingQuantity") ?: 0
            if (remaining <= 0) {
                throw IllegalAccessException("This listing is no longer available.")
            }
            transaction.update(listingRef, "remainingQuantity", remaining - 1)
            if (remaining - 1 == 0L) {
                transaction.update(listingRef, "status", "soldOut")
            }

            val reservationData = mapOf(
                "reservationId" to reservationId,
                "listingId" to listingId,
                "studentId" to currentUser.uid,
                "qrCode" to reservationId,
                "status" to "reserved",
                "createdAt" to Timestamp(Date())
            )
            transaction.set(reservationRef, reservationData)
            null
        }.addOnSuccessListener {
            val notifData = mapOf(
                "userId" to currentUser.uid,
                "title" to "Reservation Confirmed 🥗",
                "message" to "Your portion has been successfully reserved. Show your QR pass at pickup.",
                "timestamp" to Timestamp(Date()),
                "read" to false
            )
            db.collection("notifications").add(notifData)
                .addOnSuccessListener { Log.d("SharePlate", "Notification written successfully for user: ${currentUser.uid}") }
                .addOnFailureListener { Log.e("SharePlate", "Failed to write notification", it) }

            Toast.makeText(requireContext(), "Reservation confirmed!", Toast.LENGTH_SHORT).show()
            val bundle = Bundle().apply { putString("reservationId", reservationId) }
            findNavController().navigate(R.id.action_reservationConfirmationFragment_to_pickupPassFragment, bundle)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Reservation failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }
    }
}
