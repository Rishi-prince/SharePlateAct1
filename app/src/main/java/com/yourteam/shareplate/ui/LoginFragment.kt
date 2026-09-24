package com.yourteam.shareplate.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.yourteam.shareplate.R

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val rgRole = view.findViewById<RadioGroup>(R.id.rgRole)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            viewModel.login(email, pass)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val role = if (rgRole.checkedRadioButtonId == R.id.rbProvider) "provider" else "student"
            viewModel.register(email, pass, role)
        }

        tvForgotPassword.setOnClickListener {
            val emailInput = EditText(requireContext()).apply {
                hint = "Enter your account email"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setPadding(48, 32, 48, 32)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Reset Password")
                .setMessage("Enter your email address to receive a password reset link.")
                .setView(emailInput)
                .setPositiveButton("Send") { _, _ ->
                    val email = emailInput.text.toString().trim()
                    if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    progressBar.visibility = View.VISIBLE
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewModel.authState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.AuthResult.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                    btnRegister.isEnabled = false
                }
                is AuthViewModel.AuthResult.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Success!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_homeFeedFragment)
                }
                is AuthViewModel.AuthResult.Error -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    btnRegister.isEnabled = true
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
