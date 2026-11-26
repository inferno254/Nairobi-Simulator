package com.nairobisim.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.SimulationRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = SimulationRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application)
    )
    
    // Exposed LiveData
    val playerProfile = repository.playerProfile
    val activeBusinesses = repository.activeBusinesses
    val recentTransactions = repository.recentTransactions
    val activeLoans = repository.activeLoans
    val activeChamas = repository.activeChamas
    val currentEvents = repository.currentEvents
    val simulationState = repository.simulationState
    
    // UI State
    val _selectedBusiness = MutableLiveData<Business?>()
    val selectedBusiness: LiveData<Business?> = _selectedBusiness
    
    val _selectedTab = MutableLiveData<MainTab>(MainTab.DASHBOARD)
    val selectedTab: LiveData<MainTab> = _selectedTab
    
    val _isOnboardingComplete = MutableLiveData<Boolean>(false)
    val isOnboardingComplete: LiveData<Boolean> = _isOnboardingComplete
    
    val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage
    
    init {
        initializeData()
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            repository.initializeSimulationState()
            repository.initializeMarketEvents()
            
            // Check onboarding status
            val profile = repository.getPlayerProfile()
            _isOnboardingComplete.postValue(profile?.isProfileComplete ?: false)
            
            // Create default player if not exists
            if (profile == null) {
                val newProfile = PlayerProfile(unlockRequirements = UnlockRequirements())
                repository.initializePlayer(newProfile)
                _isOnboardingComplete.postValue(false)
            }
        }
    }
    
    // Business Actions
    fun startBusiness(category: BusinessCategory, location: NairobiArea, name: String? = null) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfile() ?: return@launch
            
            if (profile.level < 1) {
                _toastMessage.postValue("Need level 1 to start ${category.displayName}")
                return@launch
            }
            
            if ((profile.kesBalance ?: 0L) < category.baseCapital) {
                _toastMessage.postValue("Insufficient funds. Need KES ${category.baseCapital}")
                return@launch
            }
            
            val businessId = "biz_${category.name.lowercase()}_${System.currentTimeMillis()}"
            val business = Business(
                id = businessId,
                name = name ?: "${category.displayName} (${location.displayName})",
                category = category,
                location = location,
                capitalInvested = category.baseCapital,
                unlockRequirements = UnlockRequirements()
            )
            
            business.monthlyRevenue = business.calculateRevenue()
            business.monthlyExpenses = calculateMonthlyExpenses(business)
            
            repository.startBusiness(business)
            _toastMessage.postValue("Started ${business.name}! Capital: KES ${category.baseCapital}")
        }
    }
    
    private fun calculateMonthlyExpenses(business: Business): Long {
        val rent = when (business.location) {
            NairobiArea.CBD -> 150000
            NairobiArea.WESTLANDS -> 120000
            NairobiArea.KILELESHWA -> 100000
            NairobiArea.KAREN -> 200000
            NairobiArea.RUNDA -> 180000
            NairobiArea.KASARANI -> 50000
            NairobiArea.EMBAKASI -> 40000
            NairobiArea.KIBERA -> 15000
            NairobiArea.MATHARE -> 12000
            NairobiArea.DANDORA -> 25000
            NairobiArea.KAWANGWARE -> 30000
            NairobiArea.GITHURAI -> 28000
            NairobiArea.THIKA_ROAD -> 60000
            NairobiArea.NGONG_ROAD -> 55000
            NairobiArea.MOMBASA_ROAD -> 80000
            NairobiArea.LANGATA -> 70000
            NairobiArea.RONGAI -> 35000
            NairobiArea.KIKUYU -> 25000
            NairobiArea.LIMURU -> 20000
            NairobiArea.EASTLEIGH -> 40000
            NairobiArea.KAYOLE -> 25000
            NairobiArea.KILIMANI -> 80000
            NairobiArea.KIAMBU -> 30000
            NairobiArea.ONLINE -> 5000
        }
        
        val salaries = business.employees.sumOf { it.salary }
        val maintenance = business.assets.sumOf { it.maintenanceCost }
        val utilities = when (business.category) {
            BusinessCategory.RESTAURANT, BusinessCategory.CAR_WASH -> 30000
            BusinessCategory.MATATU_SACCO, BusinessCategory.BODABODA_FLEET -> 50000
            BusinessCategory.DAIRY_FARM, BusinessCategory.POULTRY_FARM, BusinessCategory.FISH_FARM -> 40000
            BusinessCategory.HORTICULTURE -> 60000
            BusinessCategory.CYBER_CAFE -> 20000
            else -> 15000
        }
        
        val licenses = when (business.category) {
            BusinessCategory.MATATU_SACCO -> 50000
            BusinessCategory.RESTAURANT -> 30000
            BusinessCategory.BUTCHERY, BusinessCategory.GREEN_GROCERY -> 10000
            BusinessCategory.HARDWARE, BusinessCategory.AGROVET -> 15000
            else -> 5000
        }
        
        return rent + salaries + maintenance + utilities + licenses
    }
    
    fun collectRevenue(business: Business) {
        viewModelScope.launch {
            repository.collectBusinessRevenue(business)
            _toastMessage.postValue("Collected KES ${business.netMonthlyIncome} from ${business.name}")
        }
    }
    
    fun upgradeBusiness(business: Business) {
        viewModelScope.launch {
            val upgradeCost = business.category.baseCapital / 2 * business.level
            val profile = repository.getPlayerProfile() ?: return@launch
            
            if ((profile.kesBalance ?: 0L) < upgradeCost) {
                _toastMessage.postValue("Need KES $upgradeCost to upgrade to level ${business.level + 1}")
                return@launch
            }
            
            repository.upgradeBusiness(business.id, upgradeCost)
            _toastMessage.postValue("Upgraded ${business.name} to level ${business.level + 1}!")
        }
    }
    
    // Financial Actions
    fun takeLoan(source: LoanSource, amount: Long, termMonths: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfile() ?: return@launch
            if (amount > source.maxAmount) {
                _toastMessage.postValue("Max loan from ${source.name} is KES ${source.maxAmount}")
                return@launch
            }
            
            val rate = (source.minRate + source.maxRate) / 2
            val loan = Loan(
                id = "loan_${source.name}_${System.currentTimeMillis()}".lowercase().replace(" ", "_"),
                source = source,
                principal = amount,
                outstandingBalance = amount,
                interestRate = rate,
                termMonths = termMonths,
                nextPaymentDate = Date(System.currentTimeMillis() + 2592000000) // ~30 days
            )
            
            repository.takeLoan(loan)
            _toastMessage.postValue("Loan approved! KES $amount from ${source.name} at ${rate}%/month")
        }
    }
    
    fun repayLoan(loan: Loan) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfile() ?: return@launch
            if ((profile.kesBalance ?: 0L) < loan.monthlyPayment) {
                _toastMessage.postValue("Insufficient funds for repayment (KES ${loan.monthlyPayment})")
                return@launch
            }
            
            repository.repayLoan(loan.id, loan.monthlyPayment)
            _toastMessage.postValue("Repaid KES ${loan.monthlyPayment}. Balance: KES ${loan.outstandingBalance - loan.monthlyPayment}")
        }
    }
    
    // Simulation Control
    fun setGameSpeed(speed: GameSpeed) {
        viewModelScope.launch {
            val state = repository.getSimulationState()?.copy(gameSpeed = speed)
            state?.let { repository.updateSimulationState(it) }
        }
    }
    
    fun toggleSimulation() {
        viewModelScope.launch {
            val state = repository.getSimulationState()?.copy(isRunning = !(repository.getSimulationState()?.isRunning ?: false))
            state?.let { repository.updateSimulationState(it) }
        }
    }
    
    fun advanceDay() {
        viewModelScope.launch {
            repository.advanceDay()
            _toastMessage.postValue("Advanced to next day")
        }
    }
    
    fun selectBusiness(business: Business?) {
        _selectedBusiness.postValue(business)
    }
    
    fun setTab(tab: MainTab) {
        _selectedTab.postValue(tab)
    }
    
    fun formatKes(amount: Long): String {
        return "KES ${String.format("%,d", amount)}"
    }
    
    enum class MainTab {
        DASHBOARD, BUSINESSES, FOREX, LEADERBOARD, NPC_ENCOUNTER, SHARP_BOYS
    }
}