package com.dp.padhobihar.ui.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.padhobihar.databinding.FragmentAgentEarningsBinding
import com.dp.padhobihar.domain.model.PaymentStatus
import com.dp.padhobihar.ui.common.CommissionAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgentEarningsFragment : Fragment() {

    private var _binding: FragmentAgentEarningsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AgentViewModel by activityViewModels()
    private val adapter = CommissionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAgentEarningsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvEarnings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEarnings.adapter = adapter

        viewModel.commissions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val total = list.sumOf { it.agentShare }
            val paid = list.filter { it.status == PaymentStatus.PAID }.sumOf { it.agentShare }
            val pending = total - paid
            binding.tvTotalEarnings.text = "₹$total"
            binding.tvPaid.text = "Paid: ₹$paid"
            binding.tvPendingAmount.text = "Pending: ₹$pending"
        }
        viewModel.loadEarnings()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
