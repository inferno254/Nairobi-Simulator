package com.nairobisim.ui.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.nairobisim.R
import com.nairobisim.databinding.FragmentOnboardingBinding
import com.nairobisim.model.BusinessCategory
import com.nairobisim.model.NairobiArea
import com.nairobisim.data.repository.OnboardingBackground
import com.nairobisim.data.repository.OnboardingBusinessChoice
import com.nairobisim.data.repository.OnboardingLocationChoice
import com.nairobisim.ui.onboarding.OnboardingStep

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupStepViews()
        setupClickListeners()
        setupNameTextWatcher()
    }

    private fun observeViewModel() {
        viewModel.currentStep.observe(viewLifecycleOwner) { step ->
            updateUIForStep(step)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.clearSuccess()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.nextButton.isEnabled = !loading
            binding.prevButton.isEnabled = !loading
        }
    }

    private fun setupStepViews() {
        // Background options
        binding.backgroundOptions.removeAllViews()
        val noneBg = androidx.appcompat.widget.AppCompatRadioButton(requireContext())
        noneBg.text = "None"
        noneBg.tag = null
        binding.backgroundOptions.addView(noneBg)
        for (bg in viewModel.backgrounds) {
            val radio = androidx.appcompat.widget.AppCompatRadioButton(requireContext())
            radio.text = "${bg.title} - +KES ${bg.bonus} starting capital, +${bg.reputationBonus} reputation"
            radio.tag = bg
            binding.backgroundOptions.addView(radio)
        }

        // Location options
        binding.locationOptions.removeAllViews()
        val noneLoc = androidx.appcompat.widget.AppCompatRadioButton(requireContext())
        noneLoc.text = "None"
        noneLoc.tag = null
        binding.locationOptions.addView(noneLoc)
        for (loc in viewModel.locations) {
            val radio = androidx.appcompat.widget.AppCompatRadioButton(requireContext())
            radio.text = "${loc.name} - ${loc.vibe} (Rent: ${loc.rentMultiplier}x, Traffic: ${loc.trafficMultiplier}x)"
            radio.tag = loc
            binding.locationOptions.addView(radio)
        }

        // Business options
        binding.businessOptions.removeAllViews()
        val noneBiz = androidx.appcompat.widget.AppCompatRadioButton(requireContext())
        noneBiz.text = "None"
        noneBiz.tag = null
        binding.businessOptions.addView(noneBiz)
        for (biz in viewModel.businesses) {
            val radio = androidx.appcompat.widget.AppCompatRadioButton(requireContext())
            radio.text = "${biz.title} - KES ${biz.startingCapital} capital (${biz.location.displayName})"
            radio.tag = biz
            binding.businessOptions.addView(radio)
        }
    }

    private fun setupClickListeners() {
        binding.nextButton.setOnClickListener {
            if (viewModel.currentStep.value == OnboardingStep.COMPLETE) {
                enterGame()
                return@setOnClickListener
            }
            if (!viewModel.canProceed()) {
                Toast.makeText(requireContext(), "Please complete this step first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveCurrentStepSelections()
            viewModel.nextStep()
        }

        binding.prevButton.setOnClickListener {
            viewModel.previousStep()
        }

        // Radio group listeners
        binding.backgroundOptions.setOnCheckedChangeListener { _, checkedId ->
            val radio = binding.backgroundOptions.findViewById<android.widget.RadioButton>(checkedId)
            if (radio != null) {
                if (radio.tag is OnboardingBackground) {
                    viewModel.selectBackground(radio.tag as OnboardingBackground)
                } else {
                    viewModel.selectBackground(null)
                }
                binding.nextButton.isEnabled = viewModel.canProceed()
            }
        }

        binding.locationOptions.setOnCheckedChangeListener { _, checkedId ->
            val radio = binding.locationOptions.findViewById<android.widget.RadioButton>(checkedId)
            if (radio != null) {
                if (radio.tag is OnboardingLocationChoice) {
                    viewModel.selectLocation(radio.tag as OnboardingLocationChoice)
                } else {
                    viewModel.selectLocation(null)
                }
                binding.nextButton.isEnabled = viewModel.canProceed()
            }
        }

        binding.businessOptions.setOnCheckedChangeListener { _, checkedId ->
            val radio = binding.businessOptions.findViewById<android.widget.RadioButton>(checkedId)
            if (radio != null) {
                if (radio.tag is OnboardingBusinessChoice) {
                    viewModel.selectBusiness(radio.tag as OnboardingBusinessChoice)
                } else {
                    viewModel.selectBusiness(null)
                }
                binding.nextButton.isEnabled = viewModel.canProceed()
            }
        }
    }

    private fun setupNameTextWatcher() {
        binding.playerNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setPlayerName(s?.toString() ?: "")
                binding.nextButton.isEnabled = viewModel.canProceed()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun saveCurrentStepSelections() {
        val current = viewModel.currentStep.value
        if (current == OnboardingStep.NAME) {
            val nameInput = binding.playerNameInput
            viewModel.setPlayerName(nameInput.text.toString())
        }
    }

    private fun enterGame() {
        try {
            androidx.navigation.Navigation.findNavController(requireView())
                .navigate(R.id.dashboardFragment)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not enter game: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUIForStep(step: OnboardingStep) {
        // Hide all step containers
        binding.welcomeStep.visibility = View.GONE
        binding.nameStep.visibility = View.GONE
        binding.backgroundStep.visibility = View.GONE
        binding.locationStep.visibility = View.GONE
        binding.businessStep.visibility = View.GONE
        binding.completeStep.visibility = View.GONE

        // Show current step
        when (step) {
            OnboardingStep.WELCOME -> binding.welcomeStep.visibility = View.VISIBLE
            OnboardingStep.NAME -> binding.nameStep.visibility = View.VISIBLE
            OnboardingStep.BACKGROUND -> binding.backgroundStep.visibility = View.VISIBLE
            OnboardingStep.LOCATION -> binding.locationStep.visibility = View.VISIBLE
            OnboardingStep.BUSINESS -> binding.businessStep.visibility = View.VISIBLE
            OnboardingStep.COMPLETE -> binding.completeStep.visibility = View.VISIBLE
        }

        // Update navigation buttons
        binding.prevButton.isEnabled = step != OnboardingStep.WELCOME && step != OnboardingStep.COMPLETE
        binding.nextButton.text = when (step) {
            OnboardingStep.BUSINESS -> "Start My Journey"
            OnboardingStep.COMPLETE -> "Enter Nairobi"
            else -> "Next"
        }
        binding.nextButton.isEnabled = step == OnboardingStep.COMPLETE || viewModel.canProceed()

        // Update progress
        val progress = when (step) {
            OnboardingStep.WELCOME -> 0f
            OnboardingStep.NAME -> 0.2f
            OnboardingStep.BACKGROUND -> 0.4f
            OnboardingStep.LOCATION -> 0.6f
            OnboardingStep.BUSINESS -> 0.8f
            OnboardingStep.COMPLETE -> 1.0f
        }
        binding.progressBar.progress = (progress * 100).toInt()
        binding.stepText.text = getStepLabel(step)
    }

    private fun getStepLabel(step: OnboardingStep): String = when (step) {
        OnboardingStep.WELCOME -> "Welcome"
        OnboardingStep.NAME -> "Your Name"
        OnboardingStep.BACKGROUND -> "Your Story"
        OnboardingStep.LOCATION -> "Choose Location"
        OnboardingStep.BUSINESS -> "Choose Business"
        OnboardingStep.COMPLETE -> "Ready!"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
