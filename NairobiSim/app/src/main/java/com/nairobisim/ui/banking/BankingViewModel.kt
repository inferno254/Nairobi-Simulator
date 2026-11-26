package com.nairobisim.ui.banking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.BankingRepository
import com.nairobisim.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BankingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BankingRepository(
        db,
        db.mobileMoneyAccountDao(),
        db.mobileMoneyTransactionDao(),
        db.bankAccountDao(),
        db.bankTransactionDao(),
        db.centralBankDao(),
        db.npcPlayerDao()
    )

    val _mobileAccount = MutableLiveData<MobileMoneyAccount?>(null)
    val mobileAccount: LiveData<MobileMoneyAccount?> = _mobileAccount

    val _mobileTransactions = MutableLiveData<List<MobileMoneyTransaction>>(emptyList())
    val mobileTransactions: LiveData<List<MobileMoneyTransaction>> = _mobileTransactions

    val _bankAccounts = MutableLiveData<List<BankAccount>>(emptyList())
    val bankAccounts: LiveData<List<BankAccount>> = _bankAccounts

    val _bankTransactions = MutableLiveData<List<BankTransaction>>(emptyList())
    val bankTransactions: LiveData<List<BankTransaction>> = _bankTransactions

    val _centralBank = MutableLiveData<CentralBankState>(CentralBankState())
    val centralBank: LiveData<CentralBankState> = _centralBank

    val _npcs = MutableLiveData<List<NpcPlayer>>(emptyList())
    val npcs: LiveData<List<NpcPlayer>> = _npcs

    val _result = MutableLiveData<String?>(null)
    val result: LiveData<String?> = _result

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    var playerPhone: String = "254700000000"
    var playerName: String = "Player"

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _mobileAccount.postValue(repository.getMobileMoneyAccount(playerPhone))
            if (playerPhone.isNotEmpty()) {
                _mobileTransactions.postValue(repository.getMobileMoneyTransactions(playerPhone).first())
            }
            _bankAccounts.postValue(repository.getBankAccounts(playerName))
            _centralBank.postValue(repository.getCentralBankState())
            _npcs.postValue(repository.getCpuNpcs().first())
            _isLoading.value = false
        }
    }

    fun registerMobileMoney(phone: String, provider: MobileMoneyProvider, name: String, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val account = repository.registerMobileMoney(phone, provider, name, pin)
                playerPhone = phone
                playerName = name
                _result.postValue("M-Pesa account registered! Balance: KES ${account.balance}")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Registration failed")
            }
            _isLoading.value = false
        }
    }

    fun sendMoney(toPhone: String, amount: Long, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val txn = repository.sendMoney(playerPhone, toPhone, amount, pin)
                _result.postValue("Sent KES $amount to $toPhone! Ref: ${txn.reference}")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Send failed")
            }
            _isLoading.value = false
        }
    }

    fun receiveMoney(amount: Long, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val txn = repository.receiveMoney(playerPhone, amount, description)
                _result.postValue("Received KES $amount! $description")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Receive failed")
            }
            _isLoading.value = false
        }
    }

    fun depositToMobileMoney(amount: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val txn = repository.depositToMobileMoney(playerPhone, amount)
                _result.postValue("Deposited KES $amount to M-Pesa!")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Deposit failed")
            }
            _isLoading.value = false
        }
    }

    fun withdrawToBank(amount: Long, bankAccount: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val txn = repository.withdrawToBank(playerPhone, amount, bankAccount)
                _result.postValue("Withdrew KES $amount to bank!")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Withdrawal failed")
            }
            _isLoading.value = false
        }
    }

    fun payBill(biller: String, accountNumber: String, amount: Long, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val txn = repository.payBill(playerPhone, biller, accountNumber, amount, pin)
                _result.postValue("Paid KES $amount to $biller!")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Payment failed")
            }
            _isLoading.value = false
        }
    }

    fun buyAirtime(amount: Long, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val txn = repository.buyAirtime(playerPhone, amount, pin)
                _result.postValue("Bought KES $amount airtime!")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Airtime purchase failed")
            }
            _isLoading.value = false
        }
    }

    fun openBankAccount(bank: KenyanBank, accountType: BankAccountType, initialDeposit: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val account = repository.openBankAccount(playerName, bank, accountType, initialDeposit)
                _result.postValue("${bank.displayName} account opened! Number: ${account.accountNumber}")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Account opening failed")
            }
            _isLoading.value = false
        }
    }

    fun depositToBank(accountNumber: String, amount: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.depositToBank(accountNumber, amount)
                _result.postValue("Deposited KES $amount to bank!")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Deposit failed")
            }
            _isLoading.value = false
        }
    }

    fun withdrawFromBank(accountNumber: String, amount: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.withdrawFromBank(accountNumber, amount)
                _result.postValue("Withdrew KES $amount from bank!")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Withdrawal failed")
            }
            _isLoading.value = false
        }
    }

    fun sendToNpc(npcId: String, amount: Long, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.sendMoneyToNpc(playerPhone, npcId, amount, pin)
                _result.postValue("Sent KES $amount to NPC!")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Send failed")
            }
            _isLoading.value = false
        }
    }

    fun npcSendsToPlayer(npcId: String, amount: Long, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.npcSendMoneyToPlayer(npcId, playerPhone, amount, reason)
                _result.postValue("Received KES $amount from NPC! $reason")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "NPC transaction failed")
            }
            _isLoading.value = false
        }
    }

    fun applyInterest() {
        viewModelScope.launch {
            repository.applyBankInterest()
            _result.postValue("Bank interest applied!")
            refresh()
        }
    }

    fun updateCentralBank() {
        viewModelScope.launch {
            repository.updateCentralBank()
            refresh()
        }
    }

    fun clearResult() { _result.value = null }
}