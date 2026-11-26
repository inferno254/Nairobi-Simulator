package com.nairobisim.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nairobisim.R
import com.nairobisim.databinding.ActivityMainBinding
import com.nairobisim.ui.main.MainViewModel.MainTab
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigation()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        binding.navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, _ ->
            binding.drawerLayout.close()
            when (destination.id) {
                R.id.dashboardFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
                R.id.businessesFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_businesses
                R.id.financesFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_forex
                R.id.leaderboardFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_leaderboard
                R.id.briberyFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                R.id.sharpBoysFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                R.id.freelanceFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                R.id.lifeMentorFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                R.id.bettingFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                R.id.ponziFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                R.id.bankingFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                R.id.electionsFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
            }
        }
    }

    private fun observeViewModel() {
        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.selectedTab.observe(this) { tab ->
            when (tab) {
                MainTab.DASHBOARD -> binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
                MainTab.BUSINESSES -> binding.bottomNavigation.selectedItemId = R.id.nav_businesses
                MainTab.FOREX -> binding.bottomNavigation.selectedItemId = R.id.nav_forex
                MainTab.LEADERBOARD -> binding.bottomNavigation.selectedItemId = R.id.nav_leaderboard
                MainTab.NPC_ENCOUNTER -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
                MainTab.SHARP_BOYS -> binding.bottomNavigation.selectedItemId = R.id.nav_bribery
            }
        }

        viewModel.isOnboardingComplete.observe(this) { isComplete ->
            if (isComplete == false) {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.onboardingFragment)
            }
        }
    }
}
