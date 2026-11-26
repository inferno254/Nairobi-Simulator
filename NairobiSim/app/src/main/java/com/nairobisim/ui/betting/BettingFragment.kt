package com.nairobisim.ui.betting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentBettingBinding
import com.nairobisim.databinding.ItemBetSlipBinding
import com.nairobisim.databinding.ItemBettingMatchBinding
import com.nairobisim.model.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BettingFragment : Fragment() {

    private lateinit var binding: FragmentBettingBinding
    private lateinit var viewModel: BettingViewModel

    private val matchAdapter = MatchAdapter { match, betType, selection, stake, odds ->
        viewModel.placeBet(match, betType, selection, stake, odds)
    }
    private val betAdapter = BetAdapter { slip ->
        viewModel.settleBet(slip.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BettingViewModel::class.java]

        binding.recyclerMatches.layoutManager = LinearLayoutManager(context)
        binding.recyclerMatches.adapter = matchAdapter

        binding.recyclerBets.layoutManager = LinearLayoutManager(context)
        binding.recyclerBets.adapter = betAdapter

        binding.btnAddMoney.setOnClickListener { viewModel.addMoney(50000) }
        binding.btnSimulate.setOnClickListener { viewModel.simulateResults() }
        binding.btnDismissResult.setOnClickListener { binding.resultContainer.visibility = View.GONE }

        viewModel.matches.observe(viewLifecycleOwner) { matches ->
            matchAdapter.submitList(matches)
        }

        viewModel.bets.observe(viewLifecycleOwner) { bets ->
            betAdapter.submitList(bets)
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.txtWallet.text = "KES ${balance.balance}"
            binding.txtTotalWon.text = "KES ${balance.totalWon}"
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.resultContainer.visibility = View.VISIBLE
                binding.resultMessage.text = result
            }
        }

        viewModel.refresh()
    }

    private inner class MatchAdapter(
        private val onBet: (BettingMatch, BetType, String, Long, Double) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

        private var matches = emptyList<BettingMatch>()

        fun submitList(list: List<BettingMatch>) {
            matches = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemBettingMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(matches[position])
        }

        override fun getItemCount(): Int = matches.size

        inner class ViewHolder(private val b: ItemBettingMatchBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(match: BettingMatch) {
                b.txtLeague.text = match.league
                b.txtMatch.text = "${match.homeTeam} vs ${match.awayTeam}"
                b.txtBookmaker.text = "SportPesa"
                b.txtHomeOdds.text = String.format("%.2f", match.homeOdds)
                b.txtDrawOdds.text = String.format("%.2f", match.drawOdds)
                b.txtAwayOdds.text = String.format("%.2f", match.awayOdds)
                b.txtOverUnder.text = "O/U: %.2f / %.2f".format(match.overOdds, match.underOdds)
                b.txtBtts.text = "BTTS: %.2f / %.2f".format(match.bttsYesOdds, match.bttsNoOdds)

                b.btnBetHome.setOnClickListener {
                    val stake = b.edtStake.text.toString().toLongOrNull() ?: 0L
                    if (stake <= 0) { Toast.makeText(context, "Enter stake", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    onBet(match, BetType.MATCH_WINNER, "HOME", stake, match.homeOdds)
                }

                b.btnBetDraw.setOnClickListener {
                    val stake = b.edtStake.text.toString().toLongOrNull() ?: 0L
                    if (stake <= 0) { Toast.makeText(context, "Enter stake", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    onBet(match, BetType.MATCH_WINNER, "DRAW", stake, match.drawOdds)
                }

                b.btnBetAway.setOnClickListener {
                    val stake = b.edtStake.text.toString().toLongOrNull() ?: 0L
                    if (stake <= 0) { Toast.makeText(context, "Enter stake", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    onBet(match, BetType.MATCH_WINNER, "AWAY", stake, match.awayOdds)
                }
            }
        }
    }

    private inner class BetAdapter(
        private val onSettle: (BetSlip) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<BetAdapter.ViewHolder>() {

        private var bets = emptyList<BetSlip>()

        fun submitList(list: List<BetSlip>) {
            bets = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemBetSlipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(bets[position])
        }

        override fun getItemCount(): Int = bets.size

        inner class ViewHolder(private val b: ItemBetSlipBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(slip: BetSlip) {
                b.txtBetMatch.text = slip.matchName
                b.txtBetType.text = slip.betType.displayName
                b.txtBetSelection.text = slip.selection
                b.txtBetStake.text = "KES ${slip.stake}"
                b.txtPotentialWin.text = "KES ${slip.potentialWin}"
                b.txtBetStatus.text = slip.status.displayName

                val color = when (slip.status) {
                    BetStatus.PENDING -> R.color.nairobi_warning
                    BetStatus.WON -> R.color.nairobi_primary
                    BetStatus.LOST -> R.color.nairobi_error
                    BetStatus.VOID, BetStatus.MATCH_CANCELLED -> R.color.nairobi_text_secondary
                }
                b.txtBetStatus.setTextColor(requireContext().resources.getColor(color, null))

                b.btnSettle.visibility = if (slip.status == BetStatus.PENDING) View.VISIBLE else View.GONE
                b.btnSettle.setOnClickListener { onSettle(slip) }
            }
        }
    }
}