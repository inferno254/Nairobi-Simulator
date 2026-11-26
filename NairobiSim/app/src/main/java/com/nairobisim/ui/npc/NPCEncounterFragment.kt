package com.nairobisim.ui.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.nairobisim.R
import com.nairobisim.databinding.FragmentNpcEncounterBinding
import com.nairobisim.model.EncounterOutcome
import com.nairobisim.model.NPCEncounter
import com.nairobisim.model.NPCJobRole
import com.nairobisim.ui.npc.NPCEncounterViewModel

class NPCEncounterFragment : Fragment() {

    private var _binding: FragmentNpcEncounterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NPCEncounterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNpcEncounterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNPCSpinner()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupNPCSpinner() {
        val roles = NPCJobRole.values()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles.map { it.displayName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.npcSpinner.adapter = adapter

        binding.npcSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRole = roles[position]
                val npcs = viewModel.getNPCsByRole(selectedRole)
                binding.npcInfo.text = "${npcs.size} ${selectedRole.displayName}s in Nairobi\n" +
                    "${selectedRole.moralDescription}"
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
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

        viewModel.totalBribes.observe(viewLifecycleOwner) { total ->
            binding.cityBribesText.text = "Total bribes collected in Nairobi: KES $total"
        }
    }

    private fun setupClickListeners() {
        binding.generateEncounterButton.setOnClickListener {
            val position = binding.npcSpinner.selectedItemPosition
            val roles = NPCJobRole.values()
            val selectedRole = roles[position]
            viewModel.generateEncounter(selectedRole)
        }

        binding.randomEncounterButton.setOnClickListener {
            viewModel.generateRandomCorruptEncounter()
        }
    }

    private fun showEncounterDialog(encounter: NPCEncounter) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("${encounter.npc.name} - ${encounter.npc.jobRole.displayName}")
        dialog.setMessage(
            "${encounter.description}\n\n" +
            "Location: ${encounter.npc.jobRole.encounterLocation}\n" +
            "Officer demands: KES ${encounter.bribeDemanded}\n" +
            "Minimum acceptable: KES ${encounter.minimumAcceptable}\n" +
            "Chance bribe works: ${(encounter.successChance * 100).toInt()}%\n" +
            "Risk of getting caught: ${(encounter.reputationRisk * 100).toInt()}%"
        )

        dialog.setPositiveButton("Pay Bribe") { _, _ ->
            showBribeInputDialog(encounter)
        }

        dialog.setNeutralButton("Negotiate") { _, _ ->
            showNegotiateDialog(encounter)
        }

        dialog.setNegativeButton("Refuse") { _, _ ->
            viewModel.resolveEncounter(pay = false, amount = 0)
        }

        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showBribeInputDialog(encounter: NPCEncounter) {
        val input = EditText(requireContext())
        input.setText(encounter.bribeDemanded.toString())
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Pay ${encounter.npc.name}")
        dialog.setMessage("Enter bribe amount (minimum: KES ${encounter.minimumAcceptable})")
        dialog.setView(input)
        dialog.setPositiveButton("Confirm Pay") { _, _ ->
            val amountText = input.text.toString().toLongOrNull() ?: encounter.minimumAcceptable
            viewModel.resolveEncounter(pay = true, amount = amountText)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            viewModel.resolveEncounter(pay = false, amount = 0)
        }
        dialog.show()
    }

    private fun showNegotiateDialog(encounter: NPCEncounter) {
        val input = EditText(requireContext())
        input.setText(encounter.minimumAcceptable.toString())
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Negotiate with ${encounter.npc.name}")
        dialog.setMessage("Offer a lower amount (minimum: KES ${encounter.minimumAcceptable})")
        dialog.setView(input)
        dialog.setPositiveButton("Offer") { _, _ ->
            val amountText = input.text.toString().toLongOrNull() ?: encounter.minimumAcceptable
            // Negotiation: lower amount = higher risk of rejection
            viewModel.resolveEncounter(pay = true, amount = amountText)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            viewModel.resolveEncounter(pay = false, amount = 0)
        }
        dialog.show()
    }

    private fun showOutcomeDialog(result: EncounterOutcome) {
        val title = if (result.wasCaught) "CAUGHT!" else if (result.wasPaid && result.reputationChange >= 0) "Resolved" else if (result.wasPaid) "Paid" else "Refused"
        val colorRes = if (result.wasCaught) R.color.nairobi_error else if (result.reputationChange > 0) R.color.nairobi_success else R.color.nairobi_warning

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(title)
        dialog.setMessage(
            "${result.message}\n\n" +
            "NPC: ${result.npcName} (${result.npcRole.displayName})\n" +
            "Reputation change: ${if (result.reputationChange >= 0) "+" else ""}${result.reputationChange}"
        )
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