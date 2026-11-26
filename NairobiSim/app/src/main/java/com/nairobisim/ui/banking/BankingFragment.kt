package com.nairobisim.ui.banking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentBankingBinding
import com.nairobisim.databinding.ItemBankAccountBinding
import com.nairobisim.databinding.ItemMpesaTransactionBinding
import com.nairobisim.databinding.ItemNpcPlayerBinding
import com.nairobisim.model.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BankingFragment : Fragment() {

    private lateinit var binding: FragmentBankingBinding
    private lateinit var viewModel: BankingViewModel

    private val mpesaAdapter = MpesaTransactionAdapter()
    private val bankAdapter = BankAccountAdapter(
        onDeposit = { account, amount -> viewModel.depositToBank(account.accountNumber, amount) },
        onWithdraw = { account, amount -> viewModel.withdrawFromBank(account.accountNumber, amount) }
    )
    private val npcAdapter = NpcPlayerAdapter(
        onSend = { npc, amount -> viewModel.sendToNpc(npc.id, amount, "0000") },
        onRequest = { npc, amount -> viewModel.npcSendsToPlayer(npc.id, amount, "Payment request") }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BankingViewModel::class.java]

        setupRecyclerViews()
        setupSpinners()
        setupClickListeners()
        observeViewModel()

        viewModel.refresh()
    }

    private fun setupRecyclerViews() {
        binding.recyclerMpesaTxns.layoutManager = LinearLayoutManager(context)
        binding.recyclerMpesaTxns.adapter = mpesaAdapter

        binding.recyclerBankAccounts.layoutManager = LinearLayoutManager(context)
        binding.recyclerBankAccounts.adapter = bankAdapter

        binding.recyclerNpcs.layoutManager = LinearLayoutManager(context)
        binding.recyclerNpcs.adapter = npcAdapter
    }

    private fun setupSpinners() {
        // Provider spinner
        val providers = MobileMoneyProvider.values().map { it.displayName }
        val providerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, providers)
        providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProvider.adapter = providerAdapter

        // Bank spinner
        val banks = KenyanBank.values().map { it.displayName }
        val bankAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, banks)
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBank.adapter = bankAdapter

        // Account type spinner
        val accountTypes = BankAccountType.values().map { it.displayName }
        val accountTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, accountTypes)
        accountTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAccountType.adapter = accountTypeAdapter
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val phone = binding.edtPhone.text.toString()
            val pin = binding.edtPin.text.toString()
            if (phone.length < 10 || pin.length < 4) {
                Toast.makeText(context, "Enter valid phone and PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val provider = MobileMoneyProvider.values()[binding.spinnerProvider.selectedItemPosition]
            viewModel.registerMobileMoney(phone, provider, viewModel.playerName, pin)
        }

        binding.btnSend.setOnClickListener {
            val toPhone = binding.edtSendPhone.text.toString()
            val amount = binding.edtSendAmount.text.toString().toLongOrNull() ?: 0L
            if (toPhone.length < 10 || amount <= 0) {
                Toast.makeText(context, "Enter recipient and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.sendMoney(toPhone, amount, "0000")
        }

        binding.btnPayBill.setOnClickListener {
            Toast.makeText(context, "Pay Bill feature", Toast.LENGTH_SHORT).show()
        }

        binding.btnAirtime.setOnClickListener {
            val amount = binding.edtSendAmount.text.toString().toLongOrNull() ?: 0L
            if (amount <= 0) {
                Toast.makeText(context, "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.buyAirtime(amount, "0000")
        }

        binding.btnDepositMpesa.setOnClickListener {
            Toast.makeText(context, "Deposit from bank", Toast.LENGTH_SHORT).show()
        }

        binding.btnOpenAccount.setOnClickListener {
            val bank = KenyanBank.values()[binding.spinnerBank.selectedItemPosition]
            val accountType = BankAccountType.values()[binding.spinnerAccountType.selectedItemPosition]
            val initialDeposit = binding.edtInitialDeposit.text.toString().toLongOrNull() ?: 0L
            viewModel.openBankAccount(bank, accountType, initialDeposit)
        }

        binding.btnUpdateCBK.setOnClickListener {
            viewModel.updateCentralBank()
        }

        binding.btnDismissResult.setOnClickListener {
            binding.resultContainer.visibility = View.GONE
            viewModel.clearResult()
        }
    }

    private fun observeViewModel() {
        viewModel.mobileAccount.observe(viewLifecycleOwner) { account ->
            if (account != null && account.isRegistered) {
                binding.txtMpesaBalance.text = "KES ${account.balance}"
                binding.txtMpesaPhone.text = account.phoneNumber
                binding.registerCard.visibility = View.GONE
            } else {
                binding.txtMpesaBalance.text = "KES 0"
                binding.txtMpesaPhone.text = "Not registered"
                binding.registerCard.visibility = View.VISIBLE
            }
        }

        viewModel.mobileTransactions.observe(viewLifecycleOwner) { txns ->
            mpesaAdapter.submitList(txns)
        }

        viewModel.bankAccounts.observe(viewLifecycleOwner) { accounts ->
            bankAdapter.submitList(accounts)
        }

        viewModel.centralBank.observe(viewLifecycleOwner) { cbk ->
            binding.txtCBR.text = "${cbk.centralBankRate}%"
            binding.txtMoneySupply.text = "${cbk.moneySupplyPercent.toInt()}%"
            binding.txtInflation.text = "${cbk.inflationRate}%"
        }

        viewModel.npcs.observe(viewLifecycleOwner) { npcs ->
            npcAdapter.submitList(npcs)
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.resultContainer.visibility = View.VISIBLE
                binding.resultMessage.text = result
            }
        }
    }

    private inner class MpesaTransactionAdapter :
        androidx.recyclerview.widget.RecyclerView.Adapter<MpesaTransactionAdapter.ViewHolder>() {

        private var txns = emptyList<MobileMoneyTransaction>()

        fun submitList(list: List<MobileMoneyTransaction>) {
            txns = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemMpesaTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(txns[position])
        }

        override fun getItemCount(): Int = txns.size

        inner class ViewHolder(private val b: ItemMpesaTransactionBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(txn: MobileMoneyTransaction) {
                b.txtTxnType.text = txn.type.displayName
                b.txtTxnAmount.text = "KES ${txn.amount}"
                b.txtTxnDesc.text = txn.description
                b.txtTxnStatus.text = txn.status.displayName
                b.txtTxnRef.text = txn.reference ?: ""

                val color = when (txn.status) {
                    MobileMoneyTransactionStatus.SUCCESSFUL -> R.color.nairobi_primary
                    MobileMoneyTransactionStatus.PENDING -> R.color.nairobi_warning
                    MobileMoneyTransactionStatus.FAILED -> R.color.nairobi_error
                    MobileMoneyTransactionStatus.CANCELLED -> R.color.nairobi_text_secondary
                }
                b.txtTxnStatus.setTextColor(requireContext().resources.getColor(color, null))
            }
        }
    }

    private inner class BankAccountAdapter(
        private val onDeposit: (BankAccount, Long) -> Unit,
        private val onWithdraw: (BankAccount, Long) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<BankAccountAdapter.ViewHolder>() {

        private var accounts = emptyList<BankAccount>()

        fun submitList(list: List<BankAccount>) {
            accounts = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemBankAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(accounts[position])
        }

        override fun getItemCount(): Int = accounts.size

        inner class ViewHolder(private val b: ItemBankAccountBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(account: BankAccount) {
                b.txtBankName.text = account.bank.displayName
                b.txtAccountType.text = account.accountType.displayName
                b.txtAccountNumber.text = "Account: ${account.accountNumber}"
                b.txtBankBalance.text = "KES ${account.balance}"

                b.btnBankDeposit.setOnClickListener {
                    val amount = 10000L // Default deposit
                    onDeposit(account, amount)
                }

                b.btnBankWithdraw.setOnClickListener {
                    val amount = 5000L // Default withdrawal
                    onWithdraw(account, amount)
                }
            }
        }
    }

    private inner class NpcPlayerAdapter(
        private val onSend: (NpcPlayer, Long) -> Unit,
        private val onRequest: (NpcPlayer, Long) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<NpcPlayerAdapter.ViewHolder>() {

        private var npcs = emptyList<NpcPlayer>()

        fun submitList(list: List<NpcPlayer>) {
            npcs = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemNpcPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(npcs[position])
        }

        override fun getItemCount(): Int = npcs.size

        inner class ViewHolder(private val b: ItemNpcPlayerBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(npc: NpcPlayer) {
                b.txtNpcName.text = npc.name
                b.txtNpcType.text = if (npc.isMj3engo) "FAMOUS NPC" else "CPU"
                b.txtNpcLocation.text = npc.location
                b.txtNpcWealth.text = "KES ${npc.totalWealth}"

                b.btnSendToNpc.setOnClickListener {
                    onSend(npc, 1000L)
                }

                b.btnNpcSend.setOnClickListener {
                    onRequest(npc, 5000L)
                }
            }
        }
    }
}