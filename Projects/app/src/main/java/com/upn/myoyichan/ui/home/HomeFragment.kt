package com.upn.myoyichan.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.upn.myoyichan.R
import com.upn.myoyichan.data.local.AppDatabase
import com.upn.myoyichan.databinding.FragmentHomeBinding
import com.upn.myoyichan.repository.MedicamentoRepository
import com.upn.myoyichan.ui.ViewModelFactory
import com.upn.myoyichan.ui.main.MainActivity
import com.upn.myoyichan.ui.medicamentos.MedicamentoViewModel
import com.upn.myoyichan.utils.SessionManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    private lateinit var medicamentoViewModel: MedicamentoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        binding.tvSaludo.text = getString(R.string.home_saludo, sessionManager.getNombre())

        val repository = MedicamentoRepository(AppDatabase.getInstance(requireContext()).medicamentoDao())
        val factory = ViewModelFactory(medicamentoRepository = repository)
        medicamentoViewModel = ViewModelProvider(this, factory)[MedicamentoViewModel::class.java]

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnVerSalud.setOnClickListener {
            findNavController().navigate(R.id.vitalesFragment)
        }

        binding.btnSOS.setOnClickListener {
            (activity as? MainActivity)?.llamarEmergencia()
        }
    }

    private fun setupObservers() {
        val userId = sessionManager.getUsuarioId()
        medicamentoViewModel.getMedicamentos(userId).observe(viewLifecycleOwner) { medicamentos ->
            if (medicamentos.isEmpty()) {
                binding.tvMedicamentosPendientes.text = getString(R.string.home_sin_medicamentos)
                binding.tvProximoRecordatorio.text = getString(R.string.home_sin_recordatorios)
            } else {
                val activos = medicamentos.filter { it.activo }
                binding.tvMedicamentosPendientes.text = getString(R.string.home_medicamentos_pendientes, activos.size)
                
                if (activos.isNotEmpty()) {
                    // Solo para mostrar algo, tomamos el primero como ejemplo
                    val proximo = activos.first()
                    binding.tvProximoRecordatorio.text = "Próximo recordatorio: ${proximo.horario} - ${proximo.nombre}"
                } else {
                    binding.tvProximoRecordatorio.text = getString(R.string.home_sin_recordatorios)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
