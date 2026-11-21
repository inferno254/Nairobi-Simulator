package com.nairobisim.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nairobisim.R
import com.nairobisim.databinding.ActivityMainBinding
import com.nairobisim.model.MainViewModel.MainTab
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
        
        setupNavigation()
        observeViewModel()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    private fun observeViewModel() {
        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        
        viewModel.selectedTab.observe(this) { tab ->
            when (tab) {
                MainTab.DASHBOARD -> binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
                MainTab.BUSINESSES -> binding.bottomNavigation.selectedItemId = R.id.nav_businesses
                MainTab.FINANCES -> binding.bottomNavigation.selectedItemId = R.id.nav_finances
                MainTab.LOANS -> binding.bottomNavigation.selectedItemId = R.id.nav_loans
                MainTab.CHAMA -> binding.bottomNavigation.selectedItemId = R.id.nav_chama
                MainTab.EVENTS -> binding.bottomNavigation.selectedItemId = R.id.nav_events
                MainTab.SETTINGS -> binding.bottomNavigation.selectedItemId = R.id.nav_settings
            }
        }
    }
}