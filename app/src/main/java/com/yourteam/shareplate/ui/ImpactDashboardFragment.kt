package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import java.util.Locale

class ImpactDashboardFragment : Fragment(R.layout.fragment_impact_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvMeals = view.findViewById<TextView>(R.id.tvMealsRescuedCount)
        val tvCo2 = view.findViewById<TextView>(R.id.tvCo2Avoided)
        val tvWater = view.findViewById<TextView>(R.id.tvWaterSaved)
        val btnLeaderboard = view.findViewById<MaterialButton>(R.id.btnViewLeaderboard)

        btnLeaderboard.setOnClickListener {
            findNavController().navigate(R.id.action_impactDashboardFragment_to_leaderboardFragment)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("studentId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.size()
                tvMeals.text = count.toString()
                
                val co2Kg = count * 2.5
                val waterLitres = count * 500

                tvCo2.text = "${String.format(Locale.getDefault(), "%.1f", co2Kg)} kg"
                tvWater.text = "$waterLitres L"
            }
    }
}
