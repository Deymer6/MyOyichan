package com.upn.myoyichan.ui.contactos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.FragmentContactosBinding
import com.upn.myoyichan.repository.ContactoSosRepository
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.ui.main.MainActivity
import com.upn.myoyichan.utils.SessionManager

class ContactosFragment : Fragment() {

    private var _binding: FragmentContactosBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactoViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ContactoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val repository = ContactoSosRepository(AppDatabase.getInstance(requireContext()).contactoSosDao())
        val factory = ViewModelFactory(contactoSosRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ContactoViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = ContactoAdapter(
            onCallClick = { contacto ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${contacto.telefono}")
                startActivity(intent)
            },
            onEditClick = { contacto ->
                val intent = Intent(requireContext(), ContactoFormActivity::class.java)
                intent.putExtra("CONTACTO_ID", contacto.id)
                startActivity(intent)
            },
            onDeleteClick = { contacto ->
                AlertDialog.Builder(requireContext())
                    .setMessage("¿Está seguro que desea eliminar este contacto?")
                    .setPositiveButton("Sí") { _, _ -> viewModel.deleteContacto(contacto) }
                    .setNegativeButton("No", null)
                    .show()
            }
        )
        binding.rvContactos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContactos.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), ContactoFormActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.getContactos(sessionManager.getUsuarioId()).observe(viewLifecycleOwner) { lista ->
            if (lista.isEmpty()) {
                binding.rvContactos.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvContactos.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                adapter.submitList(lista)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
