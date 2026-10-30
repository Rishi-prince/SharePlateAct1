package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import com.yourteam.shareplate.ai.FoodListingAssistant
import com.yourteam.shareplate.data.model.Listing
import java.util.Date

class CreateListingFragment : Fragment(R.layout.fragment_create_listing) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBack = view.findViewById<TextView>(R.id.tvBackCreate)
        tvBack.setOnClickListener { findNavController().popBackStack() }

        val layoutAccessDenied = view.findViewById<LinearLayout>(R.id.layoutAccessDenied)
        val layoutProviderForm = view.findViewById<LinearLayout>(R.id.layoutProviderForm)

        val etAiPrompt = view.findViewById<TextInputEditText>(R.id.etAiPrompt)
        val btnAiAssist = view.findViewById<MaterialButton>(R.id.btnAiAssist)

        val etFoodType = view.findViewById<TextInputEditText>(R.id.etFoodType)
        val etQuantity = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val etPickupPoint = view.findViewById<TextInputEditText>(R.id.etPickupPoint)
        val rbVeg = view.findViewById<RadioButton>(R.id.rbVeg)
        val rbNonVeg = view.findViewById<RadioButton>(R.id.rbNonVeg)
        val btnSubmit = view.findViewById<MaterialButton>(R.id.btnSubmitListing)

        layoutProviderForm.visibility = View.GONE
        layoutAccessDenied.visibility = View.GONE

        btnAiAssist.setOnClickListener {
            val prompt = etAiPrompt.text.toString().trim()
            if (prompt.isEmpty()) {
                Toast.makeText(requireContext(), "Please type a food description first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parsed = FoodListingAssistant.parseDescription(prompt)
            etFoodType.setText(parsed.foodType)
            etQuantity.setText(parsed.quantity.toString())
            etPickupPoint.setText(parsed.pickupPoint)
            if (parsed.isVeg) {
                rbVeg.isChecked = true
            } else {
                rbNonVeg.isChecked = true
            }

            Toast.makeText(requireContext(), "✨ AI Auto-Filled (${parsed.confidenceScore}% Confidence)", Toast.LENGTH_SHORT).show()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.action_createListingFragment_to_loginFragment)
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "student"
                if (role == "provider") {
                    layoutProviderForm.visibility = View.VISIBLE
                } else {
                    layoutAccessDenied.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                layoutAccessDenied.visibility = View.VISIBLE
            }

        btnSubmit.setOnClickListener {
            val foodType = etFoodType.text.toString().trim()
            val qtyStr = etQuantity.text.toString().trim()
            val pickupPoint = etPickupPoint.text.toString().trim()

            if (foodType.isEmpty() || qtyStr.isEmpty() || pickupPoint.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = qtyStr.toIntOrNull() ?: 0
            if (quantity <= 0) {
                etQuantity.error = "Enter valid quantity"
                return@setOnClickListener
            }

            val isVeg = rbVeg.isChecked
            val now = Date()
            val expiry = Date(now.time + 4 * 60 * 60 * 1000)

            val listing = Listing(
                providerId = currentUser.uid,
                foodType = foodType,
                isVeg = isVeg,
                quantity = quantity,
                remainingQuantity = quantity,
                pickupPoint = pickupPoint,
                pickupWindowStart = Timestamp(now),
                pickupWindowEnd = Timestamp(expiry),
                expiryTime = Timestamp(expiry),
                status = "active"
            )

            btnSubmit.isEnabled = false
            FirebaseFirestore.getInstance().collection("listings")
                .add(listing)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Listing published successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
