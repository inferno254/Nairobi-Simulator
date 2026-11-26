package com.nairobisim.ui.bribery

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
import com.nairobisim.databinding.FragmentBriberyBinding
import com.nairobisim.model.BribeEncounter
import com.nairobisim.model.BribeOutcome
import com.nairobisim.ui.bribery.BribeViewModel

class BriberyFragment : Fragment() {

    private var _binding: FragmentBriberyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BribeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBriberyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.currentEncounter.observe(viewLifecycleOwner) { encounter ->
            if (encounter != null) {
                showEncounterDialog(encounter)
            }
        }

        viewModel.outcome.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                showOutcomeDialog(result)
                viewModel.dismissOutcome()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.generateEncounterButton.isEnabled = !loading
        }
    }

    private fun setupClickListeners() {
        binding.generateEncounterButton.setOnClickListener {
            viewModel.generateEncounter()
        }
    }

    private fun showEncounterDialog(encounter: BribeEncounter) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Corruption Encounter")
        dialog.setMessage(
            "${viewModel.getEncounterDescription(encounter.type)}\n\n" +
            "Authority: ${encounter.authority}\n" +
            "Location: ${encounter.location}\n" +
            "Demanded: KES ${encounter.demandedAmount}\n" +
            "Minimum acceptable: KES ${encounter.minimumAcceptable}\n" +
            "Risk of getting caught: ${(encounter.reputationRisk * 100).toInt()}%"
        )
        dialog.setPositiveButton("Pay Bribe") { _, _ ->
            showPaymentDialog(encounter)
        }
        dialog.setNegativeButton("Refuse") { _, _ ->
            viewModel.resolveBribe(pay = false, amount = 0)
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showPaymentDialog(encounter: BribeEncounter) {
        val input = EditText(requireContext())
        input.setText(encounter.demandedAmount.toString())
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Pay Bribe")
        dialog.setMessage("Enter amount to pay (minimum: KES ${encounter.minimumAcceptable})")
        dialog.setView(input)
        dialog.setPositiveButton("Confirm") { _, _ ->
            val amountText = input.text.toString().toLongOrNull() ?: encounter.minimumAcceptable
            viewModel.resolveBribe(pay = true, amount = amountText)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            viewModel.resolveBribe(pay = false, amount = 0)
        }
        dialog.show()
    }

    private fun showOutcomeDialog(result: BribeOutcome) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(if (result.success) "Resolved" else "Consequences")
        dialog.setMessage(result.message)
        if (result.fineAmount > 0) {
            dialog.setMessage("${result.message}\n\nFine: KES ${result.fineAmount}")
        }
        dialog.setPositiveButton("OK") { _, _ -> }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}