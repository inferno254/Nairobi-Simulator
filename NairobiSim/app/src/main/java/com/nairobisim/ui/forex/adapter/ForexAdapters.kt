package com.nairobisim.ui.forex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nairobisim.R
import com.nairobisim.databinding.ItemCurrencyPairBinding
import com.nairobisim.databinding.ItemOpenPositionBinding
import com.nairobisim.databinding.ItemTradeHistoryBinding
import com.nairobisim.model.*

class CurrencyPairAdapter : ListAdapter<CurrencyPair, CurrencyPairAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CurrencyPair>() {
        override fun areItemsTheSame(oldItem: CurrencyPair, newItem: CurrencyPair): Boolean =
            oldItem.symbol == newItem.symbol
        override fun areContentsTheSame(oldItem: CurrencyPair, newItem: CurrencyPair): Boolean =
            oldItem == newItem
    }
) {

    class ViewHolder(private val binding: ItemCurrencyPairBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pair: CurrencyPair) {
            binding.pairSymbol.text = pair.symbol
            binding.pairName.text = pair.displayName
            binding.pairSpread.text = "Spread: ${pair.typicalSpread * 10000} pips"
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCurrencyPairBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class OpenPositionAdapter : ListAdapter<ForexTrade, OpenPositionAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ForexTrade>() {
        override fun areItemsTheSame(oldItem: ForexTrade, newItem: ForexTrade): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ForexTrade, newItem: ForexTrade): Boolean = oldItem == newItem
    }
) {

    var onPositionClick: ((ForexTrade) -> Unit)? = null

    class ViewHolder(private val binding: ItemOpenPositionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trade: ForexTrade, onClick: ((ForexTrade) -> Unit)?) {
            binding.positionSymbol.text = trade.symbol
            binding.positionType.text = trade.type.name
            binding.positionType.setTextColor(
                if (trade.type == com.nairobisim.model.TradeType.BUY)
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_success)
                else
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_error)
            )
            binding.positionSize.text = "${trade.lotSize} lots"
            binding.positionPrice.text = String.format("%.4f", trade.openPrice)
            binding.positionPnL.text = String.format("KES %.2f", trade.pnl)
            binding.positionPnL.setTextColor(
                if (trade.pnl >= 0)
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_success)
                else
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_error)
            )
            binding.btnClose.setOnClickListener { onClick?.invoke(trade) }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOpenPositionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onPositionClick)
    }
}

class TradeHistoryAdapter : ListAdapter<ForexTrade, TradeHistoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ForexTrade>() {
        override fun areItemsTheSame(oldItem: ForexTrade, newItem: ForexTrade): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ForexTrade, newItem: ForexTrade): Boolean = oldItem == newItem
    }
) {

    class ViewHolder(private val binding: ItemTradeHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trade: ForexTrade) {
            binding.historySymbol.text = trade.symbol
            binding.historyType.text = trade.type.name
            binding.historyType.setTextColor(
                if (trade.type == com.nairobisim.model.TradeType.BUY)
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_success)
                else
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_error)
            )
            binding.historySize.text = "${trade.lotSize} lots"
            binding.historyPrice.text = String.format("%.4f", trade.openPrice)
            binding.historyPnL.text = String.format("KES %.2f", trade.pnl)
            binding.historyPnL.setTextColor(
                if (trade.pnl >= 0)
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_success)
                else
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_error)
            )
            binding.historyStatus.text = if (trade.isClosed) "CLOSED" else "OPEN"
            binding.historyStatus.setTextColor(
                if (trade.isClosed)
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_text_secondary)
                else
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.nairobi_primary)
            )
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTradeHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
