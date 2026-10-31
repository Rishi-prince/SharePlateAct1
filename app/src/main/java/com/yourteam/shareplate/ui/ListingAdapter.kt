package com.yourteam.shareplate.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourteam.shareplate.R
import com.yourteam.shareplate.data.model.Listing

class ListingAdapter(
    private var listings: List<Listing>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    class ListingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivFoodThumbnail)
        val tvFoodType: TextView = view.findViewById(R.id.tvFoodType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvPickupPoint: TextView = view.findViewById(R.id.tvPickupPoint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]
        holder.tvFoodType.text = listing.foodType
        holder.tvQuantity.text = "${listing.remainingQuantity} / ${listing.quantity} portions remaining"
        holder.tvPickupPoint.text = "Pickup: ${listing.pickupPoint}"
        
        // Category-based icon fallback
        holder.ivThumbnail.setImageResource(R.drawable.ic_food_placeholder)

        val diffMillis = listing.expiryTime?.toDate()?.time?.minus(System.currentTimeMillis()) ?: 0
        val hoursLeft = diffMillis / (1000 * 60 * 60)
        val urgencyText = when {
            hoursLeft <= 0 -> "🔴 Expired"
            hoursLeft <= 1 -> "🔴 Ending Soon"
            hoursLeft <= 2 -> "🟡 Pickup Soon"
            else -> "🟢 Fresh"
        }
        holder.tvStatus.text = urgencyText
        
        holder.itemView.setOnClickListener { onItemClick(listing.listingId) }
    }

    override fun getItemCount(): Int = listings.size

    fun updateList(newList: List<Listing>) {
        listings = newList
        notifyDataSetChanged()
    }
}
