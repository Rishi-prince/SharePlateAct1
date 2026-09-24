package com.yourteam.shareplate.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourteam.shareplate.data.model.User

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthResult>()
    val authState: LiveData<AuthResult> = _authState

    sealed class AuthResult {
        object Loading : AuthResult()
        object Success : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthResult.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthResult.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { _authState.value = AuthResult.Success }
            .addOnFailureListener { 
                val msg = it.localizedMessage ?: "Login failed. Ensure Email/Password auth is enabled in Firebase Console."
                _authState.value = AuthResult.Error(msg)
            }
    }

    fun register(email: String, pass: String, role: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthResult.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthResult.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val user = User(
                    uid = uid,
                    name = email.substringBefore("@"),
                    email = email,
                    role = role,
                    verified = role == "student"
                )
                firestore.collection("users").document(uid).set(user)
                    .addOnSuccessListener { _authState.value = AuthResult.Success }
                    .addOnFailureListener { _authState.value = AuthResult.Error(it.localizedMessage ?: "Failed to save user data") }
            }
            .addOnFailureListener { 
                val msg = it.localizedMessage ?: "Registration failed. Ensure Email/Password auth is enabled in Firebase Console."
                _authState.value = AuthResult.Error(msg)
            }
    }
}
