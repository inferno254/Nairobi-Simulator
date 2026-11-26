package com.nairobisim.ui.cars

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentCarsBinding
import com.nairobisim.ui.cars.adapter.*
import androidx.core.content.ContextCompat

class CarsFragment : Fragment() {

    private var _binding: FragmentCarsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CarViewModel by viewModels()

    private val carsAdapter = CarModelAdapter()
    private val ownedCarsAdapter = OwnedCarAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        binding.carsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = carsAdapter
        }
        binding.ownedCarsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ownedCarsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.cars.observe(viewLifecycleOwner) { cars ->
            carsAdapter.submitList(cars)
        }

        viewModel.ownedCars.observe(viewLifecycleOwner) { cars ->
            ownedCarsAdapter.submitList(cars)
        }

        viewModel.balance.observe(viewLifecycleOwner) {
            binding.executePendingBindings()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }
    }

    private fun setupClickListeners() {
        carsAdapter.onCarClick = { car ->
            viewModel.selectCar(car)
            val brand = viewModel.brands.value?.firstOrNull { it.id == car.brandId }
            val brandName = brand?.name ?: "Unknown"
            val carName = "${brandName} ${car.name} (${car.year})"
            val price = car.priceKES

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(carName)
                .setMessage("Price: KES $price\n\nPrestige: ${car.prestige}/10\nComfort: ${car.comfort}/10\nReliability: ${car.reliability}/10\nFuel: ${car.fuelConsumption} km/L\nMonthly Maintenance: KES ${car.maintenanceCost}")
                .setPositiveButton("Buy") { _, _ ->
                    viewModel.buyCar(car.id)
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }

        ownedCarsAdapter.onCarClick = { car ->
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Sell Car")
                .setMessage("Sell this car for KES ${car.currentValue}?")
                .setPositiveButton("Sell") { _, _ ->
                    viewModel.sellCar(car.id)
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
