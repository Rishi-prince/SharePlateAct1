package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.yourteam.shareplate.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                delay(1000)
                if (!isAdded) return@repeatOnLifecycle
                val currentUser = FirebaseAuth.getInstance().currentUser
                val navController = findNavController()
                if (navController.currentDestination?.id == R.id.splashFragment) {
                    if (currentUser != null) {
                        navController.navigate(R.id.action_splashFragment_to_homeFeedFragment)
                    } else {
                        navController.navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }
            }
        }
    }
}
