package com.upn.myoyichan.ui.vitales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.upn.myoyichan.R
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.FragmentVitalesBinding
import com.upn.myoyichan.repository.SignoVitalRepository
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.utils.SessionManager

class VitalesFragment : Fragment() {

    private var _binding: FragmentVitalesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: VitalViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: SignoVitalAdapter

    private var currentFilter = "TODOS"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVitalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val repository = SignoVitalRepository(AppDatabase.getInstance(requireContext()).signoVitalDao())
        val factory = ViewModelFactory(signoVitalRepository = repository)
        viewModel = ViewModelProvider(this, factory)[VitalViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = SignoVitalAdapter(requireContext())
        binding.rvVitales.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVitales.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), VitalFormActivity::class.java))
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            
            val chipId = checkedIds.first()
            val chip = group.findViewById<Chip>(chipId)
            
            currentFilter = when (chip.text.toString()) {
                "Todos" -> "TODOS"
                "Presión" -> "PRESION"
                "Glucosa" -> "GLUCOSA"
                "Peso" -> "PESO"
                "Temperatura" -> "TEMPERATURA"
                else -> "TODOS"
            }
            loadData()
        }
    }

    private fun loadData() {
        val userId = sessionManager.getUsuarioId()
        // Remove observers before adding a new one to avoid multiple observations
        viewModel.getVitales(userId, "TODOS").removeObservers(viewLifecycleOwner)
        viewModel.getVitales(userId, "PRESION").removeObservers(viewLifecycleOwner)
        viewModel.getVitales(userId, "GLUCOSA").removeObservers(viewLifecycleOwner)
        viewModel.getVitales(userId, "PESO").removeObservers(viewLifecycleOwner)
        viewModel.getVitales(userId, "TEMPERATURA").removeObservers(viewLifecycleOwner)

        viewModel.getVitales(userId, currentFilter).observe(viewLifecycleOwner) { lista ->
            if (lista.isEmpty()) {
                binding.rvVitales.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvVitales.visibility = View.VISIBLE
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
