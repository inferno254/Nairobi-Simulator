package com.nairobisim.ui.shares.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nairobisim.R
import com.nairobisim.databinding.ItemShareCompanyBinding
import com.nairobisim.databinding.ItemSharePortfolioBinding
import com.nairobisim.databinding.ItemShareTradeBinding
import com.nairobisim.model.ShareCompany
import com.nairobisim.model.ShareTrade
import androidx.core.content.ContextCompat

class ShareCompanyAdapter : ListAdapter<ShareCompany, ShareCompanyAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ShareCompany>() {
        override fun areItemsTheSame(oldItem: ShareCompany, newItem: ShareCompany): Boolean = oldItem.symbol == newItem.symbol
        override fun areContentsTheSame(oldItem: ShareCompany, newItem: ShareCompany): Boolean = oldItem == newItem
    }
) {

    class ViewHolder(private val binding: ItemShareCompanyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(company: ShareCompany) {
            binding.companyName.text = company.name
            binding.companySymbol.text = company.symbol
            binding.companySector.text = company.sector.name
            val price = 18.50 // placeholder - would come from prices map
            binding.companyPrice.text = "KES ${String.format("%.2f", price)}"
            val change = company.changePercent
            binding.companyChange.text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%"
            binding.companyChange.setTextColor(
                if (change >= 0)
                    ContextCompat.getColor(binding.root.context, R.color.nairobi_success)
                else
                    ContextCompat.getColor(binding.root.context, R.color.nairobi_error)
            )
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShareCompanyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PortfolioAdapter : ListAdapter<ShareTrade, PortfolioAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ShareTrade>() {
        override fun areItemsTheSame(oldItem: ShareTrade, newItem: ShareTrade): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShareTrade, newItem: ShareTrade): Boolean = oldItem == newItem
    }
) {

    var onPositionClick: ((ShareTrade) -> Unit)? = null

    class ViewHolder(private val binding: ItemSharePortfolioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trade: ShareTrade, onClick: ((ShareTrade) -> Unit)?) {
            binding.portfolioSymbol.text = trade.symbol
            binding.portfolioShares.text = "${trade.shares} shares"
            binding.portfolioPrice.text = "KES ${String.format("%.2f", trade.price)}"
            binding.btnSell.setOnClickListener { onClick?.invoke(trade) }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSharePortfolioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onPositionClick)
    }
}

class ShareHistoryAdapter : ListAdapter<ShareTrade, ShareHistoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ShareTrade>() {
        override fun areItemsTheSame(oldItem: ShareTrade, newItem: ShareTrade): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShareTrade, newItem: ShareTrade): Boolean = oldItem == newItem
    }
) {

    class ViewHolder(private val binding: ItemShareTradeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trade: ShareTrade) {
            binding.historySymbol.text = trade.symbol
            binding.historyType.text = trade.type.name
            binding.historyType.setTextColor(
                if (trade.type == com.nairobisim.model.TradeType.BUY)
                    ContextCompat.getColor(binding.root.context, R.color.nairobi_success)
                else
                    ContextCompat.getColor(binding.root.context, R.color.nairobi_error)
            )
            binding.historyShares.text = "${trade.shares} shares"
            binding.historyPrice.text = "KES ${String.format("%.2f", trade.price)}"
            binding.historyTotal.text = "KES ${String.format("%.2f", trade.totalValue)}"
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShareTradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
