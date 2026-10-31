package com.yourteam.shareplate

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourteam.shareplate.data.UserSessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        UserSessionManager.fetchUserRole { role ->
            bottomNavigationView.menu.clear()
            if (role == "provider") {
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_provider)
            } else {
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_student)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFeedFragment,
                R.id.notificationsFragment,
                R.id.shareBotFragment,
                R.id.myReservationsFragment,
                R.id.impactDashboardFragment,
                R.id.profileSettingsFragment,
                R.id.providerScanFragment,
                R.id.createListingFragment -> {
                    bottomNavigationView.visibility = View.VISIBLE
                    UserSessionManager.fetchUserRole { role ->
                        if (role == "provider" && bottomNavigationView.menu.findItem(R.id.createListingFragment) == null) {
                            bottomNavigationView.menu.clear()
                            bottomNavigationView.inflateMenu(R.menu.bottom_nav_provider)
                        } else if (role == "student" && bottomNavigationView.menu.findItem(R.id.shareBotFragment) == null) {
                            bottomNavigationView.menu.clear()
                            bottomNavigationView.inflateMenu(R.menu.bottom_nav_student)
                        }
                    }
                }
                else -> {
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
}
