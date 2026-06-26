package com.upn.myoyichan.ui.medicamentos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.FragmentMedicamentosBinding
import com.upn.myoyichan.repository.MedicamentoRepository
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.utils.SessionManager

class MedicamentosFragment : Fragment() {

    private var _binding: FragmentMedicamentosBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicamentoViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: MedicamentoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMedicamentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val repository = MedicamentoRepository(AppDatabase.getInstance(requireContext()).medicamentoDao())
        val factory = ViewModelFactory(medicamentoRepository = repository)
        viewModel = ViewModelProvider(this, factory)[MedicamentoViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = MedicamentoAdapter(
            onEditClick = { medicamento ->
                val intent = Intent(requireContext(), MedicamentoFormActivity::class.java)
                intent.putExtra("MEDICAMENTO_ID", medicamento.id)
                startActivity(intent)
            },
            onDeleteClick = { medicamento ->
                AlertDialog.Builder(requireContext())
                    .setMessage("¿Está seguro que desea eliminar este medicamento?")
                    .setPositiveButton("Sí") { _, _ -> viewModel.deleteMedicamento(medicamento) }
                    .setNegativeButton("No", null)
                    .show()
            },
            onInfoClick = { medicamento ->
                val intent = Intent(requireContext(), InfoMedicamentoActivity::class.java)
                intent.putExtra("nombre_medicamento", medicamento.nombre)
                startActivity(intent)
            }
        )
        binding.rvMedicamentos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedicamentos.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), MedicamentoFormActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.getMedicamentos(sessionManager.getUsuarioId()).observe(viewLifecycleOwner) { lista ->
            if (lista.isEmpty()) {
                binding.rvMedicamentos.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvMedicamentos.visibility = View.VISIBLE
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
