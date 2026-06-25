package com.dp.padhobihar.ui.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.padhobihar.databinding.FragmentStudentCollegesBinding
import com.dp.padhobihar.ui.common.CollegeAdapter
import com.dp.padhobihar.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgentCollegesFragment : Fragment() {

    private var _binding: FragmentStudentCollegesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AgentViewModel by activityViewModels()
    private val adapter = CollegeAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentCollegesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvColleges.layoutManager = LinearLayoutManager(requireContext())
        binding.rvColleges.adapter = adapter

        viewModel.collegesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvColleges.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvColleges.visibility = View.VISIBLE
                    adapter.submitList(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.loadColleges()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
