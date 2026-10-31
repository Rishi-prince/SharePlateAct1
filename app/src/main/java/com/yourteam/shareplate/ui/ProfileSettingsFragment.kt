package com.yourteam.shareplate.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.R
import com.yourteam.shareplate.data.UserSessionManager
import java.util.Locale

class ProfileSettingsFragment : Fragment(R.layout.fragment_profile_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvRole = view.findViewById<TextView>(R.id.tvUserRole)
        val tvStat1Val = view.findViewById<TextView>(R.id.tvProfileStat1Value)
        val tvStat1Label = view.findViewById<TextView>(R.id.tvProfileStat1Label)
        val tvStat2Val = view.findViewById<TextView>(R.id.tvProfileStat2Value)
        val tvStat2Label = view.findViewById<TextView>(R.id.tvProfileStat2Label)

        val tvSettingEditProfile = view.findViewById<TextView>(R.id.tvSettingEditProfile)
        val tvSettingDiet = view.findViewById<TextView>(R.id.tvSettingDiet)
        val tvSettingNotifs = view.findViewById<TextView>(R.id.tvSettingNotifications)
        val tvSettingHelp = view.findViewById<TextView>(R.id.tvSettingHelp)
        val btnSignOut = view.findViewById<MaterialButton>(R.id.btnSignOut)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.action_profileSettingsFragment_to_loginFragment)
            return
        }

        tvEmail.text = currentUser.email ?: "User"

        val prefs = requireContext().getSharedPreferences("shareplate_prefs", android.content.Context.MODE_PRIVATE)
        val savedDiet = prefs.getString("diet_pref", "All Foods")
        tvSettingDiet.text = "🥗 Dietary Preference: $savedDiet"

        val notifsEnabled = prefs.getBoolean("notifs_enabled", true)
        tvSettingNotifs.text = if (notifsEnabled) "🔔 Push Notifications: Enabled" else "🔕 Push Notifications: Disabled"

        tvSettingNotifs.setOnClickListener {
            val newState = !notifsEnabled
            prefs.edit().putBoolean("notifs_enabled", newState).apply()
            tvSettingNotifs.text = if (newState) "🔔 Push Notifications: Enabled" else "🔕 Push Notifications: Disabled"
            Toast.makeText(requireContext(), "Notification settings updated", Toast.LENGTH_SHORT).show()
        }

        tvSettingDiet.setOnClickListener {
            val options = arrayOf("All Foods", "Vegetarian Only", "Non-Vegetarian Only")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Dietary Preference")
                .setItems(options) { _, which ->
                    val chosen = options[which]
                    prefs.edit().putString("diet_pref", chosen).apply()
                    tvSettingDiet.text = "🥗 Dietary Preference: $chosen"
                    Toast.makeText(requireContext(), "Preference updated", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        tvSettingEditProfile.setOnClickListener {
            val input = EditText(requireContext()).apply {
                hint = "Enter display name"
                setText(tvUserName.text)
                setPadding(48, 32, 48, 32)
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Edit Display Name")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                            .update("name", newName)
                            .addOnSuccessListener {
                                tvUserName.text = newName
                                UserSessionManager.cachedName = newName
                                Toast.makeText(requireContext(), "Name updated successfully", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        tvSettingHelp.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("About SharePlate & Help")
                .setMessage("SharePlate v1.0.0 Pro\n\nCampus Food Rescue & Sustainability Platform.\nRescuing surplus meals, reducing campus food waste, and tracking ESG impact metrics.")
                .setPositiveButton("Close", null)
                .show()
        }

        UserSessionManager.fetchUserRole { role ->
            tvRole.text = role.replaceFirstChar { it.uppercase() }
            if (role == "provider") {
                tvStat1Label.text = "Active Listings"
                tvStat2Label.text = "Total Published"
                FirebaseFirestore.getInstance().collection("listings")
                    .whereEqualTo("providerId", currentUser.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val total = snapshot.size()
                        val active = snapshot.documents.count { it.getString("status") == "active" }
                        tvStat1Val.text = active.toString()
                        tvStat2Val.text = total.toString()
                    }
            } else {
                tvStat1Label.text = "Meals Rescued"
                tvStat2Label.text = "CO₂ Prevented"
                FirebaseFirestore.getInstance().collection("reservations")
                    .whereEqualTo("studentId", currentUser.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val count = snapshot.size()
                        tvStat1Val.text = count.toString()
                        val co2 = count * 2.5
                        tvStat2Val.text = "${String.format(Locale.getDefault(), "%.1f", co2)} kg"
                    }
            }
        }

        FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: currentUser.email?.substringBefore("@") ?: "User"
                tvUserName.text = name.replaceFirstChar { it.uppercase() }
            }

        btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            UserSessionManager.clear()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileSettingsFragment_to_loginFragment)
        }
    }
}
