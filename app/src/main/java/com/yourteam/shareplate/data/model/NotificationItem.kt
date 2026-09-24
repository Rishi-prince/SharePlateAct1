package com.yourteam.shareplate.data.model

import com.google.firebase.Timestamp

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null,
    val read: Boolean = false
)
