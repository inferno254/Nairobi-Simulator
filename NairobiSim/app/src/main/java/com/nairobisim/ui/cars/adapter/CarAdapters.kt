package com.nairobisim.ui.cars.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nairobisim.R
import com.nairobisim.databinding.ItemCarModelBinding
import com.nairobisim.databinding.ItemOwnedCarBinding
import com.nairobisim.model.CarModel
import com.nairobisim.model.OwnedCar

class CarModelAdapter : ListAdapter<CarModel, CarModelAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CarModel>() {
        override fun areItemsTheSame(oldItem: CarModel, newItem: CarModel): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CarModel, newItem: CarModel): Boolean = oldItem == newItem
    }
) {

    var onCarClick: ((CarModel) -> Unit)? = null

    class ViewHolder(private val binding: ItemCarModelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(car: CarModel, onClick: ((CarModel) -> Unit)?) {
            binding.carName.text = "${car.name} (${car.year})"
            binding.carPrice.text = "KES ${car.priceKES}"
            binding.carPrestige.text = "Prestige: ${car.prestige}/10"
            binding.carReliability.text = "Reliability: ${car.reliability}/10"
            binding.root.setOnClickListener { onClick?.invoke(car) }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCarModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onCarClick)
    }
}

class OwnedCarAdapter : ListAdapter<OwnedCar, OwnedCarAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<OwnedCar>() {
        override fun areItemsTheSame(oldItem: OwnedCar, newItem: OwnedCar): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: OwnedCar, newItem: OwnedCar): Boolean = oldItem == newItem
    }
) {

    var onCarClick: ((OwnedCar) -> Unit)? = null

    class ViewHolder(private val binding: ItemOwnedCarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(car: OwnedCar, onClick: ((OwnedCar) -> Unit)?) {
            binding.ownedCarName.text = car.carModelId
            binding.ownedCarValue.text = "Value: KES ${car.currentValue}"
            binding.ownedCarCondition.text = "Condition: ${car.condition}%"
            binding.root.setOnClickListener { onClick?.invoke(car) }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOwnedCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onCarClick)
    }
}
