package com.yourteam.shareplate.data.model

import com.google.firebase.Timestamp

data class Listing(
    val listingId: String = "",
    val providerId: String = "",
    val foodType: String = "",
    val isVeg: Boolean = true,
    val quantity: Int = 0,
    val remainingQuantity: Int = 0,
    val pickupPoint: String = "",
    val pickupWindowStart: Timestamp? = null,
    val pickupWindowEnd: Timestamp? = null,
    val expiryTime: Timestamp? = null,
    val status: String = "active" // active, expired, soldOut
)
