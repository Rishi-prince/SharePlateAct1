package com.yourteam.shareplate.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserSessionManager {
    var cachedRole: String? = null
    var cachedName: String? = null

    fun fetchUserRole(onResult: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult("student")
            return
        }
        if (cachedRole != null) {
            onResult(cachedRole!!)
            return
        }
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "student"
                cachedRole = role
                cachedName = doc.getString("name") ?: ""
                onResult(role)
            }
            .addOnFailureListener {
                onResult("student")
            }
    }

    fun clear() {
        cachedRole = null
        cachedName = null
    }
}
