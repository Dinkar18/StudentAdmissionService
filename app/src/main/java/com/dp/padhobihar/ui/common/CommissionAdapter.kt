package com.dp.padhobihar.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dp.padhobihar.databinding.ItemCommissionBinding
import com.dp.padhobihar.domain.model.Commission
import com.dp.padhobihar.domain.model.PaymentStatus

class CommissionAdapter(
    private val onMarkPaid: ((Commission) -> Unit)? = null
) : ListAdapter<Commission, CommissionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemCommissionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(commission: Commission, onMarkPaid: ((Commission) -> Unit)?) {
            binding.tvStudentName.text = commission.studentId
            binding.tvCollegeName.text = commission.collegeId
            binding.tvAmount.text = "₹${commission.agentShare}"
            binding.tvPayStatus.text = if (commission.status == PaymentStatus.PAID) "✅ Paid" else "⏳ Pending"
            if (onMarkPaid != null && commission.status == PaymentStatus.PENDING) {
                binding.btnMarkPaid.visibility = View.VISIBLE
                binding.btnMarkPaid.setOnClickListener { onMarkPaid(commission) }
            } else {
                binding.btnMarkPaid.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onMarkPaid)
    }

    private object DiffCallback : DiffUtil.ItemCallback<Commission>() {
        override fun areItemsTheSame(oldItem: Commission, newItem: Commission) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Commission, newItem: Commission) = oldItem == newItem
    }
}
