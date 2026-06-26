package com.upn.myoyichan.ui.recordatorios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.upn.myoyichan.data.local.entity.MedicamentoEntity
import com.upn.myoyichan.databinding.ItemRecordatorioBinding

class RecordatorioAdapter(
    private val onToggleActivo: (MedicamentoEntity, Boolean) -> Unit
) : ListAdapter<MedicamentoEntity, RecordatorioAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemRecordatorioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            medicamento: MedicamentoEntity,
            onToggleActivo: (MedicamentoEntity, Boolean) -> Unit
        ) {
            binding.tvNombreMedicamento.text = medicamento.nombre
            binding.tvHora.text = "Programado: ${medicamento.horario}"
            binding.switchActivo.isChecked = medicamento.activo
            
            // Remove previous listener to avoid triggering it during recycling
            binding.switchActivo.setOnCheckedChangeListener(null)
            binding.switchActivo.isChecked = medicamento.activo
            binding.switchActivo.setOnCheckedChangeListener { _, isChecked ->
                onToggleActivo(medicamento, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordatorioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onToggleActivo)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MedicamentoEntity>() {
            override fun areItemsTheSame(oldItem: MedicamentoEntity, newItem: MedicamentoEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MedicamentoEntity, newItem: MedicamentoEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
