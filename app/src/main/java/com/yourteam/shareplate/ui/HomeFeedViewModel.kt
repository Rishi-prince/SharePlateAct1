package com.yourteam.shareplate.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.yourteam.shareplate.data.model.Listing

class HomeFeedViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _listings = MutableLiveData<List<Listing>>()
    val listings: LiveData<List<Listing>> = _listings

    private var allListings = listOf<Listing>()

    init {
        fetchListings()
    }

    private fun fetchListings() {
        listenerRegistration = firestore.collection("listings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Listing::class.java)?.copy(listingId = doc.id)
                    }
                    allListings = list
                    _listings.value = list
                }
            }
    }

    fun filterVeg(isVeg: Boolean?) {
        if (isVeg == null) {
            _listings.value = allListings
        } else {
            _listings.value = allListings.filter { it.isVeg == isVeg }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
