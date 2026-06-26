package com.upn.myoyichan.ui.medicamentos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.upn.myoyichan.data.local.entity.MedicamentoEntity
import com.upn.myoyichan.databinding.ItemMedicamentoBinding

class MedicamentoAdapter(
    private val onEditClick: (MedicamentoEntity) -> Unit,
    private val onDeleteClick: (MedicamentoEntity) -> Unit,
    private val onInfoClick: (MedicamentoEntity) -> Unit
) : ListAdapter<MedicamentoEntity, MedicamentoAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemMedicamentoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            medicamento: MedicamentoEntity,
            onEditClick: (MedicamentoEntity) -> Unit,
            onDeleteClick: (MedicamentoEntity) -> Unit,
            onInfoClick: (MedicamentoEntity) -> Unit
        ) {
            binding.tvNombre.text = medicamento.nombre
            binding.tvDosis.text = medicamento.dosis
            binding.tvHorario.text = "Horario: ${medicamento.horario} (${medicamento.frecuencia})"

            binding.btnEdit.setOnClickListener { onEditClick(medicamento) }
            binding.btnDelete.setOnClickListener { onDeleteClick(medicamento) }
            binding.btnInfo.setOnClickListener { onInfoClick(medicamento) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicamentoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onEditClick, onDeleteClick, onInfoClick)
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
