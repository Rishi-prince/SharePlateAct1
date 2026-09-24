package com.yourteam.shareplate.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import com.yourteam.shareplate.data.model.NotificationItem

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewNotifications)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyNotifs)
        val progressBar = view.findViewById<CircularProgressIndicator>(R.id.progressBarNotifs)
        val btnTestNotif = view.findViewById<MaterialButton>(R.id.btnTestNotif)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = NotificationAdapter(emptyList())
        recyclerView.adapter = adapter

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        btnTestNotif.setOnClickListener {
            val notif = mapOf(
                "userId" to currentUser.uid,
                "title" to "Test Alert 🔔",
                "message" to "This is a manually triggered test notification.",
                "timestamp" to com.google.firebase.Timestamp(java.util.Date()),
                "read" to false
            )
            FirebaseFirestore.getInstance().collection("notifications")
                .add(notif)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Test notification added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }

        progressBar.visibility = View.VISIBLE

        FirebaseFirestore.getInstance().collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                if (error != null) {
                    Log.e("SharePlate", "Error listening to notifications", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    Log.d("SharePlate", "Notifications snapshot size: ${snapshot.size()}")
                    val list = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val userId = doc.getString("userId") ?: ""
                        val title = doc.getString("title") ?: "Alert"
                        val message = doc.getString("message") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")
                        val read = doc.getBoolean("read") ?: false

                        NotificationItem(id, userId, title, message, timestamp, read)
                    }.sortedByDescending { it.timestamp?.toDate() }

                    adapter.updateList(list)
                    if (list.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
    }
}
