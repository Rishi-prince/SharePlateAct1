package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.yourteam.shareplate.R

class HomeFeedFragment : Fragment(R.layout.fragment_home_feed) {
    private val viewModel: HomeFeedViewModel by viewModels()
    private lateinit var listingAdapter: ListingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewListings)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)
        val progressBar = view.findViewById<CircularProgressIndicator>(R.id.progressBarFeed)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        listingAdapter = ListingAdapter(emptyList()) { listingId ->
            val bundle = Bundle().apply { putString("listingId", listingId) }
            findNavController().navigate(R.id.action_homeFeedFragment_to_listingDetailsFragment, bundle)
        }
        recyclerView.adapter = listingAdapter

        val chipAll = view.findViewById<Chip>(R.id.chipAll)
        val chipVeg = view.findViewById<Chip>(R.id.chipVeg)
        val chipNonVeg = view.findViewById<Chip>(R.id.chipNonVeg)

        chipAll.setOnClickListener { viewModel.filterVeg(null) }
        chipVeg.setOnClickListener { viewModel.filterVeg(true) }
        chipNonVeg.setOnClickListener { viewModel.filterVeg(false) }

        progressBar.visibility = View.VISIBLE
        viewModel.listings.observe(viewLifecycleOwner) { listings ->
            progressBar.visibility = View.GONE
            listingAdapter.updateList(listings)
            if (listings.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }
}
