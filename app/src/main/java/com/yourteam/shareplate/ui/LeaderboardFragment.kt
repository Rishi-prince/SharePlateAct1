package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import java.util.Locale

class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewLeaderboard)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val db = FirebaseFirestore.getInstance()

        // Fetch users and reservations to rank students by meals rescued
        db.collection("users").whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener { usersSnap ->
                db.collection("reservations")
                    .get()
                    .addOnSuccessListener { resSnap ->
                        val rescueCounts = mutableMapOf<String, Int>()
                        for (doc in resSnap.documents) {
                            val studentId = doc.getString("studentId") ?: continue
                            rescueCounts[studentId] = (rescueCounts[studentId] ?: 0) + 1
                        }

                        val leaderboardList = usersSnap.documents.map { userDoc ->
                            val name = userDoc.getString("name") ?: "Student"
                            val uid = userDoc.id
                            val count = rescueCounts[uid] ?: 0
                            LeaderboardItem(name, count)
                        }.sortedByDescending { it.mealsRescued }

                        recyclerView.adapter = LeaderboardAdapter(leaderboardList)
                    }
            }
    }

    data class LeaderboardItem(val name: String, val mealsRescued: Int)

    class LeaderboardAdapter(private val items: List<LeaderboardItem>) :
        RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRank: TextView = view.findViewById(R.id.tvRank)
            val tvName: TextView = view.findViewById(R.id.tvRescuerName)
            val tvImpact: TextView = view.findViewById(R.id.tvRescuerImpact)
            val tvMeals: TextView = view.findViewById(R.id.tvMealsCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvRank.text = "${position + 1}"
            holder.tvName.text = item.name.replaceFirstChar { it.uppercase() }
            holder.tvMeals.text = "${item.mealsRescued} Meals"
            val co2 = item.mealsRescued * 2.5
            holder.tvImpact.text = "CO₂ Saved: ${String.format(Locale.getDefault(), "%.1f", co2)} kg"
        }

        override fun getItemCount() = items.size
    }
}
