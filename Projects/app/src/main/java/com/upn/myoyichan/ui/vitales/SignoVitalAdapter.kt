package com.upn.myoyichan.ui.vitales

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.upn.myoyichan.R
import com.upn.myoyichan.data.local.entity.SignoVitalEntity
import com.upn.myoyichan.databinding.ItemSignoVitalBinding

class SignoVitalAdapter(private val context: Context) : 
    ListAdapter<SignoVitalEntity, SignoVitalAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemSignoVitalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vital: SignoVitalEntity) {
            binding.tvTipo.text = vital.tipo
            binding.tvValor.text = vital.valor
            binding.tvUnidad.text = vital.unidad
            binding.tvFechaHora.text = "${vital.fecha} ${vital.hora}"

            val colorRes = when (vital.tipo) {
                "PRESION" -> R.color.presion_color
                "GLUCOSA" -> R.color.glucosa_color
                "PESO" -> R.color.peso_color
                "TEMPERATURA" -> R.color.temperatura_color
                else -> R.color.primary
            }
            binding.viewIndicator.setBackgroundColor(context.getColor(colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSignoVitalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SignoVitalEntity>() {
            override fun areItemsTheSame(oldItem: SignoVitalEntity, newItem: SignoVitalEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SignoVitalEntity, newItem: SignoVitalEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
