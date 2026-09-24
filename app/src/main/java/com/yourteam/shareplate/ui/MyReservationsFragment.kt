package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import java.text.SimpleDateFormat
import java.util.Locale

class MyReservationsFragment : Fragment(R.layout.fragment_my_reservations) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var btnRetry: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMyReservations)
        layoutEmpty = view.findViewById(R.id.layoutEmptyReservations)
        layoutError = view.findViewById(R.id.layoutErrorReservations)
        progressBar = view.findViewById(R.id.progressBarReservations)
        btnRetry = view.findViewById(R.id.btnRetryReservations)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        btnRetry.setOnClickListener { loadReservations() }

        loadReservations()
    }

    private fun loadReservations() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) return

        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
        layoutError.visibility = View.GONE
        recyclerView.visibility = View.GONE

        FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("studentId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                val docs = snapshot.documents.map { doc ->
                    val reservationId = doc.id
                    val status = doc.getString("status") ?: "reserved"
                    val createdAt = doc.getTimestamp("createdAt")
                    ReservationModel(reservationId, status, createdAt)
                }.sortedByDescending { it.createdAt?.toDate() }

                recyclerView.adapter = ReservationAdapter(docs) { resId, status ->
                    if (status == "reserved") {
                        val bundle = Bundle().apply { putString("reservationId", resId) }
                        findNavController().navigate(R.id.action_myReservationsFragment_to_pickupPassFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "Pass is already $status", Toast.LENGTH_SHORT).show()
                    }
                }

                if (docs.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                layoutError.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
    }

    data class ReservationModel(val reservationId: String, val status: String, val createdAt: com.google.firebase.Timestamp?)

    class ReservationAdapter(
        private val items: List<ReservationModel>,
        private val onClick: (String, String) -> Unit
    ) : RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvInfo: TextView = view.findViewById(R.id.tvResListingInfo)
            val tvStatus: TextView = view.findViewById(R.id.tvResStatus)
            val tvDate: TextView = view.findViewById(R.id.tvResDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvInfo.text = "Reservation: ${item.reservationId.take(8)}..."
            holder.tvStatus.text = item.status.uppercase()
            
            // Color + text label for status badges
            when (item.status) {
                "reserved" -> {
                    holder.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#E6FFFA"))
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#0F766E"))
                }
                "collected" -> {
                    holder.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#F0FDF4"))
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"))
                }
                else -> {
                    holder.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FEF3C7"))
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#D97706"))
                }
            }

            val date = item.createdAt?.toDate()
            holder.tvDate.text = if (date != null) SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(date) else ""

            holder.itemView.setOnClickListener { onClick(item.reservationId, item.status) }
        }

        override fun getItemCount() = items.size
    }
}
