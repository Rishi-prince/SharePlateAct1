package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.yourteam.shareplate.R
import java.util.Date

class ProviderScanFragment : Fragment(R.layout.fragment_provider_scan) {

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedId = result.contents
            view?.findViewById<TextInputEditText>(R.id.etScanInput)?.setText(scannedId)
            lookupReservation(scannedId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etScanInput = view.findViewById<TextInputEditText>(R.id.etScanInput)
        val btnLookup = view.findViewById<MaterialButton>(R.id.btnLookupReservation)
        val btnScanCamera = view.findViewById<MaterialButton>(R.id.btnScanCamera)
        val cardResult = view.findViewById<MaterialCardView>(R.id.cardReservationResult)

        cardResult.visibility = View.GONE

        btnScanCamera.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan student's QR pickup pass")
                setBeepEnabled(true)
                setOrientationLocked(true)
            }
            scanLauncher.launch(options)
        }

        btnLookup.setOnClickListener {
            val resId = etScanInput.text.toString().trim()
            if (resId.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a reservation ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lookupReservation(resId)
        }
    }

    private fun lookupReservation(resId: String) {
        val view = view ?: return
        val cardResult = view.findViewById<MaterialCardView>(R.id.cardReservationResult)
        val tvStatus = view.findViewById<TextView>(R.id.tvScanResultStatus)
        val tvDetails = view.findViewById<TextView>(R.id.tvScanResultDetails)
        val btnMarkCollected = view.findViewById<MaterialButton>(R.id.btnMarkCollected)

        FirebaseFirestore.getInstance().collection("reservations").document(resId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(requireContext(), "Reservation not found", Toast.LENGTH_SHORT).show()
                    cardResult.visibility = View.GONE
                    return@addOnSuccessListener
                }

                cardResult.visibility = View.VISIBLE
                val status = doc.getString("status") ?: "reserved"
                val studentId = doc.getString("studentId") ?: ""
                val listingId = doc.getString("listingId") ?: ""

                tvStatus.text = "Status: ${status.uppercase()}"
                tvDetails.text = "Student ID: $studentId\nListing ID: $listingId"

                if (status == "reserved") {
                    btnMarkCollected.isEnabled = true
                    btnMarkCollected.setOnClickListener {
                        FirebaseFirestore.getInstance().collection("reservations").document(resId)
                            .update("status", "collected")
                            .addOnSuccessListener {
                                // Write notification for student
                                val notifData = mapOf(
                                    "userId" to studentId,
                                    "title" to "Meal Collected! 🎉",
                                    "message" to "Your reservation has been marked as collected. Thank you for rescuing food!",
                                    "timestamp" to Timestamp(Date()),
                                    "read" to false
                                )
                                FirebaseFirestore.getInstance().collection("notifications").add(notifData)

                                Toast.makeText(requireContext(), "Marked as collected!", Toast.LENGTH_SHORT).show()
                                tvStatus.text = "Status: COLLECTED"
                                btnMarkCollected.isEnabled = false
                            }
                    }
                } else {
                    btnMarkCollected.isEnabled = false
                    btnMarkCollected.text = "Already ${status.replaceFirstChar { it.uppercase() }}"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
