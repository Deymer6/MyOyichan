package com.upn.myoyichan.ui.recordatorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.FragmentRecordatoriosBinding
import com.upn.myoyichan.repository.MedicamentoRepository
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.utils.SessionManager

class RecordatoriosFragment : Fragment() {

    private var _binding: FragmentRecordatoriosBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecordatorioViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RecordatorioAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecordatoriosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val repository = MedicamentoRepository(AppDatabase.getInstance(requireContext()).medicamentoDao())
        val factory = ViewModelFactory(medicamentoRepository = repository)
        viewModel = ViewModelProvider(this, factory)[RecordatorioViewModel::class.java]

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = RecordatorioAdapter(
            onToggleActivo = { medicamento, activo ->
                viewModel.toggleActivo(medicamento, activo)
            }
        )
        binding.rvRecordatorios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecordatorios.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.getRecordatorios(sessionManager.getUsuarioId()).observe(viewLifecycleOwner) { lista ->
            if (lista.isEmpty()) {
                binding.rvRecordatorios.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvRecordatorios.visibility = View.VISIBLE
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
