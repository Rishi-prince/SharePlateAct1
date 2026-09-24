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

class ProfileSettingsFragment : Fragment(R.layout.fragment_profile_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvRole = view.findViewById<TextView>(R.id.tvUserRole)
        val btnSignOut = view.findViewById<MaterialButton>(R.id.btnSignOut)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.action_profileSettingsFragment_to_loginFragment)
            return
        }

        tvEmail.text = "Email: ${currentUser.email}"

        UserSessionManager.fetchUserRole { role ->
            tvRole.text = "Role: ${role.replaceFirstChar { it.uppercase() }}"
        }

        btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            UserSessionManager.clear()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileSettingsFragment_to_loginFragment)
        }
    }
}
