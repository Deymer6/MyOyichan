package com.upn.myoyichan.ui.contactos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.upn.myoyichan.data.local.entity.ContactoSosEntity
import com.upn.myoyichan.databinding.ItemContactoBinding

class ContactoAdapter(
    private val onCallClick: (ContactoSosEntity) -> Unit,
    private val onEditClick: (ContactoSosEntity) -> Unit,
    private val onDeleteClick: (ContactoSosEntity) -> Unit
) : ListAdapter<ContactoSosEntity, ContactoAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemContactoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contacto: ContactoSosEntity,
            onCallClick: (ContactoSosEntity) -> Unit,
            onEditClick: (ContactoSosEntity) -> Unit,
            onDeleteClick: (ContactoSosEntity) -> Unit
        ) {
            binding.tvNombre.text = contacto.nombre
            binding.tvRelacion.text = contacto.relacion
            binding.tvTelefono.text = contacto.telefono
            
            if (contacto.esPrincipal) {
                binding.tvPrincipal.visibility = View.VISIBLE
            } else {
                binding.tvPrincipal.visibility = View.GONE
            }

            binding.btnCall.setOnClickListener { onCallClick(contacto) }
            binding.btnEdit.setOnClickListener { onEditClick(contacto) }
            binding.btnDelete.setOnClickListener { onDeleteClick(contacto) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onCallClick, onEditClick, onDeleteClick)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ContactoSosEntity>() {
            override fun areItemsTheSame(oldItem: ContactoSosEntity, newItem: ContactoSosEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ContactoSosEntity, newItem: ContactoSosEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
