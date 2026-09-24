package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R

class ImpactDashboardFragment : Fragment(R.layout.fragment_impact_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvMeals = view.findViewById<TextView>(R.id.tvMealsRescuedCount)
        val tvWaste = view.findViewById<TextView>(R.id.tvWasteAvoided)

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("studentId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.size()
                tvMeals.text = count.toString()
                val wasteKg = count * 0.5 // assuming 0.5 kg per meal
                tvWaste.text = "Estimated Waste Avoided: $wasteKg kg"
            }
    }
}
