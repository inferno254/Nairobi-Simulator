package com.nairobisim.ui.carimport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.nairobisim.R
import com.nairobisim.databinding.FragmentCarImportBinding
import com.nairobisim.model.CustomsEncounter
import com.nairobisim.model.ForeignCarListing
import com.nairobisim.model.ImportDutyBreakdown
import com.nairobisim.ui.carimport.CarImportViewModel

class CarImportFragment : Fragment() {

    private var _binding: FragmentCarImportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CarImportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.marketListings.observe(viewLifecycleOwner) { listings ->
            val displayNames = listings.map { "${it.brand} ${it.model} (${it.year}) - ${it.sourceCountry.displayName}" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayNames)
            binding.importList.adapter = adapter
        }

        viewModel.dutyBreakdown.observe(viewLifecycleOwner) { breakdown ->
            if (breakdown != null) {
                showDutyBreakdown(breakdown)
            }
        }

        viewModel.customsEncounter.observe(viewLifecycleOwner) { encounter ->
            if (encounter != null) {
                showCustomsDialog(encounter)
            }
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

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.importButton.isEnabled = !loading
        }
    }

    private fun setupClickListeners() {
        binding.importList.setOnItemClickListener { _, _, position, _ ->
            val listing = viewModel.marketListings.value?.getOrNull(position) ?: return@setOnItemClickListener
            viewModel.calculateDutyForListing(listing)
        }

        binding.importButton.setOnClickListener {
            val selectedPosition = binding.importList.checkedItemPosition
            if (selectedPosition < 0) {
                Toast.makeText(requireContext(), "Select a car to import", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val listing = viewModel.marketListings.value?.getOrNull(selectedPosition) ?: return@setOnClickListener
            viewModel.importCar(listing)
        }
    }

    private fun showDutyBreakdown(breakdown: ImportDutyBreakdown) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Import Duty Breakdown")
        dialog.setMessage(
            "CIF Value: KES ${breakdown.cifValue}\n" +
            "Import Duty (25%): KES ${breakdown.importDuty}\n" +
            "Excise Duty: KES ${breakdown.exciseDuty}\n" +
            "VAT (16%): KES ${breakdown.vat}\n" +
            "IDF (2%): KES ${breakdown.idf}\n" +
            "RDL (1.5%): KES ${breakdown.rdl}\n" +
            "Port Charges: KES ${breakdown.portCharges}\n" +
            "Clearing Agent: KES ${breakdown.clearingAgentFee}\n\n" +
            "TOTAL TAXES: KES ${breakdown.totalTaxes}\n" +
            "TOTAL LANDED COST: KES ${breakdown.totalLandedCost}"
        )
        dialog.setPositiveButton("OK", null)
        dialog.show()
    }

    private fun showCustomsDialog(encounter: CustomsEncounter) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Customs Clearance - Mombasa Port")
        dialog.setMessage(
            "Your imported car has arrived at Mombasa port.\n\n" +
            "Duty owed: KES ${encounter.dutyOwed}\n" +
            "Customs officer demands: KES ${encounter.officerDemand}\n" +
            "Minimum acceptable: KES ${encounter.minimumAcceptable}\n" +
            "Risk of getting caught: ${(encounter.reputationRisk * 100).toInt()}%"
        )
        dialog.setPositiveButton("Pay Officer") { _, _ ->
            showBribeInputDialog(encounter, pay = true)
        }
        dialog.setNeutralButton("Pay Full Duty") { _, _ ->
            viewModel.resolveCustoms(payBribe = false, bribeAmount = encounter.dutyOwed)
        }
        dialog.setNegativeButton("Refuse Everything") { _, _ ->
            viewModel.resolveCustoms(payBribe = false, bribeAmount = 0)
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showBribeInputDialog(encounter: CustomsEncounter, pay: Boolean) {
        val input = EditText(requireContext())
        input.setText(encounter.officerDemand.toString())
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Pay Customs Officer")
        dialog.setMessage("Enter bribe amount (minimum: KES ${encounter.minimumAcceptable})")
        dialog.setView(input)
        dialog.setPositiveButton("Confirm") { _, _ ->
            val amountText = input.text.toString().toLongOrNull() ?: encounter.minimumAcceptable
            viewModel.resolveCustoms(payBribe = true, bribeAmount = amountText)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            viewModel.resolveCustoms(payBribe = false, bribeAmount = 0)
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}